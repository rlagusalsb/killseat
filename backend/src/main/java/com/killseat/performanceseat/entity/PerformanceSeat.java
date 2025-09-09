package com.killseat.performanceseat.entity;

import com.killseat.seat.entity.Seat;
import com.killseat.performance.entity.Performance;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "performance_seat",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"performance_id", "seat_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerformanceSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long PerformanceSeatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerformanceSeatStatus status = PerformanceSeatStatus.AVAILABLE;

    @Builder
    private PerformanceSeat(Performance performance, Seat seat, PerformanceSeatStatus status) {
        this.performance = performance;
        this.seat = seat;
        this.status = status;
    }
}
