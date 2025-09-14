package com.zwnsyw.ai_agent_backend.controller.Sse;

import com.zwnsyw.ai_agent_backend.service.serviceimpl.Message.SseEmitterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/sse")
public class SseController {

    @Autowired
    private SseEmitterRepository emitterRepository;

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam Long userId) {
        SseEmitter existing = emitterRepository.get(userId);
        if (existing != null) {
            existing.complete();
            emitterRepository.remove(userId);
        }

        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        emitter.onTimeout(() -> {
            emitter.complete();
            emitterRepository.remove(userId);
            log.info("连接超时：用户" + userId);
        });
        emitter.onCompletion(() -> {
            emitterRepository.remove(userId);
            log.info("连接关闭：用户" + userId);
        });
        emitter.onError(ex -> {
            emitterRepository.remove(userId);
            log.info("连接错误：用户" + userId);
        });

        emitterRepository.save(userId, emitter);
        return emitter;
    }
}