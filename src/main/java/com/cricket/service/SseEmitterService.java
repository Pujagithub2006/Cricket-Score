package com.cricket.service;

import com.cricket.model.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseEmitterService {
    private static final Logger log = LoggerFactory.getLogger(SseEmitterService.class);
    private final Map<String, List<SseEmitter>> emittersByMatch = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String matchId) {
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L); // 10 minutes timeout
        emittersByMatch.computeIfAbsent(matchId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(matchId, emitter));
        emitter.onTimeout(() -> removeEmitter(matchId, emitter));
        emitter.onError(e -> removeEmitter(matchId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("SSE Connected for match " + matchId));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE connection event: {}", e.getMessage());
        }

        return emitter;
    }

    public void broadcastMatchUpdate(Match match) {
        if (match == null || match.getId() == null) return;
        List<SseEmitter> emitters = emittersByMatch.get(match.getId());
        if (emitters == null || emitters.isEmpty()) return;

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("match-update")
                        .data(match));
            } catch (Exception ex) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    private void removeEmitter(String matchId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByMatch.get(matchId);
        if (emitters != null) {
            emitters.remove(emitter);
        }
    }
}
