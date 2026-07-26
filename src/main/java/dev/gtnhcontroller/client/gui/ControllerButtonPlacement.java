package dev.gtnhcontroller.client.gui;

final class ControllerButtonPlacement {

    private static final int MARGIN = 5;
    private static final int GAP = 5;
    private static final int SIDE_WIDTH = 120;
    private static final int FALLBACK_WIDTH = 150;

    static final class Position {

        final int x;
        final int y;
        final int width;

        private Position(int x, int y, int width) {
            this.x = x;
            this.y = y;
            this.width = width;
        }
    }

    private ControllerButtonPlacement() {}

    static Position choose(int screenWidth, int leftOptionEdge, int rightOptionEdge, int topOptionY, int fallbackY) {
        if (topOptionY != Integer.MAX_VALUE) {
            int leftX = leftOptionEdge - GAP - SIDE_WIDTH;
            if (leftX >= MARGIN) {
                return new Position(leftX, topOptionY, SIDE_WIDTH);
            }

            int rightX = rightOptionEdge + GAP;
            if (rightX + SIDE_WIDTH <= screenWidth - MARGIN) {
                return new Position(rightX, topOptionY, SIDE_WIDTH);
            }
        }

        return new Position((screenWidth - FALLBACK_WIDTH) / 2, fallbackY, FALLBACK_WIDTH);
    }
}
