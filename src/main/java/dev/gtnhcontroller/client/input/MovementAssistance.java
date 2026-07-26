package dev.gtnhcontroller.client.input;

/**
 * Small, side-effect-free decisions used by the accessibility movement assists.
 */
final class MovementAssistance {

    private MovementAssistance() {}

    static boolean mergeSneak(boolean keyboardSneak, boolean controllerSneak) {
        return keyboardSneak || controllerSneak;
    }

    static boolean shouldAutoJump(boolean enabled, boolean moving, boolean onGround, boolean collidedHorizontally,
        boolean inLiquid, boolean sneaking, boolean riding, boolean hasStepClearance) {
        return enabled && moving
            && onGround
            && collidedHorizontally
            && !inLiquid
            && !sneaking
            && !riding
            && hasStepClearance;
    }

    static boolean updateAutoSwim(boolean enabled, boolean inWater, boolean onGround, boolean jumpPressed,
        boolean currentlySwimmingUp, boolean withinSurfaceGrace) {
        if (!enabled) {
            return false;
        }
        if (jumpPressed) {
            return currentlySwimmingUp ? false : inWater;
        }
        if (!currentlySwimmingUp) {
            return false;
        }
        return inWater || (!onGround && withinSurfaceGrace);
    }
}
