package dev.gtnhcontroller.mixins;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes vanilla's protected GUI input callbacks to the controller cursor without changing their behavior.
 */
@Mixin(GuiScreen.class)
public interface GuiScreenControllerAccessor {

    @Accessor("buttonList")
    List<GuiButton> gtnhcontroller$getButtonList();

    @Invoker("mouseClicked")
    void gtnhcontroller$mouseClicked(int mouseX, int mouseY, int mouseButton);

    @Invoker("mouseMovedOrUp")
    void gtnhcontroller$mouseMovedOrUp(int mouseX, int mouseY, int mouseButton);

    @Invoker("mouseClickMove")
    void gtnhcontroller$mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceClick);
}
