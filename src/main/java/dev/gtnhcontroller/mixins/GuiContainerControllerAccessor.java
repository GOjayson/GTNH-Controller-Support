package dev.gtnhcontroller.mixins;

import net.minecraft.client.gui.inventory.GuiContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiContainer.class)
public interface GuiContainerControllerAccessor {

    @Accessor("guiLeft")
    int gtnhcontroller$getGuiLeft();

    @Accessor("guiTop")
    int gtnhcontroller$getGuiTop();
}
