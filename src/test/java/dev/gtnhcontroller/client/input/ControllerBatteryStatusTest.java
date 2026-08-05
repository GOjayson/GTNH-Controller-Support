package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ControllerBatteryStatusTest {

    @Test
    public void formatsAvailableBatteryPercentageAndState() {
        ControllerBatteryStatus status = ControllerBatteryStatus.fromSdl(3, 72);

        assertTrue(status.isAvailable());
        assertEquals(72, status.getPercent());
        assertEquals("72% (Charging)", status.getDisplayText());
    }

    @Test
    public void keepsUnavailableBatteryStateHarmless() {
        assertFalse(ControllerBatteryStatus.UNAVAILABLE.isAvailable());
        assertEquals("Unavailable", ControllerBatteryStatus.UNAVAILABLE.getDisplayText());
    }
}
