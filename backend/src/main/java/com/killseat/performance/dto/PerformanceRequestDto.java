package com.killseat.performance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PerformanceRequestDto {
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
