package com.killseat.performance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PerformanceResponseDto {
    private Long performanceId;
    private String title;
    private Long price;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String thumbnailUrl;
}
