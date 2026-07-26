package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GuiCoordinateMathTest {

    @Test
    public void scalesDisplayCoordinatesToGuiCoordinates() {
        assertEquals(480, GuiCoordinateMath.toGuiX(960, 960, 1920));
        assertEquals(269, GuiCoordinateMath.toGuiY(540, 540, 1080));
    }

    @Test
    public void flipsTheDisplayYOrigin() {
        assertEquals(539, GuiCoordinateMath.toGuiY(0, 540, 1080));
        assertEquals(0, GuiCoordinateMath.toGuiY(1079, 540, 1080));
    }

    @Test
    public void convertsGuiCoordinatesBackToMatchingDisplayCoordinates() {
        int[] guiXValues = { 0, 1, 479, 959 };
        for (int guiX : guiXValues) {
            int displayX = GuiCoordinateMath.toDisplayX(guiX, 960, 1920);
            assertEquals(guiX, GuiCoordinateMath.toGuiX(displayX, 960, 1920));
        }

        int[] guiYValues = { 0, 1, 269, 539 };
        for (int guiY : guiYValues) {
            int displayY = GuiCoordinateMath.toDisplayY(guiY, 540, 1080);
            assertEquals(guiY, GuiCoordinateMath.toGuiY(displayY, 540, 1080));
        }
    }

    @Test
    public void clampsDisplayCoordinatesToTheWindow() {
        assertEquals(0, GuiCoordinateMath.toDisplayX(-100.0F, 960, 1920));
        assertEquals(1919, GuiCoordinateMath.toDisplayX(2000.0F, 960, 1920));
        assertEquals(1079, GuiCoordinateMath.toDisplayY(-100.0F, 540, 1080));
        assertEquals(0, GuiCoordinateMath.toDisplayY(1000.0F, 540, 1080));
    }

    @Test
    public void nativeCursorSetterUsesTopDownYCoordinates() {
        int top = GuiCoordinateMath.toNativeCursorY(0.0F, 540, 1080);
        int middle = GuiCoordinateMath.toNativeCursorY(269.0F, 540, 1080);
        int bottom = GuiCoordinateMath.toNativeCursorY(539.0F, 540, 1080);

        assertEquals(0, top);
        assertEquals(1079, bottom);
        assertTrue(top < middle);
        assertTrue(middle < bottom);
    }
}
