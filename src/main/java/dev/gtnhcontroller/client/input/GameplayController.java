package dev.gtnhcontroller.client.input;

import static dev.gtnhcontroller.client.input.ControllerAction.ATTACK;
import static dev.gtnhcontroller.client.input.ControllerAction.HOTBAR_NEXT;
import static dev.gtnhcontroller.client.input.ControllerAction.HOTBAR_PREVIOUS;
import static dev.gtnhcontroller.client.input.ControllerAction.JUMP;
import static dev.gtnhcontroller.client.input.ControllerAction.OPEN_INVENTORY;
import static dev.gtnhcontroller.client.input.ControllerAction.PAUSE;
import static dev.gtnhcontroller.client.input.ControllerAction.SNEAK;
import static dev.gtnhcontroller.client.input.ControllerAction.SPRINT;
import static dev.gtnhcontroller.client.input.ControllerAction.USE;
import static dev.gtnhcontroller.client.input.ControllerAxis.LEFT_X;
import static dev.gtnhcontroller.client.input.ControllerAxis.LEFT_Y;
import static dev.gtnhcontroller.client.input.ControllerAxis.RIGHT_X;
import static dev.gtnhcontroller.client.input.ControllerAxis.RIGHT_Y;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovementInput;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import dev.gtnhcontroller.Config;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Translates the active controller profile into conservative vanilla Minecraft gameplay controls. GUI input is kept
 * in a separate controller so each context can evolve independently.
 */
public final class GameplayController {

    private static final int AUTO_SWIM_SURFACE_GRACE_TICKS = 10;

    private static GameplayController instance;

    private final SdlGamepadManager gamepadManager;
    private final ControllerProfile controllerProfile;
    private final ActivationModeState sneakActivation = new ActivationModeState();
    private final ActivationModeState sprintActivation = new ActivationModeState();
    private final ActivationModeState attackActivation = new ActivationModeState();
    private final ActivationModeState useActivation = new ActivationModeState();

    private boolean jumpHeld;
    private boolean autoSwimUp;
    private boolean autoSwimSuppressJumpUntilRelease;
    private int autoSwimDryTicks;
    private boolean sneakHeld;
    private boolean sprintHeld;
    private boolean sprintControllerOwned;
    private boolean attackHeld;
    private boolean useHeld;
    private long lastCameraUpdateNanos;

    public GameplayController(SdlGamepadManager gamepadManager, ControllerProfile controllerProfile) {
        this.gamepadManager = gamepadManager;
        this.controllerProfile = controllerProfile;
        instance = this;
    }

    /**
     * Called after vanilla has populated its keyboard movement state. The Mixin hook lets controller movement remain
     * truly analog on Forge 1.7.10, which predates Forge's InputUpdateEvent.
     */
    public static void applyAnalogMovement(MovementInput movementInput) {
        if (instance != null) {
            instance.updateAnalogMovement(movementInput);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            updateActions();
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            updateCamera();
        }
    }

