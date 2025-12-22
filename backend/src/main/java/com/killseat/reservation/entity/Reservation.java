package com.killseat.reservation.entity;

import com.killseat.member.entity.Member;
import com.killseat.payment.entity.Payment;
import com.killseat.performanceseat.entity.PerformanceSeat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservation",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"performance_seat_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_seat_id", nullable = false)
    private PerformanceSeat performanceSeat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.PENDING;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    private Reservation(Member member, PerformanceSeat performanceSeat, ReservationStatus status) {
        this.member = member;
        this.performanceSeat = performanceSeat;
        this.status = (status != null) ? status : ReservationStatus.PENDING;
    }

    public void addPayment(Payment payment) {
        this.payments.add(payment);
        if (payment.getReservation() != this) {
            payment.linkReservation(this);
        }
    }

    //상태 변경 메서드
    //결제 성공
    public void confirm() {
        if (this.status != ReservationStatus.PENDING) {
            throw new IllegalStateException("예약을 확정할 수 없는 상태입니다.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    //결제 실패
    public void payFailed() {
        if (this.status != ReservationStatus.PENDING) {
            throw new IllegalStateException("결제 실패 처리가 불가능합니다.");
        }
    }

    //결제 전 예약 취소
    public void cancelBeforePayment() {
        if (this.status == ReservationStatus.CANCELED) {
            return;
        }

        if (this.status != ReservationStatus.PENDING) {
            throw new IllegalStateException("이미 결제가 진행된 예약은 취소할 수 없습니다.");
        }

        this.status = ReservationStatus.CANCELED;
    }

    //결제 후 예약 취소
    public void cancelAfterPayment() {
        if (this.status == ReservationStatus.CANCELED) {
            return;
        }

        if (this.status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("결제 완료 건만 취소 가능합니다.");
        }

        this.status = ReservationStatus.CANCELED;
    }
}
