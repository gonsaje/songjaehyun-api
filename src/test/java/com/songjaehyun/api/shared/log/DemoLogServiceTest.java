package com.songjaehyun.api.shared.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.*;

public class DemoLogServiceTest {

    @Test
    void append_then_get_containsEvent() {
        DemoLogService dls = new DemoLogService();
        LogEvent event = new LogEvent(System.currentTimeMillis(), "demo", "method", Map.of("key", "alpha"), "beta");
        dls.append("abc", event);
        var q = dls.get("abc");
        assertEquals(1, q.size());
        LogEvent fetchedEvent = q.get(0);
        assertEquals(event, fetchedEvent);
    }

    @Test
    void get_unknownSession_returnsEmptyList() {
        DemoLogService dls = new DemoLogService();
        var q = dls.get("def");
        assertTrue(q.isEmpty());
    }

    @Test
    void append_preservesOrder() {
        DemoLogService dls = new DemoLogService();
        LogEvent event1 = new LogEvent(System.currentTimeMillis(), "demo", "method",
                Map.of("key", "alpha"), "beta");
        LogEvent event2 = new LogEvent(System.currentTimeMillis(), "demo2", "method2",
                Map.of("key2", "alpha2"), "beta2");

        dls.append("abc", event1);
        dls.append("abc", event2);

        var q = dls.get("abc");
        assertEquals(2, q.size());
        assertEquals(event1, q.get(0));
        assertEquals(event2, q.get(1));
    }

    @Test
    void clear_removesEventsForThatSessionOnly() {
        DemoLogService dls = new DemoLogService();
        LogEvent event1 = new LogEvent(System.currentTimeMillis(), "demo", "method", Map.of("key", "alpha"), "beta");
        LogEvent event2 = new LogEvent(System.currentTimeMillis(), "demo2", "method2", Map.of("key2", "alpha2"),
                "beta2");
        dls.append("abc", event1);
        dls.append("def", event2);

        var q1 = dls.get("abc");
        var q2 = dls.get("def");

        assertEquals(1, q1.size());
        assertEquals(1, q2.size());

        dls.clear("abc");
        q1 = dls.get("abc");
        q2 = dls.get("def");
        assertEquals(0, q1.size());
        assertEquals(1, q2.size());
    }

    @Test
    void count_returnsZeroForUnknownSession() {
        DemoLogService dls = new DemoLogService();
        assertEquals(0, dls.count("abc"));
    }

    @Test
    void cap_keepsOnlyLatestEvents() {
        DemoLogService dls = new DemoLogService(3);
        for (int i = 0; i < 3; i++) {
            LogEvent event = new LogEvent(System.currentTimeMillis(), "demo" + i, "method" + i,
                    Map.of("key" + i, "alpha" + i), "beta" + i);
            dls.append("abc", event);
        }
        var q = dls.get("abc");
        assertEquals(3, q.size());
        LogEvent fetchedEvent1 = q.get(0);
        LogEvent fetchedEvent2 = q.get(1);
        LogEvent fetchedEvent3 = q.get(2);

        LogEvent maxEvent = new LogEvent(System.currentTimeMillis(), "demoMax", "methodMax",
                Map.of("keyMax", "alphaMax"), "betaMax");
        dls.append("abc", maxEvent);
        var qAfter = dls.get("abc");
        LogEvent newFetchedEvent1 = qAfter.get(0);
        LogEvent newFetchedEvent2 = qAfter.get(1);
        LogEvent newFetchedEvent3 = qAfter.get(2);

        assertEquals(3, qAfter.size());
        assertEquals(maxEvent, newFetchedEvent3);
        assertNotEquals(fetchedEvent1, newFetchedEvent1);
        assertEquals(fetchedEvent2, newFetchedEvent1);
        assertEquals(fetchedEvent3, newFetchedEvent2);
    }

    @Test
    void concurrent_append_doesNotThrow_and_countMatches() throws Exception {
        int threads = 10;
        int eventsPerThread = 100;
        int expected = threads * eventsPerThread;

        DemoLogService dls = new DemoLogService(expected + 10); // make cap bigger than expected
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);

        // Collect exceptions from worker threads
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threads; i++) {
            int threadId = i;
            pool.submit(() -> {
                try {
                    startGate.await(); // wait until ready

                    for (int j = 0; j < eventsPerThread; j++) {
                        LogEvent e = new LogEvent(
                                System.currentTimeMillis(),
                                "demo",
                                "append",
                                Map.of("thread", threadId, "j", j),
                                null);
                        dls.append("abc", e);
                    }
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    doneGate.countDown();
                }
            });
        }

        // Start all threads
        startGate.countDown();

        // Wait for completion
        boolean finished = doneGate.await(5, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertTrue(finished, "Workers did not finish in time");
        assertTrue(errors.isEmpty(), "Errors occured: " + errors);
        assertEquals(expected, dls.count("abc"));
    }
}
