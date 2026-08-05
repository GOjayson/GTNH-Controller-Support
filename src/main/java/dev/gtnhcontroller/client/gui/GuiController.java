package dev.gtnhcontroller.client.gui;

import static dev.gtnhcontroller.client.input.ControllerAction.GUI_ALTERNATE;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_BACK;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_CONFIRM;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_KEYBOARD;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_NAV_DOWN;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_NAV_LEFT;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_NAV_RIGHT;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_NAV_UP;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_PRECISION;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_QUICK_MOVE;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_SCROLL_DOWN;
import static dev.gtnhcontroller.client.input.ControllerAction.GUI_SCROLL_UP;
import static dev.gtnhcontroller.client.input.ControllerAxis.LEFT_X;
import static dev.gtnhcontroller.client.input.ControllerAxis.LEFT_Y;
import static dev.gtnhcontroller.client.input.ControllerAxis.RIGHT_X;
import static dev.gtnhcontroller.client.input.ControllerAxis.RIGHT_Y;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiScreenEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.GTNHController;
import dev.gtnhcontroller.client.input.ControllerAction;
import dev.gtnhcontroller.client.input.ControllerProfile;
import dev.gtnhcontroller.client.input.InputMath;
import dev.gtnhcontroller.client.input.SdlGamepadManager;
import dev.gtnhcontroller.client.input.StickVector;
import dev.gtnhcontroller.mixins.GuiScreenControllerAccessor;

/**
 * Maintains a controller-owned cursor in scaled GUI coordinates. While the controller owns it, the native cursor is
 * hidden and synchronized to the same position for mod GUIs that read LWJGL mouse coordinates directly.
 */
public final class GuiController {

    private static GuiController instance;

    private final SdlGamepadManager gamepadManager;
    private final ControllerProfile controllerProfile;
    private final GuiInputCompatibility inputCompatibility = new GuiInputCompatibility();
    private final NativeCursorManager nativeCursorManager = new NativeCursorManager();
    private final CursorWarpTracker cursorWarpTracker = new CursorWarpTracker();
    private final OnScreenKeyboardOverlay onScreenKeyboard = new OnScreenKeyboardOverlay(inputCompatibility);
    private final Map<ControllerAction, InputRepeatTimer> repeatTimers = new EnumMap<ControllerAction, InputRepeatTimer>(
        ControllerAction.class);
    private final AcceleratedInputRepeatTimer scrollUpRepeatTimer = new AcceleratedInputRepeatTimer();
    private final AcceleratedInputRepeatTimer scrollDownRepeatTimer = new AcceleratedInputRepeatTimer();
    private final CursorTrail cursorTrail = new CursorTrail();
    private GuiScreen activeScreen;
    private boolean leftHeld;
    private boolean rightHeld;
    private boolean leftBlockedUntilRelease;
    private boolean rightBlockedUntilRelease;
    private long leftPressStartedMillis;
    private long rightPressStartedMillis;
    private long lastCursorUpdateNanos;
    private int lastPhysicalMouseX;
    private int lastPhysicalMouseY;
    private float cursorX;
    private float cursorY;
    private float cursorVelocityX;
    private float cursorVelocityY;
    private boolean physicalMouseInitialized;
    private boolean cursorInitialized;
    private boolean controllerOwnsCursor;
    private boolean controllerInputCaptureActive;

    public GuiController(SdlGamepadManager gamepadManager, ControllerProfile controllerProfile) {
        this.gamepadManager = gamepadManager;
        this.controllerProfile = controllerProfile;
        instance = this;

        if (!"LEFT".equalsIgnoreCase(Config.cursorStick) && !"RIGHT".equalsIgnoreCase(Config.cursorStick)) {
            GTNHController.LOG.warn("Invalid GUI cursor stick '{}'; using RIGHT.", Config.cursorStick);
        }
    }

    /**
     * Called by the EntityRenderer Mixin for GuiScreen.drawScreen and Forge's matching GUI events.
     */
    public static int resolveMouseX(GuiScreen screen, int vanillaMouseX) {
        GuiController controller = instance;
        return controller != null && controller.shouldSubstituteCursor(screen) ? controller.cursorX(screen)
            : vanillaMouseX;
    }

