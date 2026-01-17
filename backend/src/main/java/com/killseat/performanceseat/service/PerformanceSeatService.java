package com.killseat.performanceseat.service;

import com.killseat.common.exception.CustomErrorCode;
import com.killseat.common.exception.CustomException;
import com.killseat.performance.entity.Performance;
import com.killseat.performanceseat.dto.PerformanceSeatResponseDto;
import com.killseat.performanceseat.entity.PerformanceSeat;
import com.killseat.performanceseat.repository.PerformanceSeatRepository;
import com.killseat.performanceseat.service.mapper.PerformanceSeatMapper;
import com.killseat.seat.entity.Seat;
import com.killseat.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformanceSeatService {

    private final SeatRepository seatRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final PerformanceSeatMapper performanceSeatMapper;

    //공연 좌석 생성
    @Transactional
    public void createPerformanceSeats(Performance performance) {
        List<Seat> masterSeats = seatRepository.findAll();

        if (masterSeats.isEmpty()) {
            throw new CustomException(CustomErrorCode.SEAT_NOT_FOUND);
        }

        List<PerformanceSeat> performanceSeats = masterSeats.stream()
                .map(seat -> performanceSeatMapper.toEntity(performance, seat))
                .collect(Collectors.toList());

        performanceSeatRepository.saveAll(performanceSeats);
    }

    //관리자 전용 특정 좌석 차단
    @Transactional
    public void blockSeat(Long id) {
        PerformanceSeat seat = performanceSeatRepository.findByIdWithLock(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.SEAT_NOT_FOUND));
        seat.block();
    }

    //관리자 전용 차단된 좌석 해제
    @Transactional
    public void unblockSeat(Long id) {
        PerformanceSeat seat = performanceSeatRepository.findByIdWithLock(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.SEAT_NOT_FOUND));
        seat.unblock();
    }

    //좌석 선점
    @Transactional
    public void holdSeat(Long id) {
        PerformanceSeat seat = performanceSeatRepository.findByIdWithLock(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.SEAT_NOT_FOUND));

        seat.hold();
    }

    //결제 완료 후 자리 확정
    @Transactional
    public void confirmSeat(Long id) {
        PerformanceSeat seat = performanceSeatRepository.findByIdWithLock(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.SEAT_NOT_FOUND));

        seat.confirm();
    }

    //선점 해제
    @Transactional
    public void releaseSeat(Long id) {
        PerformanceSeat seat = performanceSeatRepository.findByIdWithLock(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.SEAT_NOT_FOUND));
        seat.releaseHold();
    }

    //예약 취소
    @Transactional
    public void cancelSeat(Long id) {
        PerformanceSeat seat = performanceSeatRepository.findByIdWithLock(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.SEAT_NOT_FOUND));
        seat.cancel();
    }

    //특정 공연 좌석 목록 조회
    @Transactional(readOnly = true)
    public List<PerformanceSeatResponseDto> getSeatsByPerformance(Long performanceId) {
        return performanceSeatRepository.findAllWithSeatByPerformanceId(performanceId).stream()
                .map(performanceSeatMapper::toDto)
                .collect(Collectors.toList());
    }
}
