package com.killseat.mypage.controller;

import com.killseat.config.CustomUserDetails;
import com.killseat.reservation.dto.ReservationResponseDto;
import com.killseat.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final ReservationService reservationService;

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponseDto>> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        List<ReservationResponseDto> reservations =
                reservationService.getMyReservations(user.getMemberId());
        return ResponseEntity.ok(reservations);
    }
}
