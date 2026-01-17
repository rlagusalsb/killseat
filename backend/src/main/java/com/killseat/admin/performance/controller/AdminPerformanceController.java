package com.killseat.admin.performance.controller;

import com.killseat.admin.performance.dto.AdminPerformanceRequestDto;
import com.killseat.performance.dto.PerformanceResponseDto;
import com.killseat.performance.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/performances")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPerformanceController {

    private final PerformanceService performanceService;

    @PostMapping
    public ResponseEntity<PerformanceResponseDto> createPerformance(@RequestBody AdminPerformanceRequestDto request) {
        return ResponseEntity.ok(performanceService.createByAdmin(request));
    }

    @GetMapping
    public ResponseEntity<List<PerformanceResponseDto>> getAllPerformances() {
        return ResponseEntity.ok(performanceService.getAllForAdmin());
    }

    @PatchMapping("/{id}/open")
    public ResponseEntity<Void> openPerformance(@PathVariable Long id) {
        performanceService.openPerformance(id);
        return ResponseEntity.ok().build();
    }
}
