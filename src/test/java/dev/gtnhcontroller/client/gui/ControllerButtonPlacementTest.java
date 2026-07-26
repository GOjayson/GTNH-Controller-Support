package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ControllerButtonPlacementTest {

    @Test
    public void wideScreensPlaceControllerBesideTheTopOptions() {
        ControllerButtonPlacement.Position position = ControllerButtonPlacement.choose(1000, 345, 655, 18, 547);

        assertEquals(220, position.x);
        assertEquals(18, position.y);
        assertEquals(120, position.width);
    }

    @Test
    public void narrowScreensUseASeparateCenteredFallbackRow() {
        ControllerButtonPlacement.Position position = ControllerButtonPlacement.choose(320, 5, 315, 18, 187);

        assertEquals(85, position.x);
        assertEquals(187, position.y);
        assertEquals(150, position.width);
    }
}
