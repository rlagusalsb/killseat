package com.killseat.queue.service;

import com.killseat.common.exception.CustomErrorCode;
import com.killseat.common.exception.CustomException;
import com.killseat.queue.dto.QueueResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String WAITING_KEY = "waiting:performance:";
    private static final String ACTIVE_KEY = "queue:active:";

    //대기열 진입
    public void addQueue(Long performanceId, Long userId) {
        String waitingKey = WAITING_KEY + performanceId;
        String activeKey = ACTIVE_KEY + userId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(activeKey))) {
            throw new CustomException(CustomErrorCode.ALREADY_ACTIVATED_USER);
        }

        Double score = redisTemplate.opsForZSet().score(waitingKey, userId.toString());
        if (score != null) {
            throw new CustomException(CustomErrorCode.ALREADY_IN_WAITING_QUEUE);
        }

        long now = System.currentTimeMillis();

        redisTemplate.opsForZSet().add(waitingKey, userId.toString(), (double) now);
    }

    //현재 순번 조회
    public QueueResponseDto getQueueStatus(Long performanceId, Long userId) {
        String waitingKey = WAITING_KEY + performanceId;
        String activeKey = ACTIVE_KEY + userId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(activeKey))) {
            return new QueueResponseDto(userId, 0L, "PROCEED");
        }

        Long rank = redisTemplate.opsForZSet().rank(waitingKey, userId.toString());

        if (rank == null) {
            return new QueueResponseDto(userId, 0L, "NOT_FOUND");
        }

        return new QueueResponseDto(userId, rank + 1, "WAIT");
    }
}
