package com.killseat.reservation.repository;

import com.killseat.reservation.entity.Reservation;
import com.killseat.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByMember_MemberId(Long memberId);

    @Query("""
        select r
          from Reservation r
          join fetch r.member m
          join fetch r.performanceSeat ps
          join fetch ps.seat s
          join fetch ps.performance p
         where r.reservationId = :id
    """)
    Reservation findDetailById(@Param("id") Long reservationId);

    //상태 조건부 변경
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Reservation r
           set r.status = :to,
               r.updatedAt = :now
         where r.reservationId = :id
           and r.status = :from
    """)
    int updateStatusIfMatch(@Param("id") Long reservationId,
                            @Param("from") ReservationStatus from,
                            @Param("to") ReservationStatus to,
                            @Param("now") LocalDateTime now);

    //만료된 PENDING 예약 조회
    @Query("""
        select r
          from Reservation r
          join fetch r.performanceSeat ps
         where r.status = com.killseat.reservation.entity.ReservationStatus.PENDING
           and r.expiresAt < :now
    """)
    List<Reservation> findExpiredPendings(@Param("now") LocalDateTime now);
}
