package com.killseat.admin.performance.controller;

import com.killseat.admin.performance.dto.AdminPerformanceRequestDto;
import com.killseat.performance.dto.PageResponse;
import com.killseat.performance.dto.PerformanceResponseDto;
import com.killseat.performance.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PageResponse<PerformanceResponseDto>> getAllPerformances(
            @PageableDefault(size = 20, sort = "performanceId", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(performanceService.getAllForAdmin(pageable));
    }

    @PatchMapping("/{id}/open")
    public ResponseEntity<Void> openPerformance(@PathVariable Long id) {
        performanceService.openPerformance(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> closePerformance(@PathVariable Long id) {
        performanceService.closePerformance(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PerformanceResponseDto> updatePerformance(
            @PathVariable Long id,
            @RequestBody AdminPerformanceRequestDto request) {
        return ResponseEntity.ok(performanceService.updatePerformance(id, request));
    }
}
