package dev.gtnhcontroller.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.client.gui.inventory.GuiContainerCreative$ContainerCreative")
public interface CreativeContainerControllerAccessor {

    @Accessor("itemList")
    List<?> gtnhcontroller$getItemList();

    @Invoker("scrollTo")
    void gtnhcontroller$scrollTo(float scrollPosition);
}
