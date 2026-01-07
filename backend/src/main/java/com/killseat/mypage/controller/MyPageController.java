package com.killseat.mypage.controller;

import com.killseat.config.CustomUserDetails;
import com.killseat.mypage.dto.MyPageReservationDto;
import com.killseat.payment.dto.PaymentCancelRequestDto;
import com.killseat.payment.dto.PaymentCancelResponseDto;
import com.killseat.payment.service.PaymentService;
import com.killseat.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final ReservationService reservationService;
    private final PaymentService paymentService;

    @GetMapping("/reservations")
    public ResponseEntity<List<MyPageReservationDto>> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        List<MyPageReservationDto> reservations =
                reservationService.getMyPageReservations(user.getMemberId());

        return ResponseEntity.ok(reservations);
    }

    @PostMapping("/payments/{paymentId}/cancel")
    public ResponseEntity<PaymentCancelResponseDto> cancelPayment(
            @PathVariable Long paymentId,
            @RequestBody PaymentCancelRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        PaymentCancelRequestDto fixed =
                new PaymentCancelRequestDto(paymentId, request.getReason());

        PaymentCancelResponseDto response =
                paymentService.cancel(fixed, user.getMemberId());

        return ResponseEntity.ok(response);
    }
}
