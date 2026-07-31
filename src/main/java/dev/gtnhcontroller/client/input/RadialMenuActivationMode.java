package dev.gtnhcontroller.client.input;

public enum RadialMenuActivationMode {

    HOLD("Hold"),
    TOGGLE("Toggle");

    public final String displayName;

    RadialMenuActivationMode(String displayName) {
        this.displayName = displayName;
    }

    public RadialMenuActivationMode next() {
        RadialMenuActivationMode[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }

    public static RadialMenuActivationMode parse(String value, RadialMenuActivationMode fallback) {
        if (value != null) {
            for (RadialMenuActivationMode mode : values()) {
                if (mode.name()
                    .equalsIgnoreCase(value.trim())) {
                    return mode;
                }
            }
        }
        return fallback;
    }
}
