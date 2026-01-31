package com.killseat.performance.service;

import com.killseat.admin.performance.dto.AdminPerformanceRequestDto;
import com.killseat.performance.dto.PageResponse;
import com.killseat.performance.dto.PerformanceResponseDto;
import org.springframework.data.domain.Pageable;

public interface PerformanceService {
    PageResponse<PerformanceResponseDto> getActivePerformances(Pageable pageable);
    PerformanceResponseDto getOne(Long id);
    PerformanceResponseDto getOneForAdmin(Long id);
    PageResponse<PerformanceResponseDto> getAllForAdmin(Pageable pageable);
    PerformanceResponseDto createByAdmin(AdminPerformanceRequestDto request);
    PerformanceResponseDto updatePerformance(Long id, AdminPerformanceRequestDto request);
    void openPerformance(Long id);
    void closePerformance(Long id);
}
