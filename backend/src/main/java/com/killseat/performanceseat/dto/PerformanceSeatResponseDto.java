package com.killseat.performanceseat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PerformanceSeatResponseDto {
    private Long performanceSeatId;
    private Long performanceId;
    private Long scheduleId;
    private Long seatId;
    private String seatNumber;
    private String status;
}
