package com.killseat.queue;

import com.killseat.queue.service.QueueService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class QueueConcurrencyTest {
    @Autowired
    private QueueService queueService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String WAITING_KEY = "waiting:performance:1";

    @AfterEach
    void tearDown() {
        redisTemplate.delete(WAITING_KEY);
    }

    @Test
    @DisplayName("대기열 100명 동시성 테스트")
    void concurrencyTest() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    queueService.addQueue(1L, userId);
                } catch (Exception e) {
                    System.out.println("에러 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Long size = redisTemplate.opsForZSet().zCard(WAITING_KEY);

        System.out.println("최종 대기열 인원: " + size);
        assertThat(size).isEqualTo(threadCount);
    }
}
