package com.killseat.performanceseat.controller;

import com.killseat.performanceseat.dto.PerformanceSeatResponseDto;
import com.killseat.performanceseat.service.PerformanceSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performance-seats")
@RequiredArgsConstructor
public class PerformanceSeatController {

    private final PerformanceSeatService performanceSeatService;

    @GetMapping("/{performanceId}")
    public ResponseEntity<List<PerformanceSeatResponseDto>> getSeatsByPerformance(
            @PathVariable Long performanceId,
            @RequestParam Long scheduleId
    )
    {
        List<PerformanceSeatResponseDto> seats =
                performanceSeatService.getSeatsByPerformance(scheduleId);
        return ResponseEntity.ok(seats);
    }

    @PostMapping("/{performanceSeatId}/hold")
    public ResponseEntity<Void> holdSeat(@PathVariable Long performanceSeatId) {
        performanceSeatService.holdSeat(performanceSeatId);
        return ResponseEntity.ok().build();
    }
}
