package dev.gtnhcontroller.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ControllerProfile;
import dev.gtnhcontroller.client.input.ModKeyBindingController;
import dev.gtnhcontroller.client.input.SdlGamepadManager;

public final class GuiControllerSettingsScreen extends GuiScreen implements ControllerConfigurationScreen {

    private static final int TOGGLE_GAMEPLAY = 1;
    private static final int TOGGLE_GUI = 2;
    private static final int GAMEPLAY_BINDINGS = 3;
    private static final int GUI_BINDINGS = 4;
    private static final int TOGGLE_AUTO_JUMP = 5;
    private static final int SENSITIVITY = 7;
    private static final int MOD_BINDINGS = 8;
    private static final int NAVIGATION = 9;
    private static final int RADIAL_MENU = 10;
    private static final int ACTIVATION_MODES = 11;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;
    private final SdlGamepadManager gamepadManager;
    private final ControllerProfile controllerProfile;
    private final ModKeyBindingController modKeyBindingController;

    public GuiControllerSettingsScreen(GuiScreen parentScreen, SdlGamepadManager gamepadManager,
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
        buttonList.add(
            new GuiButton(
                TOGGLE_GAMEPLAY,
                centerX - 155,
                52,
                150,
                20,
                toggleLabel("Gameplay Controls", Config.enableGameplayControls)));
        buttonList.add(
            new GuiButton(TOGGLE_GUI, centerX + 5, 52, 150, 20, toggleLabel("GUI Controls", Config.enableGuiControls)));
        buttonList.add(
            new GuiButton(TOGGLE_AUTO_JUMP, centerX - 155, 78, 310, 20, toggleLabel("Auto Jump", Config.autoJump)));
        buttonList.add(new GuiButton(GAMEPLAY_BINDINGS, centerX - 155, 104, 230, 20, "Gameplay Bindings..."));
        buttonList.add(new GuiButton(ACTIVATION_MODES, centerX + 80, 104, 75, 20, "Modes..."));
        buttonList.add(new GuiButton(GUI_BINDINGS, centerX - 155, 130, 310, 20, "GUI Bindings..."));
        buttonList.add(new GuiButton(SENSITIVITY, centerX - 155, 156, 150, 20, "Sensitivity..."));
        buttonList.add(new GuiButton(NAVIGATION, centerX + 5, 156, 150, 20, "Navigation..."));
        buttonList.add(new GuiButton(MOD_BINDINGS, centerX - 155, 182, 150, 20, "Minecraft & Mod..."));
        buttonList.add(new GuiButton(RADIAL_MENU, centerX + 5, 182, 150, 20, "Radial Menu..."));
        buttonList.add(new GuiButton(DONE, centerX - 100, height - 28, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case TOGGLE_GAMEPLAY:
                Config.enableGameplayControls = !Config.enableGameplayControls;
                Config.saveControllerSettings();
                button.displayString = toggleLabel("Gameplay Controls", Config.enableGameplayControls);
                break;
            case TOGGLE_GUI:
                Config.enableGuiControls = !Config.enableGuiControls;
                Config.saveControllerSettings();
                button.displayString = toggleLabel("GUI Controls", Config.enableGuiControls);
                break;
            case TOGGLE_AUTO_JUMP:
                Config.autoJump = !Config.autoJump;
                Config.saveControllerSettings();
                button.displayString = toggleLabel("Auto Jump", Config.autoJump);
                break;
            case GAMEPLAY_BINDINGS:
                mc.displayGuiScreen(new GuiControllerBindingScreen(this, gamepadManager, controllerProfile, false));
                break;
            case ACTIVATION_MODES:
                mc.displayGuiScreen(new GuiControllerActivationModeScreen(this));
                break;
            case GUI_BINDINGS:
                mc.displayGuiScreen(new GuiControllerBindingScreen(this, gamepadManager, controllerProfile, true));
                break;
            case SENSITIVITY:
                mc.displayGuiScreen(new GuiControllerSensitivityScreen(this));
                break;
            case NAVIGATION:
                mc.displayGuiScreen(new GuiControllerNavigationSettingsScreen(this));
                break;
            case MOD_BINDINGS:
                mc.displayGuiScreen(new GuiModKeyBindingScreen(this, gamepadManager, modKeyBindingController));
                break;
            case RADIAL_MENU:
                mc.displayGuiScreen(new GuiRadialMenuSettingsScreen(this, modKeyBindingController));
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
        drawCenteredString(fontRendererObj, "Controller Settings", width / 2, 15, 0xFFFFFF);
        drawCenteredString(fontRendererObj, gamepadManager.getStatusLine(), width / 2, 31, 0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static String toggleLabel(String label, boolean enabled) {
        return label + ": " + (enabled ? "\u00A7aON" : "\u00A7cOFF");
    }
}
