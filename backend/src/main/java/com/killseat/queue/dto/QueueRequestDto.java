package com.killseat.queue.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QueueRequestDto {
    private Long performanceId;
    private Long scheduleId;
    private Long userId;
}
