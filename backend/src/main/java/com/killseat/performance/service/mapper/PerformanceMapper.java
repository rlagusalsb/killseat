package com.killseat.performance.service.mapper;

import com.killseat.admin.performance.dto.AdminPerformanceRequestDto;
import com.killseat.performance.dto.PerformanceResponseDto;
import com.killseat.performance.dto.ScheduleResponseDto;
import com.killseat.performance.entity.Performance;
import com.killseat.performance.entity.PerformanceSchedule;
import com.killseat.performance.entity.PerformanceStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PerformanceMapper {

    public PerformanceResponseDto toDto(Performance performance) {
        List<ScheduleResponseDto> scheduleDtos = performance.getSchedules().stream()
                .map(s -> new ScheduleResponseDto(
                        s.getScheduleId(),
                        s.getStartTime(),
                        s.getEndTime()))
                .collect(Collectors.toList());

        return new PerformanceResponseDto(
                performance.getPerformanceId(),
                performance.getTitle(),
                performance.getContent(),
                performance.getLocation(),
                performance.getPrice(),
                performance.getStatus().name(),
                performance.getThumbnailUrl(),
                scheduleDtos
        );
    }

    public Performance toEntity(AdminPerformanceRequestDto dto) {
        Performance performance = Performance.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .location(dto.getLocation())
                .price(dto.getPrice())
                .thumbnailUrl(dto.getThumbnailUrl())
                .status(PerformanceStatus.BEFORE_OPEN)
                .build();

        if (dto.getSchedules() != null) {
            dto.getSchedules().forEach(s -> {
                PerformanceSchedule schedule = PerformanceSchedule.builder()
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .performance(performance)
                        .build();
                performance.addSchedule(schedule);
            });
        }

        return performance;
    }
}
