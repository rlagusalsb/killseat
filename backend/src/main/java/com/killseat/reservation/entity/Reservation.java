package com.killseat.reservation.entity;

import com.killseat.member.entity.Member;
import com.killseat.payment.entity.Payment;
import com.killseat.showseat.entity.ShowSeat;
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
            @UniqueConstraint(columnNames = {"show_seat_id"})
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
    @JoinColumn(name = "show_seat_id", nullable = false)
    private ShowSeat showSeat;

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
    private Reservation(Member member, ShowSeat showSeat, ReservationStatus status) {
        this.member = member;
        this.showSeat = showSeat;
        this.status = (status != null) ? status : ReservationStatus.PENDING;
    }

    public void addPayment(Payment payment) {
        this.payments.add(payment);
        if (payment.getReservation() != this) {
            payment.linkReservation(this);
        }
    }
}
