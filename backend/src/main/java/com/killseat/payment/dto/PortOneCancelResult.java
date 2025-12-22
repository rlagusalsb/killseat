package com.killseat.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PortOneCancelResult {
    private Integer cancelAmount;
    private String message;
}
