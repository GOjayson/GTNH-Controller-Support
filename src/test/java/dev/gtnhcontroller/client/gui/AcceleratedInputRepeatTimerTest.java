package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AcceleratedInputRepeatTimerTest {

    @Test
    public void keepsTheInitialDelayAndThenShortensTheRepeatInterval() {
        AcceleratedInputRepeatTimer timer = new AcceleratedInputRepeatTimer();

        assertTrue(timer.shouldActivate(true, 1000L, 350, 100, true, 3.0F));
        assertFalse(timer.shouldActivate(true, 1349L, 350, 100, true, 3.0F));
        assertTrue(timer.shouldActivate(true, 1350L, 350, 100, true, 3.0F));
        assertTrue(timer.shouldActivate(true, 3000L, 350, 100, true, 3.0F));
        assertFalse(timer.shouldActivate(true, 3032L, 350, 100, true, 3.0F));
        assertTrue(timer.shouldActivate(true, 3034L, 350, 100, true, 3.0F));
    }

    @Test
    public void disabledAccelerationKeepsTheNormalInterval() {
        AcceleratedInputRepeatTimer timer = new AcceleratedInputRepeatTimer();

        assertTrue(timer.shouldActivate(true, 1000L, 350, 100, false, 5.0F));
        assertTrue(timer.shouldActivate(true, 2000L, 350, 100, false, 5.0F));
        assertFalse(timer.shouldActivate(true, 2099L, 350, 100, false, 5.0F));
        assertTrue(timer.shouldActivate(true, 2100L, 350, 100, false, 5.0F));
    }
}
