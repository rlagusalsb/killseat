package com.killseat.queue.service;

import com.killseat.queue.dto.QueueResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String QUEUE_KEY = "waiting:performance:";

    //대기열 진입
    public void addQueue(Long performanceId, Long userId) {
        String key = QUEUE_KEY + performanceId;
        long now = System.currentTimeMillis();

        redisTemplate.opsForZSet().add(key, userId.toString(), (double) now);
    }

    //현재 순번 조회
    public QueueResponseDto getQueueStatus(Long performanceId, Long userId) {
        String key = QUEUE_KEY + performanceId;
        Long rank = redisTemplate.opsForZSet().rank(key, userId.toString());

        if (rank == null) {
            return new QueueResponseDto(userId, 0L, "NOT_FOUND");
        }

        return new QueueResponseDto(userId, rank + 1, "WAIT");
    }
}
