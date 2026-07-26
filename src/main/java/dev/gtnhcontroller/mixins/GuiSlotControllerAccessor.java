package dev.gtnhcontroller.mixins;

import net.minecraft.client.gui.GuiSlot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Lets controller clicks select entries in vanilla lists that only process native LWJGL mouse events.
 */
@Mixin(GuiSlot.class)
public interface GuiSlotControllerAccessor {

    @Invoker("elementClicked")
    void gtnhcontroller$elementClicked(int index, boolean doubleClick, int mouseX, int mouseY);
}
