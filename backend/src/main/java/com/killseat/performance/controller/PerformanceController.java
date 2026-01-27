package com.killseat.performance.controller;

import com.killseat.performance.dto.PageResponse;
import com.killseat.performance.dto.PerformanceResponseDto;
import com.killseat.performance.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping
    public ResponseEntity<PageResponse<PerformanceResponseDto>> getActivePerformances(
            @PageableDefault(size = 20,
            sort = "createdAt",
            direction = Sort.Direction.DESC)Pageable pageable
    )
    {
        return ResponseEntity.ok(performanceService.getActivePerformances(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerformanceResponseDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(performanceService.getOne(id));
    }
}
