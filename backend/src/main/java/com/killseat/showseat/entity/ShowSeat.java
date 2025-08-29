package com.killseat.showseat.entity;

import com.killseat.seat.entity.Seat;
import com.killseat.show.entity.Show;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "show_seat",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"show_id", "seat_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long showSeatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShowSeatStatus status = ShowSeatStatus.AVAILABLE;

    @Builder
    private ShowSeat(Show show, Seat seat, ShowSeatStatus status) {
        this.show = show;
        this.seat = seat;
        this.status = status;
    }
}
