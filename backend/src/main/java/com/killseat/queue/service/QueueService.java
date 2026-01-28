package com.killseat.queue.service;

import com.killseat.queue.dto.QueueResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final StringRedisTemplate redisTemplate;

    private static final String WAITING_KEY_FORMAT = "waiting:performance:%d:schedule:%d";
    private static final String ACTIVE_KEY_PREFIX = "queue:active:";

    //대기열 진입
    public void addQueue(Long performanceId, Long scheduleId, Long userId) {
        String waitingKey = String.format(WAITING_KEY_FORMAT, performanceId, scheduleId);
        String activeKey = ACTIVE_KEY_PREFIX + userId;
        String userIdStr = String.valueOf(userId);

        if (Boolean.TRUE.equals(redisTemplate.hasKey(activeKey))) {
            redisTemplate.delete(activeKey);
        }

        Double score = redisTemplate.opsForZSet().score(waitingKey, userIdStr);
        if (score != null) {
            redisTemplate.opsForZSet().remove(waitingKey, userIdStr);
        }

        redisTemplate.opsForZSet().add(waitingKey, userIdStr, (double) System.currentTimeMillis());
    }

    //현재 순번 조회
    public QueueResponseDto getQueueStatus(Long performanceId, Long scheduleId, Long userId) {
        String waitingKey = String.format(WAITING_KEY_FORMAT, performanceId, scheduleId);
        String activeKey = ACTIVE_KEY_PREFIX + userId;
        String userIdStr = String.valueOf(userId);

        if (Boolean.TRUE.equals(redisTemplate.hasKey(activeKey))) {
            return new QueueResponseDto(userId, 0L, "PROCEED");
        }

        Long rank = redisTemplate.opsForZSet().rank(waitingKey, userIdStr);

        if (rank == null) {
            return new QueueResponseDto(userId, 0L, "NOT_FOUND");
        }

        return new QueueResponseDto(userId, rank + 1, "WAIT");
    }
}