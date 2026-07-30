package dev.gtnhcontroller.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ControllerDevice;
import dev.gtnhcontroller.client.input.ControllerSelection;
import dev.gtnhcontroller.client.input.SdlGamepadManager;

public final class GuiControllerSelectionScreen extends GuiScreen implements ControllerConfigurationScreen {

    private static final int AUTOMATIC = 1;
    private static final int DEVICE_BUTTON_BASE = 100;
    private static final int REFRESH = 200;
    private static final int DONE = 201;
    private static final int PREVIOUS_PAGE = 202;
    private static final int NEXT_PAGE = 203;

    private final GuiScreen parentScreen;
    private final SdlGamepadManager gamepadManager;
    private final List<ControllerDevice> devices = new ArrayList<ControllerDevice>();
    private int page;

    public GuiControllerSelectionScreen(GuiScreen parentScreen, SdlGamepadManager gamepadManager) {
        this.parentScreen = parentScreen;
        this.gamepadManager = gamepadManager;
    }

    @Override
    public void initGui() {
        refreshDevices();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == AUTOMATIC) {
            gamepadManager.selectController(ControllerSelection.AUTOMATIC);
            rebuildButtons();
        } else if (button.id >= DEVICE_BUTTON_BASE && button.id < DEVICE_BUTTON_BASE + devices.size()) {
            ControllerDevice device = devices.get(button.id - DEVICE_BUTTON_BASE);
            gamepadManager.selectController(device.getSelectionKey());
            refreshDevices();
        } else if (button.id == REFRESH) {
            refreshDevices();
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
        drawCenteredString(fontRendererObj, "Controller Selection", width / 2, 15, 0xFFFFFF);
        drawCenteredString(fontRendererObj, gamepadManager.getStatusLine(), width / 2, 31, 0xA0A0A0);
        if (devices.isEmpty()) {
            drawCenteredString(fontRendererObj, "No SDL gamepads detected", width / 2, 92, 0xA0A0A0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void refreshDevices() {
        devices.clear();
        devices.addAll(gamepadManager.getAvailableGamepads());
        page = Math.min(page, pageCount() - 1);
        rebuildButtons();
    }

    private void rebuildButtons() {
        buttonList.clear();
        addSelectionButton(
            AUTOMATIC,
            52,
            "Automatic (first available)",
            ControllerSelection.isAutomatic(Config.controllerSelection));
        int firstDevice = page * rowsPerPage();
        int finalDevice = Math.min(firstDevice + rowsPerPage(), devices.size());
        for (int index = firstDevice; index < finalDevice; index++) {
            ControllerDevice device = devices.get(index);
            String label = device.getDisplayName() + (device.isConnected() ? " \u00A7a(connected)" : "");
            addSelectionButton(
                DEVICE_BUTTON_BASE + index,
                76 + (index - firstDevice) * 22,
                label,
                device.getSelectionKey()
                    .equals(Config.controllerSelection));
        }
        if (pageCount() > 1) {
            GuiButton previousButton = new GuiButton(PREVIOUS_PAGE, width / 2 - 155, height - 52, 90, 20, "< Previous");
            GuiButton nextButton = new GuiButton(NEXT_PAGE, width / 2 + 65, height - 52, 90, 20, "Next >");
            previousButton.enabled = page > 0;
            nextButton.enabled = page < pageCount() - 1;
            buttonList.add(previousButton);
            buttonList.add(nextButton);
        }
        buttonList.add(new GuiButton(REFRESH, width / 2 - 155, height - 28, 150, 20, "Refresh"));
        buttonList.add(new GuiButton(DONE, width / 2 + 5, height - 28, 150, 20, "Done"));
    }

    private void addSelectionButton(int id, int y, String label, boolean selected) {
        String trimmedLabel = fontRendererObj.trimStringToWidth(label, 294);
        GuiButton button = new GuiButton(id, width / 2 - 155, y, 310, 20, (selected ? "\u00A7a> " : "") + trimmedLabel);
        button.enabled = !selected;
        buttonList.add(button);
    }

    private int rowsPerPage() {
        return Math.max((height - 148) / 22, 1);
    }

    private int pageCount() {
        return Math.max((devices.size() + rowsPerPage() - 1) / rowsPerPage(), 1);
    }
}
