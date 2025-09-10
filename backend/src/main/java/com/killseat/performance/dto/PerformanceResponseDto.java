package com.killseat.performance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PerformanceResponseDto {
    private Long performanceId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
