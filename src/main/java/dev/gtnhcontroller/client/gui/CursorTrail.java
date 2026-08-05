package dev.gtnhcontroller.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class CursorTrail {

    private static final int MAX_POINTS = 8;
    private static final int MINIMUM_DISTANCE_SQUARED = 9;

    private final List<Point> points = new ArrayList<Point>();

    void record(int x, int y) {
        if (!points.isEmpty()) {
            Point newest = points.get(points.size() - 1);
            int deltaX = x - newest.x;
            int deltaY = y - newest.y;
            if (deltaX * deltaX + deltaY * deltaY < MINIMUM_DISTANCE_SQUARED) {
                return;
            }
        }
        points.add(new Point(x, y));
        if (points.size() > MAX_POINTS) {
            points.remove(0);
        }
    }

    List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }

    void clear() {
        points.clear();
    }

    static final class Point {

        final int x;
        final int y;

        private Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
