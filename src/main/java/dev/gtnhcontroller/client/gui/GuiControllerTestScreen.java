package dev.gtnhcontroller.client.gui;

import static dev.gtnhcontroller.client.input.ControllerAction.GUI_BACK;

import java.util.Locale;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ControllerAxis;
import dev.gtnhcontroller.client.input.ControllerButton;
import dev.gtnhcontroller.client.input.ControllerProfile;
import dev.gtnhcontroller.client.input.InputMath;
import dev.gtnhcontroller.client.input.SdlGamepadManager;
import dev.gtnhcontroller.client.input.StickVector;

public final class GuiControllerTestScreen extends GuiScreen
    implements ControllerConfigurationScreen, ControllerInputCaptureScreen {

    private static final int DONE = 200;
    private static final long EXIT_HOLD_DURATION_NANOS = 3_000_000_000L;

    private final GuiScreen parentScreen;
    private final SdlGamepadManager gamepadManager;
    private final ControllerProfile controllerProfile;
    private long exitHoldStartedNanos;

    public GuiControllerTestScreen(GuiScreen parentScreen, SdlGamepadManager gamepadManager,
        ControllerProfile controllerProfile) {
        this.parentScreen = parentScreen;
        this.gamepadManager = gamepadManager;
        this.controllerProfile = controllerProfile;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(DONE, width / 2 - 100, height - 28, 200, 20, "Done"));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (controllerProfile.isDown(GUI_BACK)) {
            long currentTimeNanos = System.nanoTime();
            if (exitHoldStartedNanos == 0L) {
                exitHoldStartedNanos = currentTimeNanos;
            } else if (currentTimeNanos - exitHoldStartedNanos >= EXIT_HOLD_DURATION_NANOS) {
                mc.displayGuiScreen(parentScreen);
            }
        } else {
            exitHoldStartedNanos = 0L;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == DONE) {
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
        drawCenteredString(fontRendererObj, "Controller Test", width / 2, 8, 0xFFFFFF);
        drawCenteredString(fontRendererObj, gamepadManager.getStatusLine(), width / 2, 21, 0xA0A0A0);
        drawAnalogValues(37);
        drawButtons(105);
        drawCenteredString(
            fontRendererObj,
            "Green = pressed, gray = not exposed by SDL",
            width / 2,
            height - 42,
            0xA0A0A0);
        drawCenteredString(fontRendererObj, exitInstruction(), width / 2, height - 53, 0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isCapturingControllerInput() {
        return true;
    }

    private void drawAnalogValues(int y) {
        float leftX = gamepadManager.getAxis(ControllerAxis.LEFT_X);
        float leftY = gamepadManager.getAxis(ControllerAxis.LEFT_Y);
        float rightX = gamepadManager.getAxis(ControllerAxis.RIGHT_X);
        float rightY = gamepadManager.getAxis(ControllerAxis.RIGHT_Y);
        StickVector movement = InputMath
            .applyRadialDeadZone(leftX, leftY, Config.moveDeadZone, Config.moveCurveExponent);
        StickVector camera = InputMath
            .applyRadialDeadZone(rightX, rightY, Config.lookDeadZone, Config.lookCurveExponent);
        boolean rightCursor = "RIGHT".equalsIgnoreCase(Config.cursorStick);
        StickVector cursor = InputMath.applyRadialDeadZone(
            rightCursor ? rightX : leftX,
            rightCursor ? rightY : leftY,
            Config.cursorDeadZone,
            Config.cursorCurveExponent);
        float leftTrigger = gamepadManager.getTrigger(ControllerAxis.LEFT_TRIGGER);
        float rightTrigger = gamepadManager.getTrigger(ControllerAxis.RIGHT_TRIGGER);

        drawCenteredString(
            fontRendererObj,
            String.format(
                Locale.ROOT,
                "Left stick raw %+.2f / %+.2f  -> movement %+.2f / %+.2f",
                leftX,
                leftY,
                movement.x,
                movement.y),
            width / 2,
            y,
            axisColor(ControllerAxis.LEFT_X, ControllerAxis.LEFT_Y));
        drawCenteredString(
            fontRendererObj,
            String.format(
                Locale.ROOT,
                "Right stick raw %+.2f / %+.2f -> camera %+.2f / %+.2f",
                rightX,
                rightY,
                camera.x,
                camera.y),
            width / 2,
            y + 13,
            axisColor(ControllerAxis.RIGHT_X, ControllerAxis.RIGHT_Y));
        drawCenteredString(
            fontRendererObj,
            String.format(
                Locale.ROOT,
                "Cursor (%s) after %.0f%% deadzone: %+.2f / %+.2f",
                rightCursor ? "right stick" : "left stick",
                Config.cursorDeadZone * 100.0F,
                cursor.x,
                cursor.y),
            width / 2,
            y + 26,
            0xFFFFFF);
        drawCenteredString(
            fontRendererObj,
            String.format(
                Locale.ROOT,
                "LT %.2f [%s]    RT %.2f [%s]    threshold %.0f%%",
                leftTrigger,
                leftTrigger >= Config.triggerThreshold ? "ACTIVE" : "idle",
                rightTrigger,
                rightTrigger >= Config.triggerThreshold ? "ACTIVE" : "idle",
                Config.triggerThreshold * 100.0F),
            width / 2,
            y + 39,
            triggerColor());
    }

    private void drawButtons(int startY) {
        ControllerButton[] buttons = ControllerButton.values();
        int columns = 4;
        int columnWidth = Math.min(145, Math.max((width - 24) / columns, 80));
        int totalWidth = columnWidth * columns;
        int left = width / 2 - totalWidth / 2;
        for (int index = 0; index < buttons.length; index++) {
            ControllerButton button = buttons[index];
            int column = index % columns;
            int row = index / columns;
            int color = !gamepadManager.hasButton(button) ? 0x606060
                : gamepadManager.isButtonDown(button) ? 0x60FF60 : 0xFFFFFF;
            drawString(fontRendererObj, button.getDisplayName(), left + column * columnWidth, startY + row * 12, color);
        }
    }

    private int axisColor(ControllerAxis first, ControllerAxis second) {
        return gamepadManager.hasAxis(first) && gamepadManager.hasAxis(second) ? 0xFFFFFF : 0x606060;
    }

    private int triggerColor() {
        return gamepadManager.hasAxis(ControllerAxis.LEFT_TRIGGER)
            && gamepadManager.hasAxis(ControllerAxis.RIGHT_TRIGGER) ? 0xFFFFFF : 0x606060;
    }

    private String exitInstruction() {
        if (exitHoldStartedNanos == 0L) {
            return "Hold the configured GUI Back input for 3 seconds to exit";
        }
        long elapsedNanos = Math.max(System.nanoTime() - exitHoldStartedNanos, 0L);
        float secondsRemaining = Math.max(EXIT_HOLD_DURATION_NANOS - elapsedNanos, 0L) / 1_000_000_000.0F;
        return String.format(Locale.ROOT, "Keep holding GUI Back: %.1f seconds", secondsRemaining);
    }
}
