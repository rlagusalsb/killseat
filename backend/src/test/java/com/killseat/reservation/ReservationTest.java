package com.killseat.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.killseat.config.CustomUserDetails;
import com.killseat.member.entity.Member;
import com.killseat.member.entity.Role;
import com.killseat.member.repository.MemberRepository;
import com.killseat.performance.entity.Performance;
import com.killseat.performance.entity.PerformanceStatus;
import com.killseat.performance.repository.PerformanceRepository;
import com.killseat.performanceseat.entity.PerformanceSeat;
import com.killseat.performanceseat.entity.PerformanceSeatStatus;
import com.killseat.performanceseat.repository.PerformanceSeatRepository;
import com.killseat.seat.entity.Seat;
import com.killseat.seat.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Rollback
@Transactional
class ReservationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PerformanceRepository performanceRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    PerformanceSeatRepository performanceSeatRepository;

    Long performanceSeatId;
    CustomUserDetails principal;

    @BeforeEach
    void setUp() {
        //테스트 유저 생성
        Member savedMember = memberRepository.save(
                Member.builder()
                        .email("test" + System.currentTimeMillis() + "@test.com")
                        .password("pw")
                        .name("테스터")
                        .role(Role.USER)
                        .build()
        );
        principal = new CustomUserDetails(savedMember);

        //공연 생성
        Performance savedPerformance = performanceRepository.save(
                Performance.builder()
                        .title("테스트 공연")
                        .startTime(LocalDateTime.now().plusDays(1))
                        .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                        .status(PerformanceStatus.OPEN)
                        .build()
        );

        //좌석 생성
        Seat savedSeat = seatRepository.save(
                Seat.builder()
                        .seatNumber("A-1")
                        .build()
        );

        //공연 좌석 생성
        PerformanceSeat savedPerformanceSeat = performanceSeatRepository.save(
                PerformanceSeat.builder()
                        .performance(savedPerformance)
                        .seat(savedSeat)
                        .status(PerformanceSeatStatus.AVAILABLE)
                        .build()
        );

        performanceSeatId = savedPerformanceSeat.getPerformanceSeatId();
    }

    //예약 성공
    @Test
    void reserve_seat_success() throws Exception {
        mockMvc.perform(
                post("/api/reservations/{id}", performanceSeatId)
                        .with(user(principal))
                        .with(csrf())
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").isNumber())
                .andExpect(jsonPath("$.performanceSeatId").value(performanceSeatId));
    }

    //예약 취소 성공
    @Test
    void cancel_reservation_success() throws Exception {
        // 예약
        String response = mockMvc.perform(
                        post("/api/reservations/{id}", performanceSeatId)
                                .with(user(principal))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long reservationId = objectMapper.readTree(response)
                .get("reservationId")
                .asLong();

        //취소
        mockMvc.perform(
                        delete("/api/reservations/{id}", reservationId)
                                .with(user(principal))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId))
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    //예약 조회
    @Test
    void get_my_reservations() throws Exception {
        //예약 생성
        mockMvc.perform(
                post("/api/reservations/{id}", performanceSeatId)
                        .with(user(principal))
                        .with(csrf())
        ).andExpect(status().isOk());

        //마이페이지 조회
        mockMvc.perform(
                        get("/api/mypage/reservations")
                                .with(user(principal))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].performanceSeatId").value(performanceSeatId))
                .andExpect(jsonPath("$[0].status").exists());
    }
}
