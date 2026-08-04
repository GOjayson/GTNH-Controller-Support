package dev.gtnhcontroller.mixins;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiContainer.class)
public interface GuiContainerControllerAccessor {

    @Accessor("guiLeft")
    int gtnhcontroller$getGuiLeft();

    @Accessor("guiTop")
    int gtnhcontroller$getGuiTop();

    @Invoker("handleMouseClick")
    void gtnhcontroller$handleMouseClick(Slot slot, int slotId, int mouseButton, int clickMode);
}
