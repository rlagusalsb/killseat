package com.killseat.queue.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class QueueNotificationService {

    //Map<공연ID, Map<회차ID, Map<유저ID, Emitter>>>
    private final Map<Long, Map<Long, Map<Long, SseEmitter>>> performanceEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long performanceId, Long scheduleId, Long userId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        performanceEmitters.computeIfAbsent(performanceId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(scheduleId, k -> new ConcurrentHashMap<>())
                .put(userId, emitter);

        emitter.onCompletion(() -> removeEmitter(performanceId, scheduleId, userId));
        emitter.onTimeout(() -> removeEmitter(performanceId, scheduleId, userId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected to performance: " + performanceId + ", schedule: " + scheduleId));
        } catch (IOException e) {
            removeEmitter(performanceId, scheduleId, userId);
        }

        return emitter;
    }

    //특정 공연의 특정 유저에게 알림 전송
    public void sendToUser(Long performanceId, Long scheduleId, Long userId, String name, Object data) {
        Map<Long, Map<Long, SseEmitter>> schedules = performanceEmitters.get(performanceId);
        if (schedules != null) {
            Map<Long, SseEmitter> emitters = schedules.get(scheduleId);
            if (emitters != null) {
                SseEmitter emitter = emitters.get(userId);
                if (emitter != null) {
                    try {
                        emitter.send(SseEmitter.event()
                                .name(name)
                                .data(data));
                    } catch (Exception e) {
                        emitter.complete();
                        removeEmitter(performanceId, scheduleId, userId);
                    }
                }
            }
        }
    }

    private void removeEmitter(Long performanceId, Long scheduleId, Long userId) {
        Map<Long, Map<Long, SseEmitter>> schedules = performanceEmitters.get(performanceId);
        if (schedules != null) {
            Map<Long, SseEmitter> emitters = schedules.get(scheduleId);
            if (emitters != null) {
                emitters.remove(userId);
                if (emitters.isEmpty()) {
                    schedules.remove(scheduleId);
                }
            }
            if (schedules.isEmpty()) {
                performanceEmitters.remove(performanceId);
            }
        }
    }

    public Map<Long, Map<Long, Map<Long, SseEmitter>>> getAllEmitters() {
        return performanceEmitters;
    }
}