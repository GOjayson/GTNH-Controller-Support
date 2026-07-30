package dev.gtnhcontroller.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;

public final class GuiControllerDeadZoneScreen extends GuiScreen implements ControllerConfigurationScreen {

    private static final int MOVE_DOWN = 1;
    private static final int MOVE_UP = 2;
    private static final int CAMERA_DOWN = 3;
    private static final int CAMERA_UP = 4;
    private static final int CURSOR_DOWN = 5;
    private static final int CURSOR_UP = 6;
    private static final int TRIGGER_DOWN = 7;
    private static final int TRIGGER_UP = 8;
    private static final int RESET_DEFAULTS = 100;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;

    public GuiControllerDeadZoneScreen(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        addRow(MOVE_DOWN, MOVE_UP, 58, Config.moveDeadZone);
        addRow(CAMERA_DOWN, CAMERA_UP, 84, Config.lookDeadZone);
        addRow(CURSOR_DOWN, CURSOR_UP, 110, Config.cursorDeadZone);
        addRow(TRIGGER_DOWN, TRIGGER_UP, 136, Config.triggerThreshold);
        buttonList.add(new GuiButton(RESET_DEFAULTS, width / 2 - 100, 172, 200, 20, "Reset Deadzone Defaults"));
        buttonList.add(new GuiButton(DONE, width / 2 - 100, height - 28, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case MOVE_DOWN:
                Config.moveDeadZone = DeadZoneValue.adjust(Config.moveDeadZone, -1);
                break;
            case MOVE_UP:
                Config.moveDeadZone = DeadZoneValue.adjust(Config.moveDeadZone, 1);
                break;
            case CAMERA_DOWN:
                Config.lookDeadZone = DeadZoneValue.adjust(Config.lookDeadZone, -1);
                break;
            case CAMERA_UP:
                Config.lookDeadZone = DeadZoneValue.adjust(Config.lookDeadZone, 1);
                break;
            case CURSOR_DOWN:
                Config.cursorDeadZone = DeadZoneValue.adjust(Config.cursorDeadZone, -1);
                break;
            case CURSOR_UP:
                Config.cursorDeadZone = DeadZoneValue.adjust(Config.cursorDeadZone, 1);
                break;
            case TRIGGER_DOWN:
                Config.triggerThreshold = DeadZoneValue.adjustTrigger(Config.triggerThreshold, -1);
                break;
            case TRIGGER_UP:
                Config.triggerThreshold = DeadZoneValue.adjustTrigger(Config.triggerThreshold, 1);
                break;
            case RESET_DEFAULTS:
                Config.moveDeadZone = 0.18F;
                Config.lookDeadZone = 0.15F;
                Config.cursorDeadZone = 0.15F;
                Config.triggerThreshold = 0.50F;
                break;
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
        drawCenteredString(fontRendererObj, "Controller Deadzones", width / 2, 16, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Changes are saved and applied immediately", width / 2, 32, 0xA0A0A0);
        drawString(fontRendererObj, "Movement Stick", width / 2 - 150, 64, 0xFFFFFF);
        drawString(fontRendererObj, "Camera Stick", width / 2 - 150, 90, 0xFFFFFF);
        drawString(fontRendererObj, "GUI Cursor Stick", width / 2 - 150, 116, 0xFFFFFF);
        drawString(fontRendererObj, "Triggers", width / 2 - 150, 142, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void addRow(int downId, int upId, int y, float value) {
        GuiButton valueButton = new GuiButton(0, width / 2 + 35, y, 85, 20, DeadZoneValue.format(value));
        valueButton.enabled = false;
        buttonList.add(new GuiButton(downId, width / 2 + 5, y, 25, 20, "-"));
        buttonList.add(valueButton);
        buttonList.add(new GuiButton(upId, width / 2 + 125, y, 25, 20, "+"));
    }
}
