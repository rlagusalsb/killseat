package com.killseat.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyPageReservationDto {
    private Long reservationId;
    private String performanceTitle;
    private String performanceThumbnailUrl;
    private String performanceLocation;
    private String seatInfo;
    private String reservationStatus;
    private LocalDateTime reservedAt;
    private LocalDateTime performanceStartTime;
}
