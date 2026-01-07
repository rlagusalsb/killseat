package com.killseat.reservation.service.mapper;

import com.killseat.mypage.dto.MyPageReservationDto;
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

    public MyPageReservationDto toMyPageDto(Reservation reservation) {
        return new MyPageReservationDto(
                reservation.getReservationId(),
                reservation.getPerformanceSeat().getPerformance().getTitle(),
                reservation.getPerformanceSeat().getPerformance().getThumbnailUrl(),
                buildSeatInfo(reservation),
                reservation.getStatus().name(),
                reservation.getCreatedAt()
        );
    }

    private String buildSeatInfo(Reservation reservation) {
        return String.valueOf(
                reservation.getPerformanceSeat()
                        .getSeat()
                        .getSeatNumber()
        );
    }
}
