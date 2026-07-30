package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DeadZoneValueTest {

    private static final float TOLERANCE = 0.0001F;

    @Test
    public void adjustsInFivePercentStepsAndClampsStickRange() {
        assertEquals(0.23F, DeadZoneValue.adjust(0.18F, 1), TOLERANCE);
        assertEquals(0.10F, DeadZoneValue.adjust(0.15F, -1), TOLERANCE);
        assertEquals(0.0F, DeadZoneValue.adjust(0.0F, -1), TOLERANCE);
        assertEquals(0.90F, DeadZoneValue.adjust(0.90F, 1), TOLERANCE);
    }

    @Test
    public void triggerDeadZoneNeverReachesAlwaysPressedZero() {
        assertEquals(0.05F, DeadZoneValue.adjustTrigger(0.05F, -1), TOLERANCE);
    }

    @Test
    public void formatsDeadZoneAsPercentage() {
        assertEquals("0%", DeadZoneValue.format(0.0F));
        assertEquals("15%", DeadZoneValue.format(0.15F));
        assertEquals("50%", DeadZoneValue.format(0.50F));
    }
}
