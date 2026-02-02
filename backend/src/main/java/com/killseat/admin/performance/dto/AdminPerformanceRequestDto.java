package com.killseat.admin.performance.dto;

import com.killseat.performance.dto.ScheduleRequestDto;
import com.killseat.performance.entity.PerformanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPerformanceRequestDto {
    private String title;
    private String content;
    private String location;
    private Long price;
    private String thumbnailUrl;
    private PerformanceStatus status;
    private List<ScheduleRequestDto> schedules;
}
