package com.killseat.reservation.service.mapper;

import com.killseat.reservation.dto.ReservationResponseDto;
import com.killseat.reservation.entity.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationResponseDto toDto(Reservation reservation) {
        return new ReservationResponseDto(
                reservation.getReservationId(),
                reservation.getPerformanceSeat().getPerformanceSeatId(),
                reservation.getPerformanceSeat().getSeat().getSeatNumber(),
                reservation.getPerformanceSeat().getPerformance().getTitle(),
                reservation.getStatus().name()
        );
    }
}
