package com.songjaehyun.api.demos.expiringkv.domain;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

public final class ExpiringKeyValueStore {

    private final LongSupplier nowMillis;
    private final ReentrantLock lock = new ReentrantLock();

    private final Map<String, CacheEntry> store = new HashMap<>();
    private final PriorityQueue<ExpiryNode> pq = new PriorityQueue<>(Comparator.comparingLong(en -> en.expiry));

    public ExpiringKeyValueStore() {
        this(System::currentTimeMillis);
    }

    public ExpiringKeyValueStore(LongSupplier nowMillis) {
        this.nowMillis = Objects.requireNonNull(nowMillis, "nowMillis");
    }

    /**
     * Put a key/value with TTL (relative expiration).
     * Overwrites existing key and resets TTL.
     * 
     * @param key       the key to the store
     * @param value     the associated value
     * @param ttlMillis TTL in ms (must be >= 0)
     */
    public void put(String key, String value, long ttlMillis) {
        long now = nowMillis.getAsLong();
        long expiry = now + ttlMillis;

        lock.lock();
        try {
            purgeExpired(now);

            ExpiryNode expn = new ExpiryNode(key, expiry);
            CacheEntry entry = new CacheEntry(value, expiry);
            store.put(key, entry);
            pq.offer(expn);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the value if present and not expired; otherwise returns null.
     * Lazy expiration is enforced here.
     * 
     * @param key
     * @return
     */
    public String get(String key) {
        long now = nowMillis.getAsLong();

        lock.lock();
        try {
            purgeExpired(now);

            CacheEntry entry = store.get(key);
            if (entry == null)
                return null;
            if (entry.isExpiredAt(now)) {
                store.remove(key);

                return null;
            }
            return entry.getValue();
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(String key) {
        long now = nowMillis.getAsLong();

        lock.lock();
        try {
            purgeExpired(now);
            return store.remove(key) != null;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        long now = nowMillis.getAsLong();

        lock.lock();
        try {
            purgeExpired(now);
            return this.store.size();
        } finally {
            lock.unlock();
        }
    }

    public long getRemainingTTL(String key) {
        long now = nowMillis.getAsLong();

        lock.lock();
        try {
            purgeExpired(now);

            CacheEntry entry = store.get(key);

            if (entry == null || entry.isExpiredAt(now))
                return -1;

            long remainingTime = entry.getExpiry() - now;
            return remainingTime;
        } finally {
            lock.unlock();
        }
    }

    public void putIfAbsent(String key, String value, long ttl) {
        long now = nowMillis.getAsLong();

        lock.lock();
        try {
            purgeExpired(now);
            if (store.get(key) == null) {
                long expiry = now + ttl;
                CacheEntry newEntry = new CacheEntry(value, expiry);
                store.put(key, newEntry);
                ExpiryNode newExpiry = new ExpiryNode(key, expiry);
                pq.offer(newExpiry);
            }
        } finally {
            lock.unlock();
        }
    }

    private void purgeExpired(long now) {
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

        boolean isExpiredAt(long now) {
            return now >= this.expiry;
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
