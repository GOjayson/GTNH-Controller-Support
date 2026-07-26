package dev.gtnhcontroller.mixins;

import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.gtnhcontroller.client.gui.GuiScreenControllerKeyDispatcher;

/**
 * Calls the real virtual {@link GuiScreen#keyTyped} implementation instead of an accessor's direct target.
 */
@Mixin(GuiScreen.class)
public abstract class MixinGuiScreenControllerKeyDispatch implements GuiScreenControllerKeyDispatcher {

    @Shadow
    protected abstract void keyTyped(char typedCharacter, int keyCode);

    @Override
    public void gtnhcontroller$dispatchKeyTyped(char typedCharacter, int keyCode) {
        keyTyped(typedCharacter, keyCode);
    }
}
