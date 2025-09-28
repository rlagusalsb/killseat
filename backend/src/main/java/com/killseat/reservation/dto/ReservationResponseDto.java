package com.killseat.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationResponseDto {
    private Long reservationId;
    private Long performanceSeatId;
    private String seatNumber;
    private String performanceTitle;
    private String status;
}
