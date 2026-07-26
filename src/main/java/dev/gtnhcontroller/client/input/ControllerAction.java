package dev.gtnhcontroller.client.input;

public enum ControllerAction {

    JUMP("Jump", "jump", false, "BUTTON:SOUTH"),
    SNEAK("Sneak", "sneak", false, "BUTTON:EAST"),
    SPRINT("Sprint", "sprint", false, "BUTTON:LEFT_STICK"),
    ATTACK("Attack / Mine", "attack", false, "TRIGGER:RIGHT_TRIGGER"),
    USE("Use / Place", "use", false, "TRIGGER:LEFT_TRIGGER"),
    HOTBAR_PREVIOUS("Previous Hotbar Slot", "hotbarPrevious", false, "BUTTON:LEFT_SHOULDER|BUTTON:DPAD_LEFT"),
    HOTBAR_NEXT("Next Hotbar Slot", "hotbarNext", false, "BUTTON:RIGHT_SHOULDER|BUTTON:DPAD_RIGHT"),
    OPEN_INVENTORY("Open Inventory", "openInventory", false, "BUTTON:NORTH"),
    PAUSE("Pause Menu", "pause", false, "BUTTON:START"),
    RADIAL_MENU("Radial Action Menu", "radialMenu", false, "BUTTON:BACK"),
    GUI_CONFIRM("GUI Confirm / Left-click", "guiConfirm", true, "BUTTON:SOUTH"),
    GUI_ALTERNATE("GUI Alternate / Right-click", "guiAlternate", true, "BUTTON:WEST"),
    GUI_BACK("GUI Back", "guiBack", true, "BUTTON:EAST"),
    GUI_KEYBOARD("On-screen Keyboard", "guiKeyboard", true, "BUTTON:NORTH"),
    GUI_SCROLL_UP("GUI Scroll Up", "guiScrollUp", true, "BUTTON:LEFT_SHOULDER"),
    GUI_SCROLL_DOWN("GUI Scroll Down", "guiScrollDown", true, "BUTTON:RIGHT_SHOULDER"),
    GUI_NAV_UP("GUI Navigate Up", "guiNavigateUp", true, "BUTTON:DPAD_UP"),
    GUI_NAV_DOWN("GUI Navigate Down", "guiNavigateDown", true, "BUTTON:DPAD_DOWN"),
    GUI_NAV_LEFT("GUI Navigate Left", "guiNavigateLeft", true, "BUTTON:DPAD_LEFT"),
    GUI_NAV_RIGHT("GUI Navigate Right", "guiNavigateRight", true, "BUTTON:DPAD_RIGHT"),
    GUI_PRECISION("GUI Precision Cursor", "guiPrecision", true, "BUTTON:RIGHT_STICK");

    public final String displayName;
    public final String configKey;
    public final boolean guiAction;
    public final String defaultBinding;

    ControllerAction(String displayName, String configKey, boolean guiAction, String defaultBinding) {
        this.displayName = displayName;
        this.configKey = configKey;
        this.guiAction = guiAction;
        this.defaultBinding = defaultBinding;
    }
}
