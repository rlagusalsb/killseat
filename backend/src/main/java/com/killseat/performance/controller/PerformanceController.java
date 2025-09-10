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
    public ResponseEntity<List<PerformanceResponseDto>> getAll() {
        return ResponseEntity.ok(performanceService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerformanceResponseDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(performanceService.getOne(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PerformanceResponseDto> create(@RequestBody PerformanceRequestDto request) {
        PerformanceResponseDto response = performanceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PerformanceResponseDto> update(@PathVariable Long id, @RequestBody PerformanceRequestDto request) {
        PerformanceResponseDto response = performanceService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        performanceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
