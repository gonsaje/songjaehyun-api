package com.songjaehyun.api.shared.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class SessionRegistry {
    private final ConcurrentHashMap<String, Long> lastSeen = new ConcurrentHashMap<>();

    public void touch(String sessionId) {
        lastSeen.put(sessionId, System.currentTimeMillis());
    }

    public void remove(String sessionId) {
        lastSeen.remove(sessionId);
    }

    public Map<String, Long> snapshot() {
        return Map.copyOf(lastSeen);
    }
}
