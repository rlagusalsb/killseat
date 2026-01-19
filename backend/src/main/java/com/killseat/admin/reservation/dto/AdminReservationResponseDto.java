package com.killseat.admin.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminReservationResponseDto {
    private Long reservationId;
    private String memberName;
    private String performanceTitle;
    private String seatInfo;
    private String status;
    private String performanceRound;
}
