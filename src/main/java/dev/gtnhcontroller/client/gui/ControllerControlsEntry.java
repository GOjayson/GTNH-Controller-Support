package dev.gtnhcontroller.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraftforge.client.event.GuiScreenEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.gtnhcontroller.client.input.ControllerProfile;
import dev.gtnhcontroller.client.input.ModKeyBindingController;
import dev.gtnhcontroller.client.input.SdlGamepadManager;

/**
 * Adds a controller-settings entry without squeezing the existing bottom-row buttons.
 */
public final class ControllerControlsEntry {

    private static final int CONTROLLER_SETTINGS_BUTTON = 7315;

    private final SdlGamepadManager gamepadManager;
    private final ControllerProfile controllerProfile;
    private final ModKeyBindingController modKeyBindingController;

    public ControllerControlsEntry(SdlGamepadManager gamepadManager, ControllerProfile controllerProfile,
        ModKeyBindingController modKeyBindingController) {
        this.gamepadManager = gamepadManager;
        this.controllerProfile = controllerProfile;
        this.modKeyBindingController = modKeyBindingController;
    }

    @SubscribeEvent
    public void onControlsInitialized(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.gui instanceof GuiControls)) {
            return;
        }

        int topOptionY = Integer.MAX_VALUE;
        for (Object buttonObject : event.buttonList) {
            if (!(buttonObject instanceof GuiButton)) {
                continue;
            }

            GuiButton button = (GuiButton) buttonObject;
            if (button.id == CONTROLLER_SETTINGS_BUTTON) {
                return;
            }
            if (isUpperOptionCandidate(button, event.gui.height)) {
                topOptionY = Math.min(topOptionY, button.yPosition);
            }
        }

        int leftOptionEdge = Integer.MAX_VALUE;
        int rightOptionEdge = Integer.MIN_VALUE;
        for (Object buttonObject : event.buttonList) {
            if (!(buttonObject instanceof GuiButton)) {
                continue;
            }

            GuiButton button = (GuiButton) buttonObject;
            if (isUpperOptionCandidate(button, event.gui.height) && button.yPosition == topOptionY) {
                leftOptionEdge = Math.min(leftOptionEdge, button.xPosition);
                rightOptionEdge = Math.max(rightOptionEdge, button.xPosition + button.width);
            }
        }

        ControllerButtonPlacement.Position position = ControllerButtonPlacement
            .choose(event.gui.width, leftOptionEdge, rightOptionEdge, topOptionY, event.gui.height - 53);
        event.buttonList.add(
            new GuiButton(CONTROLLER_SETTINGS_BUTTON, position.x, position.y, position.width, 20, "Controller..."));
    }

    @SubscribeEvent
    public void onControlsButtonPressed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (!(event.gui instanceof GuiControls) || event.button.id != CONTROLLER_SETTINGS_BUTTON) {
            return;
        }

        event.setCanceled(true);
        Minecraft.getMinecraft()
            .displayGuiScreen(
                new GuiControllerSettingsScreen(event.gui, gamepadManager, controllerProfile, modKeyBindingController));
    }

    private static boolean isUpperOptionCandidate(GuiButton button, int screenHeight) {
        return button.id != 200 && button.id != 201 && button.yPosition < screenHeight - 70;
    }

}
