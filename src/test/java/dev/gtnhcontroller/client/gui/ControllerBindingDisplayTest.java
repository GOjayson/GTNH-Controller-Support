package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ControllerBindingDisplayTest {

    @Test
    public void formatsButtonsTriggersAndAlternatives() {
        assertEquals("South", ControllerBindingDisplay.format("BUTTON:SOUTH"));
        assertEquals("Right Trigger", ControllerBindingDisplay.format("TRIGGER:RIGHT_TRIGGER"));
        assertEquals(
            "Left Shoulder / D-pad Left",
            ControllerBindingDisplay.format("BUTTON:LEFT_SHOULDER|BUTTON:DPAD_LEFT"));
    }

    @Test
    public void formatsEmptyBinding() {
        assertEquals("Unbound", ControllerBindingDisplay.format("NONE"));
    }
}
