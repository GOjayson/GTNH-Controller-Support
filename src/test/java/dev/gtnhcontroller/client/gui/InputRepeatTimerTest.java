package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InputRepeatTimerTest {

    @Test
    public void activatesImmediatelyThenUsesConfiguredDelays() {
        InputRepeatTimer timer = new InputRepeatTimer();

        assertTrue(timer.shouldActivate(true, 1000L, 350, 100));
        assertFalse(timer.shouldActivate(true, 1349L, 350, 100));
        assertTrue(timer.shouldActivate(true, 1350L, 350, 100));
        assertFalse(timer.shouldActivate(true, 1449L, 350, 100));
        assertTrue(timer.shouldActivate(true, 1450L, 350, 100));
    }

    @Test
    public void releaseAllowsASecondImmediateActivation() {
        InputRepeatTimer timer = new InputRepeatTimer();

        assertTrue(timer.shouldActivate(true, 1000L, 350, 100));
        assertFalse(timer.shouldActivate(false, 1100L, 350, 100));
        assertTrue(timer.shouldActivate(true, 1101L, 350, 100));
    }

    @Test
    public void delayedTicksDoNotCreateARepeatBacklog() {
        InputRepeatTimer timer = new InputRepeatTimer();

        assertTrue(timer.shouldActivate(true, 1000L, 350, 100));
        assertTrue(timer.shouldActivate(true, 2000L, 350, 100));
        assertFalse(timer.shouldActivate(true, 2001L, 350, 100));
    }
}
