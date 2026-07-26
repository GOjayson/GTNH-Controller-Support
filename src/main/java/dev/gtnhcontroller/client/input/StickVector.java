package dev.gtnhcontroller.client.input;

public final class StickVector {

    public static final StickVector ZERO = new StickVector(0.0F, 0.0F);

    public final float x;
    public final float y;

    public StickVector(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
