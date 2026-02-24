package com.songjaehyun.api.shared.log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class DemoLogService {

    private final int MAX_EVENTS_PER_SESSION;

    private final ConcurrentHashMap<String, Deque<LogEvent>> logs = new ConcurrentHashMap<>();

    DemoLogService() {
        MAX_EVENTS_PER_SESSION = 300;
    }

    DemoLogService(int max) {
        MAX_EVENTS_PER_SESSION = max;
    }

    public void append(String sessionId, LogEvent event) {
        Deque<LogEvent> q = logs.computeIfAbsent(sessionId, k -> new ArrayDeque<>());
        synchronized (q) {
            q.addLast(event);
            while (q.size() > MAX_EVENTS_PER_SESSION)
                q.removeFirst();
        }
    }

    public List<LogEvent> get(String sessionId) {
        Deque<LogEvent> q = logs.get(sessionId);
        if (q == null)
            return List.of();
        synchronized (q) {
            return new ArrayList<>(q);
        }
    }

    public void clear(String sessionId) {
        logs.remove(sessionId);
    }

    public int count(String sessionId) {
        Deque<LogEvent> q = logs.get(sessionId);
        return q == null ? 0 : q.size();
    }
}
