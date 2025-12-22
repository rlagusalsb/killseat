package com.killseat.payment.entity;

import com.killseat.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false, unique = true, length = 64)
    private String merchantUid;

    @Column(length = 64)
    private String impUid;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    private Payment(Reservation reservation, Integer amount, PaymentMethod method) {
        this.reservation = reservation;
        this.amount = amount;
        this.method = method;
    }

    public void linkReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public void assignMerchantUid(String merchantUid) {
        if (this.merchantUid != null) {
            throw new IllegalStateException("이미 merchantUid가 설정됐습니다.");
        }
        this.merchantUid = merchantUid;
    }

    public void assignImpUid(String impUid) {
        if (this.impUid != null) {
            throw new IllegalStateException("이미 impUid가 설정됐습니다.");
        }
        this.impUid = impUid;
    }

    //결제 성공 시
    public void success() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 가능 상태가 아닙니다.");
        }
        this.status = PaymentStatus.SUCCESS;
    }

    //결제 실패 시
    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 가능 상태가 아닙니다.");
        }
        this.status = PaymentStatus.FAILED;
    }

    //결제 취소 시
    public void cancel() {
        if (this.status == PaymentStatus.CANCELED) {
            return;
        }

        if (this.status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("결제 완료 건만 취소 가능합니다.");
        }

        this.status = PaymentStatus.CANCELED;
    }
}
