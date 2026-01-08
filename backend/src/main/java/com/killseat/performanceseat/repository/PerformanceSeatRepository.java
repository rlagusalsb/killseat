package com.killseat.performanceseat.repository;

import com.killseat.performanceseat.entity.PerformanceSeat;
import com.killseat.performanceseat.entity.PerformanceSeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PerformanceSeatRepository extends JpaRepository<PerformanceSeat, Long> {
    List<PerformanceSeat> findByPerformance_PerformanceId(Long performanceId);

    @Query("""
        select distinct ps
          from PerformanceSeat ps
          join fetch ps.seat
          join fetch ps.performance
         where ps.performance.performanceId = :performanceId
    """)
    List<PerformanceSeat> findAllWithSeatByPerformanceId(@Param("performanceId") Long performanceId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PerformanceSeat ps
           set ps.status = :to
         where ps.performanceSeatId = :id
           and ps.status = :from
    """)
    int updateStatusIfMatch(@Param("id") Long performanceSeatId,
                            @Param("from") PerformanceSeatStatus from,
                            @Param("to") PerformanceSeatStatus to);
}
