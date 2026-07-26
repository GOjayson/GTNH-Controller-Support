package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CreativeInventoryScrollTest {

    private static final float TOLERANCE = 0.0001F;

    @Test
    public void scrollsOneCreativeInventoryRowAtATime() {
        assertEquals(0.20F, CreativeInventoryScroll.nextPosition(0.0F, 90, 1), TOLERANCE);
        assertEquals(0.40F, CreativeInventoryScroll.nextPosition(0.20F, 90, 1), TOLERANCE);
        assertEquals(0.20F, CreativeInventoryScroll.nextPosition(0.40F, 90, -1), TOLERANCE);
    }

    @Test
    public void clampsAtBothEnds() {
        assertEquals(0.0F, CreativeInventoryScroll.nextPosition(0.0F, 90, -1), TOLERANCE);
        assertEquals(1.0F, CreativeInventoryScroll.nextPosition(1.0F, 90, 1), TOLERANCE);
    }

    @Test
    public void shortListsRemainStable() {
        assertEquals(0.0F, CreativeInventoryScroll.nextPosition(0.0F, 20, 1), TOLERANCE);
    }
}
