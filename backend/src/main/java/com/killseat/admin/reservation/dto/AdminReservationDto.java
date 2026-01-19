package com.killseat.admin.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminReservationDto {
    private Long reservationId;
    private String memberName;
    private String performanceTitle;
    private String seatInfo;
    private String status;
}
