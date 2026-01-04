package com.killseat.performanceseat.repository;

import com.killseat.performanceseat.entity.PerformanceSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PerformanceSeatRepository extends JpaRepository<PerformanceSeat, Long> {
    List<PerformanceSeat> findByPerformance_PerformanceId(Long performanceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ps from PerformanceSeat ps where ps.performanceSeatId = :id")
    Optional<PerformanceSeat> findByIdForUpdate(@Param("id") Long id);
}
