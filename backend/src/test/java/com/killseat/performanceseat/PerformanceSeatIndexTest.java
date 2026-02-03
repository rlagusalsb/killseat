package com.killseat.performanceseat;

import com.killseat.performance.entity.Performance;
import com.killseat.performance.entity.PerformanceSchedule;
import com.killseat.performance.entity.PerformanceStatus;
import com.killseat.performance.repository.PerformanceRepository;
import com.killseat.performanceseat.entity.PerformanceSeat;
import com.killseat.performanceseat.entity.PerformanceSeatStatus;
import com.killseat.seat.entity.Seat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class PerformanceSeatIndexTest {

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private EntityManager em;

    @Test
    @Transactional
    @DisplayName("인덱스 적용 테스트")
    void verifyIndexPerformanceWithLargeData() {
        //1. 공연 생성
        Performance performance = Performance.builder()
                .title("테스트 공연")
                .content("더미 공연 데이터")
                .location("테스트 공연장")
                .price(100000L)
                .status(PerformanceStatus.OPEN)
                .build();
        performanceRepository.save(performance);

        //2. 회차 생성
        PerformanceSchedule schedule = PerformanceSchedule.builder()
                .performance(performance)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .build();
        em.persist(schedule);

        //3. 좌석 및 공연-좌석 매핑 데이터 3만 건 생성
        for (int i = 1; i <= 30000; i++) {
            Seat dummySeat = Seat.builder()
                    .seatNumber(String.valueOf(i))
                    .build();
            em.persist(dummySeat);

            PerformanceSeat performanceSeat = PerformanceSeat.builder()
                    .performanceSchedule(schedule)
                    .seat(dummySeat)
                    .status(PerformanceSeatStatus.AVAILABLE)
                    .build();
            em.persist(performanceSeat);

            //1000건마다 메모리 비우기
            if (i % 1000 == 0) {
                em.flush();
                em.clear();
                //영속성 컨텍스트가 비워졌으므로 연관관계를 위해 schedule 다시 로드
                schedule = em.find(PerformanceSchedule.class, schedule.getScheduleId());
            }
        }
        em.flush();

        Query query = em.createNativeQuery(
                "EXPLAIN SELECT performance_schedule_id, seat_id, status " +
                        "FROM performance_seat " +
                        "WHERE performance_schedule_id = :id"
        );
        query.setParameter("id", schedule.getScheduleId());

        Object[] result = (Object[]) query.getSingleResult();

        String accessType = result[4].toString();
        String usedKey = String.valueOf(result[6]);
        long scannedRows = Long.parseLong(result[9].toString());

        System.out.println("\n-------------------------결과----------------------------");
        System.out.println("사용된 인덱스  : " + usedKey);
        System.out.println("접근 방식      : " + accessType);
        System.out.println("데이터 스캔량  : " + scannedRows + "건 / 전체 30,000건");

        double skipRate = (1 - (double) scannedRows / 30000) * 100;
        System.out.printf("인덱스 효율    : %.2f%% 절감\n", skipRate);

        if (result[11] != null && !result[11].toString().isEmpty()) {
            System.out.println("추가 최적화    : " + result[11]);
        }
        System.out.println("-----------------------------------------------------------");

        //Full Scan이면 테스트 실패
        assertThat(accessType).as("인덱스를 사용안함")
                .isNotEqualTo("ALL");
    }
}