package com.killseat.seat.repository;

import com.killseat.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findBySeatNumber(String seatNumber);
    boolean existsBySeatNumber(String seatNumber);
}
