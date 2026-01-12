package com.killseat.queue.controller;

import com.killseat.config.CustomUserDetails;
import com.killseat.queue.dto.QueueRequestDto;
import com.killseat.queue.dto.QueueResponseDto;
import com.killseat.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/join")
    public ResponseEntity<Void> join(
            @RequestBody QueueRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        queueService.addQueue(request.getPerformanceId(), user.getMemberId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<QueueResponseDto> getStatus(
            @RequestParam Long performanceId,
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        QueueResponseDto response = queueService.getQueueStatus(performanceId, user.getMemberId());
        return ResponseEntity.ok(response);
    }
}
