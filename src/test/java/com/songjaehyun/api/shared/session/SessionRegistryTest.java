package com.songjaehyun.api.shared.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SessionRegistryTest {

    @Test
    void touch_addsSessionAndSnapshotContainsIt() {
        SessionRegistry reg = new SessionRegistry();
        reg.touch("abc");

        var snapshot = reg.snapshot();
        assertTrue(snapshot.containsKey("abc"));
        assertNotNull(snapshot.get("abc"));
        assertTrue(snapshot.get("abc") > 0);
    }

    @Test
    void touch_updatesTimestampForSameSession() throws InterruptedException {
        SessionRegistry reg = new SessionRegistry();
        reg.touch("def");

        var snapshot = reg.snapshot();
        assertTrue(snapshot.containsKey("def"));
        var ts1 = snapshot.get("def");
        assertTrue(ts1 > 0);

        Thread.sleep(5);

        reg.touch("def");
        var snapAfter = reg.snapshot();
        assertTrue(snapAfter.containsKey("def"));
        var ts2 = snapAfter.get("def");
        assertTrue(ts2 > 0 && ts2 >= ts1);
    }

    @Test
    void remove_removesExistingSession() {
        SessionRegistry reg = new SessionRegistry();
        reg.touch("abc");

        var snapshot = reg.snapshot();
        assertTrue(snapshot.containsKey("abc"));
        assertTrue(snapshot.get("abc") > 0);

        reg.remove("abc");
        var snapAfter = reg.snapshot();
        assertFalse(snapAfter.containsKey("abc"));
        assertNull(snapAfter.get("abc"));
    }

    @Test
    void remove_nonExistingSession_doesNotThrow() {
        SessionRegistry reg = new SessionRegistry();
        assertDoesNotThrow(() -> reg.remove("missing"));
    }

    @Test
    void snapshot_returnsCopyNotLiveMap() {
        SessionRegistry reg = new SessionRegistry();
        reg.touch("abc");

        var snapshot = reg.snapshot();
        assertTrue(snapshot.containsKey("abc"));
        assertTrue(snapshot.get("abc") > 0);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.put("bad", System.currentTimeMillis()));
        assertFalse(reg.snapshot().containsKey("bad"));
    }

    @Test
    void touch_multipleSessions_snapshotContainsAll() {
        SessionRegistry reg = new SessionRegistry();
        reg.touch("abc");
        reg.touch("def");
        reg.touch("hij");

        var snapshot = reg.snapshot();
        assertTrue(snapshot.size() == 3);
        assertTrue(snapshot.containsKey("abc"));
        assertTrue(snapshot.containsKey("def"));
        assertTrue(snapshot.containsKey("hij"));
        assertTrue(snapshot.get("abc") > 0);
        assertTrue(snapshot.get("def") > 0);
        assertTrue(snapshot.get("hij") > 0);
    }
}
