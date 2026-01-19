package com.killseat.admin.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminReservationDto {
    private Long reservationId;
    private String memberName;
    private String performanceTitle;
    private String seatInfo;
    private String status;
    private String performanceRound;
}
