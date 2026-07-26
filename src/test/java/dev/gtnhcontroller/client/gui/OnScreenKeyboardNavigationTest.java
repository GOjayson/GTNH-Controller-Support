package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OnScreenKeyboardNavigationTest {

    @Test
    public void horizontalNavigationWrapsWithinTheCurrentRow() {
        OnScreenKeyboardLayout.Key[][] rows = OnScreenKeyboardLayout.getRows(false);

        assertPosition(OnScreenKeyboardNavigation.move(1, 0, -1, 0, rows), 1, 9);
        assertPosition(OnScreenKeyboardNavigation.move(1, 9, 1, 0, rows), 1, 0);
    }

    @Test
    public void verticalNavigationUsesVisualKeyCenters() {
        OnScreenKeyboardLayout.Key[][] rows = OnScreenKeyboardLayout.getRows(false);

        assertPosition(OnScreenKeyboardNavigation.move(3, 9, 0, 1, rows), 4, 5);
        assertPosition(OnScreenKeyboardNavigation.move(3, 0, 0, 1, rows), 4, 0);
    }

    @Test
    public void verticalNavigationWrapsBetweenFirstAndLastRows() {
        OnScreenKeyboardLayout.Key[][] rows = OnScreenKeyboardLayout.getRows(false);

        assertPosition(OnScreenKeyboardNavigation.move(0, 0, 0, -1, rows), 4, 0);
        assertPosition(OnScreenKeyboardNavigation.move(4, 5, 0, 1, rows), 0, 9);
    }

    private static void assertPosition(OnScreenKeyboardNavigation.Position position, int row, int column) {
        assertEquals(row, position.row);
        assertEquals(column, position.column);
    }
}
