package dev.gtnhcontroller.client.gui;

import static dev.gtnhcontroller.client.input.ControllerAction.GUI_ALTERNATE;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_BACK;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_CONFIRM;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_KEYBOARD;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_NAV_DOWN;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_NAV_LEFT;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_NAV_RIGHT;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_NAV_UP;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ControllerAction;
import dev.gtnhcontroller.client.input.ControllerProfile;

final class OnScreenKeyboardOverlay {

    private static final int PANEL_PADDING = 5;
    private static final int ROW_GAP = 3;
    private static final int KEY_HEIGHT = 19;
    private static final int KEY_GAP = 2;

    private final Map<ControllerAction, InputRepeatTimer> repeatTimers = new EnumMap<ControllerAction, InputRepeatTimer>(
        ControllerAction.class);
    private final GuiInputCompatibility inputCompatibility;

    private GuiScreen targetScreen;
    private boolean symbols;
    private boolean caps;
    private int selectedRow;
    private int selectedColumn;

    OnScreenKeyboardOverlay(GuiInputCompatibility inputCompatibility) {
        this.inputCompatibility = inputCompatibility;
    }

    void open(GuiScreen screen) {
        targetScreen = screen;
        symbols = false;
        caps = false;
        selectedRow = 1;
        selectedColumn = 0;
        resetRepeatTimers();
    }

    void close() {
        targetScreen = null;
        resetRepeatTimers();
    }

    boolean isOpenFor(GuiScreen screen) {
        return targetScreen != null && targetScreen == screen;
    }

    void update(ControllerProfile controllerProfile, GuiScreen currentScreen) {
        if (!isOpenFor(currentScreen)) {
            close();
            return;
        }
        if (controllerProfile.wasPressed(GUI_KEYBOARD)) {
            close();
            return;
        }
        if (controllerProfile.wasPressed(GUI_ALTERNATE)) {
            caps = !caps;
        }

        long currentTimeMillis = System.currentTimeMillis();
        if (shouldActivate(controllerProfile, GUI_BACK, currentTimeMillis)) {
            inject('\0', Keyboard.KEY_BACK);
            if (!isOpenFor(currentScreen)) {
                return;
            }
        }

        int directionX = direction(
            shouldActivate(controllerProfile, GUI_NAV_LEFT, currentTimeMillis),
            shouldActivate(controllerProfile, GUI_NAV_RIGHT, currentTimeMillis));
        int directionY = direction(
            shouldActivate(controllerProfile, GUI_NAV_UP, currentTimeMillis),
            shouldActivate(controllerProfile, GUI_NAV_DOWN, currentTimeMillis));
        if (directionX != 0 || directionY != 0) {
            moveSelection(directionY != 0 ? 0 : directionX, directionY);
        }

        if (shouldActivate(controllerProfile, GUI_CONFIRM, currentTimeMillis)) {
            activateSelectedKey();
        }
    }

    void draw(GuiScreen screen) {
        if (!isOpenFor(screen)) {
            return;
        }

        OnScreenKeyboardLayout.Key[][] rows = OnScreenKeyboardLayout.getRows(symbols);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int panelWidth = Math.max(1, Math.min(520, screen.width - 8));
        int rowsHeight = rows.length * KEY_HEIGHT + (rows.length - 1) * ROW_GAP;
        int panelHeight = PANEL_PADDING + 13 + 4 + rowsHeight + 5 + fontRenderer.FONT_HEIGHT + PANEL_PADDING;
        int panelLeft = (screen.width - panelWidth) / 2;
        int panelTop = Math.max(2, (screen.height - panelHeight) / 2);
        int firstRowTop = panelTop + PANEL_PADDING + 17;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.0F, 0.0F, 700.0F);

