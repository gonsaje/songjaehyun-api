package com.songjaehyun.api.demos.expiringkv.api;

import com.songjaehyun.api.demos.expiringkv.application.ExpiringKeyValueService;
import com.songjaehyun.api.demos.expiringkv.domain.ExpiringKeyValueStore.Snapshot;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demos/expiring-kv")
public class ExpiringKeyValueController {

    private final ExpiringKeyValueService service;

    public ExpiringKeyValueController(ExpiringKeyValueService service) {
        this.service = service;
    }

    @PutMapping("/entries/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void put(
            @PathVariable String key,
            @RequestBody PutRequest request) {
        service.put(key, request.value(), request.ttlMillis());
    }

    @PutMapping("/entries/{key}/if-absent")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void putIfAbsent(
            @PathVariable String key,
            @RequestBody PutRequest request) {
        service.putIfAbsent(key, request.value(), request.ttlMillis());
    }

    @GetMapping("/entries/{key}")
    public ResponseEntity<GetResponse> get(@PathVariable String key) {
        String value = service.get(key);
        if (value == null)
            return ResponseEntity.notFound().build();

        long ttl = service.getRemainingTTL(key);
        return ResponseEntity.ok(new GetResponse(key, value, ttl));
    }

    @DeleteMapping("/entries/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String key) {
        service.remove(key);
    }

    @GetMapping("/snapshot")
    public Snapshot snapshot() {
        return service.snapshot();
    }

    // ---- error mapping ----
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    // ---- DTOs (you can move these to expiringkv.api.dto later) ----
    public record PutRequest(String value, long ttlMillis) {
    }

    public record GetResponse(String key, String value, long ttlRemainingMillis) {
    }

    public record ErrorResponse(String message) {
    }
}