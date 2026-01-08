package com.killseat.performanceseat.service;

import com.killseat.performance.entity.Performance;
import com.killseat.performance.repository.PerformanceRepository;
import com.killseat.performanceseat.dto.PerformanceSeatResponseDto;
import com.killseat.performanceseat.entity.PerformanceSeat;
import com.killseat.performanceseat.entity.PerformanceSeatStatus;
import com.killseat.performanceseat.repository.PerformanceSeatRepository;
import com.killseat.performanceseat.service.mapper.PerformanceSeatMapper;
import com.killseat.seat.entity.Seat;
import com.killseat.seat.repository.SeatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceSeatService {

    private final PerformanceRepository performanceRepository;
    private final SeatRepository seatRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final PerformanceSeatMapper performanceSeatMapper;

    @Transactional
    public void createPerformanceSeats(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new EntityNotFoundException("공연을 찾을 수 없습니다."));

        List<Seat> seats = seatRepository.findAll();

        List<PerformanceSeat> performanceSeats = seats.stream()
                .map(seat -> PerformanceSeat.builder()
                        .performance(performance)
                        .seat(seat)
                        .status(PerformanceSeatStatus.AVAILABLE)
                        .build())
                .toList();

        performanceSeatRepository.saveAll(performanceSeats);
    }

    @Transactional(readOnly = true)
    public List<PerformanceSeatResponseDto> getSeatsByPerformance(Long performanceId) {
        List<PerformanceSeat> seats = performanceSeatRepository.findAllWithSeatByPerformanceId(performanceId);

        return seats.stream()
                .map(performanceSeatMapper::toDto)
                .toList();
    }

    @Transactional
    public void blockSeat(Long performanceSeatId) {
        PerformanceSeat seat = performanceSeatRepository.findById(performanceSeatId)
                .orElseThrow(() -> new EntityNotFoundException("좌석을 찾을 수 없습니다."));
        seat.block();
    }

    @Transactional
    public void unblockSeat(Long performanceSeatId) {
        PerformanceSeat seat = performanceSeatRepository.findById(performanceSeatId)
                .orElseThrow(() -> new EntityNotFoundException("좌석을 찾을 수 없습니다."));
        seat.unblock();
    }
}
