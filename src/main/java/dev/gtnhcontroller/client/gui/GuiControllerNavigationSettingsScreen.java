package dev.gtnhcontroller.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;

public final class GuiControllerNavigationSettingsScreen extends GuiScreen implements ControllerConfigurationScreen {

    private static final int TOGGLE_BUTTON_NAVIGATION = 1;
    private static final int TOGGLE_SLOT_NAVIGATION = 2;
    private static final int PRECISION_DOWN = 3;
    private static final int PRECISION_VALUE = 4;
    private static final int PRECISION_UP = 5;
    private static final int INITIAL_DELAY_DOWN = 6;
    private static final int INITIAL_DELAY_VALUE = 7;
    private static final int INITIAL_DELAY_UP = 8;
    private static final int REPEAT_INTERVAL_DOWN = 9;
    private static final int REPEAT_INTERVAL_VALUE = 10;
    private static final int REPEAT_INTERVAL_UP = 11;
    private static final int RESET_DEFAULTS = 100;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;

    private GuiButton precisionValueButton;
    private GuiButton initialDelayValueButton;
    private GuiButton repeatIntervalValueButton;

    public GuiControllerNavigationSettingsScreen(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int centerX = width / 2;

        buttonList.add(
            new GuiButton(
                TOGGLE_BUTTON_NAVIGATION,
                centerX - 155,
                50,
                150,
                20,
                toggleLabel("Button Navigation", Config.enableButtonNavigation)));
        buttonList.add(
            new GuiButton(
                TOGGLE_SLOT_NAVIGATION,
                centerX + 5,
                50,
                150,
                20,
                toggleLabel("Slot Navigation", Config.enableSlotNavigation)));
        precisionValueButton = addValueRow(
            PRECISION_DOWN,
            PRECISION_VALUE,
            PRECISION_UP,
            82,
            NavigationSettingsValue.formatPercent(Config.precisionCursorScale));
        initialDelayValueButton = addValueRow(
            INITIAL_DELAY_DOWN,
            INITIAL_DELAY_VALUE,
            INITIAL_DELAY_UP,
            108,
            NavigationSettingsValue.formatMillis(Config.navigationInitialDelayMillis));
        repeatIntervalValueButton = addValueRow(
            REPEAT_INTERVAL_DOWN,
            REPEAT_INTERVAL_VALUE,
            REPEAT_INTERVAL_UP,
            134,
            NavigationSettingsValue.formatMillis(Config.navigationRepeatIntervalMillis));

        buttonList.add(new GuiButton(RESET_DEFAULTS, centerX - 100, 166, 200, 20, "Reset Navigation Defaults"));
        buttonList.add(new GuiButton(DONE, centerX - 100, height - 28, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case TOGGLE_BUTTON_NAVIGATION:
                Config.enableButtonNavigation = !Config.enableButtonNavigation;
                button.displayString = toggleLabel("Button Navigation", Config.enableButtonNavigation);
                break;
            case TOGGLE_SLOT_NAVIGATION:
                Config.enableSlotNavigation = !Config.enableSlotNavigation;
                button.displayString = toggleLabel("Slot Navigation", Config.enableSlotNavigation);
                break;
            case PRECISION_DOWN:
                Config.precisionCursorScale = NavigationSettingsValue
                    .adjustPrecisionScale(Config.precisionCursorScale, -1);
                break;
            case PRECISION_UP:
                Config.precisionCursorScale = NavigationSettingsValue
                    .adjustPrecisionScale(Config.precisionCursorScale, 1);
                break;
            case INITIAL_DELAY_DOWN:
                Config.navigationInitialDelayMillis = NavigationSettingsValue
                    .adjustInitialDelay(Config.navigationInitialDelayMillis, -1);
                break;
            case INITIAL_DELAY_UP:
                Config.navigationInitialDelayMillis = NavigationSettingsValue
                    .adjustInitialDelay(Config.navigationInitialDelayMillis, 1);
                break;
            case REPEAT_INTERVAL_DOWN:
                Config.navigationRepeatIntervalMillis = NavigationSettingsValue
                    .adjustRepeatInterval(Config.navigationRepeatIntervalMillis, -1);
                break;
            case REPEAT_INTERVAL_UP:
                Config.navigationRepeatIntervalMillis = NavigationSettingsValue
                    .adjustRepeatInterval(Config.navigationRepeatIntervalMillis, 1);
                break;
            case RESET_DEFAULTS:
                Config.enableButtonNavigation = true;
                Config.enableSlotNavigation = true;
                Config.precisionCursorScale = NavigationSettingsValue.DEFAULT_PRECISION_SCALE;
                Config.navigationInitialDelayMillis = NavigationSettingsValue.DEFAULT_INITIAL_DELAY_MILLIS;
                Config.navigationRepeatIntervalMillis = NavigationSettingsValue.DEFAULT_REPEAT_INTERVAL_MILLIS;
                initGui();
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
        drawCenteredString(fontRendererObj, "Controller Navigation", width / 2, 16, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Changes are saved and applied immediately", width / 2, 32, 0xA0A0A0);

        drawString(fontRendererObj, "Precision Cursor", width / 2 - 150, 88, 0xFFFFFF);
        drawString(fontRendererObj, "Initial Repeat Delay", width / 2 - 150, 114, 0xFFFFFF);
        drawString(fontRendererObj, "Repeat Interval", width / 2 - 150, 140, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private GuiButton addValueRow(int downId, int valueId, int upId, int buttonY, String value) {
        int centerX = width / 2;
        GuiButton valueButton = new GuiButton(valueId, centerX + 35, buttonY, 85, 20, value);
        valueButton.enabled = false;
        buttonList.add(new GuiButton(downId, centerX + 5, buttonY, 25, 20, "-"));
        buttonList.add(valueButton);
        buttonList.add(new GuiButton(upId, centerX + 125, buttonY, 25, 20, "+"));
        return valueButton;
    }

    private void refreshValueLabels() {
        precisionValueButton.displayString = NavigationSettingsValue.formatPercent(Config.precisionCursorScale);
        initialDelayValueButton.displayString = NavigationSettingsValue
            .formatMillis(Config.navigationInitialDelayMillis);
        repeatIntervalValueButton.displayString = NavigationSettingsValue
            .formatMillis(Config.navigationRepeatIntervalMillis);
    }

    private static String toggleLabel(String label, boolean enabled) {
        return label + ": " + (enabled ? "\u00A7aON" : "\u00A7cOFF");
    }
}
