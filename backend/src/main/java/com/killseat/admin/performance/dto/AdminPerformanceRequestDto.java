package com.killseat.admin.performance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPerformanceRequestDto {
    private String title;
    private String content;
    private String location;
    private Long price;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalSeats;
}
