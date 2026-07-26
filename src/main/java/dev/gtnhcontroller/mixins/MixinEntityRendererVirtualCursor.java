package dev.gtnhcontroller.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import dev.gtnhcontroller.client.gui.GuiController;

/**
 * Supplies the virtual controller cursor to both GuiScreen and Forge's surrounding DrawScreenEvents.
 */
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRendererVirtualCursor {

    @ModifyArgs(
        method = "updateCameraAndRender",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;drawScreen(IIF)V"))
    private void gtnhcontroller$substituteDrawScreenCursor(Args args) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen == null) {
            return;
        }

        int vanillaMouseX = args.get(0);
        int vanillaMouseY = args.get(1);
        args.set(0, GuiController.resolveMouseX(screen, vanillaMouseX));
        args.set(1, GuiController.resolveMouseY(screen, vanillaMouseY));
    }

    @ModifyArgs(
        method = "updateCameraAndRender",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/client/event/GuiScreenEvent$DrawScreenEvent$Pre;"
                + "<init>(Lnet/minecraft/client/gui/GuiScreen;IIF)V",
            remap = false))
    private void gtnhcontroller$substitutePreEventCursor(Args args) {
        substituteForgeEventCursor(args);
    }

    @ModifyArgs(
        method = "updateCameraAndRender",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/client/event/GuiScreenEvent$DrawScreenEvent$Post;"
                + "<init>(Lnet/minecraft/client/gui/GuiScreen;IIF)V",
            remap = false))
    private void gtnhcontroller$substitutePostEventCursor(Args args) {
        substituteForgeEventCursor(args);
    }

    private static void substituteForgeEventCursor(Args args) {
        GuiScreen screen = args.get(0);
        int vanillaMouseX = args.get(1);
        int vanillaMouseY = args.get(2);
        args.set(1, GuiController.resolveMouseX(screen, vanillaMouseX));
        args.set(2, GuiController.resolveMouseY(screen, vanillaMouseY));
    }
}
