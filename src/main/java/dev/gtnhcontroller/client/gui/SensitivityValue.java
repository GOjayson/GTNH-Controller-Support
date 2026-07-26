package dev.gtnhcontroller.client.gui;

final class SensitivityValue {

    static final float DEFAULT = 1.0F;

    private static final float MINIMUM = 0.25F;
    private static final float MAXIMUM = 5.0F;
    private static final float STEP = 0.25F;

    private SensitivityValue() {}

    static float adjust(float currentValue, int direction) {
        float snappedValue = Math.round(currentValue / STEP) * STEP;
        return Math.max(MINIMUM, Math.min(snappedValue + direction * STEP, MAXIMUM));
    }

    static String format(float value) {
        return Math.round(value * 100.0F) + "%";
    }
}
