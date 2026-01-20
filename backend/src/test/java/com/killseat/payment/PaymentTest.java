package com.killseat.payment;

import com.killseat.member.entity.Member;
import com.killseat.member.entity.Role;
import com.killseat.member.repository.MemberRepository;
import com.killseat.payment.dto.PaymentConfirmRequestDto;
import com.killseat.payment.dto.PaymentPrepareRequestDto;
import com.killseat.payment.dto.PaymentPrepareResponseDto;
import com.killseat.payment.dto.PortOnePaymentInfo;
import com.killseat.payment.entity.PaymentMethod;
import com.killseat.payment.service.PaymentService;
import com.killseat.performanceseat.entity.PerformanceSeat;
import com.killseat.performanceseat.entity.PerformanceSeatStatus;
import com.killseat.performanceseat.repository.PerformanceSeatRepository;
import com.killseat.reservation.dto.ReservationResponseDto;
import com.killseat.reservation.entity.Reservation;
import com.killseat.reservation.entity.ReservationStatus;
import com.killseat.reservation.repository.ReservationRepository;
import com.killseat.reservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
public class PaymentTest {
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PerformanceSeatRepository performanceSeatRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private PortOneClient portOneClient;

    private Member testMember;
    private final Long targetSeatId = 14L;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .email("user@killseat.com")
                .name("tester")
                .password("testpassword")
                .role(Role.USER)
                .build());

        PerformanceSeat seat = performanceSeatRepository.findById(targetSeatId).orElseThrow();
        seat.updateStatus(PerformanceSeatStatus.AVAILABLE);
        performanceSeatRepository.saveAndFlush(seat);
    }

    @Test
    @DisplayName("결제 완료 시 예약은 CONFIRMED, 좌석은 RESERVED 상태로 전환")
    void paymentSuccessFlow() {
        //When
        ReservationResponseDto response = reservationService.reserveSeat(targetSeatId, testMember.getMemberId());

        //Then
        assertThat(response.getStatus().toString()).isEqualTo(ReservationStatus.PENDING.name());

        PerformanceSeat seat = performanceSeatRepository.findById(targetSeatId).get();
        assertThat(seat.getStatus()).isEqualTo(PerformanceSeatStatus.HELD);
    }

    @Test
    @DisplayName("예약 취소 시 좌석이 AVAILABLE 상태로 복구")
    void cancelRestoreFlow() {
        //Given
        ReservationResponseDto response = reservationService.reserveSeat(targetSeatId, testMember.getMemberId());
        Long reservationId = response.getReservationId();

        //When
        reservationService.cancelReservation(reservationId, testMember.getMemberId());

        //Then
        PerformanceSeat seat = performanceSeatRepository.findById(targetSeatId).get();
        assertThat(seat.getStatus()).isEqualTo(PerformanceSeatStatus.AVAILABLE);

        Reservation reservation = reservationRepository.findById(reservationId).get();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("결제 서버 검증 성공 시 최종 확정")
    void realPaymentConfirmTest() {
        //Given
        ReservationResponseDto res = reservationService.reserveSeat(targetSeatId, testMember.getMemberId());
        PaymentPrepareRequestDto prepReq = new PaymentPrepareRequestDto(res.getReservationId(), PaymentMethod.CARD);
        PaymentPrepareResponseDto prepRes = paymentService.prepare(prepReq);

        PaymentConfirmRequestDto confirmReq = new PaymentConfirmRequestDto(
                prepRes.getMerchantUid(),
                "imp_123"
        );

        //When
        PortOnePaymentInfo mockInfo = new PortOnePaymentInfo(
                "imp_123", prepRes.getMerchantUid(), "paid", prepRes.getAmount());
        given(portOneClient.getPaymentInfo("imp_123")).willReturn(mockInfo);

        paymentService.confirm(confirmReq);

        //Then
        Reservation finalRes = reservationRepository.findById(res.getReservationId()).get();
        assertThat(finalRes.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}
