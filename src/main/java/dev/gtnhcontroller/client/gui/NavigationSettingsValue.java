package dev.gtnhcontroller.client.gui;

final class NavigationSettingsValue {

    static final float DEFAULT_PRECISION_SCALE = 0.30F;
    static final int DEFAULT_INITIAL_DELAY_MILLIS = 350;
    static final int DEFAULT_REPEAT_INTERVAL_MILLIS = 100;

    private NavigationSettingsValue() {}

    static float adjustPrecisionScale(float currentValue, int direction) {
        float step = 0.10F;
        float snappedValue = Math.round(currentValue / step) * step;
        return Math.max(0.10F, Math.min(snappedValue + direction * step, 1.0F));
    }

    static int adjustInitialDelay(int currentValue, int direction) {
        return adjustInteger(currentValue, direction, 50, 100, 1000);
    }

    static int adjustRepeatInterval(int currentValue, int direction) {
        return adjustInteger(currentValue, direction, 25, 50, 500);
    }

    static String formatPercent(float value) {
        return Math.round(value * 100.0F) + "%";
    }

    static String formatMillis(int value) {
        return value + " ms";
    }

    private static int adjustInteger(int currentValue, int direction, int step, int minimum, int maximum) {
        int snappedValue = Math.round(currentValue / (float) step) * step;
        return Math.max(minimum, Math.min(snappedValue + direction * step, maximum));
    }
}
