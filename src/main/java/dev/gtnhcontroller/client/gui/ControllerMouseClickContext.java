package dev.gtnhcontroller.client.gui;

/** Supplies a controller button to NEI while dispatching one BetterQuesting click. */
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

    /** Returns -1 when NEI should read the normal native mouse event. */
    public static int getMouseButton() {
        Integer mouseButton = MOUSE_BUTTON.get();
        return mouseButton == null ? -1 : mouseButton.intValue();
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
