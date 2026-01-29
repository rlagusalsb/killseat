package com.killseat.queue;

import com.killseat.queue.service.QueueService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class QueueConcurrencyTest {
    @Autowired
    private QueueService queueService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final Long PERFORMANCE_ID = 1L;
    private final Long SCHEDULE_ID = 1L;
    private final String WAITING_KEY = String.format("waiting:performance:%d:schedule:%d", PERFORMANCE_ID, SCHEDULE_ID);

    @AfterEach
    void tearDown() {
        redisTemplate.delete(WAITING_KEY);
    }

    @Test
    @DisplayName("대기열 100명 동시성 테스트")
    void concurrencyTest() throws InterruptedException {
        //given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            long userId = (long) i;
            executorService.submit(() -> {
                try {
                    queueService.addQueue(PERFORMANCE_ID, SCHEDULE_ID, userId);
                } catch (Exception e) {
                    System.err.println("에러 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        //then
        Long size = redisTemplate.opsForZSet().zCard(WAITING_KEY);

        System.out.println("=====테스트 결과=====");
        System.out.println("최종 대기열 인원: " + size);
        assertThat(size).isEqualTo((long) threadCount);
    }
}
