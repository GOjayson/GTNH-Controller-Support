package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ControllerMouseClickContextTest {

    @Test
    public void usesNativeEventsOutsideControllerDispatch() {
        assertEquals(-1, ControllerMouseClickContext.getMouseButton());
    }

    @Test
    public void preservesLeftAndRightButtonsUntilDispatchEnds() {
        for (int button = 0; button <= 1; button++) {
            try (ControllerMouseClickContext ignored = ControllerMouseClickContext.open(button)) {
                assertEquals(button, ControllerMouseClickContext.getMouseButton());
            }
            assertEquals(-1, ControllerMouseClickContext.getMouseButton());
        }
    }

    @Test
    public void restoresOuterButtonAfterNestedDispatch() {
        try (ControllerMouseClickContext outer = ControllerMouseClickContext.open(0)) {
            try (ControllerMouseClickContext inner = ControllerMouseClickContext.open(1)) {
                assertEquals(1, ControllerMouseClickContext.getMouseButton());
            }
            assertEquals(0, ControllerMouseClickContext.getMouseButton());
        }
        assertEquals(-1, ControllerMouseClickContext.getMouseButton());
    }

    @Test
    public void releasesOverrideWhenDispatchThrows() {
        try {
            try (ControllerMouseClickContext ignored = ControllerMouseClickContext.open(1)) {
                throw new IllegalStateException("Simulated item handler failure");
            }
        } catch (IllegalStateException expected) {
            assertEquals(-1, ControllerMouseClickContext.getMouseButton());
        }
    }

    @Test
    public void restoresOuterButtonWhenNestedDispatchThrows() {
        try (ControllerMouseClickContext outer = ControllerMouseClickContext.open(0)) {
            try {
                try (ControllerMouseClickContext inner = ControllerMouseClickContext.open(1)) {
                    throw new IllegalStateException("Simulated nested handler failure");
                }
            } catch (IllegalStateException expected) {
                assertEquals(0, ControllerMouseClickContext.getMouseButton());
            }
        }
        assertEquals(-1, ControllerMouseClickContext.getMouseButton());
    }

    @Test
    public void doesNotExposeControllerClicksToOtherThreads() throws Exception {
        try (ControllerMouseClickContext ignored = ControllerMouseClickContext.open(0)) {
            FutureTask<Integer> otherThread = new FutureTask<Integer>(ControllerMouseClickContext::getMouseButton);
            new Thread(otherThread, "controller-mouse-context-test").start();
            assertEquals(
                -1,
                otherThread.get(5, TimeUnit.SECONDS)
                    .intValue());
            assertEquals(0, ControllerMouseClickContext.getMouseButton());
        }
        assertEquals(-1, ControllerMouseClickContext.getMouseButton());
    }

    @Test
    public void rejectsNonClickEventsWithoutChangingTheCurrentButton() {
        try (ControllerMouseClickContext ignored = ControllerMouseClickContext.open(0)) {
            try {
                ControllerMouseClickContext.open(-1);
                fail("A wheel or movement event must not become a controller click");
            } catch (IllegalArgumentException expected) {
                assertEquals(0, ControllerMouseClickContext.getMouseButton());
            }
        }
        assertEquals(-1, ControllerMouseClickContext.getMouseButton());
    }

    @Test
    public void repeatedCloseDoesNotClearANewerDispatch() {
        ControllerMouseClickContext closed = ControllerMouseClickContext.open(0);
        closed.close();
        try (ControllerMouseClickContext ignored = ControllerMouseClickContext.open(1)) {
            closed.close();
            assertEquals(1, ControllerMouseClickContext.getMouseButton());
        }
        assertEquals(-1, ControllerMouseClickContext.getMouseButton());
    }
}
