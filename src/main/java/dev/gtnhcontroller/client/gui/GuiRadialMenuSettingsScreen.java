package dev.gtnhcontroller.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ChatMacro;
import dev.gtnhcontroller.client.input.ControllerButton;
import dev.gtnhcontroller.client.input.ModKeyBindingController;
import dev.gtnhcontroller.client.input.RadialMenuConfigCodec;
import dev.gtnhcontroller.client.input.RadialMenuPage;
import dev.gtnhcontroller.client.input.RegisteredKeyBinding;
import dev.gtnhcontroller.client.input.SdlGamepadManager;

public final class GuiRadialMenuSettingsScreen extends GuiScreen implements ControllerConfigurationScreen {

    private static final int SLOT_BUTTON_BASE = 100;
    private static final int CLEAR_BUTTON_BASE = 200;
    private static final int PREVIOUS_PAGE = 300;
    private static final int NEXT_PAGE = 301;
    private static final int DONE = 302;
    private static final int ACTIVATION_MODE = 303;
    private static final int CHAT_MACROS = 304;
    private static final int RADIAL_PAGE_BASE = 400;
    private static final int SLOTS_PER_PAGE = 4;
    private static final String[] SLOT_NAMES = { "Up", "Up-right", "Right", "Down-right", "Down", "Down-left", "Left",
        "Up-left" };

    private final GuiScreen parentScreen;
    private final SdlGamepadManager gamepadManager;
    private final ModKeyBindingController keyBindingController;

    private int page;
    private RadialMenuPage radialPage = RadialMenuPage.BASE;

    public GuiRadialMenuSettingsScreen(GuiScreen parentScreen, SdlGamepadManager gamepadManager,
        ModKeyBindingController keyBindingController) {
        this.parentScreen = parentScreen;
        this.gamepadManager = gamepadManager;
        this.keyBindingController = keyBindingController;
    }

