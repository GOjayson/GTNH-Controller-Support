package dev.gtnhcontroller.mixins;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.gtnhcontroller.client.gui.ControllerMouseClickContext;

/** Preserves the button passed by a controller through BetterQuesting's NEI item shortcut. */
@Pseudo
@Mixin(targets = "codechicken.nei.api.ShortcutInputHandler", remap = false)
public abstract class MixinNeiShortcutInputHandlerControllerMouse {

    @Redirect(
        method = "handleMouseClick",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I"),
        remap = false,
        require = 0)
    private static int gtnhcontroller$useControllerMouseButton() {
        int mouseButton = ControllerMouseClickContext.getMouseButton();
        return mouseButton >= 0 ? mouseButton : Mouse.getEventButton();
    }
}
