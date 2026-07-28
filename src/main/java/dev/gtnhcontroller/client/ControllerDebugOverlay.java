package dev.gtnhcontroller.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.SdlGamepadManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public final class ControllerDebugOverlay {

    private final SdlGamepadManager gamepadManager;

    public ControllerDebugOverlay(SdlGamepadManager gamepadManager) {
        this.gamepadManager = gamepadManager;
    }

    @SubscribeEvent
    public void onOverlayText(RenderGameOverlayEvent.Text event) {
        if (!Config.showDebugOverlay || !Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            return;
        }

        event.left.add("[GTNH Controller] " + gamepadManager.getStatusLine());
        if (gamepadManager.isConnected()) {
            event.left.add("Axes: " + gamepadManager.getAxisLine());
            event.left.add("Buttons: " + gamepadManager.getButtonsLine());
        }
    }
}
