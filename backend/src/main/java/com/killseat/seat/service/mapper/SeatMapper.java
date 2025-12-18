package com.killseat.seat.service.mapper;

import com.killseat.seat.dto.SeatRequestDto;
import com.killseat.seat.dto.SeatResponseDto;
import com.killseat.seat.entity.Seat;
import org.springframework.stereotype.Component;

@Component
public class SeatMapper {

    public Seat toEntity(SeatRequestDto request) {
        return Seat.builder()
                .seatNumber(request.getSeatNumber())
                .build();
    }

    public SeatResponseDto toDto(Seat seat) {
        return new SeatResponseDto(
                seat.getSeatId(),
                seat.getSeatNumber()
        );
    }
}
