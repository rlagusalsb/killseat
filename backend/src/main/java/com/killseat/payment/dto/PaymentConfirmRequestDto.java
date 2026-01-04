package com.killseat.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentConfirmRequestDto {
    @NotBlank
    private String merchantUid;

    @NotBlank
    private String impUid;
}
