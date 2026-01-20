package com.killseat.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequestDto {
    @NotBlank
    private String merchantUid;

    @NotBlank
    private String impUid;
}
