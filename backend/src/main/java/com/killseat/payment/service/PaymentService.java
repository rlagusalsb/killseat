package com.killseat.payment.service;

import com.killseat.payment.PortOneClient;
import com.killseat.payment.dto.*;
import com.killseat.payment.entity.Payment;
import com.killseat.payment.entity.PaymentStatus;
import com.killseat.payment.repository.PaymentRepository;
import com.killseat.performanceseat.entity.PerformanceSeatStatus;
import com.killseat.performanceseat.repository.PerformanceSeatRepository;
import com.killseat.reservation.entity.Reservation;
import com.killseat.reservation.entity.ReservationStatus;
import com.killseat.reservation.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final PortOneClient portOneClient;

    //결제 준비
    //서버에서 금액 계산 -> merchantUid 생성 payment 생성/저장 -> 반환
    @Transactional
    public PaymentPrepareResponseDto prepare(PaymentPrepareRequestDto request) {
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("결제 준비가 가능한 예약 상태가 아닙니다.");
        }

        if (reservation.getPerformanceSeat().getStatus() != PerformanceSeatStatus.HELD) {
            throw new IllegalStateException("좌석이 결제 대기 상태가 아닙니다.");
        }

        Long expectedAmount = calculateExpectedAmount(reservation);
        String merchantUid = "reservation-" + reservation.getReservationId() + "-" + UUID.randomUUID();

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(expectedAmount)
                .method(request.getMethod())
                .build();
        payment.assignMerchantUid(merchantUid);

        paymentRepository.save(payment);

        return new PaymentPrepareResponseDto(
                payment.getPaymentId(),
                payment.getMerchantUid(),
                payment.getAmount(),
                "공연 예매"
        );
    }

    //결제 확정
    //merchantUid로 payment 조회 -> 멱등 -> 결제 조회
    @Transactional
    public PaymentConfirmResponseDto confirm(PaymentConfirmRequestDto request) {
        Payment payment = paymentRepository.findByMerchantUid(request.getMerchantUid())
                .orElseThrow(() -> new EntityNotFoundException("결제 요청을 찾을 수 없습니다."));

        Reservation reservation = payment.getReservation();
        Long seatId = reservation.getPerformanceSeat().getPerformanceSeatId();

        //이미 처리된 결제면 현재 상태 그대로 응답
        if (payment.getStatus() != PaymentStatus.PENDING) {
            boolean paid = payment.getStatus() == PaymentStatus.SUCCESS;
            return new PaymentConfirmResponseDto(
                    paid,
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    reservation.getStatus().name(),
                    "이미 처리된 결제입니다."
            );
        }

        //예약 상태 체크
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            return new PaymentConfirmResponseDto(
                    false,
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    reservation.getStatus().name(),
                    "결제 확정이 가능한 예약 상태가 아닙니다."
            );
        }

        //결제 조회
        PortOnePaymentInfo info = portOneClient.getPaymentInfo(request.getImpUid());

        //조회 실패
        if (info == null) {
            markFailAndReleaseSeat(payment, reservation, seatId);
            return new PaymentConfirmResponseDto(
                    false,
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    reservation.getStatus().name(),
                    "결제 조회에 실패했습니다."
            );
        }

        //1.merchantUid 검증
        if (info.getMerchantUid() == null || !payment.getMerchantUid().equals(info.getMerchantUid())) {
            markFailAndReleaseSeat(payment, reservation, seatId);
            return new PaymentConfirmResponseDto(
                    false,
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    reservation.getStatus().name(),
                    "결제 요청이 일치하지 않습니다."
            );
        }

        //2.결제 상태 검증
        if (info.getStatus() == null || !"paid".equalsIgnoreCase(info.getStatus())) {
            markFailAndReleaseSeat(payment, reservation, seatId);
            return new PaymentConfirmResponseDto(
                    false,
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    reservation.getStatus().name(),
                    "결제가 완료되지 않았습니다."
            );
        }

        //3.금액 검증 (서버 기준 amount와 PortOne amount)
        if (info.getAmount() == null || info.getAmount().longValue() != payment.getAmount()) {
            markFailAndReleaseSeat(payment, reservation, seatId);
            return new PaymentConfirmResponseDto(
                    false,
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    reservation.getStatus().name(),
                    "결제 금액이 일치하지 않습니다."
            );
        }

        //확정
        int pUpdated = paymentRepository.updateStatusIfMatch(
                payment.getPaymentId(),
                PaymentStatus.PENDING,
                PaymentStatus.SUCCESS
        );

        if (pUpdated == 0) {
            boolean paid = payment.getStatus() == PaymentStatus.SUCCESS;
            return new PaymentConfirmResponseDto(
                    paid,
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    reservation.getStatus().name(),
                    "이미 처리된 결제입니다."
            );
        }

        payment.assignImpUid(info.getImpUid());

        //예약 확정: PENDING -> CONFIRMED
        reservationRepository.updateStatusIfMatch(
                reservation.getReservationId(),
                ReservationStatus.PENDING,
                ReservationStatus.CONFIRMED,
                LocalDateTime.now()
        );

        //좌석 확정: HELD -> RESERVED
        performanceSeatRepository.updateStatusIfMatch(
                seatId,
                PerformanceSeatStatus.HELD,
                PerformanceSeatStatus.RESERVED
        );

        return new PaymentConfirmResponseDto(
                true,
                payment.getPaymentId(),
                payment.getStatus().name(),
                reservation.getStatus().name(),
                "결제가 완료되었습니다."
        );
    }

    //결제 취소
    @Transactional
    public PaymentCancelResponseDto cancel(PaymentCancelRequestDto request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new EntityNotFoundException("결제를 찾을 수 없습니다."));

        Reservation reservation = payment.getReservation();
        Long seatId = reservation.getPerformanceSeat().getPerformanceSeatId();

        if (payment.getStatus() == PaymentStatus.CANCELED) {
            return new PaymentCancelResponseDto(
                    true,
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    reservation.getStatus().name(),
                    "이미 취소된 결제입니다."
            );
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            return new PaymentCancelResponseDto(
                    false,
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    reservation.getStatus().name(),
                    "취소 가능한 결제 상태가 아닙니다."
            );
        }

        //취소에는 impUid 필요
        if (payment.getImpUid() == null || payment.getImpUid().isBlank()) {
            return new PaymentCancelResponseDto(
                    false,
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    reservation.getStatus().name(),
                    "impUid가 없어 결제를 취소할 수 없습니다."
            );
        }

        //1.PortOne 취소 호출
        PortOneCancelResult cancelRes = portOneClient.cancelPayment(payment.getImpUid(), request.getReason());

        //2.취소 성공 판단
        if (cancelRes == null || cancelRes.getCancelAmount() == null || cancelRes.getCancelAmount() <= 0) {
            return new PaymentCancelResponseDto(
                    false,
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    reservation.getStatus().name(),
                    "결제 취소에 실패했습니다."
            );
        }

        //성공 처리
        payment.cancel();
        reservation.cancelAfterPayment();

        performanceSeatRepository.updateStatusIfMatch(
                seatId,
                PerformanceSeatStatus.RESERVED,
                PerformanceSeatStatus.AVAILABLE
        );

        return new PaymentCancelResponseDto(
                true,
                payment.getPaymentId(),
                payment.getStatus().name(),
                reservation.getStatus().name(),
                "결제가 취소되었습니다."
        );
    }

    private void markFailAndReleaseSeat(Payment payment, Reservation reservation, Long seatId) {
        //결제: PENDING -> FAILED
        paymentRepository.updateStatusIfMatch(
                payment.getPaymentId(),
                PaymentStatus.PENDING,
                PaymentStatus.FAILED
        );

        //예약: PENDING -> CANCELED
        reservationRepository.updateStatusIfMatch(
                reservation.getReservationId(),
                ReservationStatus.PENDING,
                ReservationStatus.CANCELED,
                LocalDateTime.now()
        );

        //좌석: HELD -> AVAILABLE
        performanceSeatRepository.updateStatusIfMatch(
                seatId,
                PerformanceSeatStatus.HELD,
                PerformanceSeatStatus.AVAILABLE
        );
    }

    private Long calculateExpectedAmount(Reservation reservation) {
        return reservation.getPerformanceSeat().getPerformance().getPrice();
    }
}
