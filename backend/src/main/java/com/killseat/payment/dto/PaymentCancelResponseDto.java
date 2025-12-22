package com.killseat.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentCancelResponseDto {
    private boolean canceled;
    private Long paymentId;
    private String paymentStatus;
    private String reservationStatus;
    private String message;
}
