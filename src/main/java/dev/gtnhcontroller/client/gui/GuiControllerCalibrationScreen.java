package dev.gtnhcontroller.client.gui;

import java.util.Locale;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ControllerAxis;
import dev.gtnhcontroller.client.input.ControllerButton;
import dev.gtnhcontroller.client.input.ControllerCalibration;
import dev.gtnhcontroller.client.input.SdlGamepadManager;

public final class GuiControllerCalibrationScreen extends GuiScreen
    implements ControllerConfigurationScreen, ControllerInputCaptureScreen {

    private static final int START = 1;
    private static final int APPLY = 2;
    private static final int REDO = 3;
    private static final int DONE = 200;
    private static final int REST_TICKS = 60;
    private static final int RANGE_TICKS = 160;

    private final GuiScreen parentScreen;
    private final SdlGamepadManager gamepadManager;

    private ControllerCalibration calibration = new ControllerCalibration();
    private Phase phase = Phase.INTRO;
    private int phaseTicks;

    public GuiControllerCalibrationScreen(GuiScreen parentScreen, SdlGamepadManager gamepadManager) {
        this.parentScreen = parentScreen;
        this.gamepadManager = gamepadManager;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int centerX = width / 2;
        if (phase == Phase.INTRO) {
            GuiButton startButton = new GuiButton(START, centerX - 100, height - 52, 200, 20, "Start Calibration");
            startButton.enabled = gamepadManager.isConnected();
            buttonList.add(startButton);
        } else if (phase == Phase.RESULTS) {
            buttonList.add(new GuiButton(APPLY, centerX - 155, height - 52, 150, 20, "Apply Suggestions"));
            buttonList.add(new GuiButton(REDO, centerX + 5, height - 52, 150, 20, "Run Again"));
        }
        buttonList.add(new GuiButton(DONE, centerX - 100, height - 28, 200, 20, "Back"));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if ((phase == Phase.REST || phase == Phase.RANGE) && !gamepadManager.isConnected()) {
            calibration = new ControllerCalibration();
            phase = Phase.INTRO;
            phaseTicks = 0;
            initGui();
            return;
        }
        if (phase == Phase.INTRO) {
            if (gamepadManager.wasButtonPressed(ControllerButton.SOUTH) && gamepadManager.isConnected()) {
                beginCalibration();
            }
            return;
        }
        if (phase == Phase.REST) {
            sampleRest();
            phaseTicks++;
            if (phaseTicks >= REST_TICKS) {
                phase = Phase.RANGE;
                phaseTicks = 0;
                initGui();
            }
            return;
        }
        if (phase == Phase.RANGE) {
            sampleRange();
            phaseTicks++;
            if (phaseTicks >= RANGE_TICKS) {
                phase = Phase.RESULTS;
                phaseTicks = 0;
                initGui();
            }
            return;
        }
        if (gamepadManager.wasButtonPressed(ControllerButton.SOUTH)) {
            applySuggestions();
        } else if (gamepadManager.wasButtonPressed(ControllerButton.WEST)) {
            beginCalibration();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == START || button.id == REDO) {
            beginCalibration();
        } else if (button.id == APPLY) {
            applySuggestions();
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
        drawCenteredString(fontRendererObj, "Controller Calibration", width / 2, 12, 0xFFFFFF);
        drawCenteredString(fontRendererObj, gamepadManager.getStatusLine(), width / 2, 27, 0xA0A0A0);

        if (phase == Phase.INTRO) {
            drawCenteredString(
                fontRendererObj,
                "This wizard measures analog drift and usable range.",
                width / 2,
                58,
                0xFFFFFF);
            drawCenteredString(
                fontRendererObj,
                "Put the controller on a flat surface before starting.",
                width / 2,
                74,
                0xFFFFFF);
            drawCenteredString(fontRendererObj, "Press South / A or click Start.", width / 2, 98, 0xA0A0A0);
        } else if (phase == Phase.REST) {
            drawCenteredString(
                fontRendererObj,
                "Step 1 / 2: Do not touch either stick or trigger",
                width / 2,
                48,
                0xFFFF80);
            drawProgress(phaseTicks, REST_TICKS, 64);
            drawLiveValues(88);
        } else if (phase == Phase.RANGE) {
            drawCenteredString(
                fontRendererObj,
                "Step 2 / 2: Rotate both sticks and fully press both triggers",
                width / 2,
                48,
                0xFFFF80);
            drawProgress(phaseTicks, RANGE_TICKS, 64);
            drawLiveValues(88);
            drawRangeStatus(136);
        } else {
            drawResults();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isCapturingControllerInput() {
        return phase == Phase.REST || phase == Phase.RANGE;
    }

    private void beginCalibration() {
        calibration = new ControllerCalibration();
        phase = Phase.REST;
        phaseTicks = 0;
        initGui();
    }

    private void sampleRest() {
        calibration.sampleRest(
            gamepadManager.getAxis(ControllerAxis.LEFT_X),
            gamepadManager.getAxis(ControllerAxis.LEFT_Y),
            gamepadManager.getAxis(ControllerAxis.RIGHT_X),
            gamepadManager.getAxis(ControllerAxis.RIGHT_Y),
            gamepadManager.getTrigger(ControllerAxis.LEFT_TRIGGER),
            gamepadManager.getTrigger(ControllerAxis.RIGHT_TRIGGER));
    }

    private void sampleRange() {
        calibration.sampleRange(
            gamepadManager.getAxis(ControllerAxis.LEFT_X),
            gamepadManager.getAxis(ControllerAxis.LEFT_Y),
            gamepadManager.getAxis(ControllerAxis.RIGHT_X),
            gamepadManager.getAxis(ControllerAxis.RIGHT_Y),
            gamepadManager.getTrigger(ControllerAxis.LEFT_TRIGGER),
            gamepadManager.getTrigger(ControllerAxis.RIGHT_TRIGGER));
    }

    private void applySuggestions() {
        Config.moveDeadZone = calibration.suggestMovementDeadZone();
        Config.lookDeadZone = calibration.suggestCameraDeadZone();
        Config.cursorDeadZone = calibration.suggestCursorDeadZone("RIGHT".equalsIgnoreCase(Config.cursorStick));
        Config.triggerThreshold = calibration.suggestTriggerThreshold();
        Config.saveControllerSettings();
        mc.displayGuiScreen(parentScreen);
    }

    private void drawLiveValues(int y) {
        drawCenteredString(
            fontRendererObj,
            String.format(
                Locale.ROOT,
                "LS %+.2f / %+.2f    RS %+.2f / %+.2f",
                gamepadManager.getAxis(ControllerAxis.LEFT_X),
                gamepadManager.getAxis(ControllerAxis.LEFT_Y),
                gamepadManager.getAxis(ControllerAxis.RIGHT_X),
                gamepadManager.getAxis(ControllerAxis.RIGHT_Y)),
            width / 2,
            y,
            0xFFFFFF);
        drawCenteredString(
            fontRendererObj,
            String.format(
                Locale.ROOT,
                "LT %.2f    RT %.2f",
                gamepadManager.getTrigger(ControllerAxis.LEFT_TRIGGER),
                gamepadManager.getTrigger(ControllerAxis.RIGHT_TRIGGER)),
            width / 2,
            y + 14,
            0xFFFFFF);
    }

    private void drawRangeStatus(int y) {
        drawCenteredString(
            fontRendererObj,
            "Left stick " + pass(calibration.isLeftStickRangeComplete())
                + "    Right stick "
                + pass(calibration.isRightStickRangeComplete()),
            width / 2,
            y,
            0xFFFFFF);
        drawCenteredString(
            fontRendererObj,
            "Left trigger " + pass(calibration.isLeftTriggerRangeComplete())
                + "    Right trigger "
                + pass(calibration.isRightTriggerRangeComplete()),
            width / 2,
            y + 13,
            0xFFFFFF);
    }

    private void drawResults() {
        boolean rightCursor = "RIGHT".equalsIgnoreCase(Config.cursorStick);
        int startY = 46;
        drawCenteredString(
            fontRendererObj,
            "Suggested values (nothing changes until Apply)",
            width / 2,
            startY,
            0xFFFFFF);
        drawCenteredString(
            fontRendererObj,
            percentLine("Movement deadzone", calibration.suggestMovementDeadZone(), calibration.getLeftRest()),
            width / 2,
            startY + 18,
            driftColor(calibration.hasLeftStickDrift()));
        drawCenteredString(
            fontRendererObj,
            percentLine("Camera deadzone", calibration.suggestCameraDeadZone(), calibration.getRightRest()),
            width / 2,
            startY + 32,
            driftColor(calibration.hasRightStickDrift()));
        drawCenteredString(
            fontRendererObj,
            "Cursor deadzone: " + percent(calibration.suggestCursorDeadZone(rightCursor))
                + " ("
                + (rightCursor ? "right" : "left")
                + " stick)",
            width / 2,
            startY + 46,
            0xFFFFFF);
        drawCenteredString(
            fontRendererObj,
            "Trigger threshold: " + percent(calibration.suggestTriggerThreshold())
                + " (rest LT "
                + percent(calibration.getLeftTriggerRest())
                + ", RT "
                + percent(calibration.getRightTriggerRest())
                + ")",
            width / 2,
            startY + 60,
            driftColor(calibration.hasTriggerDrift()));

        if (!calibration.isRangeComplete()) {
            drawCenteredString(
                fontRendererObj,
                "Warning: not every control reached 75%; run again for best results.",
                width / 2,
                startY + 82,
                0xFF8080);
        } else {
            drawCenteredString(
                fontRendererObj,
                "All analog controls reached the expected range.",
                width / 2,
                startY + 82,
                0x80FF80);
        }
        drawCenteredString(
            fontRendererObj,
            "South / A applies; West / X runs the wizard again.",
            width / 2,
            startY + 100,
            0xA0A0A0);
    }

    private void drawProgress(int current, int total, int y) {
        int barWidth = Math.min(240, width - 40);
        int left = width / 2 - barWidth / 2;
        int filled = Math.round(barWidth * Math.min(current / (float) total, 1.0F));
        drawRect(left, y, left + barWidth, y + 8, 0xFF303030);
        drawRect(left, y, left + filled, y + 8, 0xFF60A0FF);
    }

    private static String percentLine(String label, float suggestion, float measuredRest) {
        return label + ": " + percent(suggestion) + " (measured rest " + percent(measuredRest) + ")";
    }

    private static String percent(float value) {
        return Math.round(value * 100.0F) + "%";
    }

    private static int driftColor(boolean drift) {
        return drift ? 0xFFFF80 : 0xFFFFFF;
    }

    private static String pass(boolean complete) {
        return complete ? "\u00A7aOK" : "\u00A7cINCOMPLETE";
    }

    private enum Phase {
        INTRO,
        REST,
        RANGE,
        RESULTS
    }
}