    private void updateActions() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!canControlGameplay(minecraft)) {
            releaseControlledBindings(minecraft);
            return;
        }

        if (controllerProfile.wasPressed(PAUSE)) {
            releaseControlledBindings(minecraft);
            minecraft.displayInGameMenu();
            return;
        }

        if (controllerProfile.wasPressed(OPEN_INVENTORY)) {
            releaseControlledBindings(minecraft);
            minecraft.getNetHandler()
                .addToSendQueue(
                    new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            minecraft.displayGuiScreen(new GuiInventory(minecraft.thePlayer));
            return;
        }

        boolean jumpPressed = controllerProfile.wasPressed(JUMP);
        boolean jumpDown = controllerProfile.isDown(JUMP);
        boolean inWater = minecraft.thePlayer.isInWater();
        if (inWater) {
            autoSwimDryTicks = 0;
        } else if (autoSwimUp) {
            autoSwimDryTicks++;
        }

        boolean toggleSwim = Config.swimActivationMode == ActivationMode.TOGGLE;
        boolean wasAutoSwimUp = autoSwimUp;
        autoSwimUp = MovementAssistance.updateAutoSwim(
            toggleSwim,
            inWater,
            minecraft.thePlayer.onGround,
            jumpPressed,
            autoSwimUp,
            autoSwimDryTicks <= AUTO_SWIM_SURFACE_GRACE_TICKS);
        if (wasAutoSwimUp && jumpPressed && !autoSwimUp) {
            autoSwimSuppressJumpUntilRelease = true;
        }
        if (!jumpDown) {
            autoSwimSuppressJumpUntilRelease = false;
        }
        if (!autoSwimUp && !autoSwimSuppressJumpUntilRelease) {
            autoSwimDryTicks = 0;
        }

        boolean autoSwimContext = toggleSwim
            && (inWater || autoSwimUp || autoSwimSuppressJumpUntilRelease);
        boolean controllerJump = autoSwimContext ? autoSwimUp : jumpDown || shouldAutoJump(minecraft.thePlayer);
        boolean controllerSneak = sneakActivation.update(
            Config.sneakActivationMode,
            controllerProfile.isDown(SNEAK),
            controllerProfile.wasPressed(SNEAK));
        boolean controllerSprint = sprintActivation.update(
            Config.sprintActivationMode,
            controllerProfile.isDown(SPRINT),
            controllerProfile.wasPressed(SPRINT));
        boolean controllerAttack = attackActivation.update(
            Config.attackActivationMode,
            controllerProfile.isDown(ATTACK),
            controllerProfile.wasPressed(ATTACK));
        boolean controllerUse = useActivation.update(
            Config.useActivationMode,
            controllerProfile.isDown(USE),
            controllerProfile.wasPressed(USE));
        jumpHeld = updateBinding(minecraft.gameSettings.keyBindJump, jumpHeld, controllerJump);
        sneakHeld = updateBinding(minecraft.gameSettings.keyBindSneak, sneakHeld, controllerSneak);
        sprintHeld = controllerSprint;
        attackHeld = updateBinding(minecraft.gameSettings.keyBindAttack, attackHeld, controllerAttack);
        useHeld = updateBinding(minecraft.gameSettings.keyBindUseItem, useHeld, controllerUse);

        if (controllerProfile.wasPressed(HOTBAR_PREVIOUS)) {
            minecraft.thePlayer.inventory.changeCurrentItem(1);
        }
        if (controllerProfile.wasPressed(HOTBAR_NEXT)) {
            minecraft.thePlayer.inventory.changeCurrentItem(-1);
        }
    }

    private void updateAnalogMovement(MovementInput movementInput) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!canControlGameplay(minecraft)) {
            return;
        }

        movementInput.sneak = MovementAssistance.mergeSneak(
            movementInput.sneak,
            sneakHeld);
        StickVector movement = InputMath.applyMovementResponse(movementStick(), Config.moveSensitivity);
        float forward = -movement.y;
        float strafe = -movement.x;

        if (movementInput.sneak) {
            forward *= 0.3F;
            strafe *= 0.3F;
        }

        movementInput.moveForward = InputMath.mergeAxisByMagnitude(movementInput.moveForward, forward);
        movementInput.moveStrafe = InputMath.mergeAxisByMagnitude(movementInput.moveStrafe, strafe);
        updateSprint(minecraft.thePlayer, movementInput);
    }

    private void updateCamera() {
        long currentTimeNanos = System.nanoTime();
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!canControlGameplay(minecraft)) {
            lastCameraUpdateNanos = currentTimeNanos;
            return;
        }

        float elapsedSeconds = lastCameraUpdateNanos == 0L ? 0.0F
            : Math.min((currentTimeNanos - lastCameraUpdateNanos) / 1_000_000_000.0F, 0.1F);
        lastCameraUpdateNanos = currentTimeNanos;
        if (elapsedSeconds <= 0.0F) {
            return;
        }

        StickVector look = InputMath.applyRadialDeadZone(
            gamepadManager.getAxis(RIGHT_X),
            gamepadManager.getAxis(RIGHT_Y),
            Config.lookDeadZone,
            Config.lookCurveExponent);
        float pitchDirection = Config.invertLookY ? -1.0F : 1.0F;

        float effectiveLookSpeed = Config.lookSpeed * Config.lookSensitivity;
        minecraft.thePlayer.rotationYaw += look.x * effectiveLookSpeed * elapsedSeconds;
        minecraft.thePlayer.rotationPitch = InputMath.clamp(
            minecraft.thePlayer.rotationPitch + look.y * effectiveLookSpeed * pitchDirection * elapsedSeconds,
            -90.0F,
            90.0F);
    }

    private boolean canControlGameplay(Minecraft minecraft) {
        return Config.enableGameplayControls && gamepadManager.isConnected()
            && minecraft.thePlayer != null
            && minecraft.currentScreen == null
            && minecraft.inGameHasFocus;
    }

    private void releaseControlledBindings(Minecraft minecraft) {
        autoSwimUp = false;
        autoSwimSuppressJumpUntilRelease = false;
        autoSwimDryTicks = 0;
        sneakActivation.reset();
        sprintActivation.reset();
        attackActivation.reset();
        useActivation.reset();
        if (minecraft.gameSettings == null) {
            sprintHeld = false;
            sprintControllerOwned = false;
            return;
        }

        if (sprintControllerOwned
            && minecraft.thePlayer != null
            && !isPhysicalInputDown(minecraft.gameSettings.keyBindSprint)) {
            minecraft.thePlayer.setSprinting(false);
        }
        sprintHeld = false;
        sprintControllerOwned = false;
        jumpHeld = updateBinding(minecraft.gameSettings.keyBindJump, jumpHeld, false);
        sneakHeld = updateBinding(minecraft.gameSettings.keyBindSneak, sneakHeld, false);
        attackHeld = updateBinding(minecraft.gameSettings.keyBindAttack, attackHeld, false);
        useHeld = updateBinding(minecraft.gameSettings.keyBindUseItem, useHeld, false);
    }

    private boolean updateBinding(KeyBinding binding, boolean wasControllerDown, boolean controllerDown) {
        if (controllerDown && !wasControllerDown) {
            KeyBinding.onTick(binding.getKeyCode());
        }

        if (wasControllerDown || controllerDown) {
            KeyBinding.setKeyBindState(binding.getKeyCode(), controllerDown || isPhysicalInputDown(binding));
        }

        return controllerDown;
    }

    private void updateSprint(EntityPlayer player, MovementInput movementInput) {
        boolean canStart = movementInput.moveForward >= 0.8F
            && !movementInput.sneak
            && !player.isCollidedHorizontally
            && player.ridingEntity == null
            && !player.isUsingItem()
            && (player.getFoodStats()
                .getFoodLevel() > 6 || player.capabilities.allowFlying)
            && !player.isPotionActive(Potion.blindness);
        SprintControl.Decision decision =
            SprintControl.decide(Config.sprintActivationMode, sprintHeld, sprintControllerOwned, canStart);
        switch (decision) {
            case START:
                player.setSprinting(true);
                sprintControllerOwned = Config.sprintActivationMode != ActivationMode.PRESS;
                break;
            case STOP:
                if (!isPhysicalInputDown(Minecraft.getMinecraft().gameSettings.keyBindSprint)) {
                    player.setSprinting(false);
                }
                sprintControllerOwned = false;
                break;
            case UNCHANGED:
                break;
            default:
                throw new IllegalStateException("Unknown sprint decision: " + decision);
        }
    }

    private boolean isPhysicalInputDown(KeyBinding binding) {
        int keyCode = binding.getKeyCode();
        if (keyCode < 0) {
            int mouseButton = keyCode + 100;
            return mouseButton >= 0 && Mouse.isButtonDown(mouseButton);
        }
        return keyCode > 0 && Keyboard.isKeyDown(keyCode);
    }

    private boolean shouldAutoJump(EntityPlayer player) {
        StickVector movement = movementStick();
        boolean moving = movement != StickVector.ZERO;
        boolean inLiquid = player.isInWater() || player.handleLavaMovement();
        boolean preliminaryDecision = MovementAssistance.shouldAutoJump(
            Config.autoJump,
            moving,
            player.onGround,
            player.isCollidedHorizontally,
            inLiquid,
            player.isSneaking(),
            player.ridingEntity != null,
            true);
        return preliminaryDecision && hasStepClearance(player, movement);
    }

    private boolean hasStepClearance(EntityPlayer player, StickVector movement) {
        float forward = -movement.y;
        float strafe = -movement.x;
        double magnitude = Math.sqrt(forward * forward + strafe * strafe);
        if (magnitude <= 0.0001D) {
            return false;
        }

        double yawRadians = Math.toRadians(player.rotationYaw);
        double offsetX = (strafe * Math.cos(yawRadians) - forward * Math.sin(yawRadians)) / magnitude * 0.2D;
        double offsetZ = (forward * Math.cos(yawRadians) + strafe * Math.sin(yawRadians)) / magnitude * 0.2D;
        AxisAlignedBB clearance = player.boundingBox.getOffsetBoundingBox(offsetX, 1.0D, offsetZ);
        return player.worldObj.getCollidingBoundingBoxes(player, clearance)
            .isEmpty();
    }

    private StickVector movementStick() {
        return InputMath.applyRadialDeadZone(
            gamepadManager.getAxis(LEFT_X),
            gamepadManager.getAxis(LEFT_Y),
            Config.moveDeadZone,
            Config.moveCurveExponent);
    }
}
