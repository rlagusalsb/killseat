package com.killseat.performance.service;

import com.killseat.admin.performance.dto.AdminPerformanceRequestDto;
import com.killseat.common.exception.CustomErrorCode;
import com.killseat.common.exception.CustomException;
import com.killseat.performance.dto.PageResponse;
import com.killseat.performance.dto.PerformanceResponseDto;
import com.killseat.performance.dto.ScheduleRequestDto;
import com.killseat.performance.entity.Performance;
import com.killseat.performance.entity.PerformanceSchedule;
import com.killseat.performance.entity.PerformanceStatus;
import com.killseat.performance.repository.PerformanceRepository;
import com.killseat.performance.service.mapper.PerformanceMapper;
import com.killseat.performanceseat.service.PerformanceSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "performance.cache-enabled", havingValue = "true", matchIfMissing = true)
public class CachePerformanceService implements PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final PerformanceSeatService performanceSeatService;
    private final PerformanceMapper performanceMapper;

    //OPEN 상태인 공연들만 조회
    @Override
    @Cacheable(value = "performanceList", key = "#pageable.pageNumber")
    @Transactional(readOnly = true)
    public PageResponse<PerformanceResponseDto> getActivePerformances(Pageable pageable) {
        Page<PerformanceResponseDto> page = performanceRepository.findAllByStatus(PerformanceStatus.OPEN, pageable)
                .map(performanceMapper::toDto);

        return new PageResponse<>(page);
    }

    //공연 상세 조회
    @Override
    @Cacheable(value = "performanceDetail", key = "#id")
    @Transactional(readOnly = true)
    public PerformanceResponseDto getOne(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PERFORMANCE_NOT_FOUND));

        if (performance.getStatus() != PerformanceStatus.OPEN) {
            throw new CustomException(CustomErrorCode.PERFORMANCE_NOT_FOUND);
        }

        return performanceMapper.toDto(performance);
    }

    //관리자 전용 공연 상세 조회
    @Override
    @Transactional(readOnly = true)
    public PerformanceResponseDto getOneForAdmin(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PERFORMANCE_NOT_FOUND));

        return performanceMapper.toDto(performance);
    }

    //관리자 전용 전체 공연 목록 조회
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PerformanceResponseDto> getAllForAdmin(Pageable pageable) {
        Page<PerformanceResponseDto> page = performanceRepository.findAll(pageable)
                .map(performanceMapper::toDto);

        return new PageResponse<>(page);
    }

    //관리자 전용 공연 및 좌석 등록
    @Override
    @CacheEvict(value = "performanceList", allEntries = true)
    @Transactional
    public PerformanceResponseDto createByAdmin(AdminPerformanceRequestDto request) {
        if (request.getSchedules() == null || request.getSchedules().isEmpty()) {
            throw new CustomException(CustomErrorCode.MISSING_SCHEDULE);
        }

        Performance performance = performanceMapper.toEntity(request);

        Performance savedPerformance = performanceRepository.save(performance);

        performanceSeatService.createPerformanceSeats(savedPerformance);

        return performanceMapper.toDto(savedPerformance);
    }

    //공연 정보 수정
    @Override
    @Caching(evict = {
            @CacheEvict(value = "performanceList", allEntries = true),
            @CacheEvict(value = "performanceDetail", key = "#id")
    })
    @Transactional
    public PerformanceResponseDto updatePerformance(Long id, AdminPerformanceRequestDto request) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PERFORMANCE_NOT_FOUND));

        performance.update(
                request.getTitle(),
                request.getContent(),
                request.getLocation(),
                request.getPrice(),
                request.getStatus(),
                request.getThumbnailUrl(),
                performance.getSchedules()
        );

        if (request.getSchedules() != null) {
            List<PerformanceSchedule> existingSchedules = performance.getSchedules();
            List<ScheduleRequestDto> requestedSchedules = request.getSchedules();

            for (int i = 0; i < requestedSchedules.size(); i++) {
                ScheduleRequestDto reqDto = requestedSchedules.get(i);
                if (i < existingSchedules.size()) {
                    existingSchedules.get(i).updateTime(reqDto.getStartTime(), reqDto.getEndTime());
                } else {
                    PerformanceSchedule newSchedule = PerformanceSchedule.builder()
                            .startTime(reqDto.getStartTime())
                            .endTime(reqDto.getEndTime())
                            .performance(performance)
                            .build();
                    existingSchedules.add(newSchedule);
                }
            }

            if (existingSchedules.size() > requestedSchedules.size()) {
                existingSchedules.subList(requestedSchedules.size(), existingSchedules.size()).clear();
            }
        }

        return performanceMapper.toDto(performance);
    }

    //공연 상태 수정 (BEFORE_OPEN -> OPEN)
    @Override
    @CacheEvict(value = "performanceList", allEntries = true)
    @Transactional
    public void openPerformance(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PERFORMANCE_NOT_FOUND));
        performance.openSales();
    }

    //공연 상태 수정 (OPEN -> CLOSED)
    @Override
    @CacheEvict(value = "performanceList", allEntries = true)
    @Transactional
    public void closePerformance(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PERFORMANCE_NOT_FOUND));
        performance.closeSales();
    }

    //공연 검색
    @Override
    @Cacheable(value = "performanceSearch", key = "#title + '-' + #pageable.pageNumber", condition = "#title != null")
    @Transactional(readOnly = true)
    public PageResponse<PerformanceResponseDto> searchActivePerformances(String title, Pageable pageable) {
        Page<PerformanceResponseDto> page = performanceRepository
                .findAllByStatusAndTitleContaining(PerformanceStatus.OPEN, title, pageable)
                .map(performanceMapper::toDto);

        return new PageResponse<>(page);
    }
}
