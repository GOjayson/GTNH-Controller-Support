package dev.gtnhcontroller.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.client.input.ControllerProfile;
import dev.gtnhcontroller.client.input.ModKeyBindingController;
import dev.gtnhcontroller.client.input.SdlGamepadManager;

public final class GuiControllerDeviceScreen extends GuiScreen implements ControllerConfigurationScreen {

    private static final int SELECT_CONTROLLER = 1;
    private static final int CALIBRATION = 2;
    private static final int TEST_INPUTS = 3;
    private static final int RUMBLE = 4;
    private static final int PROFILES = 5;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;
    private final SdlGamepadManager gamepadManager;
    private final ControllerProfile controllerProfile;
    private final ModKeyBindingController modKeyBindingController;

    public GuiControllerDeviceScreen(GuiScreen parentScreen, SdlGamepadManager gamepadManager,
        ControllerProfile controllerProfile, ModKeyBindingController modKeyBindingController) {
        this.parentScreen = parentScreen;
        this.gamepadManager = gamepadManager;
        this.controllerProfile = controllerProfile;
        this.modKeyBindingController = modKeyBindingController;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int centerX = width / 2;
        buttonList.add(new GuiButton(SELECT_CONTROLLER, centerX - 100, 54, 200, 20, "Select Controller"));
        buttonList.add(new GuiButton(CALIBRATION, centerX - 100, 78, 200, 20, "Calibration Wizard"));
        buttonList.add(new GuiButton(TEST_INPUTS, centerX - 100, 102, 200, 20, "Test Controller Inputs"));
        buttonList.add(new GuiButton(RUMBLE, centerX - 100, 126, 200, 20, "Rumble Feedback"));
        buttonList.add(new GuiButton(PROFILES, centerX - 100, 150, 200, 20, "Profile Import & Export"));
        buttonList.add(new GuiButton(DONE, centerX - 100, height - 28, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case SELECT_CONTROLLER:
                mc.displayGuiScreen(new GuiControllerSelectionScreen(this, gamepadManager));
                break;
            case CALIBRATION:
                mc.displayGuiScreen(new GuiControllerCalibrationScreen(this, gamepadManager));
                break;
            case TEST_INPUTS:
                mc.displayGuiScreen(new GuiControllerTestScreen(this, gamepadManager, controllerProfile));
                break;
            case RUMBLE:
                mc.displayGuiScreen(new GuiControllerRumbleScreen(this, gamepadManager));
                break;
            case PROFILES:
                mc.displayGuiScreen(new GuiControllerProfileScreen(this, controllerProfile, modKeyBindingController));
                break;
            case DONE:
                mc.displayGuiScreen(parentScreen);
                break;
            default:
                break;
        }
    }

    @Override
    protected void keyTyped(char typedCharacter, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parentScreen);
            return;
        }
        super.keyTyped(typedCharacter, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Controller Setup & Test", width / 2, 16, 0xFFFFFF);
        drawCenteredString(fontRendererObj, gamepadManager.getStatusLine(), width / 2, 32, 0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
