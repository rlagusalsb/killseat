package com.killseat.payment.dto;

import com.killseat.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPrepareRequestDto {
    @NotNull
    private Long reservationId;

    @NotNull
    private PaymentMethod method;
}
