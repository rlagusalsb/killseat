package com.killseat.admin.payment.controller;

import com.killseat.admin.payment.dto.PaymentResponseDto;
import com.killseat.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<Page<PaymentResponseDto>> getAllPayments(
            @PageableDefault(size = 10, sort = "paymentId", direction = Sort.Direction.DESC)Pageable pageable
    )
    {
        Page<PaymentResponseDto> payments = paymentService.getAllPaymentsForAdmin(pageable);
        return ResponseEntity.ok(payments);
    }
}
