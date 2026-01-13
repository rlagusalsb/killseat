package com.killseat.queue.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String WAITING_KEY_PATTERN = "waiting:performance:*";
    private static final String ACTIVE_KEY_PREFIX = "queue:active:";

    @Scheduled(fixedDelay = 1000)
    public void processQueue() {
        Set<String> waitingKeys = redisTemplate.keys(WAITING_KEY_PATTERN);

        if (waitingKeys.isEmpty()) {
            return;
        }

        for (String waitingKey : waitingKeys) {
            Set<String> waitingUsers = redisTemplate.opsForZSet().range(waitingKey, 0, 9);

            if (waitingUsers == null || waitingUsers.isEmpty()) {
                continue;
            }

            for (String userId : waitingUsers) {
                redisTemplate.opsForValue().set(ACTIVE_KEY_PREFIX + userId, "PROCEED", Duration.ofMinutes(5));
                redisTemplate.opsForZSet().remove(waitingKey, userId);
            }
        }
    }
}
