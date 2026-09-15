package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

/** Exercises NEI's press-then-release shortcut contract without loading Minecraft or native mouse libraries. */
public class NeiMouseClickDispatchTest {

    @Test
    public void itemPanelRecipeUsesTheControllerReleaseInsteadOfTheLastNativeEvent() {
        for (int nativeButton = -1; nativeButton <= 2; nativeButton++) {
            ReleaseShortcut shortcut = new ReleaseShortcut(nativeButton);
            ControllerMouseClickContext.dispatch(0, shortcut::press);
            assertEquals(0, shortcut.recipes);
            ControllerMouseClickContext.dispatch(0, shortcut::release);
            assertEquals(1, shortcut.recipes);
            assertEquals(0, shortcut.usages);
            assertEquals(nativeButton, ControllerMouseClickContext.resolveMouseButton(() -> shortcut.nativeButton));
        }
    }

    @Test
    public void itemPanelUsagesUseTheControllerReleaseInsteadOfTheLastNativeEvent() {
        for (int nativeButton = -1; nativeButton <= 2; nativeButton++) {
            ReleaseShortcut shortcut = new ReleaseShortcut(nativeButton);
            ControllerMouseClickContext.dispatch(1, shortcut::press);
            assertEquals(0, shortcut.usages);
            ControllerMouseClickContext.dispatch(1, shortcut::release);
            assertEquals(0, shortcut.recipes);
            assertEquals(1, shortcut.usages);
        }
    }

    @Test
    public void forcedReleaseClearsThePendingClickWithoutOpeningARecipeOrUsage() {
        for (int button = 0; button <= 1; button++) {
            ReleaseShortcut shortcut = new ReleaseShortcut(button);
            ControllerMouseClickContext.dispatch(button, shortcut::press);
            ControllerMouseClickContext.dispatchCancelled(shortcut::release);
            assertFalse(shortcut.pressed);
            assertEquals(0, shortcut.recipes);
            assertEquals(0, shortcut.usages);

            // A subsequent deliberate gesture must still work after reconnecting or reopening the screen.
            ControllerMouseClickContext.dispatch(0, shortcut::press);
            ControllerMouseClickContext.dispatch(0, shortcut::release);
            assertEquals(1, shortcut.recipes);
        }
    }

    @Test
    public void questBookShortcutsCanStillActivateDuringThePressCallback() {
        ReleaseShortcut shortcut = new ReleaseShortcut(1);
        ControllerMouseClickContext.dispatch(0, () -> {
            try (ControllerMouseClickContext ignored = ControllerMouseClickContext.open(0)) {
                shortcut.openShortcut();
            }
        });
        ControllerMouseClickContext.dispatchCancelled(shortcut::release);
        assertEquals(1, shortcut.recipes);
        assertEquals(0, shortcut.usages);
    }

    @Test
    public void nativeStateIsReadOnlyWhenThereIsNoControllerCallback() {
        ControllerMouseClickContext.dispatch(0, () -> assertEquals(0, resolveWithoutNativeMouse()));
        ControllerMouseClickContext.dispatchCancelled(() -> assertEquals(-1, resolveWithoutNativeMouse()));
        assertEquals(1, ControllerMouseClickContext.resolveMouseButton(() -> 1));
        assertEquals(0, ControllerMouseClickContext.resolveMouseButton(() -> 0));
    }

    @Test
    public void nestedCleanupRestoresTheOuterButtonEvenIfTheHandlerThrows() {
        ControllerMouseClickContext.dispatch(1, () -> {
            try {
                ControllerMouseClickContext.dispatchCancelled(() -> {
                    assertEquals(-1, resolveWithoutNativeMouse());
                    ControllerMouseClickContext.dispatch(0, () -> assertEquals(0, resolveWithoutNativeMouse()));
                    assertEquals(-1, resolveWithoutNativeMouse());
                    throw new IllegalStateException("Simulated GUI cleanup failure");
                });
                fail("The callback failure must propagate");
            } catch (IllegalStateException expected) {
                assertEquals(1, resolveWithoutNativeMouse());
            }
        });
        assertEquals(0, ControllerMouseClickContext.resolveMouseButton(() -> 0));
    }

    @Test
    public void aFailedReleaseDoesNotAffectTheNextPhysicalMouseEvent() {
        try {
            ControllerMouseClickContext
                .dispatch(1, () -> { throw new IllegalStateException("Simulated recipe handler failure"); });
            fail("The callback failure must propagate");
        } catch (IllegalStateException expected) {
            assertEquals(0, ControllerMouseClickContext.resolveMouseButton(() -> 0));
        }
    }

    private static int resolveWithoutNativeMouse() {
        return ControllerMouseClickContext.resolveMouseButton(
            () -> { throw new AssertionError("Controller dispatch must not read a stale native mouse event"); });
    }

    private static final class ReleaseShortcut {

        final int nativeButton;
        boolean pressed;
        int recipes;
        int usages;

        ReleaseShortcut(int nativeButton) {
            this.nativeButton = nativeButton;
        }

        void press() {
            pressed = true;
        }

        void release() {
            if (pressed) {
                openShortcut();
                pressed = false;
            }
        }

        void openShortcut() {
            int button = ControllerMouseClickContext.resolveMouseButton(() -> nativeButton);
            if (button == 0) {
                recipes++;
            } else if (button == 1) {
                usages++;
            }
        }
    }
}
