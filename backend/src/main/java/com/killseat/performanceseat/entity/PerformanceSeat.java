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
    private Long performanceSeatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerformanceSeatStatus status = PerformanceSeatStatus.AVAILABLE;

    public void updateStatus(PerformanceSeatStatus status) {
        this.status = status;
    }

    @Builder
    private PerformanceSeat(Performance performance, Seat seat, PerformanceSeatStatus status) {
        this.performance = performance;
        this.seat = seat;
        this.status = (status != null) ? status : PerformanceSeatStatus.AVAILABLE;
    }

    //결제 전 선점
    public void hold() {
        if (this.status != PerformanceSeatStatus.AVAILABLE) {
            throw new IllegalStateException("예약할 수 없는 좌석입니다.");
        }
        this.status = PerformanceSeatStatus.HELD;
    }

    //결제 성공
    public void confirm() {
        if (this.status != PerformanceSeatStatus.HELD) {
            throw new IllegalStateException("선점된 좌석만 확정할 수 있습니다.");
        }
        this.status = PerformanceSeatStatus.RESERVED;
    }

    //선점 해제
    public void releaseHold() {
        if (this.status != PerformanceSeatStatus.HELD) {
            throw new IllegalStateException("선점 상태만 해제할 수 있습니다.");
        }
        this.status = PerformanceSeatStatus.AVAILABLE;
    }

    public void cancel() {
        if (this.status != PerformanceSeatStatus.RESERVED) {
            throw new IllegalStateException("예약되지 않은 좌석만 취소할 수 있습니다.");
        }
        this.status = PerformanceSeatStatus.AVAILABLE;
    }

    public void block() {
        if (this.status == PerformanceSeatStatus.RESERVED) {
            throw new IllegalStateException("이미 예약된 좌석은 블록할 수 없습니다.");
        }
        if (this.status == PerformanceSeatStatus.BLOCKED) {
            throw new IllegalStateException("이미 블록된 좌석입니다.");
        }
        this.status = PerformanceSeatStatus.BLOCKED;
    }

    public void unblock() {
        if (this.status != PerformanceSeatStatus.BLOCKED) {
            throw new IllegalStateException("블록 상태가 아닌 좌석은 해제할 수 없습니다.");
        }
        this.status = PerformanceSeatStatus.AVAILABLE;
    }
}
