package dev.gtnhcontroller.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;

public final class GuiControllerAxisScreen extends GuiScreen implements ControllerConfigurationScreen {

    private static final int CAMERA_X = 1;
    private static final int CAMERA_Y = 2;
    private static final int CURSOR_X = 3;
    private static final int CURSOR_Y = 4;
    private static final int CURSOR_STICK = 5;
    private static final int RESET_DEFAULTS = 100;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;

    public GuiControllerAxisScreen(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int centerX = width / 2;
        buttonList.add(new GuiButton(CAMERA_X, centerX - 155, 54, 150, 20, label("Camera X", Config.invertLookX)));
        buttonList.add(new GuiButton(CAMERA_Y, centerX + 5, 54, 150, 20, label("Camera Y", Config.invertLookY)));
        buttonList.add(new GuiButton(CURSOR_X, centerX - 155, 80, 150, 20, label("Cursor X", Config.invertCursorX)));
        buttonList.add(new GuiButton(CURSOR_Y, centerX + 5, 80, 150, 20, label("Cursor Y", Config.invertCursorY)));
        buttonList.add(new GuiButton(CURSOR_STICK, centerX - 155, 106, 310, 20, cursorStickLabel()));
        buttonList.add(new GuiButton(RESET_DEFAULTS, centerX - 100, 142, 200, 20, "Reset Defaults"));
        buttonList.add(new GuiButton(DONE, centerX - 100, height - 28, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case CAMERA_X:
                Config.invertLookX = !Config.invertLookX;
                break;
            case CAMERA_Y:
                Config.invertLookY = !Config.invertLookY;
                break;
            case CURSOR_X:
                Config.invertCursorX = !Config.invertCursorX;
                break;
            case CURSOR_Y:
                Config.invertCursorY = !Config.invertCursorY;
                break;
            case CURSOR_STICK:
                Config.cursorStick = "LEFT".equalsIgnoreCase(Config.cursorStick) ? "RIGHT" : "LEFT";
                break;
            case RESET_DEFAULTS:
                Config.invertLookX = false;
                Config.invertLookY = false;
                Config.invertCursorX = false;
                Config.invertCursorY = false;
                Config.cursorStick = "RIGHT";
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
        drawCenteredString(fontRendererObj, "Axes & Cursor Stick", width / 2, 16, 0xFFFFFF);
        drawCenteredString(
            fontRendererObj,
            "Invert axes independently and choose which stick moves the GUI cursor",
            width / 2,
            32,
            0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static String label(String axis, boolean inverted) {
        return axis + ": " + (inverted ? "\u00A7aInverted" : "\u00A77Normal");
    }

    private static String cursorStickLabel() {
        return "GUI Cursor Stick: \u00A7a" + ("LEFT".equalsIgnoreCase(Config.cursorStick) ? "Left" : "Right");
    }
}
