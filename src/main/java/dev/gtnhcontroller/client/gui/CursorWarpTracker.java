package dev.gtnhcontroller.client.gui;

/**
 * Separates programmatic native-cursor repositioning from genuine physical mouse movement.
 */
final class CursorWarpTracker {

    private static final int WARP_GRACE_FRAMES = 3;

    private int targetX;
    private int targetY;
    private int graceFrames;
    private boolean active;

    boolean needsWarp(int requestedX, int requestedY) {
        return !active || requestedX != targetX || requestedY != targetY;
    }

    void recordWarp(int requestedX, int requestedY) {
        targetX = requestedX;
        targetY = requestedY;
        graceFrames = WARP_GRACE_FRAMES;
        active = true;
    }

    boolean isPhysicalMovement(boolean controllerOwnsCursor, boolean controllerCursorMoving, int currentX, int currentY,
        int previousX, int previousY) {
        boolean positionChanged = currentX != previousX || currentY != previousY;
        if (controllerOwnsCursor && controllerCursorMoving) {
            return false;
        }
        if (controllerOwnsCursor && active && graceFrames > 0) {
            graceFrames--;
            return false;
        }
        return positionChanged;
    }

    void reset() {
        active = false;
        graceFrames = 0;
    }
}
