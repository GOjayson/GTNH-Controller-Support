package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CursorWarpTrackerTest {

    @Test
    public void ignoresDelayedReadbackAfterProgrammaticWarp() {
        CursorWarpTracker tracker = new CursorWarpTracker();
        tracker.recordWarp(200, 100);

        assertFalse(tracker.isPhysicalMovement(true, false, 50, 25, 20, 10));
        assertFalse(tracker.isPhysicalMovement(true, false, 200, 100, 50, 25));
        assertFalse(tracker.isPhysicalMovement(true, false, 200, 100, 200, 100));
        assertTrue(tracker.isPhysicalMovement(true, false, 210, 100, 200, 100));
    }

    @Test
    public void controllerMovementKeepsOwnershipBeyondTheGracePeriod() {
        CursorWarpTracker tracker = new CursorWarpTracker();
        tracker.recordWarp(200, 100);

        for (int frame = 0; frame < 10; frame++) {
            assertFalse(tracker.isPhysicalMovement(true, true, 50 + frame, 25, 20 + frame, 10));
        }
    }

    @Test
    public void comparesWarpRequestsAgainstTheRequestedTarget() {
        CursorWarpTracker tracker = new CursorWarpTracker();
        tracker.recordWarp(200, 100);

        assertFalse(tracker.needsWarp(200, 100));
        assertTrue(tracker.needsWarp(201, 100));
        assertTrue(tracker.needsWarp(200, 101));
    }

    @Test
    public void resetAllowsTheSamePositionToBeWarpedAgain() {
        CursorWarpTracker tracker = new CursorWarpTracker();
        tracker.recordWarp(200, 100);
        tracker.reset();

        assertTrue(tracker.needsWarp(200, 100));
    }

    @Test
    public void mouseMovementIsImmediateWithoutControllerOwnership() {
        CursorWarpTracker tracker = new CursorWarpTracker();
        tracker.recordWarp(200, 100);

        assertTrue(tracker.isPhysicalMovement(false, false, 50, 25, 20, 10));
    }
}
