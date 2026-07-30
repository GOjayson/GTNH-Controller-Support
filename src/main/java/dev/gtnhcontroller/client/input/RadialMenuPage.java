package dev.gtnhcontroller.client.input;

public enum RadialMenuPage {

    BASE("Base", "entries"),
    LEFT_SHOULDER("Hold LB", "leftShoulderEntries"),
    RIGHT_SHOULDER("Hold RB", "rightShoulderEntries");

    public final String displayName;
    public final String configKey;

    RadialMenuPage(String displayName, String configKey) {
        this.displayName = displayName;
        this.configKey = configKey;
    }

    public static RadialMenuPage select(boolean leftShoulderDown, boolean rightShoulderDown) {
        if (leftShoulderDown == rightShoulderDown) {
            return BASE;
        }
        return leftShoulderDown ? LEFT_SHOULDER : RIGHT_SHOULDER;
    }
}
