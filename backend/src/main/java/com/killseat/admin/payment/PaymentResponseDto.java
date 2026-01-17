package com.killseat.admin.payment;

import com.killseat.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PaymentResponseDto {
    private Long paymentId;
    private String merchantUid;
    private Long amount;
    private String buyerName;
    private String buyerEmail;
    private PaymentStatus status;
    private String performanceTitle;
    private LocalDateTime createdAt;
}
