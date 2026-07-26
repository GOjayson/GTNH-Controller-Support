package dev.gtnhcontroller.mixins;

import net.minecraft.client.gui.inventory.GuiContainerCreative;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiContainerCreative.class)
public interface GuiContainerCreativeControllerAccessor {

    @Accessor("currentScroll")
    float gtnhcontroller$getCurrentScroll();

    @Accessor("currentScroll")
    void gtnhcontroller$setCurrentScroll(float currentScroll);

    @Invoker("needsScrollBars")
    boolean gtnhcontroller$needsScrollBars();
}