    @Override
    public void initGui() {
        keyBindingController.refreshBindings();
        buttonList.clear();

        int firstSlot = page * SLOTS_PER_PAGE;
        int finalSlot = Math.min(firstSlot + SLOTS_PER_PAGE, RadialMenuConfigCodec.SLOT_COUNT);
        for (int slot = firstSlot; slot < finalSlot; slot++) {
            int row = slot - firstSlot;
            int buttonY = 90 + row * 24;
            String identifier = Config.getRadialMenuEntry(radialPage, slot);
            ChatMacro macro = Config.findChatMacro(identifier);
            RegisteredKeyBinding binding = keyBindingController.findRegisteredBinding(identifier);
            String label = identifier.isEmpty() ? "(empty)"
                : macro != null ? "Chat: " + macro.getName()
                    : binding == null ? "\u00A7cMissing action" : binding.getDisplayName();
            label = fontRendererObj.trimStringToWidth(label, 164);

            buttonList.add(new GuiButton(SLOT_BUTTON_BASE + slot, width / 2 - 35, buttonY, 170, 20, label));
            GuiButton clearButton = new GuiButton(CLEAR_BUTTON_BASE + slot, width / 2 + 140, buttonY, 50, 20, "Clear");
            clearButton.enabled = !identifier.isEmpty();
            buttonList.add(clearButton);
        }
        int radialPageWidth = 100;
        for (RadialMenuPage candidate : RadialMenuPage.values()) {
            GuiButton radialPageButton = new GuiButton(
                RADIAL_PAGE_BASE + candidate.ordinal(),
                width / 2 - 155 + candidate.ordinal() * 105,
                66,
                radialPageWidth,
                20,
                candidate.displayName);
            radialPageButton.enabled = candidate != radialPage;
            buttonList.add(radialPageButton);
        }
        buttonList.add(
            new GuiButton(
                ACTIVATION_MODE,
                width / 2 - 155,
                42,
                150,
                20,
                "Opening: " + Config.radialMenuActivationMode.displayName));
        buttonList.add(new GuiButton(CHAT_MACROS, width / 2 + 5, 42, 150, 20, "Chat Macros"));

        GuiButton previousButton = new GuiButton(PREVIOUS_PAGE, width / 2 - 155, height - 52, 80, 20, "< Previous");
        GuiButton nextButton = new GuiButton(NEXT_PAGE, width / 2 + 75, height - 52, 80, 20, "Next >");
        previousButton.enabled = page > 0;
        nextButton.enabled = page < pageCount() - 1;
        buttonList.add(previousButton);
        buttonList.add(nextButton);
        buttonList.add(new GuiButton(DONE, width / 2 - 100, height - 28, 200, 20, "Done"));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (gamepadManager.wasButtonPressed(ControllerButton.LEFT_SHOULDER)) {
            setRadialPage(RadialMenuPage.LEFT_SHOULDER);
        } else if (gamepadManager.wasButtonPressed(ControllerButton.RIGHT_SHOULDER)) {
            setRadialPage(RadialMenuPage.RIGHT_SHOULDER);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= SLOT_BUTTON_BASE && button.id < SLOT_BUTTON_BASE + RadialMenuConfigCodec.SLOT_COUNT) {
            int slot = button.id - SLOT_BUTTON_BASE;
            mc.displayGuiScreen(new GuiRadialMenuActionScreen(this, keyBindingController, radialPage, slot));
        } else if (button.id >= CLEAR_BUTTON_BASE && button.id < CLEAR_BUTTON_BASE + RadialMenuConfigCodec.SLOT_COUNT) {
            Config.setRadialMenuEntry(radialPage, button.id - CLEAR_BUTTON_BASE, "");
            Config.saveControllerSettings();
            initGui();
        } else if (button.id >= RADIAL_PAGE_BASE && button.id < RADIAL_PAGE_BASE + RadialMenuPage.values().length) {
            setRadialPage(RadialMenuPage.values()[button.id - RADIAL_PAGE_BASE]);
        } else if (button.id == PREVIOUS_PAGE && page > 0) {
            page--;
            initGui();
        } else if (button.id == NEXT_PAGE && page < pageCount() - 1) {
            page++;
            initGui();
        } else if (button.id == ACTIVATION_MODE) {
            Config.radialMenuActivationMode = Config.radialMenuActivationMode.next();
            Config.saveControllerSettings();
            initGui();
        } else if (button.id == CHAT_MACROS) {
            mc.displayGuiScreen(new GuiChatMacroListScreen(this));
        } else if (button.id == DONE) {
            mc.displayGuiScreen(parentScreen);
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
        drawCenteredString(fontRendererObj, "Radial Action Menu", width / 2, 15, 0xFFFFFF);
        drawCenteredString(
            fontRendererObj,
            "Assign registered actions or one-message chat macros",
            width / 2,
            31,
            0xA0A0A0);

        int firstSlot = page * SLOTS_PER_PAGE;
        int finalSlot = Math.min(firstSlot + SLOTS_PER_PAGE, RadialMenuConfigCodec.SLOT_COUNT);
        for (int slot = firstSlot; slot < finalSlot; slot++) {
            int row = slot - firstSlot;
            String label = SLOT_NAMES[slot];
            drawString(
                fontRendererObj,
                label,
                width / 2 - 45 - fontRendererObj.getStringWidth(label),
                96 + row * 24,
                0xFFFFFF);
        }

        drawCenteredString(
            fontRendererObj,
            "Page " + (page + 1) + " / " + pageCount(),
            width / 2,
            height - 46,
            0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void setRadialPage(RadialMenuPage requestedPage) {
        if (requestedPage == radialPage) {
            return;
        }
        radialPage = requestedPage;
        page = 0;
        initGui();
    }

    private int pageCount() {
        return (RadialMenuConfigCodec.SLOT_COUNT + SLOTS_PER_PAGE - 1) / SLOTS_PER_PAGE;
    }
}
