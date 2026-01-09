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

        List<Reservation> expired = reservationRepository.findExpiredPendingOnly(now);

        if (expired.isEmpty()) {
            return;
        }

        for (Reservation reservation : expired) {
            int changed = reservationRepository.updateStatusIfMatch(
                    reservation.getReservationId(),
                    ReservationStatus.PENDING,
                    ReservationStatus.CANCELED,
                    now
            );

            if (changed > 0) {
                performanceSeatRepository.updateStatusIfMatch(
                        reservation.getPerformanceSeat().getPerformanceSeatId(),
                        PerformanceSeatStatus.HELD,
                        PerformanceSeatStatus.AVAILABLE
                );
            }
        }
    }
}
