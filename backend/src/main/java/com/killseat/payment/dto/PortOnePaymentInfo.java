package com.killseat.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PortOnePaymentInfo {
    private String impUid;
    private String merchantUid;
    private String status;
    private Integer amount;
}
