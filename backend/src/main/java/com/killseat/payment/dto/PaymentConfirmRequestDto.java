package com.killseat.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentConfirmRequestDto {
    @NotBlank
    String merchantUid;

    @NotBlank
    String impUid;
}
