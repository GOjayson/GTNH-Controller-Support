package dev.gtnhcontroller.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ChatMacro;
import dev.gtnhcontroller.client.input.RadialMenuPage;

public final class GuiChatMacroListScreen extends GuiScreen implements ControllerConfigurationScreen, GuiYesNoCallback {

    private static final int MACRO_BUTTON_BASE = 100;
    private static final int DELETE_BUTTON_BASE = 200;
    private static final int ADD_MACRO = 300;
    private static final int PREVIOUS_PAGE = 301;
    private static final int NEXT_PAGE = 302;
    private static final int DONE = 303;
    private static final int DELETE_CONFIRM_BASE = 1000;
    private static final int ROW_HEIGHT = 24;

    private final GuiScreen parentScreen;
    private final RadialMenuPage assignmentPage;
    private final int assignmentSlot;
    private final List<ChatMacro> macros = new ArrayList<ChatMacro>();

    private int page;
    private String status = "";

    public GuiChatMacroListScreen(GuiScreen parentScreen) {
        this(parentScreen, null, -1);
    }

    public GuiChatMacroListScreen(GuiScreen parentScreen, RadialMenuPage assignmentPage, int assignmentSlot) {
        this.parentScreen = parentScreen;
        this.assignmentPage = assignmentPage;
        this.assignmentSlot = assignmentSlot;
    }

    @Override
    public void initGui() {
        macros.clear();
        macros.addAll(Config.getChatMacros());
        page = Math.min(page, pageCount() - 1);
        rebuildButtons();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        int firstMacro = page * rowsPerPage();
        int visibleCount = Math.min(macros.size() - firstMacro, rowsPerPage());
        if (button.id >= MACRO_BUTTON_BASE && button.id < MACRO_BUTTON_BASE + Math.max(visibleCount, 0)) {
            ChatMacro macro = macros.get(firstMacro + button.id - MACRO_BUTTON_BASE);
            if (isAssignmentMode()) {
                Config.setRadialMenuEntry(assignmentPage, assignmentSlot, macro.getRadialIdentifier());
                Config.saveControllerSettings();
                mc.displayGuiScreen(parentScreen);
            } else {
                mc.displayGuiScreen(new GuiChatMacroEditScreen(this, macro));
            }
        } else if (button.id >= DELETE_BUTTON_BASE && button.id < DELETE_BUTTON_BASE + Math.max(visibleCount, 0)) {
            int macroIndex = firstMacro + button.id - DELETE_BUTTON_BASE;
            ChatMacro macro = macros.get(macroIndex);
            mc.displayGuiScreen(
                new GuiYesNo(
                    this,
                    "Delete chat macro '" + macro.getName() + "'?",
                    "It will also be removed from every radial slot.",
                    DELETE_CONFIRM_BASE + macroIndex));
        } else if (button.id == ADD_MACRO) {
            mc.displayGuiScreen(new GuiChatMacroEditScreen(this, null));
        } else if (button.id == PREVIOUS_PAGE && page > 0) {
            page--;
            rebuildButtons();
        } else if (button.id == NEXT_PAGE && page < pageCount() - 1) {
            page++;
            rebuildButtons();
        } else if (button.id == DONE) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    public void confirmClicked(boolean confirmed, int id) {
        int macroIndex = id - DELETE_CONFIRM_BASE;
        if (confirmed && macroIndex >= 0 && macroIndex < macros.size()) {
            String macroName = macros.get(macroIndex)
                .getName();
            Config.removeChatMacro(
                macros.get(macroIndex)
                    .getId());
            Config.saveControllerSettings();
            status = "Deleted " + macroName;
        }
        mc.displayGuiScreen(this);
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
        drawCenteredString(
            fontRendererObj,
            isAssignmentMode() ? "Choose Chat Macro" : "Chat Macros",
            width / 2,
            14,
            0xFFFFFF);
        drawCenteredString(
            fontRendererObj,
            status.isEmpty() ? "Each activation sends exactly one message or command" : status,
            width / 2,
            30,
            status.isEmpty() ? 0xA0A0A0 : 0x80FF80);
        if (macros.isEmpty()) {
            drawCenteredString(fontRendererObj, "No chat macros yet", width / 2, 88, 0xA0A0A0);
        } else if (pageCount() > 1) {
            drawCenteredString(
                fontRendererObj,
                "Page " + (page + 1) + " / " + pageCount(),
                width / 2,
                height - 46,
                0xA0A0A0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void rebuildButtons() {
        buttonList.clear();
        int firstMacro = page * rowsPerPage();
        int finalMacro = Math.min(firstMacro + rowsPerPage(), macros.size());
        for (int macroIndex = firstMacro; macroIndex < finalMacro; macroIndex++) {
            int row = macroIndex - firstMacro;
            ChatMacro macro = macros.get(macroIndex);
            String label = (isAssignmentMode() ? "Use: " : "Edit: ") + macro.getName();
            label = fontRendererObj.trimStringToWidth(label, 218);
            buttonList
                .add(new GuiButton(MACRO_BUTTON_BASE + row, width / 2 - 155, 50 + row * ROW_HEIGHT, 230, 20, label));
            buttonList
                .add(new GuiButton(DELETE_BUTTON_BASE + row, width / 2 + 80, 50 + row * ROW_HEIGHT, 75, 20, "Delete"));
        }

        if (pageCount() > 1) {
            GuiButton previousButton = new GuiButton(PREVIOUS_PAGE, width / 2 - 155, height - 52, 80, 20, "< Previous");
            GuiButton nextButton = new GuiButton(NEXT_PAGE, width / 2 + 75, height - 52, 80, 20, "Next >");
            previousButton.enabled = page > 0;
            nextButton.enabled = page < pageCount() - 1;
            buttonList.add(previousButton);
            buttonList.add(nextButton);
        }
        buttonList.add(new GuiButton(ADD_MACRO, width / 2 - 155, height - 28, 150, 20, "Add Macro"));
        buttonList.add(new GuiButton(DONE, width / 2 + 5, height - 28, 150, 20, "Done"));
    }

    private boolean isAssignmentMode() {
        return assignmentPage != null && assignmentSlot >= 0;
    }

    private int rowsPerPage() {
        return Math.max((height - 128) / ROW_HEIGHT, 1);
    }

    private int pageCount() {
        return Math.max((macros.size() + rowsPerPage() - 1) / rowsPerPage(), 1);
    }
}
