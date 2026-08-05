package dev.gtnhcontroller.client.gui;

final class AcceleratedInputRepeatTimer {

    private static final int ACCELERATION_RAMP_MILLIS = 1500;

    private boolean held;
    private long heldSinceMillis;
    private long nextActivationMillis;

    boolean shouldActivate(boolean down, long currentTimeMillis, int initialDelayMillis, int repeatIntervalMillis,
        boolean accelerationEnabled, float maximumMultiplier) {
        if (!down) {
            reset();
            return false;
        }
        if (!held) {
            held = true;
            heldSinceMillis = currentTimeMillis;
            nextActivationMillis = currentTimeMillis + initialDelayMillis;
            return true;
        }
        if (currentTimeMillis < nextActivationMillis) {
            return false;
        }

        float multiplier = accelerationEnabled
            ? accelerationMultiplier(currentTimeMillis, initialDelayMillis, maximumMultiplier)
            : 1.0F;
        int interval = Math.max(Math.round(repeatIntervalMillis / multiplier), 1);
        nextActivationMillis = currentTimeMillis + interval;
        return true;
    }

    void reset() {
        held = false;
        heldSinceMillis = 0L;
        nextActivationMillis = 0L;
    }

    private float accelerationMultiplier(long currentTimeMillis, int initialDelayMillis, float maximumMultiplier) {
        float maximum = Math.max(maximumMultiplier, 1.0F);
        long accelerationTime = Math.max(currentTimeMillis - heldSinceMillis - initialDelayMillis, 0L);
        float progress = Math.min(accelerationTime / (float) ACCELERATION_RAMP_MILLIS, 1.0F);
        return 1.0F + (maximum - 1.0F) * progress;
    }
}
