package com.killseat.reservation.scheduler;

import com.killseat.reservation.entity.Reservation;
import com.killseat.reservation.entity.ReservationStatus;
import com.killseat.reservation.repository.ReservationRepository;
import com.killseat.performanceseat.entity.PerformanceSeatStatus;
import com.killseat.performanceseat.repository.PerformanceSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationExpireScheduler {

    private final ReservationRepository reservationRepository;
    private final PerformanceSeatRepository performanceSeatRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireReservations() {
        LocalDateTime now = LocalDateTime.now();

        List<Reservation> expired = reservationRepository.findExpiredHoldOrPaying(now);

        if (expired.isEmpty()) {
            return;
        }

        for (Reservation reservation : expired) {
            Long reservationId = reservation.getReservationId();

            int changed = reservationRepository.updateStatusIfMatch(
                    reservationId,
                    ReservationStatus.PENDING,
                    ReservationStatus.CANCELED,
                    now
            );

            if (changed == 0) {
                changed = reservationRepository.updateStatusIfMatch(
                        reservationId,
                        ReservationStatus.PAYING,
                        ReservationStatus.CANCELED,
                        now
                );
            }

            if (changed == 0) {
                continue;
            }

            Long performanceSeatId = reservation.getPerformanceSeat().getPerformanceSeatId();

            performanceSeatRepository.updateStatusIfMatch(
                    performanceSeatId,
                    PerformanceSeatStatus.HELD,
                    PerformanceSeatStatus.AVAILABLE
            );
        }
    }
}