        Gui.drawRect(0, 0, screen.width, screen.height, 0x70000000);
        Gui.drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xF0101010);
        drawCentered(
            fontRenderer,
            "On-screen Keyboard - " + (symbols ? "Symbols" : caps ? "ABC (Caps)" : "abc"),
            screen.width / 2,
            panelTop + PANEL_PADDING,
            0xFFFFFF);

        for (int row = 0; row < rows.length; row++) {
            drawRow(fontRenderer, rows[row], row, panelLeft, panelWidth, firstRowTop + row * (KEY_HEIGHT + ROW_GAP));
        }

        String help = "D-pad Move  A Type  B Backspace  X Caps  Y Done";
        help = fontRenderer.trimStringToWidth(help, Math.max(panelWidth - PANEL_PADDING * 2, 1));
        drawCentered(fontRenderer, help, screen.width / 2, firstRowTop + rowsHeight + 5, 0xB0B0B0);

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private void drawRow(FontRenderer fontRenderer, OnScreenKeyboardLayout.Key[] keys, int row, int panelLeft,
        int panelWidth, int top) {
        int innerLeft = panelLeft + PANEL_PADDING;
        int innerWidth = Math.max(panelWidth - PANEL_PADDING * 2, 1);
        int weightedWidth = Math.max(innerWidth - (keys.length - 1) * KEY_GAP, 1);
        float totalWeight = totalWeight(keys);
        float widthBefore = 0.0F;

        for (int column = 0; column < keys.length; column++) {
            OnScreenKeyboardLayout.Key key = keys[column];
            int left = innerLeft + Math.round(weightedWidth * widthBefore / totalWeight) + column * KEY_GAP;
            widthBefore += key.getWidthWeight();
            int right = innerLeft + Math.round(weightedWidth * widthBefore / totalWeight) + column * KEY_GAP;
            boolean selected = row == selectedRow && column == selectedColumn;
            int backgroundColor = selected ? 0xFF3E9EA8 : 0xE0383838;
            int textColor = selected ? 0xFFFFFF : 0xE0E0E0;

            Gui.drawRect(left, top, right, top + KEY_HEIGHT, backgroundColor);
            String label = fontRenderer.trimStringToWidth(key.getLabel(caps), Math.max(right - left - 4, 1));
            drawCentered(
                fontRenderer,
                label,
                (left + right) / 2,
                top + (KEY_HEIGHT - fontRenderer.FONT_HEIGHT) / 2,
                textColor);
        }
    }

    private void moveSelection(int directionX, int directionY) {
        OnScreenKeyboardNavigation.Position position = OnScreenKeyboardNavigation
            .move(selectedRow, selectedColumn, directionX, directionY, OnScreenKeyboardLayout.getRows(symbols));
        selectedRow = position.row;
        selectedColumn = position.column;
    }

    private void activateSelectedKey() {
        OnScreenKeyboardLayout.Key key = OnScreenKeyboardLayout.getRows(symbols)[selectedRow][selectedColumn];
        switch (key.getCommand()) {
            case CHARACTER:
                char character = key.getCharacter(caps);
                inject(character, OnScreenKeyboardKeyCode.forCharacter(character));
                break;
            case CAPS:
                caps = !caps;
                break;
            case SYMBOLS:
                symbols = !symbols;
                clampSelection();
                break;
            case SPACE:
                inject(' ', Keyboard.KEY_SPACE);
                break;
            case BACKSPACE:
                inject('\0', Keyboard.KEY_BACK);
                break;
            case ENTER:
                inject('\r', Keyboard.KEY_RETURN);
                break;
            case DONE:
                close();
                break;
            default:
                throw new IllegalStateException("Unknown on-screen keyboard command: " + key.getCommand());
        }
    }

    private void inject(char character, int keyCode) {
        GuiScreen screen = targetScreen;
        if (screen == null) {
            return;
        }
        inputCompatibility.keyTyped(screen, character, keyCode);
        if (Minecraft.getMinecraft().currentScreen != screen) {
            close();
        }
    }

    private boolean shouldActivate(ControllerProfile controllerProfile, ControllerAction action,
        long currentTimeMillis) {
        InputRepeatTimer repeatTimer = repeatTimers.get(action);
        if (repeatTimer == null) {
            repeatTimer = new InputRepeatTimer();
            repeatTimers.put(action, repeatTimer);
        }
        return repeatTimer.shouldActivate(
            controllerProfile.isDown(action),
            currentTimeMillis,
            Config.navigationInitialDelayMillis,
            Config.navigationRepeatIntervalMillis);
    }

    private void clampSelection() {
        OnScreenKeyboardLayout.Key[][] rows = OnScreenKeyboardLayout.getRows(symbols);
        selectedRow = Math.max(0, Math.min(selectedRow, rows.length - 1));
        selectedColumn = Math.max(0, Math.min(selectedColumn, rows[selectedRow].length - 1));
    }

    private void resetRepeatTimers() {
        for (InputRepeatTimer repeatTimer : repeatTimers.values()) {
            repeatTimer.reset();
        }
    }

    private static int direction(boolean negative, boolean positive) {
        return negative == positive ? 0 : negative ? -1 : 1;
    }

    private static float totalWeight(OnScreenKeyboardLayout.Key[] keys) {
        float totalWeight = 0.0F;
        for (OnScreenKeyboardLayout.Key key : keys) {
            totalWeight += key.getWidthWeight();
        }
        return totalWeight;
    }

    private static void drawCentered(FontRenderer fontRenderer, String text, int centerX, int y, int color) {
        fontRenderer.drawStringWithShadow(text, centerX - fontRenderer.getStringWidth(text) / 2, y, color);
    }
}
