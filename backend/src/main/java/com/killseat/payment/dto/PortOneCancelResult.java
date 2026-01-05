package com.killseat.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PortOneCancelResult {
    private Long cancelAmount;
    private String message;
}
