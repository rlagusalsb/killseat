package com.killseat.reservation.service.mapper;

import com.killseat.admin.reservation.dto.AdminReservationDto;
import com.killseat.mypage.dto.MyPageReservationDto;
import com.killseat.reservation.dto.ReservationResponseDto;
import com.killseat.reservation.entity.Reservation;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ReservationMapper {

    private static final DateTimeFormatter ROUND_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReservationResponseDto toDto(Reservation reservation) {
        return new ReservationResponseDto(
                reservation.getReservationId(),
                reservation.getPerformanceSeat().getPerformanceSeatId(),
                reservation.getPerformanceSeat().getSeat().getSeatNumber(),
                reservation.getPerformanceSeat().getPerformanceSchedule().getPerformance().getTitle(),
                reservation.getStatus().name()
        );
    }

    public MyPageReservationDto toMyPageDto(Reservation reservation) {
        return new MyPageReservationDto(
                reservation.getReservationId(),
                reservation.getPerformanceSeat().getPerformanceSchedule().getPerformance().getTitle(),
                reservation.getPerformanceSeat().getPerformanceSchedule().getPerformance().getThumbnailUrl(),
                reservation.getPerformanceSeat().getPerformanceSchedule().getPerformance().getLocation(),
                buildSeatInfo(reservation),
                reservation.getStatus().name(),
                reservation.getCreatedAt(),
                reservation.getPerformanceSeat().getPerformanceSchedule().getStartTime()
        );
    }

    public AdminReservationDto toAdminDto(Reservation reservation) {
        return new AdminReservationDto(
                reservation.getReservationId(),
                reservation.getMember().getName(),
                reservation.getPerformanceSeat().getPerformanceSchedule().getPerformance().getTitle(),
                buildSeatInfo(reservation),
                reservation.getStatus().name(),
                reservation.getPerformanceSeat()
                        .getPerformanceSchedule()
                        .getStartTime()
                        .format(ROUND_FORMATTER)
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
