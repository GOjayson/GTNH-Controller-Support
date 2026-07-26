package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CursorMotionTest {

    private static final float TOLERANCE = 0.0001F;

    @Test
    public void accelerationIsLimited() {
        float velocity = CursorMotion.updateVelocity(0.0F, 420.0F, 1400.0F, 2400.0F, 0.1F);

        assertEquals(140.0F, velocity, TOLERANCE);
    }

    @Test
    public void decelerationIsLimitedWithoutOvershooting() {
        float velocity = CursorMotion.updateVelocity(420.0F, 0.0F, 1400.0F, 2400.0F, 0.1F);

        assertEquals(180.0F, velocity, TOLERANCE);
        assertEquals(0.0F, CursorMotion.updateVelocity(20.0F, 0.0F, 1400.0F, 2400.0F, 0.1F), TOLERANCE);
    }

    @Test
    public void reversalStopsBeforeAcceleratingTheOtherWay() {
        float velocity = CursorMotion.updateVelocity(100.0F, -420.0F, 1400.0F, 2400.0F, 0.1F);

        assertEquals(0.0F, velocity, TOLERANCE);
    }

    @Test
    public void stoppedRequiresBothAxesToBeZero() {
        assertTrue(CursorMotion.isStopped(0.0F, 0.0F));
    }

    @Test
    public void highSensitivityScalesTheResponseRate() {
        assertEquals(2800.0F, CursorMotion.responseRate(1400.0F, 2.0F), TOLERANCE);
        assertEquals(1400.0F, CursorMotion.responseRate(1400.0F, 0.25F), TOLERANCE);
    }

    @Test
    public void decelerationBoundsMaximumCoastTime() {
        assertEquals(5600.0F, CursorMotion.decelerationRate(2400.0F, 2.0F, 840.0F), 0.01F);
        assertEquals(10000.0F, CursorMotion.decelerationRate(5000.0F, 2.0F, 840.0F), TOLERANCE);
    }
}
