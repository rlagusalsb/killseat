package com.killseat.admin.performance.dto;

import com.killseat.performance.dto.ScheduleRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private int totalSeats;
    private List<ScheduleRequestDto> schedules;
}
