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

    @Column(name = "merchant_uid", nullable = false, unique = true, length = 64)
    private String merchantUid;

    @Column(name = "imp_uid", length = 64)
    private String impUid;

    @Column(nullable = false)
    private Long amount;

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
    private Payment(Reservation reservation, Long amount, PaymentMethod method) {
        this.reservation = reservation;
        this.amount = amount;
        this.method = method;
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
}
