package dev.gtnhcontroller.client.gui;

import java.util.function.IntSupplier;

/** Supplies a controller button to NEI during a GUI mouse callback, including button release. */
public final class ControllerMouseClickContext implements AutoCloseable {

    private static final ThreadLocal<Integer> MOUSE_BUTTON = new ThreadLocal<Integer>();

    private final Integer previousButton;
    private boolean closed;

    private ControllerMouseClickContext(int mouseButton) {
        previousButton = MOUSE_BUTTON.get();
        MOUSE_BUTTON.set(Integer.valueOf(mouseButton));
    }

    public static ControllerMouseClickContext open(int mouseButton) {
        if (mouseButton < 0) {
            throw new IllegalArgumentException("A controller click must have a mouse button");
        }
        return new ControllerMouseClickContext(mouseButton);
    }

    public static void dispatch(int mouseButton, Runnable callback) {
        try (ControllerMouseClickContext ignored = open(mouseButton)) {
            callback.run();
        }
    }

    /** Releases GUI state without turning a disconnect, focus loss or screen change into an NEI shortcut. */
    public static void dispatchCancelled(Runnable callback) {
        try (ControllerMouseClickContext ignored = new ControllerMouseClickContext(-1)) {
            callback.run();
        }
    }

    /** Returns -1 outside controller dispatch or during a cancelled dispatch. */
    public static int getMouseButton() {
        Integer mouseButton = MOUSE_BUTTON.get();
        return mouseButton == null ? -1 : mouseButton.intValue();
    }

    /** Reads native state only outside controller dispatch; cancellation deliberately returns no button. */
    public static int resolveMouseButton(IntSupplier nativeMouseButton) {
        Integer mouseButton = MOUSE_BUTTON.get();
        return mouseButton == null ? nativeMouseButton.getAsInt() : mouseButton.intValue();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (previousButton == null) {
            MOUSE_BUTTON.remove();
        } else {
            MOUSE_BUTTON.set(previousButton);
        }
    }
}
