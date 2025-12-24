package com.killseat.performance.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "performance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long performanceId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerformanceStatus status = PerformanceStatus.BEFORE_OPEN;

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    private Performance(String title, LocalDateTime startTime, LocalDateTime endTime, PerformanceStatus status) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = (status != null) ? status : PerformanceStatus.BEFORE_OPEN;
    }

    public void update(String title, LocalDateTime startTime, LocalDateTime endTime) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void openSales() {
        if (this.status != PerformanceStatus.BEFORE_OPEN) {
            throw new IllegalStateException("예매는 시작 전 상태에서만 열 수 있습니다.");
        }
        this.status = PerformanceStatus.OPEN;
    }

    public void closeSales() {
        if (this.status != PerformanceStatus.OPEN) {
            throw new IllegalStateException("예매 진행 중일 때만 종료할 수 있습니다.");
        }
        this.status = PerformanceStatus.CLOSED;
    }
}
