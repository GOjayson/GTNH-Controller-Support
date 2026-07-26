package dev.gtnhcontroller.client.input;

public final class InputMath {

    private InputMath() {}

    public static float normalizeSignedAxis(short rawValue) {
        return rawValue < 0 ? rawValue / 32768.0F : rawValue / 32767.0F;
    }

    public static float normalizeTrigger(short rawValue) {
        return clamp(rawValue / 32767.0F, 0.0F, 1.0F);
    }

    public static StickVector applyRadialDeadZone(float x, float y, float deadZone, float exponent) {
        float magnitude = (float) Math.sqrt(x * x + y * y);
        if (magnitude <= deadZone) {
            return StickVector.ZERO;
        }

        float clampedMagnitude = Math.min(magnitude, 1.0F);
        float normalizedMagnitude = (clampedMagnitude - deadZone) / (1.0F - deadZone);
        float curvedMagnitude = (float) Math.pow(normalizedMagnitude, exponent);
        float scale = curvedMagnitude / magnitude;
        return new StickVector(x * scale, y * scale);
    }

    public static StickVector applyMovementResponse(StickVector input, float sensitivity) {
        if (input == StickVector.ZERO) {
            return StickVector.ZERO;
        }

        float magnitude = (float) Math.sqrt(input.x * input.x + input.y * input.y);
        if (magnitude <= 0.0F) {
            return StickVector.ZERO;
        }

        float clampedMagnitude = Math.min(magnitude, 1.0F);
        float safeSensitivity = Math.max(sensitivity, 0.01F);
        float responseMagnitude = (float) Math.pow(clampedMagnitude, 1.0F / safeSensitivity);
        float scale = responseMagnitude / magnitude;
        return new StickVector(input.x * scale, input.y * scale);
    }

    public static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(value, maximum));
    }

    public static float mergeAxisByMagnitude(float existingValue, float controllerValue) {
        return Math.abs(controllerValue) > Math.abs(existingValue) ? controllerValue : existingValue;
    }
}
