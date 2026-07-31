package dev.gtnhcontroller.client.input;

/**
 * Collects calibration samples without depending on Minecraft or SDL. Rest samples measure drift; range samples make
 * sure the player exercised each analog control before accepting the recommendation.
 */
public final class ControllerCalibration {

    private static final float STICK_MARGIN = 0.04F;
    private static final float TRIGGER_MARGIN = 0.08F;
    private static final float MIN_STICK_DEAD_ZONE = 0.05F;
    private static final float MAX_STICK_DEAD_ZONE = 0.40F;
    private static final float MIN_TRIGGER_THRESHOLD = 0.10F;
    private static final float MAX_TRIGGER_THRESHOLD = 0.80F;
    private static final float REQUIRED_RANGE = 0.75F;
    private static final float DRIFT_WARNING = 0.04F;

    private float leftRest;
    private float rightRest;
    private float leftTriggerRest;
    private float rightTriggerRest;
    private float leftRange;
    private float rightRange;
    private float leftTriggerRange;
    private float rightTriggerRange;
    private int restSamples;
    private int rangeSamples;

    public void sampleRest(float leftX, float leftY, float rightX, float rightY, float leftTrigger,
        float rightTrigger) {
        leftRest = Math.max(leftRest, magnitude(leftX, leftY));
        rightRest = Math.max(rightRest, magnitude(rightX, rightY));
        leftTriggerRest = Math.max(leftTriggerRest, clamp01(leftTrigger));
        rightTriggerRest = Math.max(rightTriggerRest, clamp01(rightTrigger));
        restSamples++;
    }

    public void sampleRange(float leftX, float leftY, float rightX, float rightY, float leftTrigger,
        float rightTrigger) {
        leftRange = Math.max(leftRange, magnitude(leftX, leftY));
        rightRange = Math.max(rightRange, magnitude(rightX, rightY));
        leftTriggerRange = Math.max(leftTriggerRange, clamp01(leftTrigger));
        rightTriggerRange = Math.max(rightTriggerRange, clamp01(rightTrigger));
        rangeSamples++;
    }

    public float suggestMovementDeadZone() {
        return suggestedStickDeadZone(leftRest);
    }

    public float suggestCameraDeadZone() {
        return suggestedStickDeadZone(rightRest);
    }

    public float suggestCursorDeadZone(boolean rightStickCursor) {
        return rightStickCursor ? suggestCameraDeadZone() : suggestMovementDeadZone();
    }

    public float suggestTriggerThreshold() {
        return roundUpToPercent(
            InputMath.clamp(
                Math.max(leftTriggerRest, rightTriggerRest) + TRIGGER_MARGIN,
                MIN_TRIGGER_THRESHOLD,
                MAX_TRIGGER_THRESHOLD));
    }

    public boolean hasLeftStickDrift() {
        return leftRest >= DRIFT_WARNING;
    }

    public boolean hasRightStickDrift() {
        return rightRest >= DRIFT_WARNING;
    }

    public boolean hasTriggerDrift() {
        return Math.max(leftTriggerRest, rightTriggerRest) >= DRIFT_WARNING;
    }

    public boolean hasEnoughRestSamples() {
        return restSamples > 0;
    }

    public boolean hasEnoughRangeSamples() {
        return rangeSamples > 0;
    }

    public boolean isLeftStickRangeComplete() {
        return leftRange >= REQUIRED_RANGE;
    }

    public boolean isRightStickRangeComplete() {
        return rightRange >= REQUIRED_RANGE;
    }

    public boolean isLeftTriggerRangeComplete() {
        return leftTriggerRange >= REQUIRED_RANGE;
    }

    public boolean isRightTriggerRangeComplete() {
        return rightTriggerRange >= REQUIRED_RANGE;
    }

    public boolean isRangeComplete() {
        return isLeftStickRangeComplete() && isRightStickRangeComplete()
            && isLeftTriggerRangeComplete()
            && isRightTriggerRangeComplete();
    }

    public float getLeftRest() {
        return leftRest;
    }

    public float getRightRest() {
        return rightRest;
    }

    public float getLeftTriggerRest() {
        return leftTriggerRest;
    }

    public float getRightTriggerRest() {
        return rightTriggerRest;
    }

    public float getLeftRange() {
        return leftRange;
    }

    public float getRightRange() {
        return rightRange;
    }

    public float getLeftTriggerRange() {
        return leftTriggerRange;
    }

    public float getRightTriggerRange() {
        return rightTriggerRange;
    }

    private static float suggestedStickDeadZone(float restValue) {
        return roundUpToPercent(InputMath.clamp(restValue + STICK_MARGIN, MIN_STICK_DEAD_ZONE, MAX_STICK_DEAD_ZONE));
    }

    private static float magnitude(float x, float y) {
        return Math.min((float) Math.sqrt(x * x + y * y), 1.0F);
    }

    private static float clamp01(float value) {
        return InputMath.clamp(value, 0.0F, 1.0F);
    }

    private static float roundUpToPercent(float value) {
        return (float) Math.ceil(value * 100.0F) / 100.0F;
    }
}
