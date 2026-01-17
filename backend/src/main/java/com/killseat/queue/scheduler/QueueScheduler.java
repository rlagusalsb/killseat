package com.killseat.queue.scheduler;

import com.killseat.queue.service.QueueNotificationService;
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
    private final QueueNotificationService queueNotificationService;
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
                //입장 권한 부여
                redisTemplate.opsForValue().set(ACTIVE_KEY_PREFIX + userId, "PROCEED", Duration.ofMinutes(5));
                redisTemplate.opsForZSet().remove(waitingKey, userId);

                Long parsedUserId = Long.parseLong(userId.replace("\"", ""));

                //유저에게 SSE로 실시간 알림 발송
                queueNotificationService.sendToUser(parsedUserId, "proceed", "입장 가능");

            }

            Set<String> allWaitingUsers = redisTemplate.opsForZSet().range(waitingKey, 0, -1);

            for (String uid : allWaitingUsers) {
                Long rank = redisTemplate.opsForZSet().rank(waitingKey, uid);

                Long parsedUid = Long.parseLong(uid.replace("\"", ""));

                queueNotificationService.sendToUser(parsedUid, "update", (rank + 1) + "명 남았습니다.");
            }
        }
    }
}
