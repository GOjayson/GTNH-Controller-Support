package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SensitivityValueTest {

    private static final float TOLERANCE = 0.0001F;

    @Test
    public void adjustsInQuarterStepsAndClampsTheRange() {
        assertEquals(1.25F, SensitivityValue.adjust(1.0F, 1), TOLERANCE);
        assertEquals(0.75F, SensitivityValue.adjust(1.0F, -1), TOLERANCE);
        assertEquals(5.0F, SensitivityValue.adjust(5.0F, 1), TOLERANCE);
        assertEquals(0.25F, SensitivityValue.adjust(0.25F, -1), TOLERANCE);
    }

    @Test
    public void formatsSensitivityAsPercentage() {
        assertEquals("25%", SensitivityValue.format(0.25F));
        assertEquals("100%", SensitivityValue.format(1.0F));
        assertEquals("500%", SensitivityValue.format(5.0F));
    }
}
