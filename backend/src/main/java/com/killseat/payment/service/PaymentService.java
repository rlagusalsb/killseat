package com.killseat.payment.service;

import com.killseat.common.exception.CustomErrorCode;
import com.killseat.common.exception.CustomException;
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
    @Transactional
    public PaymentPrepareResponseDto prepare(PaymentPrepareRequestDto request) {
        Reservation reservation = reservationRepository.findForPaymentPrepare(request.getReservationId());

        if (reservation == null) {
            throw new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND);
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new CustomException(CustomErrorCode.INVALID_PAYMENT_STATUS);
        }

        int changedReservationStatus = reservationRepository.updateStatusIfMatch(
                reservation.getReservationId(),
                ReservationStatus.PENDING,
                ReservationStatus.PAYING,
                LocalDateTime.now()
        );

        if (changedReservationStatus == 0) {
            throw new CustomException(CustomErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        Long expectedAmount = calculateExpectedAmount(reservation);
        String merchantUid = "reservation-" + reservation.getReservationId() + "-" + UUID.randomUUID();

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(expectedAmount)
                .method(request.getMethod())
                .build();
        payment.assignMerchantUid(merchantUid);

        try {
            paymentRepository.save(payment);
        } catch (RuntimeException e) {
            reservationRepository.updateStatusIfMatch(
                    reservation.getReservationId(),
                    ReservationStatus.PAYING,
                    ReservationStatus.PENDING,
                    LocalDateTime.now()
            );
            throw e;
        }

        return new PaymentPrepareResponseDto(payment.getPaymentId(), payment.getMerchantUid(), payment.getAmount(), "공연 예매");
    }

    //결제 확정
    @Transactional
    public PaymentConfirmResponseDto confirm(PaymentConfirmRequestDto request) {
        Payment payment = paymentRepository.findByMerchantUid(request.getMerchantUid())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        Reservation reservation = payment.getReservation();
        Long seatId = reservation.getPerformanceSeat().getPerformanceSeatId();

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return new PaymentConfirmResponseDto(false, payment.getPaymentId(), payment.getStatus().name(), reservation.getStatus().name(), "이미 처리된 결제입니다.");
        }

        PortOnePaymentInfo info = portOneClient.getPaymentInfo(request.getImpUid());
        if (info == null || info.getAmount().longValue() != payment.getAmount()) {
            markFailAndReleaseSeat(payment, reservation, seatId);
            return new PaymentConfirmResponseDto(false, payment.getPaymentId(), payment.getStatus().name(), reservation.getStatus().name(), "결제 검증 실패");
        }

        int reservationConfirmed = reservationRepository.updateStatusIfMatch(
                reservation.getReservationId(),
                ReservationStatus.PAYING,
                ReservationStatus.CONFIRMED,
                LocalDateTime.now()
        );

        if (reservationConfirmed == 0) {
            return new PaymentConfirmResponseDto(false, payment.getPaymentId(), payment.getStatus().name(), reservation.getStatus().name(), "예약 상태 변경 실패");
        }

        int seatReserved = performanceSeatRepository.updateStatusIfMatch(
                seatId,
                PerformanceSeatStatus.HELD,
                PerformanceSeatStatus.RESERVED
        );

        if (seatReserved == 0) {
            markFailAndReleaseSeat(payment, reservation, seatId);
            return new PaymentConfirmResponseDto(false, payment.getPaymentId(), payment.getStatus().name(), reservation.getStatus().name(), "좌석 선점 만료");
        }

        paymentRepository.updateStatusIfMatch(payment.getPaymentId(), PaymentStatus.PENDING, PaymentStatus.SUCCESS);
        payment.assignImpUid(info.getImpUid());

        return new PaymentConfirmResponseDto(true, payment.getPaymentId(), PaymentStatus.SUCCESS.name(), ReservationStatus.CONFIRMED.name(), "결제 완료");
    }

    //결제 취소
    @Transactional
    public PaymentCancelResponseDto cancel(PaymentCancelRequestDto request, Long memberId) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));


        Reservation reservation = payment.getReservation();

        if (!reservation.getMember().getMemberId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.REJECTED_PERMISSION);
        }

        if (payment.getStatus() == PaymentStatus.CANCELED) {
            return new PaymentCancelResponseDto(true, payment.getPaymentId(), payment.getStatus().name(), reservation.getStatus().name(), "이미 취소되었습니다.");
        }

        if (payment.getImpUid() != null) {
            portOneClient.cancelPayment(payment.getImpUid(), request.getReason());
        }

        paymentRepository.updateStatusIfMatch(payment.getPaymentId(), PaymentStatus.SUCCESS, PaymentStatus.CANCELED);
        reservationRepository.updateStatusIfMatch(reservation.getReservationId(), ReservationStatus.CONFIRMED, ReservationStatus.CANCELED, LocalDateTime.now());
        performanceSeatRepository.updateStatusIfMatch(reservation.getPerformanceSeat().getPerformanceSeatId(), PerformanceSeatStatus.RESERVED, PerformanceSeatStatus.AVAILABLE);

        return new PaymentCancelResponseDto(true, payment.getPaymentId(), PaymentStatus.CANCELED.name(), ReservationStatus.CANCELED.name(), "취소 완료");
    }

    //실패 처리
    private void markFailAndReleaseSeat(Payment payment, Reservation reservation, Long seatId) {
        paymentRepository.updateStatusIfMatch(payment.getPaymentId(), PaymentStatus.PENDING, PaymentStatus.FAILED);
        reservationRepository.updateStatusIfMatch(reservation.getReservationId(), reservation.getStatus(), ReservationStatus.CANCELED, LocalDateTime.now());
        performanceSeatRepository.updateStatusIfMatch(seatId, PerformanceSeatStatus.HELD, PerformanceSeatStatus.AVAILABLE);
    }

    private Long calculateExpectedAmount(Reservation reservation) {
        return reservation.getPerformanceSeat().getPerformance().getPrice();
    }
}