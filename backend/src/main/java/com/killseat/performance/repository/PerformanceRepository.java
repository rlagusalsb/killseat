package com.killseat.performance.repository;

import com.killseat.performance.entity.Performance;
import com.killseat.performance.entity.PerformanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    Page<Performance> findAllByStatus(PerformanceStatus status, Pageable pageable);

    Page<Performance> findAllByStatusAndTitleContaining(PerformanceStatus status, String title, Pageable pageable);
}
