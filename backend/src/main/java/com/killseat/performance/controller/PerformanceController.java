package com.killseat.performance.controller;

import com.killseat.performance.dto.PerformanceRequestDto;
import com.killseat.performance.dto.PerformanceResponseDto;
import com.killseat.performance.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping
    public ResponseEntity<List<PerformanceResponseDto>> getActivePerformances() {
        return ResponseEntity.ok(performanceService.getActivePerformances());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerformanceResponseDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(performanceService.getOne(id));
    }
}
