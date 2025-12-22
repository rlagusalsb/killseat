package com.killseat.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentPrepareResponseDto {
    private Long paymentId;
    private String merchantUid;
    private Integer amount;
    private String name;
}
