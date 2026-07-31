package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ControllerCalibrationTest {

    private static final float EPSILON = 0.0001F;

    @Test
    public void recommendsMinimumValuesForCenteredControls() {
        ControllerCalibration calibration = new ControllerCalibration();
        calibration.sampleRest(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        assertEquals(0.05F, calibration.suggestMovementDeadZone(), EPSILON);
        assertEquals(0.05F, calibration.suggestCameraDeadZone(), EPSILON);
        assertEquals(0.10F, calibration.suggestTriggerThreshold(), EPSILON);
        assertFalse(calibration.hasLeftStickDrift());
        assertFalse(calibration.hasRightStickDrift());
        assertFalse(calibration.hasTriggerDrift());
    }

    @Test
    public void addsSafetyMarginAndRoundsRecommendationsUp() {
        ControllerCalibration calibration = new ControllerCalibration();
        calibration.sampleRest(0.071F, 0.0F, 0.0F, -0.053F, 0.021F, 0.061F);

        assertEquals(0.12F, calibration.suggestMovementDeadZone(), EPSILON);
        assertEquals(0.10F, calibration.suggestCameraDeadZone(), EPSILON);
        assertEquals(0.15F, calibration.suggestTriggerThreshold(), EPSILON);
        assertTrue(calibration.hasLeftStickDrift());
        assertTrue(calibration.hasRightStickDrift());
        assertTrue(calibration.hasTriggerDrift());
    }

    @Test
    public void cursorRecommendationFollowsConfiguredPhysicalStick() {
        ControllerCalibration calibration = new ControllerCalibration();
        calibration.sampleRest(0.02F, 0.0F, 0.12F, 0.0F, 0.0F, 0.0F);

        assertEquals(calibration.suggestMovementDeadZone(), calibration.suggestCursorDeadZone(false), EPSILON);
        assertEquals(calibration.suggestCameraDeadZone(), calibration.suggestCursorDeadZone(true), EPSILON);
    }

    @Test
    public void rangeRequiresBothSticksAndBothTriggers() {
        ControllerCalibration calibration = new ControllerCalibration();
        calibration.sampleRange(1.0F, 0.0F, 0.80F, 0.0F, 1.0F, 0.50F);

        assertTrue(calibration.isLeftStickRangeComplete());
        assertTrue(calibration.isRightStickRangeComplete());
        assertTrue(calibration.isLeftTriggerRangeComplete());
        assertFalse(calibration.isRightTriggerRangeComplete());
        assertFalse(calibration.isRangeComplete());

        calibration.sampleRange(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
        assertTrue(calibration.isRangeComplete());
    }

    @Test
    public void recommendationsAreClampedAgainstExtremeDrift() {
        ControllerCalibration calibration = new ControllerCalibration();
        calibration.sampleRest(0.90F, 0.0F, 0.0F, 0.95F, 0.90F, 1.0F);

        assertEquals(0.40F, calibration.suggestMovementDeadZone(), EPSILON);
        assertEquals(0.40F, calibration.suggestCameraDeadZone(), EPSILON);
        assertEquals(0.80F, calibration.suggestTriggerThreshold(), EPSILON);
    }
}
