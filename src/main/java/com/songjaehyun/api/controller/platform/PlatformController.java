package com.songjaehyun.api.controller.platform;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.songjaehyun.api.shared.log.DemoLogService;
import com.songjaehyun.api.shared.log.LogEvent;
import com.songjaehyun.api.shared.session.SessionRegistry;

@RestController
@RequestMapping("/platform")
public class PlatformController {
    
    private final DemoLogService logService;
    private final SessionRegistry sessionRegistry;

    public PlatformController(DemoLogService logService, SessionRegistry sessionRegistry) {
        this.logService = logService;
        this.sessionRegistry = sessionRegistry;
    }

    public record AppendLogRequest(String demo, String method, Map<String, Object> args, Object result) {};

    @PostMapping("/{sid}/touch")
    public ResponseEntity<?> touch(@PathVariable String sid) {
        sessionRegistry.touch(sid);
        return ResponseEntity.ok(Map.of("sid", sid, "touched", true));
    }
    
    @PostMapping("/{sid}/log")
    public ResponseEntity<?> append(@PathVariable String sid, @RequestBody AppendLogRequest req) {
        sessionRegistry.touch(sid);

        LogEvent event = new LogEvent(
                System.currentTimeMillis(),
                req.demo(),
                req.method(),
                req.args() == null ? Map.of() : req.args(),
                req.result()
        );

        logService.append(sid, event);
        return ResponseEntity.ok(Map.of(
                "appended", true,
                "count", logService.count(sid),
                "event", event
        ));
    }

    @GetMapping("/{sid}/log")
    public ResponseEntity<?> get(@PathVariable String sid) {
        sessionRegistry.touch(sid);
        return ResponseEntity.ok(logService.get(sid)); 
    }

    @GetMapping("sessions")
    public ResponseEntity<?> sessions() {
        return ResponseEntity.ok(sessionRegistry.snapshot());
    }

    @PostMapping("/{sid}/clear")
    public ResponseEntity<?> clear(@PathVariable String sid) {
        logService.clear(sid);
        sessionRegistry.remove(sid);
        return ResponseEntity.ok(Map.of("sid", sid, "cleared", true));
    }
}
