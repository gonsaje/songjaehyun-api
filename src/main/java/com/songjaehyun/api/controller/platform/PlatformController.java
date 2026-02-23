package com.songjaehyun.api.controller.platform;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.songjaehyun.api.shared.log.DemoLogService;
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

}