    /**
     * Called by the EntityRenderer Mixin for GuiScreen.drawScreen and Forge's matching GUI events.
     */
    public static int resolveMouseY(GuiScreen screen, int vanillaMouseY) {
        GuiController controller = instance;
        return controller != null && controller.shouldSubstituteCursor(screen) ? controller.cursorY(screen)
            : vanillaMouseY;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (synchronizeScreen(minecraft)) {
            return;
        }

        if (!canControlGui(minecraft)) {
            releaseHeldButtons(minecraft, activeScreen);
            blockCurrentlyHeldButtons();
            resetRepeatTimers();
            closeOnScreenKeyboard();
            return;
        }
        if (onScreenKeyboard.isOpenFor(activeScreen)) {
            releaseHeldButtons(minecraft, activeScreen);
            onScreenKeyboard.update(controllerProfile, activeScreen);
            resetRepeatTimers();
            if (!onScreenKeyboard.isOpenFor(activeScreen)) {
                restoreNativeCursorWithoutControllerOwnership();
                blockCurrentlyHeldButtons();
            }
            return;
        }
        if (isControllerInputCaptured()) {
            releaseHeldButtons(minecraft, activeScreen);
            blockCurrentlyHeldButtons();
            resetRepeatTimers();
            return;
        }
        if (controllerProfile.wasPressed(GUI_KEYBOARD)) {
            releaseHeldButtons(minecraft, activeScreen);
            blockCurrentlyHeldButtons();
            resetRepeatTimers();
            onScreenKeyboard.open(activeScreen);
            return;
        }

        updateNavigation();
        updateScrolling();

        if (controllerProfile.wasPressed(GUI_QUICK_MOVE)) {
            releaseHeldButtons(minecraft, activeScreen);
            if (inputCompatibility.quickMove(minecraft, activeScreen, cursorX(activeScreen), cursorY(activeScreen))) {
                return;
            }
        }

        updateMouseButton(activeScreen, 0, controllerProfile.isDown(GUI_CONFIRM));
        if (minecraft.currentScreen != activeScreen) {
            return;
        }
        updateMouseButton(activeScreen, 1, controllerProfile.isDown(GUI_ALTERNATE));
        if (minecraft.currentScreen != activeScreen) {
            return;
        }

        if (controllerProfile.wasPressed(GUI_BACK)) {
            inputCompatibility.keyTyped(activeScreen, '\0', Keyboard.KEY_ESCAPE);
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        synchronizeScreen(minecraft);
        if (!canControlGui(minecraft)) {
            lastCursorUpdateNanos = System.nanoTime();
            controllerInputCaptureActive = false;
            clearCursor();
            return;
        }
        if (isControllerInputCaptured()) {
            lastCursorUpdateNanos = System.nanoTime();
            controllerInputCaptureActive = true;
            hideCursorForCapture();
            return;
        }
        if (controllerInputCaptureActive) {
            controllerInputCaptureActive = false;
            restoreNativeCursorWithoutControllerOwnership();
        }
        if (onScreenKeyboard.isOpenFor(activeScreen)) {
            lastCursorUpdateNanos = System.nanoTime();
            hideCursorForKeyboard();
            return;
        }

        updateCursor(minecraft, activeScreen);
        invokeDragCallbacks();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (onScreenKeyboard.isOpenFor(event.gui)) {
            onScreenKeyboard.draw(event.gui);
            return;
        }
        if (Config.showControllerPrompts && gamepadManager.isConnected()
            && !isControllerInputCaptured()
            && !(event.gui instanceof ControllerConfigurationScreen)) {
            drawControllerPrompts(event.gui);
        }
        if (!shouldSubstituteCursor(event.gui)) {
            return;
        }

        drawVirtualCursor(event.gui, cursorX(event.gui), cursorY(event.gui));
    }

    private void updateCursor(Minecraft minecraft, GuiScreen screen) {
        long currentTimeNanos = System.nanoTime();
        float elapsedSeconds = lastCursorUpdateNanos == 0L ? 0.0F
            : Math.min((currentTimeNanos - lastCursorUpdateNanos) / 1_000_000_000.0F, 0.05F);
        lastCursorUpdateNanos = currentTimeNanos;

        boolean useLeftStick = "LEFT".equalsIgnoreCase(Config.cursorStick);
        StickVector cursorInput = InputMath.applyRadialDeadZone(
            gamepadManager.getAxis(useLeftStick ? LEFT_X : RIGHT_X),
            gamepadManager.getAxis(useLeftStick ? LEFT_Y : RIGHT_Y),
            Config.cursorDeadZone,
            Config.cursorCurveExponent);
        float cursorInputX = Config.invertCursorX ? -cursorInput.x : cursorInput.x;
        float cursorInputY = Config.invertCursorY ? -cursorInput.y : cursorInput.y;
        boolean stickActive = cursorInput != StickVector.ZERO;
        boolean controllerCursorMoving = stickActive || !CursorMotion.isStopped(cursorVelocityX, cursorVelocityY);

        int physicalMouseX = Mouse.getX();
        int physicalMouseY = Mouse.getY();
        if (!cursorInitialized) {
            synchronizeCursorWithMouse(minecraft, screen, physicalMouseX, physicalMouseY);
        }
        if (!physicalMouseInitialized) {
            rememberPhysicalMouse(physicalMouseX, physicalMouseY);
        }

        boolean physicalMouseMoved = cursorWarpTracker.isPhysicalMovement(
            controllerOwnsCursor,
            controllerCursorMoving,
            physicalMouseX,
            physicalMouseY,
            lastPhysicalMouseX,
            lastPhysicalMouseY);
        rememberPhysicalMouse(physicalMouseX, physicalMouseY);
        if (physicalMouseMoved) {
            synchronizeCursorWithMouse(minecraft, screen, physicalMouseX, physicalMouseY);
            controllerOwnsCursor = false;
            cursorWarpTracker.reset();
            nativeCursorManager.restore();
            stopCursorMotion();
        }

        if (!stickActive && CursorMotion.isStopped(cursorVelocityX, cursorVelocityY)) {
            synchronizeNativeCursor(minecraft, screen);
            return;
        }

        controllerOwnsCursor = true;
        float horizontalScale = screen.width / (float) Math.max(minecraft.displayWidth, 1);
        float verticalScale = screen.height / (float) Math.max(minecraft.displayHeight, 1);
        float precisionScale = controllerProfile.isDown(GUI_PRECISION) ? Config.precisionCursorScale : 1.0F;
        float fullCursorSpeed = Config.cursorSpeed * Config.cursorSensitivity;
        float effectiveCursorSpeed = fullCursorSpeed * precisionScale;
        float effectiveAcceleration = CursorMotion.responseRate(Config.cursorAcceleration, Config.cursorSensitivity);
        float effectiveDeceleration = CursorMotion
            .decelerationRate(Config.cursorDeceleration, Config.cursorSensitivity, fullCursorSpeed);
        cursorVelocityX = CursorMotion.updateVelocity(
            cursorVelocityX,
            cursorInputX * effectiveCursorSpeed * horizontalScale,
            effectiveAcceleration * horizontalScale,
            effectiveDeceleration * horizontalScale,
            elapsedSeconds);
        cursorVelocityY = CursorMotion.updateVelocity(
            cursorVelocityY,
            cursorInputY * effectiveCursorSpeed * verticalScale,
            effectiveAcceleration * verticalScale,
            effectiveDeceleration * verticalScale,
            elapsedSeconds);

        float nextCursorX = cursorX + cursorVelocityX * elapsedSeconds;
        float nextCursorY = cursorY + cursorVelocityY * elapsedSeconds;
        cursorX = InputMath.clamp(nextCursorX, 0.0F, Math.max(screen.width - 1, 0));
        cursorY = InputMath.clamp(nextCursorY, 0.0F, Math.max(screen.height - 1, 0));
        if (cursorX != nextCursorX) {
            cursorVelocityX = 0.0F;
        }
        if (cursorY != nextCursorY) {
            cursorVelocityY = 0.0F;
        }
        if (Config.cursorTrail) {
            cursorTrail.record(Math.round(cursorX), Math.round(cursorY));
        } else {
            cursorTrail.clear();
        }
        synchronizeNativeCursor(minecraft, screen);
    }

    private void updateMouseButton(GuiScreen screen, int mouseButton, boolean controllerDown) {
        boolean blocked = mouseButton == 0 ? leftBlockedUntilRelease : rightBlockedUntilRelease;
        if (blocked) {
            if (!controllerDown) {
                setBlocked(mouseButton, false);
            }
            return;
        }

        boolean held = mouseButton == 0 ? leftHeld : rightHeld;
        if (controllerDown && !held) {
            if (Mouse.isButtonDown(mouseButton)) {
                setBlocked(mouseButton, true);
                return;
            }
            int mouseX = cursorX(screen);
            int mouseY = cursorY(screen);
            if (!inputCompatibility.interceptMousePressed(screen, mouseX, mouseY, mouseButton)) {
                accessor(screen).gtnhcontroller$mouseClicked(mouseX, mouseY, mouseButton);
            }
            inputCompatibility.mousePressed(screen, mouseX, mouseY, mouseButton);
            setHeld(mouseButton, true);
            setPressStarted(mouseButton, System.currentTimeMillis());
        } else if (!controllerDown && held) {
            if (!Mouse.isButtonDown(mouseButton)) {
                int mouseX = cursorX(screen);
                int mouseY = cursorY(screen);
                inputCompatibility.beforeMouseReleased(screen, mouseX, mouseY, mouseButton);
                if (!inputCompatibility.interceptMouseReleased(screen, mouseX, mouseY, mouseButton)) {
                    accessor(screen).gtnhcontroller$mouseMovedOrUp(mouseX, mouseY, mouseButton);
                }
                inputCompatibility.mouseReleased(screen, mouseX, mouseY, mouseButton);
            }
            setHeld(mouseButton, false);
        }
    }

    private void invokeDragCallbacks() {
        int mouseX = cursorX(activeScreen);
        int mouseY = cursorY(activeScreen);
        long currentTimeMillis = System.currentTimeMillis();

        if (leftHeld) {
            accessor(activeScreen)
                .gtnhcontroller$mouseClickMove(mouseX, mouseY, 0, currentTimeMillis - leftPressStartedMillis);
            inputCompatibility
                .mouseDragged(activeScreen, mouseX, mouseY, 0, currentTimeMillis - leftPressStartedMillis);
        }
        if (rightHeld) {
            accessor(activeScreen)
                .gtnhcontroller$mouseClickMove(mouseX, mouseY, 1, currentTimeMillis - rightPressStartedMillis);
            inputCompatibility
                .mouseDragged(activeScreen, mouseX, mouseY, 1, currentTimeMillis - rightPressStartedMillis);
        }
    }

    private boolean synchronizeScreen(Minecraft minecraft) {
        if (minecraft.currentScreen == activeScreen) {
            return false;
        }

        GuiScreen previousScreen = activeScreen;
        releaseHeldButtons(minecraft, previousScreen);
        closeOnScreenKeyboard();
        activeScreen = minecraft.currentScreen;
        stopCursorMotion();
        resetRepeatTimers();
        lastCursorUpdateNanos = System.nanoTime();
        blockCurrentlyHeldButtons();

        if (activeScreen == null) {
            clearCursor();
        } else if (controllerOwnsCursor && cursorInitialized) {
            cursorX = InputMath.clamp(cursorX, 0.0F, Math.max(activeScreen.width - 1, 0));
            cursorY = InputMath.clamp(cursorY, 0.0F, Math.max(activeScreen.height - 1, 0));
            rememberPhysicalMouse(Mouse.getX(), Mouse.getY());
        } else {
            synchronizeCursorWithMouse(minecraft, activeScreen, Mouse.getX(), Mouse.getY());
            rememberPhysicalMouse(Mouse.getX(), Mouse.getY());
        }
        return true;
    }

    private void updateNavigation() {
        long currentTimeMillis = System.currentTimeMillis();
        boolean navigateUp = shouldActivate(GUI_NAV_UP, currentTimeMillis);
        boolean navigateDown = shouldActivate(GUI_NAV_DOWN, currentTimeMillis);
        boolean navigateLeft = shouldActivate(GUI_NAV_LEFT, currentTimeMillis);
        boolean navigateRight = shouldActivate(GUI_NAV_RIGHT, currentTimeMillis);
        if (!Config.enableButtonNavigation && !Config.enableSlotNavigation) {
            return;
        }

        int directionX = 0;
        int directionY = 0;
        if (navigateUp) {
            directionY = -1;
        } else if (navigateDown) {
            directionY = 1;
        } else if (navigateLeft) {
            directionX = -1;
        } else if (navigateRight) {
            directionX = 1;
        } else {
            return;
        }

        List<GuiNavigationTarget> targets = GuiNavigationTargets
            .collect(activeScreen, Config.enableButtonNavigation, Config.enableSlotNavigation);
        GuiNavigationTarget target = DirectionalNavigation
            .findNext(cursorX(activeScreen), cursorY(activeScreen), directionX, directionY, targets);
        if (target != null) {
            moveCursorTo(target.x, target.y);
        }
    }

    private void updateScrolling() {
        long currentTimeMillis = System.currentTimeMillis();
        boolean scrollUp = shouldActivateScroll(GUI_SCROLL_UP, scrollUpRepeatTimer, currentTimeMillis);
        boolean scrollDown = shouldActivateScroll(GUI_SCROLL_DOWN, scrollDownRepeatTimer, currentTimeMillis);
        if (scrollUp == scrollDown) {
            return;
        }

        int mouseX = cursorX(activeScreen);
        int mouseY = cursorY(activeScreen);
        inputCompatibility.scroll(activeScreen, mouseX, mouseY, scrollUp ? -1 : 1);
    }

    private boolean shouldActivate(ControllerAction action, long currentTimeMillis) {
        InputRepeatTimer repeatTimer = repeatTimers.get(action);
        if (repeatTimer == null) {
            repeatTimer = new InputRepeatTimer();
            repeatTimers.put(action, repeatTimer);
        }
        return repeatTimer.shouldActivate(
            controllerProfile.isDown(action),
            currentTimeMillis,
            Config.navigationInitialDelayMillis,
            Config.navigationRepeatIntervalMillis);
    }

    private boolean shouldActivateScroll(ControllerAction action, AcceleratedInputRepeatTimer repeatTimer,
        long currentTimeMillis) {
        return repeatTimer.shouldActivate(
            controllerProfile.isDown(action),
            currentTimeMillis,
            Config.navigationInitialDelayMillis,
            Config.navigationRepeatIntervalMillis,
            Config.scrollAccelerationEnabled,
            Config.scrollAccelerationMultiplier);
    }

    private void moveCursorTo(int x, int y) {
        cursorX = InputMath.clamp(x, 0.0F, Math.max(activeScreen.width - 1, 0));
        cursorY = InputMath.clamp(y, 0.0F, Math.max(activeScreen.height - 1, 0));
        cursorInitialized = true;
        controllerOwnsCursor = true;
        if (Config.cursorTrail) {
            cursorTrail.record(Math.round(cursorX), Math.round(cursorY));
        }
        stopCursorMotion();
    }

    private void resetRepeatTimers() {
        for (InputRepeatTimer repeatTimer : repeatTimers.values()) {
            repeatTimer.reset();
        }
        scrollUpRepeatTimer.reset();
        scrollDownRepeatTimer.reset();
    }

    private void releaseHeldButtons(Minecraft minecraft, GuiScreen screen) {
        boolean screenChanged = screen != minecraft.currentScreen;
        if (screen != null && leftHeld && (screenChanged || !Mouse.isButtonDown(0))) {
            int mouseX = cursorX(screen);
            int mouseY = cursorY(screen);
            inputCompatibility.beforeMouseReleased(screen, mouseX, mouseY, 0);
            if (!inputCompatibility.interceptMouseReleased(screen, mouseX, mouseY, 0)) {
                accessor(screen).gtnhcontroller$mouseMovedOrUp(mouseX, mouseY, 0);
            }
            inputCompatibility.mouseReleased(screen, mouseX, mouseY, 0);
        }
        if (screen != null && rightHeld && (screenChanged || !Mouse.isButtonDown(1))) {
            int mouseX = cursorX(screen);
            int mouseY = cursorY(screen);
            inputCompatibility.beforeMouseReleased(screen, mouseX, mouseY, 1);
            if (!inputCompatibility.interceptMouseReleased(screen, mouseX, mouseY, 1)) {
                accessor(screen).gtnhcontroller$mouseMovedOrUp(mouseX, mouseY, 1);
            }
            inputCompatibility.mouseReleased(screen, mouseX, mouseY, 1);
        }
        leftHeld = false;
        rightHeld = false;
    }

    private void blockCurrentlyHeldButtons() {
        leftBlockedUntilRelease = controllerProfile.isDown(GUI_CONFIRM);
        rightBlockedUntilRelease = controllerProfile.isDown(GUI_ALTERNATE);
    }

    private boolean canControlGui(Minecraft minecraft) {
        boolean configurationScreen = minecraft.currentScreen instanceof ControllerConfigurationScreen;
        return (Config.enableGuiControls || configurationScreen) && gamepadManager.isConnected()
            && minecraft.currentScreen != null
            && minecraft.currentScreen == activeScreen
            && minecraft.displayWidth > 0
            && minecraft.displayHeight > 0
            && activeScreen.width > 0
            && activeScreen.height > 0;
    }

    private boolean isControllerInputCaptured() {
        return activeScreen instanceof ControllerInputCaptureScreen
            && ((ControllerInputCaptureScreen) activeScreen).isCapturingControllerInput();
    }

    private boolean shouldSubstituteCursor(GuiScreen screen) {
        Minecraft minecraft = Minecraft.getMinecraft();
        return controllerOwnsCursor && cursorInitialized
            && screen != null
            && screen == activeScreen
            && screen == minecraft.currentScreen
            && !onScreenKeyboard.isOpenFor(screen)
            && canControlGui(minecraft);
    }

    private int cursorX(GuiScreen screen) {
        if (controllerOwnsCursor && cursorInitialized) {
            return Math.round(InputMath.clamp(cursorX, 0.0F, Math.max(screen.width - 1, 0)));
        }
        return GuiCoordinateMath.toGuiX(Mouse.getX(), screen.width, Minecraft.getMinecraft().displayWidth);
    }

    private int cursorY(GuiScreen screen) {
        if (controllerOwnsCursor && cursorInitialized) {
            return Math.round(InputMath.clamp(cursorY, 0.0F, Math.max(screen.height - 1, 0)));
        }
        return GuiCoordinateMath.toGuiY(Mouse.getY(), screen.height, Minecraft.getMinecraft().displayHeight);
    }

    private void synchronizeCursorWithMouse(Minecraft minecraft, GuiScreen screen, int physicalMouseX,
        int physicalMouseY) {
        cursorX = GuiCoordinateMath.toGuiX(physicalMouseX, screen.width, minecraft.displayWidth);
        cursorY = GuiCoordinateMath.toGuiY(physicalMouseY, screen.height, minecraft.displayHeight);
        cursorInitialized = true;
    }

    private void rememberPhysicalMouse(int mouseX, int mouseY) {
        lastPhysicalMouseX = mouseX;
        lastPhysicalMouseY = mouseY;
        physicalMouseInitialized = true;
    }

    private void stopCursorMotion() {
        cursorVelocityX = 0.0F;
        cursorVelocityY = 0.0F;
    }

    private void clearCursor() {
        nativeCursorManager.restore();
        stopCursorMotion();
        physicalMouseInitialized = false;
        cursorInitialized = false;
        controllerOwnsCursor = false;
        cursorWarpTracker.reset();
        cursorTrail.clear();
    }

    private void hideCursorForCapture() {
        nativeCursorManager.hide();
        stopCursorMotion();
        physicalMouseInitialized = false;
        cursorInitialized = false;
        controllerOwnsCursor = false;
        cursorWarpTracker.reset();
        cursorTrail.clear();
    }

    private void hideCursorForKeyboard() {
        nativeCursorManager.hide();
        stopCursorMotion();
    }

    private void closeOnScreenKeyboard() {
        boolean wasOpen = onScreenKeyboard.isOpenFor(activeScreen);
        onScreenKeyboard.close();
        if (wasOpen) {
            restoreNativeCursorWithoutControllerOwnership();
        }
    }

    private void restoreNativeCursorWithoutControllerOwnership() {
        if (!controllerOwnsCursor) {
            nativeCursorManager.restore();
        }
    }

    private void synchronizeNativeCursor(Minecraft minecraft, GuiScreen screen) {
        if (!controllerOwnsCursor || !Mouse.isCreated()) {
            return;
        }

        nativeCursorManager.hide();
        int displayX = GuiCoordinateMath.toDisplayX(cursorX, screen.width, minecraft.displayWidth);
        int displayY = GuiCoordinateMath.toNativeCursorY(cursorY, screen.height, minecraft.displayHeight);
        if (cursorWarpTracker.needsWarp(displayX, displayY)) {
            Mouse.setCursorPosition(displayX, displayY);
            rememberPhysicalMouse(displayX, displayY);
            cursorWarpTracker.recordWarp(displayX, displayY);
        }
    }

    private void drawVirtualCursor(GuiScreen screen, int mouseX, int mouseY) {
        int dark = 0xFF10242A;
        int light = 0xFF7FFFFF;
        int previousMatrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, screen.width, screen.height, 0.0D, -1000.0D, 1000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        if (Config.cursorTrail) {
            drawCursorTrail();
        }
        int scale = Config.largeCursor ? 2 : 1;
        Gui.drawRect(mouseX - scale, mouseY - scale, mouseX + 3 * scale, mouseY + 12 * scale, dark);
        Gui.drawRect(mouseX - scale, mouseY - scale, mouseX + 9 * scale, mouseY + 3 * scale, dark);
        Gui.drawRect(mouseX, mouseY, mouseX + scale, mouseY + 10 * scale, light);
        Gui.drawRect(mouseX, mouseY, mouseX + 7 * scale, mouseY + scale, light);

        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(previousMatrixMode);
        GL11.glPopAttrib();
    }

    private void drawCursorTrail() {
        List<CursorTrail.Point> points = cursorTrail.getPoints();
        for (int index = 0; index < points.size(); index++) {
            CursorTrail.Point point = points.get(index);
            int alpha = 32 + 112 * (index + 1) / Math.max(points.size(), 1);
            int color = alpha << 24 | 0x007FFFFF;
            int radius = Config.largeCursor ? 2 : 1;
            Gui.drawRect(point.x - radius, point.y - radius, point.x + radius + 1, point.y + radius + 1, color);
        }
    }

    private void drawControllerPrompts(GuiScreen screen) {
        StringBuilder prompts = new StringBuilder();
        appendPrompt(prompts, Config.guiConfirmBinding, "Select");
        appendPrompt(prompts, Config.guiBackBinding, "Back");
        boolean containerScreen = screen instanceof GuiContainer;
        if (containerScreen) {
            appendPrompt(prompts, Config.guiQuickMoveBinding, "Quick Move");
        }
        appendPrompt(prompts, Config.guiKeyboardBinding, "Keyboard");
        String promptText = Minecraft.getMinecraft().fontRenderer
            .trimStringToWidth(prompts.toString(), Math.max(screen.width - 8, 1));
        int promptY = containerScreen ? screen.height - 12 : 2;
        Gui.drawRect(0, promptY - 2, screen.width, promptY + 12, 0xB0101820);
        int promptX = (screen.width - Minecraft.getMinecraft().fontRenderer.getStringWidth(promptText)) / 2;
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(promptText, promptX, promptY, 0xFFFFFF);
        if (containerScreen && gamepadManager.getBatteryStatus()
            .isAvailable()) {
            String battery = "Controller: " + gamepadManager.getBatteryStatus()
                .getDisplayText();
            int batteryX = screen.width - Minecraft.getMinecraft().fontRenderer.getStringWidth(battery) - 5;
            Gui.drawRect(batteryX - 3, 3, screen.width - 2, 16, 0xA0101820);
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(battery, batteryX, 5, 0xFFFFFF);
        }
    }

    private static void appendPrompt(StringBuilder prompts, String binding, String action) {
        if ("NONE".equalsIgnoreCase(binding)) {
            return;
        }
        if (prompts.length() > 0) {
            prompts.append("   ");
        }
        prompts.append('[')
            .append(ControllerPromptFormatter.format(binding))
            .append("] ")
            .append(action);
    }

    private void setHeld(int mouseButton, boolean held) {
        if (mouseButton == 0) {
            leftHeld = held;
        } else {
            rightHeld = held;
        }
    }

    private void setBlocked(int mouseButton, boolean blocked) {
        if (mouseButton == 0) {
            leftBlockedUntilRelease = blocked;
        } else {
            rightBlockedUntilRelease = blocked;
        }
    }

    private void setPressStarted(int mouseButton, long currentTimeMillis) {
        if (mouseButton == 0) {
            leftPressStartedMillis = currentTimeMillis;
        } else {
            rightPressStartedMillis = currentTimeMillis;
        }
    }

    private GuiScreenControllerAccessor accessor(GuiScreen screen) {
        return (GuiScreenControllerAccessor) (Object) screen;
    }
}
