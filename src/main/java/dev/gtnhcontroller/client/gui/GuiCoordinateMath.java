package dev.gtnhcontroller.client.gui;

import dev.gtnhcontroller.client.input.InputMath;

final class GuiCoordinateMath {

    private GuiCoordinateMath() {}

    static int toGuiX(int displayX, int guiWidth, int displayWidth) {
        return displayX * guiWidth / Math.max(displayWidth, 1);
    }

    static int toGuiY(int displayY, int guiHeight, int displayHeight) {
        return guiHeight - displayY * guiHeight / Math.max(displayHeight, 1) - 1;
    }

    static int toDisplayX(float guiX, int guiWidth, int displayWidth) {
        return scaleGuiCoordinateToDisplay(guiX, guiWidth, displayWidth);
    }

    static int toDisplayY(float guiY, int guiHeight, int displayHeight) {
        int clampedGuiY = Math.round(InputMath.clamp(guiY, 0.0F, Math.max(guiHeight - 1, 0)));
        return scaleGuiCoordinateToDisplay(guiHeight - clampedGuiY - 1, guiHeight, displayHeight);
    }

    /**
     * lwjgl3ify's native window cursor setter uses a top-left origin even though LWJGL's reported mouse Y uses a
     * bottom-left origin.
     */
    static int toNativeCursorY(float guiY, int guiHeight, int displayHeight) {
        return scaleGuiCoordinateToDisplay(guiY, guiHeight, displayHeight);
    }

    private static int scaleGuiCoordinateToDisplay(float guiCoordinate, int guiSize, int displaySize) {
        if (guiSize <= 0 || displaySize <= 0) {
            return 0;
        }

        int clampedGuiCoordinate = Math.round(InputMath.clamp(guiCoordinate, 0.0F, guiSize - 1));
        if (clampedGuiCoordinate == 0) {
            return 0;
        }
        if (clampedGuiCoordinate == guiSize - 1) {
            return displaySize - 1;
        }

        int lowerDisplayCoordinate = divideRoundingUp((long) clampedGuiCoordinate * displaySize, guiSize);
        int upperDisplayCoordinate = divideRoundingUp((long) (clampedGuiCoordinate + 1) * displaySize, guiSize) - 1;
        return (lowerDisplayCoordinate + upperDisplayCoordinate) / 2;
    }

    private static int divideRoundingUp(long dividend, int divisor) {
        return (int) ((dividend + divisor - 1L) / divisor);
    }
}
