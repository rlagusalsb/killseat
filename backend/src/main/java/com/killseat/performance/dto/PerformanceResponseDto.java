package com.killseat.performance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceResponseDto {
    private Long performanceId;
    private String title;
    private String content;
    private String location;
    private Long price;
    private String status;
    private String thumbnailUrl;
    private List<ScheduleResponseDto> schedules;
}
