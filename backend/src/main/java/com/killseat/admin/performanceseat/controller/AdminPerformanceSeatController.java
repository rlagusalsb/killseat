package com.killseat.admin.performanceseat.controller;

import com.killseat.performanceseat.dto.PerformanceSeatResponseDto;
import com.killseat.performanceseat.service.PerformanceSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/performance-seats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPerformanceSeatController {

    private final PerformanceSeatService performanceSeatService;

    @GetMapping("/performance/{performanceId}")
    public ResponseEntity<List<PerformanceSeatResponseDto>> getSeatsByPerformance(
            @PathVariable Long performanceId) {
        return ResponseEntity.ok(performanceSeatService.getSeatsByPerformance(performanceId));
    }

    @PatchMapping("/{performanceSeatId}/block")
    public ResponseEntity<Void> blockSeat(@PathVariable Long performanceSeatId) {
        performanceSeatService.blockSeat(performanceSeatId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{performanceSeatId}/unblock")
    public ResponseEntity<Void> unblockSeat(@PathVariable Long performanceSeatId) {
        performanceSeatService.unblockSeat(performanceSeatId);
        return ResponseEntity.ok().build();
    }
}
