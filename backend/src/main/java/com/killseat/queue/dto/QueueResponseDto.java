package com.killseat.queue.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QueueResponseDto {
    private Long userId;
    private Long rank;
    private String status;
}
