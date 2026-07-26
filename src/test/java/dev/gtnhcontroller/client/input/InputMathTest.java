package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class InputMathTest {

    private static final float TOLERANCE = 0.0001F;

    @Test
    public void radialDeadZoneSuppressesSmallMovement() {
        StickVector result = InputMath.applyRadialDeadZone(0.1F, 0.1F, 0.2F, 1.0F);

        assertSame(StickVector.ZERO, result);
    }

    @Test
    public void radialDeadZonePreservesFullDeflection() {
        StickVector result = InputMath.applyRadialDeadZone(1.0F, 0.0F, 0.2F, 1.0F);

        assertEquals(1.0F, result.x, TOLERANCE);
        assertEquals(0.0F, result.y, TOLERANCE);
    }

    @Test
    public void curveRetainsDirectionAndAddsPrecisionNearCenter() {
        StickVector linear = InputMath.applyRadialDeadZone(0.6F, 0.0F, 0.2F, 1.0F);
        StickVector curved = InputMath.applyRadialDeadZone(0.6F, 0.0F, 0.2F, 2.0F);

        assertEquals(0.5F, linear.x, TOLERANCE);
        assertEquals(0.25F, curved.x, TOLERANCE);
        assertEquals(0.0F, curved.y, TOLERANCE);
    }

    @Test
    public void triggerNormalizationNeverReturnsNegativeValues() {
        assertEquals(0.0F, InputMath.normalizeTrigger(Short.MIN_VALUE), TOLERANCE);
        assertEquals(0.0F, InputMath.normalizeTrigger((short) 0), TOLERANCE);
        assertEquals(1.0F, InputMath.normalizeTrigger(Short.MAX_VALUE), TOLERANCE);
    }

    @Test
    public void axisMergePreservesTheStrongerInput() {
        assertEquals(1.0F, InputMath.mergeAxisByMagnitude(1.0F, -0.5F), TOLERANCE);
        assertEquals(-0.8F, InputMath.mergeAxisByMagnitude(0.2F, -0.8F), TOLERANCE);
    }

    @Test
    public void movementResponseChangesPartialDeflection() {
        StickVector precise = InputMath.applyMovementResponse(new StickVector(0.5F, 0.0F), 0.5F);
        StickVector aggressive = InputMath.applyMovementResponse(new StickVector(0.5F, 0.0F), 2.0F);

        assertEquals(0.25F, precise.x, TOLERANCE);
        assertEquals(0.0F, precise.y, TOLERANCE);
        assertEquals((float) Math.sqrt(0.5D), aggressive.x, TOLERANCE);
        assertEquals(0.0F, aggressive.y, TOLERANCE);
    }

    @Test
    public void movementResponsePreservesFullDeflectionAtEverySensitivity() {
        StickVector lowSensitivity = InputMath.applyMovementResponse(new StickVector(1.0F, 0.0F), 0.25F);
        StickVector highSensitivity = InputMath.applyMovementResponse(new StickVector(1.0F, 0.0F), 2.0F);

        assertEquals(1.0F, lowSensitivity.x, TOLERANCE);
        assertEquals(1.0F, highSensitivity.x, TOLERANCE);
    }
}
