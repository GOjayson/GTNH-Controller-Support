package dev.gtnhcontroller.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ControllerAction;
import dev.gtnhcontroller.client.input.ControllerBindingLayer;
import dev.gtnhcontroller.client.input.ControllerProfile;
import dev.gtnhcontroller.client.input.ModKeyBindingController;
import dev.gtnhcontroller.client.input.SdlGamepadManager;

public final class GuiControllerBindingScreen extends GuiScreen
    implements ControllerConfigurationScreen, ControllerInputCaptureScreen {

    private static final int BINDING_BUTTON_BASE = 1000;
    private static final int CLEAR_BUTTON_BASE = 2000;
    private static final int PREVIOUS_PAGE = 3000;
    private static final int NEXT_PAGE = 3001;
    private static final int RESET_DEFAULTS = 3002;
    private static final int DONE = 3003;
    private static final int TOGGLE_LAYER = 3004;
    private static final int FIRST_ROW_Y = 68;
    private static final int ROW_HEIGHT = 22;
    private static final float CAPTURE_TRIGGER_THRESHOLD = 0.50F;

    private final GuiScreen parentScreen;
    private final SdlGamepadManager gamepadManager;
    private final ControllerProfile controllerProfile;
    private final ModKeyBindingController modKeyBindingController;
    private final List<ControllerAction> allActions = new ArrayList<ControllerAction>();
    private final List<ControllerAction> filteredActions = new ArrayList<ControllerAction>();
    private final String title;
    private final boolean guiBindings;

    private GuiTextField searchField;
    private int page;
    private ControllerBindingLayer bindingLayer = ControllerBindingLayer.PRIMARY;
    private ControllerAction captureAction;
    private final ControllerChordCapture chordCapture = new ControllerChordCapture();
    private boolean captureArmed;
    private boolean waitingForCapturedInputRelease;

    public GuiControllerBindingScreen(GuiScreen parentScreen, SdlGamepadManager gamepadManager,
        ControllerProfile controllerProfile, ModKeyBindingController modKeyBindingController, boolean guiBindings) {
        this.parentScreen = parentScreen;
        this.gamepadManager = gamepadManager;
        this.controllerProfile = controllerProfile;
        this.modKeyBindingController = modKeyBindingController;
        this.guiBindings = guiBindings;
        title = guiBindings ? "GUI Controller Bindings" : "Gameplay Controller Bindings";
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        String previousSearch = searchField == null ? "" : searchField.getText();
        searchField = new GuiTextField(fontRendererObj, width / 2 - 155, 43, 310, 18);
        searchField.setMaxStringLength(100);
        searchField.setText(previousSearch);
        rebuildActionList();
        filterActions();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        searchField.updateCursorCounter();
        if (waitingForCapturedInputRelease) {
            if (!gamepadManager.hasBindableInputDown(CAPTURE_TRIGGER_THRESHOLD)) {
                waitingForCapturedInputRelease = false;
                rebuildButtons();
            }
            return;
        }
        if (captureAction == null) {
            return;
        }

        if (!captureArmed) {
            if (!gamepadManager.hasBindableInputDown(CAPTURE_TRIGGER_THRESHOLD)) {
                captureArmed = true;
                rebuildButtons();
            }
            return;
        }

        String capturedBinding = chordCapture.update(gamepadManager.getBindableInputsDown(CAPTURE_TRIGGER_THRESHOLD));
        if (capturedBinding != null) {
            applyBinding(capturedBinding);
        } else if (chordCapture.hasStarted()) {
            rebuildButtons();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        int visibleRows = visibleRowCount();
        if (button.id >= BINDING_BUTTON_BASE && button.id < BINDING_BUTTON_BASE + visibleRows) {
            beginCapture(visibleAction(button.id - BINDING_BUTTON_BASE));
        } else if (button.id >= CLEAR_BUTTON_BASE && button.id < CLEAR_BUTTON_BASE + visibleRows) {
            ControllerAction action = visibleAction(button.id - CLEAR_BUTTON_BASE);
            controllerProfile.setBinding(action, "NONE", bindingLayer);
            waitForInputRelease();
        } else if (button.id == PREVIOUS_PAGE && page > 0) {
            page--;
            rebuildButtons();
        } else if (button.id == NEXT_PAGE && page < getPageCount() - 1) {
            page++;
            rebuildButtons();
        } else if (button.id == TOGGLE_LAYER && !guiBindings) {
            bindingLayer = bindingLayer == ControllerBindingLayer.PRIMARY ? ControllerBindingLayer.MODIFIER
                : ControllerBindingLayer.PRIMARY;
            page = 0;
            rebuildActionList();
            filterActions();
        } else if (button.id == RESET_DEFAULTS) {
            controllerProfile.resetBindings(guiBindings, bindingLayer);
            waitForInputRelease();
        } else if (button.id == DONE) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    protected void keyTyped(char typedCharacter, int keyCode) {
        if (captureAction != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                waitForInputRelease();
            } else if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                applyBinding("NONE");
            }
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parentScreen);
            return;
        }
        if (searchField.textboxKeyTyped(typedCharacter, keyCode)) {
            page = 0;
            filterActions();
            return;
        }
        super.keyTyped(typedCharacter, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, title, width / 2, 10, 0xFFFFFF);
        drawCenteredString(fontRendererObj, statusLine(), width / 2, 25, 0xA0A0A0);

        if (searchField.getText()
            .isEmpty() && !searchField.isFocused()) {
            drawString(
                fontRendererObj,
                "Search actions",
                searchField.xPosition + 4,
                searchField.yPosition + 5,
                0x707070);
        }

        int firstAction = page * rowsPerPage();
        int finalAction = Math.min(firstAction + rowsPerPage(), filteredActions.size());
        for (int actionIndex = firstAction; actionIndex < finalAction; actionIndex++) {
            int row = actionIndex - firstAction;
            String actionName = filteredActions.get(actionIndex).displayName;
            drawString(
                fontRendererObj,
                actionName,
                width / 2 - 45 - fontRendererObj.getStringWidth(actionName),
                FIRST_ROW_Y + 6 + row * ROW_HEIGHT,
                0xFFFFFF);
        }

        if (filteredActions.isEmpty()) {
            drawCenteredString(fontRendererObj, "No matching controller actions", width / 2, 94, 0xA0A0A0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        searchField.drawTextBox();
        drawConflictTooltip(mouseX, mouseY);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    @Override
    public boolean isCapturingControllerInput() {
        return captureAction != null || waitingForCapturedInputRelease;
    }

    private void rebuildActionList() {
        allActions.clear();
        for (ControllerAction action : ControllerAction.values()) {
            if (action.guiAction == guiBindings
                && !(bindingLayer == ControllerBindingLayer.MODIFIER && action == ControllerAction.MODIFIER_LAYER)) {
                allActions.add(action);
            }
        }
    }

    private void filterActions() {
        String query = searchField.getText()
            .trim()
            .toLowerCase(Locale.ROOT);
        filteredActions.clear();
        for (ControllerAction action : allActions) {
            if (query.isEmpty() || action.displayName.toLowerCase(Locale.ROOT)
                .contains(query)
                || action.configKey.toLowerCase(Locale.ROOT)
                    .contains(query)) {
                filteredActions.add(action);
            }
        }
        page = Math.min(page, getPageCount() - 1);
        rebuildButtons();
    }

    private void rebuildButtons() {
        buttonList.clear();
        int firstAction = page * rowsPerPage();
        int finalAction = Math.min(firstAction + rowsPerPage(), filteredActions.size());
        for (int actionIndex = firstAction; actionIndex < finalAction; actionIndex++) {
            int row = actionIndex - firstAction;
            ControllerAction action = filteredActions.get(actionIndex);
            String bindingLabel = action == captureAction
                ? (captureArmed ? "> " + chordCapture.displayValue() + " <" : "Release inputs")
                : formatBinding(action);

            GuiButton bindingButton = new GuiButton(
                BINDING_BUTTON_BASE + row,
                width / 2 - 40,
                FIRST_ROW_Y + row * ROW_HEIGHT,
                150,
                20,
                bindingLabel);
            GuiButton clearButton = new GuiButton(
                CLEAR_BUTTON_BASE + row,
                width / 2 + 115,
                FIRST_ROW_Y + row * ROW_HEIGHT,
                40,
                20,
                "Clear");
            boolean rowEnabled = captureAction == null && !waitingForCapturedInputRelease;
            bindingButton.enabled = rowEnabled || action == captureAction;
            clearButton.enabled = rowEnabled && !"NONE".equalsIgnoreCase(Config.getBinding(action, bindingLayer));
            buttonList.add(bindingButton);
            buttonList.add(clearButton);
        }

        int pageCount = getPageCount();
        if (pageCount > 1) {
            GuiButton previousButton = new GuiButton(PREVIOUS_PAGE, width / 2 - 155, height - 52, 70, 20, "< Previous");
            GuiButton nextButton = new GuiButton(NEXT_PAGE, width / 2 + 85, height - 52, 70, 20, "Next >");
            previousButton.enabled = page > 0 && captureAction == null && !waitingForCapturedInputRelease;
            nextButton.enabled = page < pageCount - 1 && captureAction == null && !waitingForCapturedInputRelease;
            buttonList.add(previousButton);
            buttonList.add(nextButton);
        }
        if (!guiBindings) {
            GuiButton layerButton = new GuiButton(
                TOGGLE_LAYER,
                width / 2 - 75,
                height - 52,
                150,
                20,
                "Layer: " + bindingLayer.displayName);
            layerButton.enabled = captureAction == null && !waitingForCapturedInputRelease;
            buttonList.add(layerButton);
        }

        GuiButton resetButton = new GuiButton(RESET_DEFAULTS, width / 2 - 155, height - 28, 150, 20, "Reset Defaults");
        GuiButton doneButton = new GuiButton(DONE, width / 2 + 5, height - 28, 150, 20, "Done");
        resetButton.enabled = captureAction == null && !waitingForCapturedInputRelease;
        doneButton.enabled = captureAction == null && !waitingForCapturedInputRelease;
        buttonList.add(resetButton);
        buttonList.add(doneButton);
    }

    private String formatBinding(ControllerAction action) {
        String formatted = ControllerBindingDisplay.format(Config.getBinding(action, bindingLayer));
        return getConflictNames(action).isEmpty() ? formatted : "\u00A7c! " + formatted;
    }

    private List<String> getConflictNames(ControllerAction action) {
        List<String> conflicts = new ArrayList<String>();
        for (ControllerAction coreConflict : controllerProfile.getConflictingActions(action, bindingLayer)) {
            conflicts.add((coreConflict.guiAction ? "GUI / " : "Gameplay / ") + coreConflict.displayName);
        }
        conflicts.addAll(modKeyBindingController.getConflictNamesForCoreAction(action, bindingLayer));
        return conflicts;
    }

    private void drawConflictTooltip(int mouseX, int mouseY) {
        int row = (mouseY - FIRST_ROW_Y) / ROW_HEIGHT;
        if (mouseX < width / 2 - 40 || mouseX >= width / 2 + 110
            || mouseY < FIRST_ROW_Y
            || row < 0
            || row >= visibleRowCount()
            || mouseY >= FIRST_ROW_Y + row * ROW_HEIGHT + 20) {
            return;
        }
        List<String> conflicts = getConflictNames(visibleAction(row));
        if (conflicts.isEmpty()) {
            return;
        }
        List<String> tooltip = new ArrayList<String>();
        tooltip.add("\u00A7cConflicts with:");
        tooltip.addAll(conflicts);
        drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
    }

    private void beginCapture(ControllerAction action) {
        captureAction = action;
        captureArmed = false;
        chordCapture.reset();
        searchField.setFocused(false);
        rebuildButtons();
    }

    private void applyBinding(String bindingSpecification) {
        controllerProfile.setBinding(captureAction, bindingSpecification, bindingLayer);
        waitForInputRelease();
    }

    private void waitForInputRelease() {
        captureAction = null;
        captureArmed = false;
        chordCapture.reset();
        waitingForCapturedInputRelease = true;
        rebuildButtons();
    }

    private ControllerAction visibleAction(int row) {
        return filteredActions.get(page * rowsPerPage() + row);
    }

    private int visibleRowCount() {
        int firstAction = page * rowsPerPage();
        return Math.max(Math.min(filteredActions.size() - firstAction, rowsPerPage()), 0);
    }

    private int getPageCount() {
        int rows = rowsPerPage();
        return Math.max((filteredActions.size() + rows - 1) / rows, 1);
    }

    private int rowsPerPage() {
        return Math.max(Math.min((height - 126) / ROW_HEIGHT, 6), 3);
    }

    private String statusLine() {
        if (waitingForCapturedInputRelease) {
            return "Release the controller input to continue";
        }
        if (captureAction != null) {
            return captureArmed ? "Hold a button combination, then release it - Escape cancels"
                : "Release all controller buttons and triggers";
        }
        return "Hover a red ! binding to see the exact conflicting actions";
    }
}
