package com.killseat.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentCancelRequestDto {
    @NotNull
    private Long paymentId;

    @NotNull
    private String reason;
}
