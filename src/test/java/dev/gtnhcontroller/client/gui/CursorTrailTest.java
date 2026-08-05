package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CursorTrailTest {

    @Test
    public void limitsPointsAndIgnoresTinyMovements() {
        CursorTrail trail = new CursorTrail();
        trail.record(0, 0);
        trail.record(1, 1);
        for (int value = 1; value <= 10; value++) {
            trail.record(value * 4, 0);
        }

        assertEquals(
            8,
            trail.getPoints()
                .size());
        assertEquals(
            12,
            trail.getPoints()
                .get(0).x);
    }
}
