package com.killseat.reservation.service;

import com.killseat.common.exception.CustomErrorCode;
import com.killseat.common.exception.CustomException;
import com.killseat.member.entity.Member;
import com.killseat.member.repository.MemberRepository;
import com.killseat.mypage.dto.MyPageReservationDto;
import com.killseat.performance.entity.PerformanceStatus;
import com.killseat.performanceseat.entity.PerformanceSeat;
import com.killseat.performanceseat.entity.PerformanceSeatStatus;
import com.killseat.performanceseat.repository.PerformanceSeatRepository;
import com.killseat.reservation.dto.ReservationResponseDto;
import com.killseat.reservation.entity.Reservation;
import com.killseat.reservation.entity.ReservationStatus;
import com.killseat.reservation.repository.ReservationRepository;
import com.killseat.reservation.service.mapper.ReservationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final int HOLD_MINUTES = 5;

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final ReservationMapper reservationMapper;

    @Transactional(readOnly = true)
    public Page<MyPageReservationDto> getMyPageReservations(Long memberId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);

        return reservationRepository
                .findMyPageReservations(memberId, ReservationStatus.CANCELED, pageable)
                .map(reservationMapper::toMyPageDto);
    }

    @Transactional
    public ReservationResponseDto reserveSeat(Long performanceSeatId, Long memberId) {
        PerformanceSeat seat = performanceSeatRepository.findById(performanceSeatId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.SEAT_NOT_FOUND));

        if (seat.getPerformanceSchedule().getPerformance().getStatus() != PerformanceStatus.OPEN) {
            throw new CustomException(CustomErrorCode.PERFORMANCE_NOT_OPEN);
        }

        int held = performanceSeatRepository.updateStatusIfMatch(
                performanceSeatId,
                PerformanceSeatStatus.AVAILABLE,
                PerformanceSeatStatus.HELD
        );

        if (held == 0) {
            throw new CustomException(CustomErrorCode.SEAT_ALREADY_OCCUPIED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        Reservation reservation = Reservation.builder()
                .member(member)
                .performanceSeat(seat)
                .status(ReservationStatus.PENDING)
                .expiresAt(now.plusMinutes(HOLD_MINUTES))
                .build();

        try {
            reservationRepository.save(reservation);

            Reservation detail = reservationRepository.findDetailById(reservation.getReservationId());
            return reservationMapper.toDto(detail);

        } catch (RuntimeException e) {
            performanceSeatRepository.updateStatusIfMatch(
                    performanceSeatId,
                    PerformanceSeatStatus.HELD,
                    PerformanceSeatStatus.AVAILABLE
            );
            throw e;
        }
    }

    @Transactional
    public ReservationResponseDto cancelReservation(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getMember().getMemberId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.REJECTED_PERMISSION);
        }

        if (reservation.getPerformanceSeat().getPerformanceSchedule().getPerformance().getStatus() != PerformanceStatus.OPEN) {
            throw new CustomException(CustomErrorCode.CANNOT_CANCEL_AFTER_CLOSE);
        }

        ReservationStatus previousStatus = reservation.getStatus();

        if (previousStatus == ReservationStatus.CONFIRMED) {
            reservation.cancelAfterPayment();
        } else {
            reservation.cancelBeforePayment();
        }

        PerformanceSeatStatus fromStatus = (previousStatus == ReservationStatus.CONFIRMED)
                ? PerformanceSeatStatus.RESERVED
                : PerformanceSeatStatus.HELD;

        performanceSeatRepository.updateStatusIfMatch(
                reservation.getPerformanceSeat().getPerformanceSeatId(),
                fromStatus,
                PerformanceSeatStatus.AVAILABLE
        );

        Reservation detail = reservationRepository.findDetailById(reservationId);
        return reservationMapper.toDto(detail);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponseDto> getAllReservationsForAdmin(Pageable pageable) {
        return reservationRepository.findAll(pageable)
                .map(reservationMapper::toDto);
    }

    @Transactional
    public ReservationResponseDto cancelReservationByAdmin(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));

        ReservationStatus previousStatus = reservation.getStatus();

        if (previousStatus == ReservationStatus.CONFIRMED) {
            reservation.cancelAfterPayment();
        } else {
            reservation.cancelBeforePayment();
        }

        PerformanceSeatStatus fromStatus = (previousStatus == ReservationStatus.CONFIRMED)
                ? PerformanceSeatStatus.RESERVED
                : PerformanceSeatStatus.HELD;

        performanceSeatRepository.updateStatusIfMatch(
                reservation.getPerformanceSeat().getPerformanceSeatId(),
                fromStatus,
                PerformanceSeatStatus.AVAILABLE
        );

        Reservation detail = reservationRepository.findDetailById(reservationId);
        return reservationMapper.toDto(detail);
    }
}
