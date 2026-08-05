package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class ControllerChordCaptureTest {

    @Test
    public void accumulatesInputsUntilTheCombinationIsReleased() {
        ControllerChordCapture capture = new ControllerChordCapture();

        assertNull(capture.update(Arrays.asList("BUTTON:LEFT_SHOULDER")));
        assertNull(capture.update(Arrays.asList("BUTTON:LEFT_SHOULDER", "BUTTON:SOUTH")));
        assertNull(capture.update(Arrays.asList("BUTTON:SOUTH")));
        assertEquals("BUTTON:LEFT_SHOULDER+BUTTON:SOUTH", capture.update(Collections.<String>emptyList()));
    }

    @Test
    public void ignoresIdleFramesBeforeCaptureStarts() {
        ControllerChordCapture capture = new ControllerChordCapture();

        assertNull(capture.update(Collections.<String>emptyList()));
    }
}
