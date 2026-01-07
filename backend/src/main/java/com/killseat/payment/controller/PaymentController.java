package com.killseat.payment.controller;

import com.killseat.payment.dto.*;
import com.killseat.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    //결제 준비
    @PostMapping("/prepare")
    public ResponseEntity<PaymentPrepareResponseDto> prepare(@RequestBody PaymentPrepareRequestDto request) {
        PaymentPrepareResponseDto response = paymentService.prepare(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //결제 확정
    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponseDto> confirm(@RequestBody PaymentConfirmRequestDto request) {
        PaymentConfirmResponseDto response = paymentService.confirm(request);
        return ResponseEntity.ok(response);
    }
}
