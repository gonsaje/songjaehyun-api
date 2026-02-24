package com.songjaehyun.api.demos.expiringkv.domain;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class ExpiringKeyValueStore {

    private final Map<String, CacheEntry> store = new HashMap<>();
    private final PriorityQueue<ExpiryNode> pq = new PriorityQueue<>(Comparator.comparingLong(en -> en.expiry));

    // Stores the key/value
    // Key should expire after ttlMillis
    // If key already exists → overwrite and reset TTL
    void put(String key, String value, long ttlMillis) {
        long now = System.currentTimeMillis();
        long expiry = now + ttlMillis;
        CacheEntry entry = new CacheEntry(value, expiry);
        store.put(key, entry);

        ExpiryNode expn = new ExpiryNode(key, expiry);
        pq.offer(expn);

        purgeExpired(now);
    }

    // Returns the value only if not expired
    // If expired:
    // Remove it from store
    // Return null
    String get(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null)
            return null;
        if (entry.isExpired()) {
            store.remove(key);
            return null;
        }
        return entry.getValue();
    }

    boolean remove(String key) {
        return store.remove(key) != null;
    }

    int size() {
        long now = System.currentTimeMillis();
        purgeExpired(now);
        return this.store.size();
    }

    long getRemainingTTL(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null || entry.isExpired())
            return -1;
        long remainingTime = entry.getExpiry() - System.currentTimeMillis();
        return remainingTime;
    }

    void putIfAbsent(String key, String value, long ttl) {
        long now = System.currentTimeMillis();
        if (get(key) == null) {
            long expiry = now + ttl;
            CacheEntry newEntry = new CacheEntry(value, expiry);
            store.put(key, newEntry);
            ExpiryNode newExpiry = new ExpiryNode(key, expiry);
            pq.offer(newExpiry);
        }
        purgeExpired(now);
    }

    void purgeExpired(long now) {
        while (!this.pq.isEmpty() && this.pq.peek().expiry <= now) {
            ExpiryNode en = pq.poll();

            CacheEntry ce = this.store.get(en.key);
            if (ce == null)
                continue;
            if (ce.getExpiry() != en.expiry)
                continue;

            store.remove(en.key);
        }
    }

    /*
     * Cache Entry sub class
     */
    private static final class CacheEntry {
        private final String value;
        private final long expiry;

        CacheEntry(String value, long expiry) {
            this.value = value;
            this.expiry = expiry;
        }

        String getValue() {
            return this.value;
        }

        long getExpiry() {
            return this.expiry;
        }

        boolean isExpired() {
            return System.currentTimeMillis() >= this.expiry;
        }
    }

    private static final class ExpiryNode {
        private final String key;
        private final long expiry;

        ExpiryNode(String key, long expiry) {
            this.key = key;
            this.expiry = expiry;
        }

    }
}
