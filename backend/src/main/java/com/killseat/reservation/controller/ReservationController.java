package com.killseat.reservation.controller;

import com.killseat.config.CustomUserDetails;
import com.killseat.reservation.dto.ReservationResponseDto;
import com.killseat.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{performanceSeatId}")
    public ResponseEntity<ReservationResponseDto> reserveSeat(
            @PathVariable Long performanceSeatId,
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        ReservationResponseDto response =
                reservationService.reserveSeat(performanceSeatId, user.getMemberId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reservationId}")
    public ResponseEntity<ReservationResponseDto> cancelReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        ReservationResponseDto response =
                reservationService.cancelReservation(reservationId, user.getMemberId());
        return ResponseEntity.ok(response);
    }
}
