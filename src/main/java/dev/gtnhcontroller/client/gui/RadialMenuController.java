package dev.gtnhcontroller.client.gui;

import static dev.gtnhcontroller.client.input.ControllerAction.RADIAL_MENU;

import net.minecraft.client.Minecraft;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ControllerProfile;
import dev.gtnhcontroller.client.input.ModKeyBindingController;
import dev.gtnhcontroller.client.input.SdlGamepadManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public final class RadialMenuController {

    private final SdlGamepadManager gamepadManager;
    private final ControllerProfile controllerProfile;
    private final ModKeyBindingController keyBindingController;

    public RadialMenuController(SdlGamepadManager gamepadManager, ControllerProfile controllerProfile,
        ModKeyBindingController keyBindingController) {
        this.gamepadManager = gamepadManager;
        this.controllerProfile = controllerProfile;
        this.keyBindingController = keyBindingController;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (canOpen(minecraft) && controllerProfile.wasPressed(RADIAL_MENU)) {
            minecraft.displayGuiScreen(
                new GuiRadialMenuScreen(gamepadManager, controllerProfile, keyBindingController));
        }
    }

    private boolean canOpen(Minecraft minecraft) {
        return Config.enableGameplayControls && gamepadManager.isConnected()
            && minecraft.thePlayer != null
            && minecraft.currentScreen == null
            && minecraft.inGameHasFocus;
    }
}
