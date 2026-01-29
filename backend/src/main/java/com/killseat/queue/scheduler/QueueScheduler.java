package com.killseat.queue.scheduler;

import com.killseat.queue.service.QueueNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {
    private final StringRedisTemplate redisTemplate;
    private final QueueNotificationService queueNotificationService;

    private static final String WAITING_KEY_FORMAT = "waiting:performance:%d:schedule:%d";
    private static final String ACTIVE_KEY_PREFIX = "queue:active:";

    //1초마다 입장시킬 인원을 정의
    private static final int MAX_ENTER_COUNT = 10;

    @Scheduled(fixedDelay = 1000)
    public void processQueue() {
        Map<Long, Map<Long, Map<Long, SseEmitter>>> allEmitters = queueNotificationService.getAllEmitters();

        for (Long performanceId : allEmitters.keySet()) {
            Map<Long, Map<Long, SseEmitter>> schedules = allEmitters.get(performanceId);
            for (Long scheduleId : schedules.keySet()) {
                String waitingKey = String.format(WAITING_KEY_FORMAT, performanceId, scheduleId);

                //설정한 인원(MAX_ENTER_COUNT)만큼만 대기열에서 추출
                Set<String> entryUsers = redisTemplate.opsForZSet().range(waitingKey, 0, MAX_ENTER_COUNT - 1);

                if (entryUsers != null && !entryUsers.isEmpty()) {
                    for (String userIdStr : entryUsers) {
                        //추출된 인원을 Redis 대기열에서 즉시 삭제
                        redisTemplate.opsForZSet().remove(waitingKey, userIdStr);

                        try {
                            String cleanUserId = userIdStr.replace("\"", "").replaceAll("[^0-9]", "");
                            if (cleanUserId.isEmpty()) continue;
                            Long userId = Long.parseLong(cleanUserId);

                            //해당 유저에게 'PROCEED' 권한 부여 (5분간 유효)
                            redisTemplate.opsForValue().set(ACTIVE_KEY_PREFIX + userId, "PROCEED", Duration.ofMinutes(5));

                            //입장 신호 전송
                            queueNotificationService.sendToUser(performanceId, scheduleId, userId, "proceed", "ENTER");

                            log.info("[Queue] User {} entered. Performance: {}, Schedule: {}", userId, performanceId, scheduleId);
                        } catch (Exception e) {
                            log.error("[Queue] Failed to process user entry: {}", userIdStr);
                        }
                    }
                }

                //남은 유저들에게만 순번 알림
                Set<String> remainingUsers = redisTemplate.opsForZSet().range(waitingKey, 0, -1);
                if (remainingUsers != null) {
                    int rank = 1;
                    Map<Long, SseEmitter> activeEmitters = schedules.getOrDefault(scheduleId, Map.of());

                    for (String uidStr : remainingUsers) {
                        try {
                            String cleanUid = uidStr.replace("\"", "").replaceAll("[^0-9]", "");
                            if (cleanUid.isEmpty()) { rank++; continue; }
                            Long userId = Long.parseLong(cleanUid);

                            //현재 접속 중인 유저에게만 순번 전송
                            if (activeEmitters.containsKey(userId)) {
                                queueNotificationService.sendToUser(performanceId, scheduleId, userId, "queueStatus", Map.of("rank", rank));
                            }
                            rank++;
                        } catch (Exception e) {
                            rank++;
                        }
                    }
                }
            }
        }
    }
}