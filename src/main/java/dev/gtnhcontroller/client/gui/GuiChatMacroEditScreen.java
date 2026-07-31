package dev.gtnhcontroller.client.gui;

import java.util.UUID;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ChatMacro;

public final class GuiChatMacroEditScreen extends GuiScreen implements ControllerConfigurationScreen {

    private static final int SAVE = 1;
    private static final int CANCEL = 2;

    private final GuiScreen parentScreen;
    private final ChatMacro existingMacro;

    private GuiTextField nameField;
    private GuiTextField messageField;
    private String status = "";

    public GuiChatMacroEditScreen(GuiScreen parentScreen, ChatMacro existingMacro) {
        this.parentScreen = parentScreen;
        this.existingMacro = existingMacro;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        String previousName = nameField == null ? existingMacro == null ? "" : existingMacro.getName()
            : nameField.getText();
        String previousMessage = messageField == null ? existingMacro == null ? "" : existingMacro.getMessage()
            : messageField.getText();

        nameField = new GuiTextField(fontRendererObj, width / 2 - 155, 54, 310, 20);
        nameField.setMaxStringLength(ChatMacro.MAX_NAME_LENGTH);
        nameField.setText(previousName);
        messageField = new GuiTextField(fontRendererObj, width / 2 - 155, 92, 310, 20);
        messageField.setMaxStringLength(ChatMacro.MAX_MESSAGE_LENGTH);
        messageField.setText(previousMessage);
        nameField.setFocused(existingMacro == null);
        messageField.setFocused(existingMacro != null);

        buttonList.clear();
        buttonList.add(new GuiButton(SAVE, width / 2 - 155, 124, 150, 20, "Save"));
        buttonList.add(new GuiButton(CANCEL, width / 2 + 5, 124, 150, 20, "Cancel"));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        nameField.updateCursorCounter();
        messageField.updateCursorCounter();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == SAVE) {
            saveMacro();
        } else if (button.id == CANCEL) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    protected void keyTyped(char typedCharacter, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parentScreen);
            return;
        }
        if (keyCode == Keyboard.KEY_TAB) {
            boolean focusName = !nameField.isFocused();
            nameField.setFocused(focusName);
            messageField.setFocused(!focusName);
            return;
        }
        if (nameField.textboxKeyTyped(typedCharacter, keyCode)
            || messageField.textboxKeyTyped(typedCharacter, keyCode)) {
            status = "";
            return;
        }
        super.keyTyped(typedCharacter, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        messageField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(
            fontRendererObj,
            existingMacro == null ? "Add Chat Macro" : "Edit Chat Macro",
            width / 2,
            15,
            0xFFFFFF);
        drawString(fontRendererObj, "Name", width / 2 - 155, 43, 0xA0A0A0);
        drawString(fontRendererObj, "Message or command", width / 2 - 155, 81, 0xA0A0A0);
        drawCenteredString(
            fontRendererObj,
            status.isEmpty() ? "Example: /tpa accept  or  Hi all" : status,
            width / 2,
            151,
            status.isEmpty() ? 0xA0A0A0 : 0xFF8080);
        drawCenteredString(
            fontRendererObj,
            "Macros send once only; there are no loops or automatic triggers",
            width / 2,
            166,
            0x707070);
        super.drawScreen(mouseX, mouseY, partialTicks);
        nameField.drawTextBox();
        messageField.drawTextBox();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    private void saveMacro() {
        try {
            String id = existingMacro == null ? UUID.randomUUID()
                .toString() : existingMacro.getId();
            Config.putChatMacro(new ChatMacro(id, nameField.getText(), messageField.getText()));
            Config.saveControllerSettings();
            mc.displayGuiScreen(parentScreen);
        } catch (IllegalArgumentException exception) {
            status = exception.getMessage();
        }
    }
}
