package com.killseat.performanceseat.repository;

import com.killseat.performanceseat.entity.PerformanceSeat;
import com.killseat.performanceseat.entity.PerformanceSeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PerformanceSeatRepository extends JpaRepository<PerformanceSeat, Long> {
    //좌석 선점
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ps from PerformanceSeat ps where ps.performanceSeatId = :id")
    Optional<PerformanceSeat> findByIdWithLock(@Param("id") Long id);

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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PerformanceSeat ps
           set ps.status = :to
         where ps.performanceSeatId in :ids
           and ps.status = :from
    """)
    int bulkUpdateSeatStatus(@Param("ids") List<Long> performanceSeatIds,
                             @Param("from") PerformanceSeatStatus from,
                             @Param("to") PerformanceSeatStatus to);
}
