package com.killseat.performance.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "performance_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerformanceSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id")
    private Performance performance;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Builder
    public PerformanceSchedule(LocalDateTime startTime, LocalDateTime endTime, Performance performance) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.performance = performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    public void updateTime(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
