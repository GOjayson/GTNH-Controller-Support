package dev.gtnhcontroller.client.gui;

final class InputRepeatTimer {

    private boolean held;
    private long nextActivationMillis;

    boolean shouldActivate(boolean down, long currentTimeMillis, int initialDelayMillis, int repeatIntervalMillis) {
        if (!down) {
            reset();
            return false;
        }
        if (!held) {
            held = true;
            nextActivationMillis = currentTimeMillis + initialDelayMillis;
            return true;
        }
        if (currentTimeMillis < nextActivationMillis) {
            return false;
        }

        nextActivationMillis = currentTimeMillis + repeatIntervalMillis;
        return true;
    }

    void reset() {
        held = false;
        nextActivationMillis = 0L;
    }
}
