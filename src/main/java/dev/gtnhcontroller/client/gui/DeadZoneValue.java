package dev.gtnhcontroller.client.gui;

import java.util.Locale;

public final class DeadZoneValue {

    public static final float MINIMUM = 0.0F;
    public static final float MAXIMUM = 0.90F;
    public static final float STEP = 0.05F;

    private DeadZoneValue() {}

    public static float adjust(float value, int direction) {
        return adjust(value, direction, MINIMUM);
    }

    public static float adjustTrigger(float value, int direction) {
        return adjust(value, direction, STEP);
    }

    public static String format(float value) {
        return String.format(Locale.ROOT, "%d%%", Math.round(value * 100.0F));
    }

    private static float adjust(float value, int direction, float minimum) {
        float adjusted = Math.round((value + direction * STEP) * 100.0F) / 100.0F;
        return Math.max(minimum, Math.min(MAXIMUM, adjusted));
    }
}
