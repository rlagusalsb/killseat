package com.killseat.reservation.service;

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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final int HOLD_MINUTES = 10;

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final ReservationMapper reservationMapper;

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getMyReservations(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMember_MemberId(memberId);
        return reservations.stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyPageReservationDto> getMyPageReservations(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMember_MemberId(memberId);
        return reservations.stream()
                .map(reservationMapper::toMyPageDto)
                .toList();
    }

    @Transactional
    public ReservationResponseDto reserveSeat(Long performanceSeatId, Long memberId) {
        PerformanceSeat seat = performanceSeatRepository.findById(performanceSeatId)
                .orElseThrow(() -> new EntityNotFoundException("좌석을 찾을 수 없습니다."));

        if (seat.getPerformance().getStatus() != PerformanceStatus.OPEN) {
            throw new IllegalStateException("예매를 할 수 없습니다.");
        }

        int held = performanceSeatRepository.updateStatusIfMatch(
                performanceSeatId,
                PerformanceSeatStatus.AVAILABLE,
                PerformanceSeatStatus.HELD
        );

        if (held == 0) {
            throw new IllegalStateException("이미 선점되었거나 예약된 좌석입니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

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
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

        if (!reservation.getMember().getMemberId().equals(memberId)) {
            throw new IllegalStateException("사용자 본인 예약만 취소할 수 있습니다.");
        }

        if (reservation.getPerformanceSeat().getPerformance().getStatus() != PerformanceStatus.OPEN) {
            throw new IllegalStateException("예매 종료 후에는 취소할 수 없습니다.");
        }

        reservation.cancelBeforePayment();

        performanceSeatRepository.updateStatusIfMatch(
                reservation.getPerformanceSeat().getPerformanceSeatId(),
                PerformanceSeatStatus.HELD,
                PerformanceSeatStatus.AVAILABLE
        );

        return reservationMapper.toDto(reservation);
    }
}
