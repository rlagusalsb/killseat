package com.killseat.seat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatResponseDto {
    private Long seatId;
    private String seatNumber;
}
