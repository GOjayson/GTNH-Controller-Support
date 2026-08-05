package dev.gtnhcontroller.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.gui.GuiRadialMenuScreen;
import dev.gtnhcontroller.client.input.ControllerProfile;
import dev.gtnhcontroller.client.input.GameplayController;
import dev.gtnhcontroller.client.input.SdlGamepadManager;

public final class ControllerActiveModeOverlay {

    private final SdlGamepadManager gamepadManager;
    private final ControllerProfile controllerProfile;
    private final GameplayController gameplayController;

    public ControllerActiveModeOverlay(SdlGamepadManager gamepadManager, ControllerProfile controllerProfile,
        GameplayController gameplayController) {
        this.gamepadManager = gamepadManager;
        this.controllerProfile = controllerProfile;
        this.gameplayController = gameplayController;
    }

    @SubscribeEvent
    public void onOverlayText(RenderGameOverlayEvent.Text event) {
        if (!Config.showActiveModeHud || !gamepadManager.isConnected()) {
            return;
        }

        List<String> activeModes = new ArrayList<String>();
        if (gameplayController.isSneakActive()) activeModes.add("Sneak");
        if (gameplayController.isSprintActive()) activeModes.add("Sprint");
        if (gameplayController.isSwimActive()) activeModes.add("Swim");
        if (controllerProfile.isModifierActive()) activeModes.add("Modifier");
        if (Minecraft.getMinecraft().currentScreen instanceof GuiRadialMenuScreen) {
            activeModes.add("Radial: " + ((GuiRadialMenuScreen) Minecraft.getMinecraft().currentScreen).getPageName());
        }
        if (!activeModes.isEmpty()) {
            event.right.add("Controller: " + join(activeModes));
        }
    }

    private static String join(List<String> values) {
        StringBuilder joined = new StringBuilder();
        for (String value : values) {
            if (joined.length() > 0) joined.append(" | ");
            joined.append(value);
        }
        return joined.toString();
    }
}
