package com.songjaehyun.api.demos.expiringkv.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

/**
 * Thread-safe in-memory expiring key-value store.
 *
 * <p>
 * Design:
 * - Backed by a HashMap for O(1) average lookup.
 * - Expirations tracked via a min-heap ordered by expiry time.
 * - Lazy expiration: expired entries are removed during read/write operations.
 * - TTL is capped at 1 year to prevent overflow and unbounded retention.
 * - Time source is injected via LongSupplier for testability.
 *
 * <p>
 */
public final class ExpiringKeyValueStore {

    private final LongSupplier nowMillis;
    private final ReentrantLock lock = new ReentrantLock();

    private final Map<String, CacheEntry> store = new HashMap<>();
    private final PriorityQueue<ExpiryNode> expiryMinHeap = new PriorityQueue<>(
            Comparator.comparingLong(en -> en.expiry));

    private static final long MAX_TTL_MILLIS = 365L * 24 * 60 * 60 * 1000; // 1 year

    public ExpiringKeyValueStore() {
        this(System::currentTimeMillis);
    }

    public ExpiringKeyValueStore(LongSupplier nowMillis) {
        this.nowMillis = Objects.requireNonNull(nowMillis, "nowMillis");
    }

    /**
     * Put a key/value with TTL (relative expiration).
     * Overwrites existing key and resets TTL.
     * Validates key, value, and expiry inputs.
     * 
     * @param key       the key to the store
     * @param value     the associated value
     * @param ttlMillis time-to-live in milliseconds; must be > 0 and <= 1 year
     */
    public void put(String key, String value, long ttlMillis) {
        requireKey(key);
        requireValue(value);

        long ttl = validateTtl(ttlMillis);
        long now = nowMillis.getAsLong();
        long expiry = now + ttl;

        lock.lock();
        try {
            purgeExpired(now);

            ExpiryNode expn = new ExpiryNode(key, expiry);
            CacheEntry entry = new CacheEntry(value, expiry);
            store.put(key, entry);
            expiryMinHeap.offer(expn);
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
        requireKey(key);

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
            return entry.value;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(String key) {
        requireKey(key);

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

    /**
     * Returns the remaining TTL in milliseconds for the given key.
     *
     * @param key the key
     * @return remaining TTL in milliseconds, or -1 if the key does not exist
     *         or has expired
     * @throws IllegalArgumentException if the key is null or blank
     */
    public long getRemainingTTL(String key) {
        requireKey(key);

        long now = nowMillis.getAsLong();

        lock.lock();
        try {
            purgeExpired(now);

            CacheEntry entry = store.get(key);

            if (entry == null || entry.isExpiredAt(now))
                return -1;

            long remainingTime = entry.expiry - now;
            return remainingTime;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts the key/value pair only if the key is absent (or expired).
     * Does not overwrite or reset TTL if the key already exists and is active.
     *
     * @param key   the key
     * @param value the value
     * @param ttl   time-to-live in milliseconds; must be > 0 and <= 1 year
     * @throws IllegalArgumentException if key/value is invalid or TTL is out of
     *                                  range
     */
    public void putIfAbsent(String key, String value, long ttl) {
        requireKey(key);
        requireValue(value);

        long validatedTtl = validateTtl(ttl);
        long now = nowMillis.getAsLong();

        lock.lock();
        try {
            purgeExpired(now);
            if (store.get(key) == null) {
                long expiry = now + validatedTtl;
                CacheEntry newEntry = new CacheEntry(value, expiry);
                store.put(key, newEntry);
                ExpiryNode newExpiry = new ExpiryNode(key, expiry);
                expiryMinHeap.offer(newExpiry);
            }
        } finally {
            lock.unlock();
        }
    }

    public Snapshot snapshot() {
        long now = nowMillis.getAsLong();
        lock.lock();
        try {
            purgeExpired(now);

            List<SnapshotEntry> entries = new ArrayList<>(store.size());
            for (Map.Entry<String, CacheEntry> entry : store.entrySet()) {
                String key = entry.getKey();
                CacheEntry ce = entry.getValue();
                long remaining = Math.max(0L, ce.expiry - now);
                entries.add(new SnapshotEntry(key, ce.value, ce.expiry, remaining));
            }
            entries.sort((a, b) -> a.key().compareTo(b.key()));

            return new Snapshot(now, Collections.unmodifiableList(entries));
        } finally {
            lock.unlock();
        }
    }

    private void purgeExpired(long now) {
        while (!this.expiryMinHeap.isEmpty() && this.expiryMinHeap.peek().expiry <= now) {
            ExpiryNode en = expiryMinHeap.poll();
            CacheEntry ce = this.store.get(en.key);

            if (ce == null)
                continue;
            if (ce.expiry != en.expiry)
                continue;

            store.remove(en.key);
        }
    }

    /**
     * Validates TTL against domain constraints.
     * Enforces TTL > 0 and <= 1 year.
     * 
     * @param ttl
     * @return
     */
    private static long validateTtl(long ttl) {
        if (ttl <= 0) {
            throw new IllegalArgumentException("TTL must be > 0.");
        }

        if (ttl > MAX_TTL_MILLIS) {
            throw new IllegalArgumentException("TTL exceeds maximum of 1 year.");
        }

        return ttl;
    }

    private static void requireKey(String key) {
        if (key == null || key.isBlank())
            throw new IllegalArgumentException("Key must not be null or blank");
    }

    private static void requireValue(String value) {
        if (value == null)
            throw new IllegalArgumentException("Value must not be null");
    }

    private record CacheEntry(String value, long expiry) {
        boolean isExpiredAt(long now) {
            return now >= expiry;
        }
    }

    private record ExpiryNode(String key, long expiry) {
    }

    // ----------------------------
    // Snapshot DTOs (domain-level)
    // ----------------------------

    public record Snapshot(long nowMillis, List<SnapshotEntry> entries) {
    }

    public record SnapshotEntry(
            String key,
            String value,
            long expiryMillis,
            long ttlRemainingMillis) {
    }
}
