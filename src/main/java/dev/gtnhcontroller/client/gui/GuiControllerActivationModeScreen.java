package dev.gtnhcontroller.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ActivationMode;

public final class GuiControllerActivationModeScreen extends GuiScreen implements ControllerConfigurationScreen {

    private static final int SWIM = 1;
    private static final int SNEAK = 2;
    private static final int SPRINT = 3;
    private static final int ATTACK = 4;
    private static final int USE = 5;
    private static final int PROMPTS = 6;
    private static final int ACTIVE_HUD = 7;
    private static final int LARGE_CURSOR = 8;
    private static final int CURSOR_TRAIL = 9;
    private static final int RESET_DEFAULTS = 100;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;

    public GuiControllerActivationModeScreen(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int centerX = width / 2;
        buttonList.add(new GuiButton(SWIM, centerX - 155, 40, 310, 20, modeLabel("Swim", Config.swimActivationMode)));
        buttonList
            .add(new GuiButton(SNEAK, centerX - 155, 64, 150, 20, modeLabel("Sneak", Config.sneakActivationMode)));
        buttonList
            .add(new GuiButton(SPRINT, centerX + 5, 64, 150, 20, modeLabel("Sprint", Config.sprintActivationMode)));
        buttonList
            .add(new GuiButton(ATTACK, centerX - 155, 88, 150, 20, modeLabel("Attack", Config.attackActivationMode)));
        buttonList.add(new GuiButton(USE, centerX + 5, 88, 150, 20, modeLabel("Use", Config.useActivationMode)));
        buttonList.add(
            new GuiButton(
                PROMPTS,
                centerX - 155,
                116,
                150,
                20,
                toggleLabel("Button Prompts", Config.showControllerPrompts)));
        buttonList.add(
            new GuiButton(
                ACTIVE_HUD,
                centerX + 5,
                116,
                150,
                20,
                toggleLabel("Active-mode HUD", Config.showActiveModeHud)));
        buttonList.add(
            new GuiButton(LARGE_CURSOR, centerX - 155, 140, 150, 20, toggleLabel("Large Cursor", Config.largeCursor)));
        buttonList.add(
            new GuiButton(CURSOR_TRAIL, centerX + 5, 140, 150, 20, toggleLabel("Cursor Trail", Config.cursorTrail)));
        buttonList.add(new GuiButton(RESET_DEFAULTS, centerX - 155, height - 28, 150, 20, "Reset Defaults"));
        buttonList.add(new GuiButton(DONE, centerX + 5, height - 28, 150, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case SWIM:
                Config.swimActivationMode = Config.swimActivationMode == ActivationMode.HOLD ? ActivationMode.TOGGLE
                    : ActivationMode.HOLD;
                break;
            case SNEAK:
                Config.sneakActivationMode = Config.sneakActivationMode.next();
                break;
            case SPRINT:
                Config.sprintActivationMode = Config.sprintActivationMode.next();
                break;
            case ATTACK:
                Config.attackActivationMode = Config.attackActivationMode.next();
                break;
            case USE:
                Config.useActivationMode = Config.useActivationMode.next();
                break;
            case PROMPTS:
                Config.showControllerPrompts = !Config.showControllerPrompts;
                break;
            case ACTIVE_HUD:
                Config.showActiveModeHud = !Config.showActiveModeHud;
                break;
            case LARGE_CURSOR:
                Config.largeCursor = !Config.largeCursor;
                break;
            case CURSOR_TRAIL:
                Config.cursorTrail = !Config.cursorTrail;
                break;
            case RESET_DEFAULTS:
                Config.resetActivationModes();
                initGui();
                return;
            case DONE:
                mc.displayGuiScreen(parentScreen);
                return;
            default:
                return;
        }

        Config.saveControllerSettings();
        initGui();
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
        drawCenteredString(fontRendererObj, "Accessibility Modes", width / 2, 15, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "HOLD follows the physical input.", width / 2, 168, 0xC0C0C0);
        drawCenteredString(fontRendererObj, "TOGGLE latches. PRESS emits one game tick.", width / 2, 180, 0xC0C0C0);
        drawCenteredString(fontRendererObj, "PRESS is mainly useful for Attack or Use.", width / 2, 192, 0xC0C0C0);
        drawCenteredString(
            fontRendererObj,
            "All latches clear on menus, focus loss, or disconnect.",
            width / 2,
            204,
            0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static String modeLabel(String label, ActivationMode mode) {
        String color = mode == ActivationMode.HOLD ? "\u00A7e" : mode == ActivationMode.TOGGLE ? "\u00A7a" : "\u00A7b";
        return label + ": " + color + mode.name();
    }

    private static String toggleLabel(String label, boolean enabled) {
        return label + ": " + (enabled ? "§aOn" : "§cOff");
    }
}
