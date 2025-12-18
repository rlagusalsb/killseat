package com.killseat.performance.service.mapper;

import com.killseat.performance.dto.PerformanceRequestDto;
import com.killseat.performance.dto.PerformanceResponseDto;
import com.killseat.performance.entity.Performance;
import org.springframework.stereotype.Component;

@Component
public class PerformanceMapper {

    public PerformanceResponseDto toDto(Performance performance) {
        return new PerformanceResponseDto(
                performance.getPerformanceId(),
                performance.getTitle(),
                performance.getStartTime(),
                performance.getEndTime(),
                performance.getStatus().name()
        );
    }

    public Performance toEntity(PerformanceRequestDto dto) {
        return Performance.builder()
                .title(dto.getTitle())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }
}
