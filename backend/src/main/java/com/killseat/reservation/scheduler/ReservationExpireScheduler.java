package com.killseat.reservation.scheduler;

import com.killseat.reservation.entity.ReservationStatus;
import com.killseat.reservation.repository.ReservationRepository;
import com.killseat.performanceseat.entity.PerformanceSeatStatus;
import com.killseat.performanceseat.repository.PerformanceSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpireScheduler {

    private final ReservationRepository reservationRepository;
    private final PerformanceSeatRepository performanceSeatRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireReservations() {
        LocalDateTime now = LocalDateTime.now();

        //선점 및 결제창 진입 후 5분 내에 최종 확정되지 않은 미완료 건들을 처리
        List<ReservationStatus> targets = List.of(ReservationStatus.PENDING, ReservationStatus.PAYING);

        //만료된 예약들에 연결된 좌석 ID 리스트만 가져옴 (엔티티를 가져오는거보다 ID만 가져오는게 가벼움)
        List<Long> seatIdsToRelease = reservationRepository.findSeatIdsByExpiredReservation(
                targets,
                now
        );

        //처리할 대상이 없으면 종료
        if (seatIdsToRelease.isEmpty()) {
            return;
        }

        //예약 테이블의 상태를 한꺼번에 CANCELED로 바꿈
        int updatedReservations = reservationRepository.bulkUpdateStatusForExpired(
                targets,
                ReservationStatus.CANCELED,
                now
        );

        //좌석 테이블의 상태를 한꺼번에 AVAILABLE로 되돌림
        if (updatedReservations > 0) {
            int updatedSeats = performanceSeatRepository.bulkUpdateSeatStatus(
                    seatIdsToRelease,
                    PerformanceSeatStatus.HELD,
                    PerformanceSeatStatus.AVAILABLE
            );

            log.info("스케줄러 - 만료 예약 {}건 취소 및 좌석 {}개 해제 완료", updatedReservations, updatedSeats);
        }
    }
}