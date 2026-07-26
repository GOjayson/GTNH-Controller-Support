package dev.gtnhcontroller.client.gui;

final class CursorMotion {

    private static final float MAXIMUM_STOP_SECONDS = 0.15F;

    private CursorMotion() {}

    static float updateVelocity(float currentVelocity, float targetVelocity, float acceleration, float deceleration,
        float elapsedSeconds) {
        boolean reversing = currentVelocity != 0.0F && targetVelocity != 0.0F
            && Math.signum(currentVelocity) != Math.signum(targetVelocity);
        if (reversing) {
            return approach(currentVelocity, 0.0F, deceleration * elapsedSeconds);
        }

        boolean sameDirection = currentVelocity == 0.0F || Math.signum(currentVelocity) == Math.signum(targetVelocity);
        boolean speedingUp = sameDirection && Math.abs(targetVelocity) > Math.abs(currentVelocity);
        float rate = speedingUp ? acceleration : deceleration;
        return approach(currentVelocity, targetVelocity, rate * elapsedSeconds);
    }

    static boolean isStopped(float velocityX, float velocityY) {
        return velocityX == 0.0F && velocityY == 0.0F;
    }

    static float responseRate(float configuredRate, float sensitivity) {
        return configuredRate * Math.max(sensitivity, 1.0F);
    }

    static float decelerationRate(float configuredRate, float sensitivity, float maximumSpeed) {
        float sensitivityAdjustedRate = responseRate(configuredRate, sensitivity);
        float rateForBoundedStop = Math.abs(maximumSpeed) / MAXIMUM_STOP_SECONDS;
        return Math.max(sensitivityAdjustedRate, rateForBoundedStop);
    }

    private static float approach(float value, float target, float maximumChange) {
        if (value < target) {
            return Math.min(value + maximumChange, target);
        }
        if (value > target) {
            return Math.max(value - maximumChange, target);
        }
        return target;
    }
}
