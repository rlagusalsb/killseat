package com.killseat.seat.repository;

import com.killseat.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    boolean existsBySeatNumber(String seatNumber);
}
