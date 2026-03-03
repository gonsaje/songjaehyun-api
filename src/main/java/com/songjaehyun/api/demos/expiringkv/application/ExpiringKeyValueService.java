package com.songjaehyun.api.demos.expiringkv.application;

import java.util.Objects;
import java.util.function.LongSupplier;

import com.songjaehyun.api.demos.expiringkv.domain.ExpiringKeyValueStore;
import com.songjaehyun.api.demos.expiringkv.domain.ExpiringKeyValueStore.Snapshot;

public final class ExpiringKeyValueService {

    private final ExpiringKeyValueStore store;

    public ExpiringKeyValueService() {
        this(System::currentTimeMillis);
    }

    public ExpiringKeyValueService(LongSupplier nowMillis) {
        this.store = new ExpiringKeyValueStore(nowMillis);
    }

    public ExpiringKeyValueService(ExpiringKeyValueStore store) {
        this.store = Objects.requireNonNull(store);
    }

    public void put(String key, String value, long ttlMillis) {
        store.put(key, value, ttlMillis);
    }

    public void putIfAbsent(String key, String value, long ttlMillis) {
        store.putIfAbsent(key, value, ttlMillis);
    }

    public String get(String key) {
        return store.get(key);
    }

    public boolean remove(String key) {
        return store.remove(key);
    }

    public int size() {
        return store.size();
    }

    public long getRemainingTTL(String key) {
        return store.getRemainingTTL(key);
    }

    public Snapshot snapshot() {
        return store.snapshot();
    }
}
