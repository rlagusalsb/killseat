package com.killseat.reservation.repository;

import com.killseat.reservation.entity.Reservation;
import com.killseat.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    //예약 상세 조회
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

    //단건 상태 변경
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

    //PENDING상태만 만료 처리 대상으로 조회
    @Query("""
        select r
          from Reservation r
          join fetch r.performanceSeat ps
         where r.status = com.killseat.reservation.entity.ReservationStatus.PENDING
           and r.expiresAt < :now
    """)
    List<Reservation> findExpiredPendingOnly(@Param("now") LocalDateTime now);

    //마이페이지 예약 목록 조회
    @Query(value = """
        select r
          from Reservation r
          join fetch r.performanceSeat ps
          join fetch ps.seat s
          join fetch ps.performance p
         where r.member.memberId = :memberId
           and r.status <> :excludedStatus
         order by r.createdAt desc
    """,
            countQuery = """
        select count(r)
          from Reservation r
         where r.member.memberId = :memberId
           and r.status <> :excludedStatus
    """)
    Page<Reservation> findMyPageReservations(
            @Param("memberId") Long memberId,
            @Param("excludedStatus") ReservationStatus excludedStatus,
            Pageable pageable
    );

    //결제 준비 조회
    @Query("""
        select r
          from Reservation r
          join fetch r.performanceSeat ps
          join fetch ps.performance p
         where r.reservationId = :id
    """)
    Reservation findForPaymentPrepare(@Param("id") Long reservationId);

    //만료된 좌석 ID 리스트 조회
    @Query("""
        select ps.performanceSeatId
          from Reservation r
          join r.performanceSeat ps
         where r.status = :status
           and r.expiresAt < :now
    """)
    List<Long> findSeatIdsByExpiredReservation(@Param("status") ReservationStatus status,
                                               @Param("now") LocalDateTime now);

    //예약 상태 일괄 변경
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Reservation r
           set r.status = :to,
               r.updatedAt = :now
         where r.status = :from
           and r.expiresAt < :now
    """)
    int bulkUpdateStatusForExpired(@Param("from") ReservationStatus from,
                                   @Param("to") ReservationStatus to,
                                   @Param("now") LocalDateTime now);
}
