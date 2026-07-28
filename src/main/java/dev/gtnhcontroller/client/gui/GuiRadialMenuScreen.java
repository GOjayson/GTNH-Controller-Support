package dev.gtnhcontroller.client.gui;

import static dev.gtnhcontroller.client.input.ControllerAction.GUI_BACK;
import static dev.gtnhcontroller.client.input.ControllerAction.RADIAL_MENU;
import static dev.gtnhcontroller.client.input.ControllerAxis.RIGHT_X;
import static dev.gtnhcontroller.client.input.ControllerAxis.RIGHT_Y;
import static dev.gtnhcontroller.client.input.ControllerButton.DPAD_DOWN;
import static dev.gtnhcontroller.client.input.ControllerButton.DPAD_LEFT;
import static dev.gtnhcontroller.client.input.ControllerButton.DPAD_RIGHT;
import static dev.gtnhcontroller.client.input.ControllerButton.DPAD_UP;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ControllerProfile;
import dev.gtnhcontroller.client.input.ModKeyBindingController;
import dev.gtnhcontroller.client.input.RadialMenuConfigCodec;
import dev.gtnhcontroller.client.input.RegisteredKeyBinding;
import dev.gtnhcontroller.client.input.SdlGamepadManager;

public final class GuiRadialMenuScreen extends GuiScreen implements ControllerInputCaptureScreen {

    private static final float SELECTION_THRESHOLD = 0.45F;
    private static final int INNER_RADIUS = 25;
    private static final int OUTER_RADIUS = 66;
    private static final int LABEL_RADIUS = 92;
    private static final int LABEL_WIDTH = 86;
    private static final int LABEL_HEIGHT = 18;
    private static final int ARC_SEGMENTS = 5;

    private final SdlGamepadManager gamepadManager;
    private final ControllerProfile controllerProfile;
    private final ModKeyBindingController keyBindingController;
    private final RegisteredKeyBinding[] entries = new RegisteredKeyBinding[RadialMenuConfigCodec.SLOT_COUNT];

    private int selectedSlot = -1;
    private int latchedDPadSlot = -1;
    private boolean finished;

    public GuiRadialMenuScreen(SdlGamepadManager gamepadManager, ControllerProfile controllerProfile,
        ModKeyBindingController keyBindingController) {
        this.gamepadManager = gamepadManager;
        this.controllerProfile = controllerProfile;
        this.keyBindingController = keyBindingController;
    }

    @Override
    public void initGui() {
        keyBindingController.refreshBindings();
        for (int slot = 0; slot < entries.length; slot++) {
            entries[slot] = keyBindingController.findRegisteredBinding(Config.getRadialMenuEntry(slot));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        int stickSelection = RadialMenuSelection
            .select(gamepadManager.getAxis(RIGHT_X), gamepadManager.getAxis(RIGHT_Y), SELECTION_THRESHOLD);
        int dPadSelection = RadialMenuSelection.selectDPad(
            gamepadManager.isButtonDown(DPAD_UP),
            gamepadManager.isButtonDown(DPAD_DOWN),
            gamepadManager.isButtonDown(DPAD_LEFT),
            gamepadManager.isButtonDown(DPAD_RIGHT));
        latchedDPadSlot = RadialMenuSelection.updateDPadLatch(latchedDPadSlot, dPadSelection);
        selectedSlot = dPadSelection >= 0 ? dPadSelection : stickSelection >= 0 ? stickSelection : latchedDPadSlot;
        if (controllerProfile.wasPressed(GUI_BACK)) {
            finish(false);
            return;
        }
        if (!controllerProfile.isDown(RADIAL_MENU)) {
            finish(true);
        }
    }

    @Override
    protected void keyTyped(char typedCharacter, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            finish(false);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0xA0000000);
        int centerX = width / 2;
        int centerY = height / 2;

        for (int slot = 0; slot < entries.length; slot++) {
            drawSector(centerX, centerY, slot, slot == selectedSlot);
        }
        for (int slot = 0; slot < entries.length; slot++) {
            drawEntryLabel(centerX, centerY, slot);
        }

        String centerLabel = selectedSlot < 0 ? "Aim with stick or D-pad"
            : entries[selectedSlot] == null ? "Slot is empty" : "Release to activate";
        drawCenteredString(fontRendererObj, centerLabel, centerX, centerY - 4, 0xFFFFFF);
        drawCenteredString(
            fontRendererObj,
            "D-pad selection stays selected; B cancels",
            centerX,
            height - 18,
            0xA0A0A0);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public boolean isCapturingControllerInput() {
        return true;
    }

    private void drawSector(int centerX, int centerY, int slot, boolean selected) {
        double sectorSize = Math.PI * 2.0D / entries.length;
        double centerAngle = -Math.PI / 2.0D + slot * sectorSize;
        double startAngle = centerAngle - sectorSize / 2.0D + 0.02D;
        double endAngle = centerAngle + sectorSize / 2.0D - 0.02D;
        int color = selected ? 0xFF3E9EA8 : entries[slot] == null ? 0xFF252525 : 0xFF4A4A4A;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        setColor(color);
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        for (int step = 0; step <= ARC_SEGMENTS; step++) {
            double progress = step / (double) ARC_SEGMENTS;
            double angle = startAngle + (endAngle - startAngle) * progress;
            GL11.glVertex2d(centerX + Math.cos(angle) * OUTER_RADIUS, centerY + Math.sin(angle) * OUTER_RADIUS);
            GL11.glVertex2d(centerX + Math.cos(angle) * INNER_RADIUS, centerY + Math.sin(angle) * INNER_RADIUS);
        }
        GL11.glEnd();
        GL11.glPopAttrib();
    }

    private void drawEntryLabel(int centerX, int centerY, int slot) {
        double angle = -Math.PI / 2.0D + slot * Math.PI * 2.0D / entries.length;
        int labelCenterX = centerX + (int) Math.round(Math.cos(angle) * LABEL_RADIUS);
        int labelCenterY = centerY + (int) Math.round(Math.sin(angle) * LABEL_RADIUS);
        int left = labelCenterX - LABEL_WIDTH / 2;
        int top = labelCenterY - LABEL_HEIGHT / 2;
        int background = slot == selectedSlot ? 0xE03E9EA8 : 0xD0202020;
        drawRect(left, top, left + LABEL_WIDTH, top + LABEL_HEIGHT, background);

        String label = entries[slot] == null ? "(empty)" : entries[slot].getDisplayName();
        label = fontRendererObj.trimStringToWidth(label, LABEL_WIDTH - 6);
        int color = entries[slot] == null ? 0x808080 : 0xFFFFFF;
        drawCenteredString(fontRendererObj, label, labelCenterX, labelCenterY - 4, color);
    }

    private void finish(boolean activateSelection) {
        if (finished) {
            return;
        }
        finished = true;

        String identifier = activateSelection && selectedSlot >= 0 ? Config.getRadialMenuEntry(selectedSlot) : "";
        mc.displayGuiScreen(null);
        if (mc.thePlayer != null) {
            mc.setIngameFocus();
        }
        if (!identifier.isEmpty()) {
            keyBindingController.pulseBinding(identifier);
        }
    }

    private static void setColor(int color) {
        float alpha = (color >>> 24 & 255) / 255.0F;
        float red = (color >>> 16 & 255) / 255.0F;
        float green = (color >>> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }
}
