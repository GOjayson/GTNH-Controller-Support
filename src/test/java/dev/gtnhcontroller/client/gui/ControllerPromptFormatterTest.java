package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ControllerPromptFormatterTest {

    @Test
    public void usesFamiliarXboxStylePromptLabels() {
        assertEquals("A", ControllerPromptFormatter.format("BUTTON:SOUTH"));
        assertEquals("LB+A", ControllerPromptFormatter.format("BUTTON:LEFT_SHOULDER+BUTTON:SOUTH"));
        assertEquals("LT/Y", ControllerPromptFormatter.format("TRIGGER:LEFT_TRIGGER|BUTTON:NORTH"));
    }
}
