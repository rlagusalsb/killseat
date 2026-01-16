package com.killseat.seat.service;

import com.killseat.common.exception.CustomErrorCode;
import com.killseat.common.exception.CustomException;
import com.killseat.seat.dto.SeatRequestDto;
import com.killseat.seat.dto.SeatResponseDto;
import com.killseat.seat.entity.Seat;
import com.killseat.seat.repository.SeatRepository;
import com.killseat.seat.service.mapper.SeatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final SeatMapper seatMapper;

    @Transactional
    public SeatResponseDto createSeat(SeatRequestDto request) {
        if (seatRepository.existsBySeatNumber(request.getSeatNumber())) {
            throw new CustomException(CustomErrorCode.INVALID_INPUT_FORMAT);
        }
        Seat seat = seatMapper.toEntity(request);
        Seat saved = seatRepository.save(seat);
        return seatMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SeatResponseDto> getAllSeats() {
        return seatRepository.findAll()
                .stream()
                .map(seatMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SeatResponseDto getSeat(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.SEAT_NOT_FOUND));
        return seatMapper.toDto(seat);
    }

    @Transactional
    public SeatResponseDto updateSeat(Long id, SeatRequestDto request) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.SEAT_NOT_FOUND));
        seat.updateSeatNumber(request.getSeatNumber());
        return seatMapper.toDto(seat);
    }

    @Transactional
    public void deleteSeat(Long id) {
        seatRepository.deleteById(id);
    }
}
