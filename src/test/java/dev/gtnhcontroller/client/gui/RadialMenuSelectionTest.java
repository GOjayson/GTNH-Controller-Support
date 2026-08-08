package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RadialMenuSelectionTest {

    @Test
    public void selectsCardinalDirectionsClockwiseFromUp() {
        assertEquals(0, RadialMenuSelection.select(0.0F, -1.0F, 0.45F));
        assertEquals(2, RadialMenuSelection.select(1.0F, 0.0F, 0.45F));
        assertEquals(4, RadialMenuSelection.select(0.0F, 1.0F, 0.45F));
        assertEquals(6, RadialMenuSelection.select(-1.0F, 0.0F, 0.45F));
    }

    @Test
    public void selectsDiagonalDirections() {
        assertEquals(1, RadialMenuSelection.select(1.0F, -1.0F, 0.45F));
        assertEquals(3, RadialMenuSelection.select(1.0F, 1.0F, 0.45F));
        assertEquals(5, RadialMenuSelection.select(-1.0F, 1.0F, 0.45F));
        assertEquals(7, RadialMenuSelection.select(-1.0F, -1.0F, 0.45F));
    }

    @Test
    public void deadZoneCancelsSelection() {
        assertEquals(-1, RadialMenuSelection.select(0.20F, 0.20F, 0.45F));
    }

    @Test
    public void dPadSelectsCardinalDirections() {
        assertEquals(0, RadialMenuSelection.selectDPad(true, false, false, false));
        assertEquals(2, RadialMenuSelection.selectDPad(false, false, false, true));
        assertEquals(4, RadialMenuSelection.selectDPad(false, true, false, false));
        assertEquals(6, RadialMenuSelection.selectDPad(false, false, true, false));
    }

    @Test
    public void simultaneousDPadDirectionsSelectDiagonals() {
        assertEquals(1, RadialMenuSelection.selectDPad(true, false, false, true));
        assertEquals(3, RadialMenuSelection.selectDPad(false, true, false, true));
        assertEquals(5, RadialMenuSelection.selectDPad(false, true, true, false));
        assertEquals(7, RadialMenuSelection.selectDPad(true, false, true, false));
    }

    @Test
    public void opposingOrReleasedDPadDirectionsCancelSelection() {
        assertEquals(-1, RadialMenuSelection.selectDPad(false, false, false, false));
        assertEquals(-1, RadialMenuSelection.selectDPad(true, true, false, false));
        assertEquals(-1, RadialMenuSelection.selectDPad(false, false, true, true));
    }

    @Test
    public void dPadSelectionRemainsLatchedAfterRelease() {
        assertEquals(4, RadialMenuSelection.updateDPadLatch(-1, 4));
        assertEquals(4, RadialMenuSelection.updateDPadLatch(4, -1));
        assertEquals(6, RadialMenuSelection.updateDPadLatch(4, 6));
    }

    @Test
    public void toggleModeRemembersStickSelectionAfterCentering() {
        assertEquals(0, RadialMenuSelection.updateToggleLatch(-1, 0, -1, true));
        assertEquals(0, RadialMenuSelection.updateToggleLatch(0, -1, -1, true));
    }

    @Test
    public void newestToggleDirectionReplacesTheLatchedSelection() {
        assertEquals(2, RadialMenuSelection.updateToggleLatch(0, 2, -1, true));
        assertEquals(6, RadialMenuSelection.updateToggleLatch(2, -1, 6, true));
    }

    @Test
    public void holdModeDoesNotLatchStickSelection() {
        assertEquals(-1, RadialMenuSelection.updateToggleLatch(0, -1, -1, false));
        assertEquals(-1, RadialMenuSelection.updateToggleLatch(-1, 4, -1, false));
    }
}
