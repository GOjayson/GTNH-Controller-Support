package dev.gtnhcontroller.mixins;

import net.minecraft.client.settings.KeyBinding;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Targets one exact key-binding object. The vanilla static key-code lookup cannot do that safely when GTNH mods use
 * the same keyboard key or register duplicate bindings.
 */
@Mixin(KeyBinding.class)
public interface KeyBindingControllerAccessor {

    @Accessor("pressed")
    boolean gtnhcontroller$isPressed();

    @Accessor("pressed")
    void gtnhcontroller$setPressed(boolean pressed);

    @Accessor("pressTime")
    int gtnhcontroller$getPressTime();

    @Accessor("pressTime")
    void gtnhcontroller$setPressTime(int pressTime);
}
