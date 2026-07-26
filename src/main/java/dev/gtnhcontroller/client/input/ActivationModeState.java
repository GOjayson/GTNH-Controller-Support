package dev.gtnhcontroller.client.input;

/**
 * Resolves a physical controller action into its configured accessibility activation behavior.
 */
final class ActivationModeState {

    private boolean toggled;

    boolean update(ActivationMode mode, boolean down, boolean pressed) {
        switch (mode) {
            case HOLD:
                toggled = false;
                return down;
            case TOGGLE:
                if (pressed) {
                    toggled = !toggled;
                }
                return toggled;
            case PRESS:
                toggled = false;
                return pressed;
            default:
                throw new IllegalStateException("Unknown activation mode: " + mode);
        }
    }

    void reset() {
        toggled = false;
    }
}
