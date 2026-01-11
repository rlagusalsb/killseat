package com.killseat.performance.service;

import com.killseat.performance.dto.PerformanceRequestDto;
import com.killseat.performance.dto.PerformanceResponseDto;
import com.killseat.performance.entity.Performance;
import com.killseat.performance.repository.PerformanceRepository;
import com.killseat.performance.service.mapper.PerformanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private  final PerformanceMapper performanceMapper;

    @Cacheable(value = "performanceList", key = "'all'")
    @Transactional(readOnly = true)
    public List<PerformanceResponseDto> getAll() {
        return performanceRepository.findAll().stream()
                .map(performanceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PerformanceResponseDto getOne(Long id) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공연을 찾을 수 없습니다."));
        return performanceMapper.toDto(performance);
    }

    @CacheEvict(value = "performanceList", allEntries = true)
    @Transactional
    public PerformanceResponseDto create(PerformanceRequestDto request) {
        Performance performance = performanceMapper.toEntity(request);
        return performanceMapper.toDto(performanceRepository.save(performance));
    }

    @CacheEvict(value = "performanceList", allEntries = true)

    @Transactional
    public PerformanceResponseDto update(Long id, PerformanceRequestDto request) {
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공연을 찾을 수 없습니다."));
        performance.update(request.getTitle(), request.getPrice(), request.getStartTime(), request.getEndTime());
        return performanceMapper.toDto(performance);
    }

    @CacheEvict(value = "performanceList", allEntries = true)
    @Transactional
    public void delete(Long id) {
        performanceRepository.deleteById(id);
    }
}
