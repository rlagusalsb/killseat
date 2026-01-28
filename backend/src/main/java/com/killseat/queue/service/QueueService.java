package com.killseat.queue.service;

import com.killseat.common.exception.CustomErrorCode;
import com.killseat.common.exception.CustomException;
import com.killseat.queue.dto.QueueResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final StringRedisTemplate redisTemplate;

    private static final String WAITING_KEY_PREFIX = "waiting:performance:";
    private static final String ACTIVE_KEY_PREFIX = "queue:active:";

    //대기열 진입
    public void addQueue(Long performanceId, Long userId) {
        String waitingKey = WAITING_KEY_PREFIX + performanceId;
        String activeKey = ACTIVE_KEY_PREFIX + userId;
        String userIdStr = String.valueOf(userId);

        //대기열을 통과한 유저인지 검증
        if (Boolean.TRUE.equals(redisTemplate.hasKey(activeKey))) {
            throw new CustomException(CustomErrorCode.ALREADY_ACTIVATED_USER);
        }

        //대기열 중복 진입 방지
        Double score = redisTemplate.opsForZSet().score(waitingKey, userIdStr);
        if (score != null) {
            throw new CustomException(CustomErrorCode.ALREADY_IN_WAITING_QUEUE);
        }

        //대기열 진입
        redisTemplate.opsForZSet().add(waitingKey, userIdStr, (double) System.currentTimeMillis());
    }

    //현재 순번 조회
    public QueueResponseDto getQueueStatus(Long performanceId, Long userId) {
        String waitingKey = WAITING_KEY_PREFIX + performanceId;
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
