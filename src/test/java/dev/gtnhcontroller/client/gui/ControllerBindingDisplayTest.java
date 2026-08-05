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
        assertEquals(
            "Left Shoulder + South / Left Trigger + North",
            ControllerBindingDisplay.format("BUTTON:LEFT_SHOULDER+BUTTON:SOUTH|TRIGGER:LEFT_TRIGGER+BUTTON:NORTH"));
    }

    @Test
    public void formatsEmptyBinding() {
        assertEquals("Unbound", ControllerBindingDisplay.format("NONE"));
    }
}
