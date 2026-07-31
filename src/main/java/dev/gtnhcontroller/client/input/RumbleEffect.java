package dev.gtnhcontroller.client.input;

public enum RumbleEffect {

    MINING(1),
    DAMAGE(2),
    EXPLOSION(3),
    TEST(4);

    final int priority;

    RumbleEffect(int priority) {
        this.priority = priority;
    }
}
