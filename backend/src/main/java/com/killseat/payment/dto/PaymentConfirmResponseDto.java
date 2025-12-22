package com.killseat.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentConfirmResponseDto {
    private boolean paid;
    private Long paymentId;
    private String paymentStatus;
    private String reservationStatus;
    private String message;
}
