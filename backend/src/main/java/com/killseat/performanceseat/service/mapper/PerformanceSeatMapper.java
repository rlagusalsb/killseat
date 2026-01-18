package com.killseat.performanceseat.service.mapper;

import com.killseat.performance.entity.PerformanceSchedule;
import com.killseat.performanceseat.dto.PerformanceSeatResponseDto;
import com.killseat.performanceseat.entity.PerformanceSeat;
import com.killseat.performanceseat.entity.PerformanceSeatStatus;
import com.killseat.seat.entity.Seat;
import org.springframework.stereotype.Component;

@Component
public class PerformanceSeatMapper {

    public PerformanceSeat toEntity(PerformanceSchedule performanceSchedule, Seat seat) {
        return PerformanceSeat.builder()
                .performanceSchedule(performanceSchedule)
                .seat(seat)
                .status(PerformanceSeatStatus.AVAILABLE)
                .build();
    }

    public PerformanceSeatResponseDto toDto(PerformanceSeat entity) {
        return new PerformanceSeatResponseDto(
                entity.getPerformanceSeatId(),
                entity.getPerformanceSchedule().getPerformance().getPerformanceId(),
                entity.getPerformanceSchedule().getScheduleId(),
                entity.getSeat().getSeatId(),
                entity.getSeat().getSeatNumber(),
                entity.getStatus().name()
        );
    }
}