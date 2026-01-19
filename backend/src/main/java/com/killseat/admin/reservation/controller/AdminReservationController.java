package com.killseat.admin.reservation.controller;

import com.killseat.admin.reservation.dto.AdminReservationDto;
import com.killseat.reservation.dto.ReservationResponseDto;
import com.killseat.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<Page<AdminReservationDto>> getAllReservations(
            @PageableDefault(size = 10, sort = "reservationId", direction = Sort.Direction.DESC) Pageable pageable
    )
    {
        Page<AdminReservationDto> reservations = reservationService.getAllReservationsForAdmin(pageable);
        return ResponseEntity.ok(reservations);
    }

    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancelReservationByAdmin(@PathVariable Long reservationId) {
        reservationService.cancelReservationByAdmin(reservationId);
        return ResponseEntity.ok().build();
    }
}
