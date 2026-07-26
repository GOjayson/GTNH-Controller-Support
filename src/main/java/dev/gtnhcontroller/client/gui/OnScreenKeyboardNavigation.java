package dev.gtnhcontroller.client.gui;

final class OnScreenKeyboardNavigation {

    static final class Position {

        final int row;
        final int column;

        Position(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }

    private OnScreenKeyboardNavigation() {}

    static Position move(int row, int column, int directionX, int directionY, OnScreenKeyboardLayout.Key[][] rows) {
        int clampedRow = clamp(row, 0, rows.length - 1);
        int clampedColumn = clamp(column, 0, rows[clampedRow].length - 1);
        if (directionX != 0) {
            int targetColumn = Math.floorMod(clampedColumn + Integer.signum(directionX), rows[clampedRow].length);
            return new Position(clampedRow, targetColumn);
        }
        if (directionY == 0) {
            return new Position(clampedRow, clampedColumn);
        }

        float sourceCenter = normalizedCenter(rows[clampedRow], clampedColumn);
        int targetRow = Math.floorMod(clampedRow + Integer.signum(directionY), rows.length);
        int targetColumn = nearestColumn(rows[targetRow], sourceCenter);
        return new Position(targetRow, targetColumn);
    }

    private static int nearestColumn(OnScreenKeyboardLayout.Key[] row, float sourceCenter) {
        int nearestColumn = 0;
        float nearestDistance = Float.MAX_VALUE;
        for (int column = 0; column < row.length; column++) {
            float distance = Math.abs(normalizedCenter(row, column) - sourceCenter);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestColumn = column;
            }
        }
        return nearestColumn;
    }

    private static float normalizedCenter(OnScreenKeyboardLayout.Key[] row, int column) {
        float totalWidth = 0.0F;
        float widthBefore = 0.0F;
        for (int index = 0; index < row.length; index++) {
            float width = row[index].getWidthWeight();
            totalWidth += width;
            if (index < column) {
                widthBefore += width;
            }
        }
        return (widthBefore + row[column].getWidthWeight() * 0.5F) / totalWidth;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(value, maximum));
    }
}
