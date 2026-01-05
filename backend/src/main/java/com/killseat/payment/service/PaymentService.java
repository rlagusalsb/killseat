package com.killseat.payment.service;

import com.killseat.payment.PortOneClient;
import com.killseat.payment.dto.*;
import com.killseat.payment.entity.Payment;
import com.killseat.payment.entity.PaymentStatus;
import com.killseat.payment.repository.PaymentRepository;
import com.killseat.reservation.entity.Reservation;
import com.killseat.reservation.entity.ReservationStatus;
import com.killseat.reservation.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PortOneClient portOneClient;

    //결제 준비
    //서버에서 금액 계산 -> merchantUid 생성 payment 생성/저장 -> 반환
    @Transactional
    public PaymentPrepareResponseDto prepare(PaymentPrepareRequestDto req) {
        Reservation reservation = reservationRepository.findById(req.getReservationId())
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("결제 준비가 가능한 예약 상태가 아닙니다.");
        }

        Long expectedAmount = calculateExpectedAmount(reservation);
        String merchantUid = "reservation-" + reservation.getReservationId() + "-" + UUID.randomUUID();

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(expectedAmount)
                .method(req.getMethod())
                .build();
        payment.assignMerchantUid(merchantUid);

        paymentRepository.save(payment);

        String name = "공연 예매";

        return new PaymentPrepareResponseDto(
                payment.getPaymentId(),
                payment.getMerchantUid(),
                payment.getAmount(),
                name
        );
    }

    //결제 확정
    //merchantUid로 payment 조회 -> 멱등 -> 결제 조회
    @Transactional
    public PaymentConfirmResponseDto confirm(PaymentConfirmRequestDto req) {
        Payment payment = paymentRepository.findByMerchantUid(req.getMerchantUid())
                .orElseThrow(() -> new EntityNotFoundException("결제 요청을 찾을 수 없습니다."));

        Reservation reservation = payment.getReservation();

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
        PortOnePaymentInfo info = portOneClient.getPaymentInfo(req.getImpUid());

        //조회 실패
        if (info == null) {
            payment.fail();
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
            payment.fail();
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
            payment.fail();
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
            payment.fail();
            return new PaymentConfirmResponseDto(
                    false,
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    reservation.getStatus().name(),
                    "결제 금액이 일치하지 않습니다."
            );
        }

        //확정
        payment.assignImpUid(info.getImpUid());
        payment.success();
        reservation.confirm();

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
    public PaymentCancelResponseDto cancel(PaymentCancelRequestDto req) {
        Payment payment = paymentRepository.findById(req.getPaymentId())
                .orElseThrow(() -> new EntityNotFoundException("결제를 찾을 수 없습니다."));

        Reservation reservation = payment.getReservation();

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
        PortOneCancelResult cancelRes = portOneClient.cancelPayment(payment.getImpUid(), req.getReason());

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

        reservation.getPerformanceSeat().cancel();

        return new PaymentCancelResponseDto(
                true,
                payment.getPaymentId(),
                payment.getStatus().name(),
                reservation.getStatus().name(),
                "결제가 취소되었습니다."
        );
    }

    private Long calculateExpectedAmount(Reservation reservation) {
        return reservation.getPerformanceSeat().getPerformance().getPrice();
    }
}
