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

        List<Reservation> expired =
                reservationRepository.findExpiredPendings(now);

        if (expired.isEmpty()) {
            return;
        }

        for (Reservation res : expired) {
            int updated = reservationRepository.updateStatusIfMatch(
                    res.getReservationId(),
                    ReservationStatus.PENDING,
                    ReservationStatus.CANCELED,
                    now
            );

            if (updated == 0) {
                continue;
            }

            Long seatId = res.getPerformanceSeat().getPerformanceSeatId();

            performanceSeatRepository.updateStatusIfMatch(
                    seatId,
                    PerformanceSeatStatus.HELD,
                    PerformanceSeatStatus.AVAILABLE
            );
        }
    }
}
