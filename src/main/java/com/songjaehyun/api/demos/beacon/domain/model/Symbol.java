package com.songjaehyun.api.demos.beacon.domain.model;

public record Symbol(String value) {
    public Symbol {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Symbol must not be blank");
        }
    }
}