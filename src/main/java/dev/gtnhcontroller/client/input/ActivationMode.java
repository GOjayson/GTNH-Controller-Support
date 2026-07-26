package dev.gtnhcontroller.client.input;

public enum ActivationMode {

    HOLD,
    TOGGLE,
    PRESS;

    public ActivationMode next() {
        ActivationMode[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }

    public static ActivationMode parse(String value, ActivationMode fallback) {
        if (value != null) {
            for (ActivationMode mode : values()) {
                if (mode.name()
                    .equalsIgnoreCase(value.trim())) {
                    return mode;
                }
            }
        }
        return fallback;
    }
}
