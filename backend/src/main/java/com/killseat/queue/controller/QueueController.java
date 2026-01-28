package com.killseat.queue.controller;

import com.killseat.config.CustomUserDetails;
import com.killseat.queue.dto.QueueRequestDto;
import com.killseat.queue.dto.QueueResponseDto;
import com.killseat.queue.service.QueueNotificationService;
import com.killseat.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;
    private final QueueNotificationService queueNotificationService;

    @PostMapping("/join")
    public ResponseEntity<Void> join(
            @RequestBody QueueRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        queueService.addQueue(request.getPerformanceId(), request.getScheduleId(), user.getMemberId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<QueueResponseDto> getStatus(
            @RequestParam Long performanceId,
            @RequestParam Long scheduleId,
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        QueueResponseDto response = queueService.getQueueStatus(performanceId, scheduleId, user.getMemberId());
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/subscribe/{performanceId}/{scheduleId}", produces = "text/event-stream")
    public SseEmitter subscribe(
            @PathVariable Long performanceId,
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        return queueNotificationService.subscribe(performanceId, scheduleId, user.getMemberId());
    }
}