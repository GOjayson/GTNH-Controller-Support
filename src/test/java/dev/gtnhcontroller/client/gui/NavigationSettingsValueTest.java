package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NavigationSettingsValueTest {

    private static final float TOLERANCE = 0.0001F;

    @Test
    public void precisionScaleUsesTenPercentStepsAndBounds() {
        assertEquals(0.40F, NavigationSettingsValue.adjustPrecisionScale(0.30F, 1), TOLERANCE);
        assertEquals(0.10F, NavigationSettingsValue.adjustPrecisionScale(0.10F, -1), TOLERANCE);
        assertEquals(1.0F, NavigationSettingsValue.adjustPrecisionScale(1.0F, 1), TOLERANCE);
    }

    @Test
    public void repeatValuesSnapAndStayWithinBounds() {
        assertEquals(400, NavigationSettingsValue.adjustInitialDelay(350, 1));
        assertEquals(100, NavigationSettingsValue.adjustInitialDelay(100, -1));
        assertEquals(125, NavigationSettingsValue.adjustRepeatInterval(100, 1));
        assertEquals(500, NavigationSettingsValue.adjustRepeatInterval(500, 1));
    }

    @Test
    public void valuesHaveReadableLabels() {
        assertEquals("30%", NavigationSettingsValue.formatPercent(0.30F));
        assertEquals("350 ms", NavigationSettingsValue.formatMillis(350));
    }
}
