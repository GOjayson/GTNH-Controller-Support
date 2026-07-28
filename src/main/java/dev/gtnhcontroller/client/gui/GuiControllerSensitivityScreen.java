package dev.gtnhcontroller.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;

public final class GuiControllerSensitivityScreen extends GuiScreen implements ControllerConfigurationScreen {

    private static final int MOVE_DOWN = 1;
    private static final int MOVE_VALUE = 2;
    private static final int MOVE_UP = 3;
    private static final int CAMERA_DOWN = 4;
    private static final int CAMERA_VALUE = 5;
    private static final int CAMERA_UP = 6;
    private static final int CURSOR_DOWN = 7;
    private static final int CURSOR_VALUE = 8;
    private static final int CURSOR_UP = 9;
    private static final int RESET_DEFAULTS = 100;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;

    private GuiButton moveValueButton;
    private GuiButton cameraValueButton;
    private GuiButton cursorValueButton;

    public GuiControllerSensitivityScreen(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int centerX = width / 2;

        addSensitivityRow(MOVE_DOWN, MOVE_VALUE, MOVE_UP, 58, Config.moveSensitivity);
        addSensitivityRow(CAMERA_DOWN, CAMERA_VALUE, CAMERA_UP, 84, Config.lookSensitivity);
        addSensitivityRow(CURSOR_DOWN, CURSOR_VALUE, CURSOR_UP, 110, Config.cursorSensitivity);

        buttonList.add(new GuiButton(RESET_DEFAULTS, centerX - 100, 146, 200, 20, "Reset Sensitivity Defaults"));
        buttonList.add(new GuiButton(DONE, centerX - 100, height - 28, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case MOVE_DOWN:
                Config.moveSensitivity = SensitivityValue.adjust(Config.moveSensitivity, -1);
                break;
            case MOVE_UP:
                Config.moveSensitivity = SensitivityValue.adjust(Config.moveSensitivity, 1);
                break;
            case CAMERA_DOWN:
                Config.lookSensitivity = SensitivityValue.adjust(Config.lookSensitivity, -1);
                break;
            case CAMERA_UP:
                Config.lookSensitivity = SensitivityValue.adjust(Config.lookSensitivity, 1);
                break;
            case CURSOR_DOWN:
                Config.cursorSensitivity = SensitivityValue.adjust(Config.cursorSensitivity, -1);
                break;
            case CURSOR_UP:
                Config.cursorSensitivity = SensitivityValue.adjust(Config.cursorSensitivity, 1);
                break;
            case RESET_DEFAULTS:
                Config.moveSensitivity = SensitivityValue.DEFAULT;
                Config.lookSensitivity = SensitivityValue.DEFAULT;
                Config.cursorSensitivity = SensitivityValue.DEFAULT;
                break;
            case DONE:
                mc.displayGuiScreen(parentScreen);
                return;
            default:
                return;
        }

        Config.saveControllerSettings();
        refreshValueLabels();
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
        drawCenteredString(fontRendererObj, "Controller Sensitivity", width / 2, 16, 0xFFFFFF);
        drawCenteredString(
            fontRendererObj,
            "Changes are saved and applied immediately",
            width / 2,
            32,
            0xA0A0A0);

        drawString(fontRendererObj, "Movement Response", width / 2 - 150, 64, 0xFFFFFF);
        drawString(fontRendererObj, "Camera", width / 2 - 150, 90, 0xFFFFFF);
        drawString(fontRendererObj, "GUI Cursor", width / 2 - 150, 116, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void addSensitivityRow(int downId, int valueId, int upId, int buttonY, float value) {
        int centerX = width / 2;
        GuiButton valueButton = new GuiButton(valueId, centerX + 35, buttonY, 85, 20, SensitivityValue.format(value));
        valueButton.enabled = false;

        buttonList.add(new GuiButton(downId, centerX + 5, buttonY, 25, 20, "-"));
        buttonList.add(valueButton);
        buttonList.add(new GuiButton(upId, centerX + 125, buttonY, 25, 20, "+"));

        if (valueId == MOVE_VALUE) {
            moveValueButton = valueButton;
        } else if (valueId == CAMERA_VALUE) {
            cameraValueButton = valueButton;
        } else if (valueId == CURSOR_VALUE) {
            cursorValueButton = valueButton;
        }
    }

    private void refreshValueLabels() {
        moveValueButton.displayString = SensitivityValue.format(Config.moveSensitivity);
        cameraValueButton.displayString = SensitivityValue.format(Config.lookSensitivity);
        cursorValueButton.displayString = SensitivityValue.format(Config.cursorSensitivity);
    }
}
