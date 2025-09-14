package com.zwnsyw.ai_agent_backend.service.serviceimpl.Message;

import com.zwnsyw.ai_agent_backend.entity.Message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitterRepository {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter get(Long userId) {
        return emitters.get(userId);
    }

    public void save(Long userId, SseEmitter emitter) {
        log.info("新增用户连接：" + userId);
        emitters.put(userId, emitter);
    }

    public void remove(Long userId) {
        emitters.remove(userId);
    }

    public void pushMessage(Long userId, Message message) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(message));
            } catch (Exception e) {
                emitter.complete();
                remove(userId);
            }
        }
    }
}
