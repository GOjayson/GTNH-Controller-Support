package dev.gtnhcontroller.client.input;

public enum ControllerBindingLayer {

    PRIMARY("Primary"),
    MODIFIER("Modifier");

    public final String displayName;

    ControllerBindingLayer(String displayName) {
        this.displayName = displayName;
    }

    public static ControllerBindingLayer select(boolean modifierDown, ControllerAction action) {
        return modifierDown && !action.guiAction && action != ControllerAction.MODIFIER_LAYER ? MODIFIER : PRIMARY;
    }
}
