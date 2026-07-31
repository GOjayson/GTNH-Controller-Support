package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RadialMenuActivationModeTest {

    @Test
    public void cyclesBetweenHoldAndToggle() {
        assertEquals(RadialMenuActivationMode.TOGGLE, RadialMenuActivationMode.HOLD.next());
        assertEquals(RadialMenuActivationMode.HOLD, RadialMenuActivationMode.TOGGLE.next());
    }

    @Test
    public void parsesCaseInsensitivelyAndFallsBack() {
        assertEquals(RadialMenuActivationMode.TOGGLE, RadialMenuActivationMode.parse(" toggle ", null));
        assertEquals(
            RadialMenuActivationMode.HOLD,
            RadialMenuActivationMode.parse("unsupported", RadialMenuActivationMode.HOLD));
    }
}
