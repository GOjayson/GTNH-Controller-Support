package dev.gtnhcontroller.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ControllerAction;
import dev.gtnhcontroller.client.input.ControllerProfile;
import dev.gtnhcontroller.client.input.SdlGamepadManager;

public final class GuiControllerBindingScreen extends GuiScreen
    implements ControllerConfigurationScreen, ControllerInputCaptureScreen {

    private static final int BINDING_BUTTON_BASE = 1000;
    private static final int CLEAR_BUTTON_BASE = 2000;
    private static final int PREVIOUS_PAGE = 3000;
    private static final int NEXT_PAGE = 3001;
    private static final int RESET_DEFAULTS = 3002;
    private static final int DONE = 3003;
    private static final int ROWS_PER_PAGE = 6;
    private static final float CAPTURE_TRIGGER_THRESHOLD = 0.50F;

    private final GuiScreen parentScreen;
    private final SdlGamepadManager gamepadManager;
    private final ControllerProfile controllerProfile;
    private final ControllerAction[] actions;
    private final String title;

    private int page;
    private ControllerAction captureAction;
    private boolean captureArmed;
    private boolean waitingForCapturedInputRelease;

    public GuiControllerBindingScreen(GuiScreen parentScreen, SdlGamepadManager gamepadManager,
        ControllerProfile controllerProfile, boolean guiBindings) {
        this.parentScreen = parentScreen;
        this.gamepadManager = gamepadManager;
        this.controllerProfile = controllerProfile;
        actions = getActions(guiBindings);
        title = guiBindings ? "GUI Controller Bindings" : "Gameplay Controller Bindings";
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int firstAction = page * ROWS_PER_PAGE;
        int finalAction = Math.min(firstAction + ROWS_PER_PAGE, actions.length);
        for (int actionIndex = firstAction; actionIndex < finalAction; actionIndex++) {
            int row = actionIndex - firstAction;
            int buttonY = 48 + row * 22;
            ControllerAction action = actions[actionIndex];
            String bindingLabel = action == captureAction ? (captureArmed ? "> Press input <" : "Release inputs...")
                : ControllerBindingDisplay.format(Config.getBinding(action));

            GuiButton bindingButton = new GuiButton(
                BINDING_BUTTON_BASE + actionIndex,
                width / 2 - 40,
                buttonY,
                150,
                20,
                bindingLabel);
            GuiButton clearButton = new GuiButton(
                CLEAR_BUTTON_BASE + actionIndex,
                width / 2 + 115,
                buttonY,
                40,
                20,
                "Clear");
            boolean rowEnabled = captureAction == null && !waitingForCapturedInputRelease;
            bindingButton.enabled = rowEnabled || action == captureAction;
            clearButton.enabled = rowEnabled;
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

        GuiButton resetButton = new GuiButton(RESET_DEFAULTS, width / 2 - 155, height - 28, 150, 20, "Reset Defaults");
        GuiButton doneButton = new GuiButton(DONE, width / 2 + 5, height - 28, 150, 20, "Done");
        resetButton.enabled = captureAction == null && !waitingForCapturedInputRelease;
        doneButton.enabled = captureAction == null && !waitingForCapturedInputRelease;
        buttonList.add(resetButton);
        buttonList.add(doneButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (waitingForCapturedInputRelease) {
            if (!gamepadManager.hasBindableInputDown(CAPTURE_TRIGGER_THRESHOLD)) {
                waitingForCapturedInputRelease = false;
                initGui();
            }
            return;
        }
        if (captureAction == null) {
            return;
        }

        if (!captureArmed) {
            if (!gamepadManager.hasBindableInputDown(CAPTURE_TRIGGER_THRESHOLD)) {
                captureArmed = true;
                initGui();
            }
            return;
        }

        String capturedBinding = gamepadManager.getNewBindableInput(CAPTURE_TRIGGER_THRESHOLD);
        if (capturedBinding != null) {
            applyBinding(capturedBinding);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= BINDING_BUTTON_BASE && button.id < BINDING_BUTTON_BASE + actions.length) {
            beginCapture(actions[button.id - BINDING_BUTTON_BASE]);
        } else if (button.id >= CLEAR_BUTTON_BASE && button.id < CLEAR_BUTTON_BASE + actions.length) {
            ControllerAction action = actions[button.id - CLEAR_BUTTON_BASE];
            controllerProfile.setBinding(action, "NONE");
            waitForInputRelease();
        } else if (button.id == PREVIOUS_PAGE && page > 0) {
            page--;
            initGui();
        } else if (button.id == NEXT_PAGE && page < getPageCount() - 1) {
            page++;
            initGui();
        } else if (button.id == RESET_DEFAULTS) {
            controllerProfile.resetBindings(actions[0].guiAction);
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
        super.keyTyped(typedCharacter, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, title, width / 2, 12, 0xFFFFFF);
        drawCenteredString(fontRendererObj, statusLine(), width / 2, 27, 0xA0A0A0);

        int firstAction = page * ROWS_PER_PAGE;
        int finalAction = Math.min(firstAction + ROWS_PER_PAGE, actions.length);
        for (int actionIndex = firstAction; actionIndex < finalAction; actionIndex++) {
            int row = actionIndex - firstAction;
            String actionName = actions[actionIndex].displayName;
            drawString(
                fontRendererObj,
                actionName,
                width / 2 - 45 - fontRendererObj.getStringWidth(actionName),
                54 + row * 22,
                0xFFFFFF);
        }

        if (getPageCount() > 1) {
            drawCenteredString(
                fontRendererObj,
                "Page " + (page + 1) + " / " + getPageCount(),
                width / 2,
                height - 46,
                0xA0A0A0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isCapturingControllerInput() {
        return captureAction != null || waitingForCapturedInputRelease;
    }

    private void beginCapture(ControllerAction action) {
        captureAction = action;
        captureArmed = false;
        initGui();
    }

    private void applyBinding(String bindingSpecification) {
        controllerProfile.setBinding(captureAction, bindingSpecification);
        waitForInputRelease();
    }

    private void waitForInputRelease() {
        captureAction = null;
        captureArmed = false;
        waitingForCapturedInputRelease = true;
        initGui();
    }

    private int getPageCount() {
        return (actions.length + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE;
    }

    private String statusLine() {
        if (waitingForCapturedInputRelease) {
            return "Release the controller input to continue";
        }
        if (captureAction != null) {
            return captureArmed ? "Press a controller button or trigger - Escape cancels"
                : "Release all controller buttons and triggers";
        }
        return gamepadManager.getStatusLine();
    }

    private static ControllerAction[] getActions(boolean guiBindings) {
        List<ControllerAction> selectedActions = new ArrayList<ControllerAction>();
        for (ControllerAction action : ControllerAction.values()) {
            if (action.guiAction == guiBindings) {
                selectedActions.add(action);
            }
        }
        return selectedActions.toArray(new ControllerAction[selectedActions.size()]);
    }
}
