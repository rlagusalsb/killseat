package com.killseat.payment.dto;

import com.killseat.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentPrepareRequestDto {
    @NotNull
    private Long reservationId;

    @NotNull
    private PaymentMethod method;
}
