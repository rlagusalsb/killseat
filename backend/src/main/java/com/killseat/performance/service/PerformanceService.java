package com.killseat.performance.service;

import com.killseat.admin.performance.dto.AdminPerformanceRequestDto;
import com.killseat.performance.dto.PerformanceResponseDto;

import java.util.List;

public interface PerformanceService {
    List<PerformanceResponseDto> getActivePerformances();
    PerformanceResponseDto getOne(Long id);
    PerformanceResponseDto getOneForAdmin(Long id);
    List<PerformanceResponseDto> getAllForAdmin();
    PerformanceResponseDto createByAdmin(AdminPerformanceRequestDto request);
    PerformanceResponseDto updatePerformance(Long id, AdminPerformanceRequestDto request);
    void openPerformance(Long id);
    void closePerformance(Long id);
}
