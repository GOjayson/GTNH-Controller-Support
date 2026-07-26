package dev.gtnhcontroller.client.gui;

final class CreativeInventoryScroll {

    private static final int COLUMNS = 9;
    private static final int VISIBLE_ROWS = 5;

    private CreativeInventoryScroll() {}

    static float nextPosition(float currentPosition, int itemCount, int direction) {
        int totalRows = (Math.max(itemCount, 0) + COLUMNS - 1) / COLUMNS;
        int scrollableRows = totalRows - VISIBLE_ROWS;
        if (scrollableRows <= 0) {
            return Math.max(0.0F, Math.min(currentPosition, 1.0F));
        }
        float nextPosition = currentPosition + direction / (float) scrollableRows;
        return Math.max(0.0F, Math.min(nextPosition, 1.0F));
    }
}
