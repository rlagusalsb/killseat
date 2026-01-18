package com.killseat.performance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDto {
    private Long scheduleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
