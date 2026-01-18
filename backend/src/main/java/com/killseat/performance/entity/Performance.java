package com.killseat.performance.entity;

import com.killseat.common.exception.CustomErrorCode;
import com.killseat.common.exception.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "performance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Performance {

    @Id
    @Column(name = "performance_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long performanceId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerformanceStatus status = PerformanceStatus.BEFORE_OPEN;

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    @OneToMany(mappedBy = "performance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerformanceSchedule> schedules = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    private Performance(String title, String content, String location, Long price,
                        PerformanceStatus status, String thumbnailUrl,
                        List<PerformanceSchedule> schedules) {
        this.title = title;
        this.content = content;
        this.location = location;
        this.price = price;
        this.thumbnailUrl = thumbnailUrl;
        this.status = (status != null) ? status : PerformanceStatus.BEFORE_OPEN;

        if (schedules != null) {
            schedules.forEach(this::addSchedule);
        }
    }

    public void addSchedule(PerformanceSchedule schedule) {
        this.schedules.add(schedule);
        if (schedule.getPerformance() != this) {
            schedule.setPerformance(this);
        }
    }

    public void update(String title, String content, String location, Long price,
                       PerformanceStatus status, String thumbnailUrl,
                       List<PerformanceSchedule> newSchedules) {
        this.title = title;
        this.content = content;
        this.location = location;
        this.price = price;
        this.status = status;
        this.thumbnailUrl = thumbnailUrl;

        this.schedules.clear();
        if (newSchedules != null) {
            newSchedules.forEach(this::addSchedule);
        }
    }

    public void openSales() {
        if (this.status != PerformanceStatus.BEFORE_OPEN) {
            throw new CustomException(CustomErrorCode.INVALID_PERFORMANCE_STATUS);
        }
        this.status = PerformanceStatus.OPEN;
    }

    public void closeSales() {
        if (this.status != PerformanceStatus.OPEN) {
            throw new CustomException(CustomErrorCode.INVALID_PERFORMANCE_STATUS);
        }
        this.status = PerformanceStatus.CLOSED;
    }
}