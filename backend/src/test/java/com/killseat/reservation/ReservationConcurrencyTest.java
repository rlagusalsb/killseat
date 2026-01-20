package com.killseat.reservation;

import com.killseat.common.exception.CustomErrorCode;
import com.killseat.common.exception.CustomException;
import com.killseat.member.entity.Member;
import com.killseat.member.entity.Role;
import com.killseat.member.repository.MemberRepository;
import com.killseat.payment.repository.PaymentRepository;
import com.killseat.performanceseat.entity.PerformanceSeat;
import com.killseat.performanceseat.entity.PerformanceSeatStatus;
import com.killseat.performanceseat.repository.PerformanceSeatRepository;
import com.killseat.reservation.repository.ReservationRepository;
import com.killseat.reservation.service.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PerformanceSeatRepository performanceSeatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private final Long targetSeatId = 14L;
    private List<Long> testMembersIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        cleanUp();

        for (int i = 0; i < 100; i++) {
            Member members = memberRepository.save(Member.builder()
                    .email("user" + i + "@killseat.com")
                    .name("tester" + i)
                    .password("testpassword")
                    .role(Role.USER)
                    .build());
            testMembersIds.add(members.getMemberId());
        }

        PerformanceSeat seat = performanceSeatRepository.findById(targetSeatId)
                .orElseThrow(() -> new RuntimeException("좌석이 없습니다."));

        seat.updateStatus(PerformanceSeatStatus.AVAILABLE);
        performanceSeatRepository.saveAndFlush(seat);
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    private void cleanUp() {
        try {
            paymentRepository.deleteAllInBatch();
            reservationRepository.deleteAllInBatch();
            if (!testMembersIds.isEmpty()) {
                memberRepository.deleteAllByIdInBatch(testMembersIds);
                testMembersIds.clear();
            }
        } catch (Exception e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("비관적 락 검증")
    void concurrentReservationTest() throws InterruptedException {
        //Given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        //When
        for (int i = 0; i < threadCount; i++) {
            Long memberId = testMembersIds.get(i);
            executorService.submit(() -> {
                try {
                    reservationService.reserveSeat(targetSeatId, memberId);
                    successCount.incrementAndGet();
                } catch (CustomException e) {
                    if (e.getErrorCode() == CustomErrorCode.SEAT_ALREADY_OCCUPIED) {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.out.println("다른 에러: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        //Then
        System.out.println("=====테스트 결과=====");
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수(이미 점유됨) : " + failCount.get());

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);

        PerformanceSeat finalSeat = performanceSeatRepository.findById(targetSeatId).get();
        assertThat(finalSeat.getStatus()).isNotEqualTo(PerformanceSeatStatus.AVAILABLE);
    }
}
