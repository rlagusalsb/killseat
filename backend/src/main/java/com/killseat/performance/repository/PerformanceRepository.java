package com.killseat.performance.repository;

import com.killseat.performance.entity.Performance;
import com.killseat.performance.entity.PerformanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    List<Performance> findAllByStatus(PerformanceStatus status);
}
