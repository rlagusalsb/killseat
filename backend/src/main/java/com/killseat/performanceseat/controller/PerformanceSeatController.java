package com.killseat.performanceseat.controller;

import com.killseat.performanceseat.dto.PerformanceSeatResponseDto;
import com.killseat.performanceseat.service.PerformanceSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performance-seats")
@RequiredArgsConstructor
public class PerformanceSeatController {

    private final PerformanceSeatService performanceSeatService;

    @GetMapping("/{performanceId}")
    public ResponseEntity<List<PerformanceSeatResponseDto>> getSeatsByPerformance(
            @PathVariable Long performanceId) {
        List<PerformanceSeatResponseDto> seats =
                performanceSeatService.getSeatsByPerformance(performanceId);
        return ResponseEntity.ok(seats);
    }

    @PostMapping("/{performanceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createPerformanceSeats(@PathVariable Long performanceId) {
        performanceSeatService.createPerformanceSeats(performanceId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{performanceSeatId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockSeat(@PathVariable Long performanceSeatId) {
        performanceSeatService.blockSeat(performanceSeatId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{performanceSeatId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unblockSeat(@PathVariable Long performanceSeatId) {
        performanceSeatService.unblockSeat(performanceSeatId);
        return ResponseEntity.ok().build();
    }
}
