package dev.gtnhcontroller.client.gui;

import dev.gtnhcontroller.client.input.RadialMenuConfigCodec;

final class RadialMenuSelection {

    private static final double FULL_CIRCLE = Math.PI * 2.0D;

    private RadialMenuSelection() {}

    static int select(float axisX, float axisY, float threshold) {
        if (axisX * axisX + axisY * axisY < threshold * threshold) {
            return -1;
        }

        double sectorSize = FULL_CIRCLE / RadialMenuConfigCodec.SLOT_COUNT;
        double angleFromTop = Math.atan2(axisY, axisX) + Math.PI / 2.0D;
        double normalizedAngle = (angleFromTop + sectorSize / 2.0D + FULL_CIRCLE) % FULL_CIRCLE;
        return (int) (normalizedAngle / sectorSize);
    }

    static int selectDPad(boolean up, boolean down, boolean left, boolean right) {
        int directionX = (right ? 1 : 0) - (left ? 1 : 0);
        int directionY = (down ? 1 : 0) - (up ? 1 : 0);
        return select(directionX, directionY, 0.5F);
    }

    static int updateDPadLatch(int previousSelection, int currentSelection) {
        return currentSelection >= 0 ? currentSelection : previousSelection;
    }

    static int updateToggleLatch(int previousSelection, int stickSelection, int dPadSelection, boolean toggleMode) {
        if (!toggleMode) {
            return -1;
        }
        if (dPadSelection >= 0) {
            return dPadSelection;
        }
        return stickSelection >= 0 ? stickSelection : previousSelection;
    }
}
