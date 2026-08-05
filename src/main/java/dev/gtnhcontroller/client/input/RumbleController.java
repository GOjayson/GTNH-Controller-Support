package dev.gtnhcontroller.client.input;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.mixins.KeyBindingControllerAccessor;

/**
 * Produces optional client-only haptic feedback. SDL capability checks and effect priority are owned by the device
 * layer so unsupported controllers and overlapping events remain harmless.
 */
public final class RumbleController {

    private static final int MINING_PULSE_INTERVAL_TICKS = 5;
    private static final int LOW_HEALTH_PULSE_INTERVAL_TICKS = 100;
    private static final float LOW_HEALTH_THRESHOLD = 6.0F;

    private final SdlGamepadManager gamepadManager;

    private EntityClientPlayerMP observedPlayer;
    private float previousHealth;
    private int previousHurtTime;
    private int miningPulseTicks;
    private int lowHealthPulseTicks;

    public RumbleController(SdlGamepadManager gamepadManager) {
        this.gamepadManager = gamepadManager;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityClientPlayerMP player = minecraft.thePlayer;
        if (player != observedPlayer) {
            observedPlayer = player;
            previousHealth = player == null ? 0.0F : player.getHealth();
            previousHurtTime = player == null ? 0 : player.hurtTime;
            miningPulseTicks = 0;
            lowHealthPulseTicks = 0;
            return;
        }
        if (player == null) {
            return;
        }

        detectDamage(player);
        updateLowHealth(player);
        updateMining(minecraft);
        previousHealth = player.getHealth();
        previousHurtTime = player.hurtTime;
    }

    @SubscribeEvent
    public void onSoundPlayed(PlaySoundEvent17 event) {
        if (!Config.rumbleEnabled || event.name == null || event.result == null) {
            return;
        }

        String soundName = event.name.toLowerCase(Locale.ROOT);
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.thePlayer == null) {
            return;
        }
        ISound sound = event.result;
        if ((soundName.contains("explode") || soundName.contains("explosion")) && Config.rumbleExplosions) {
            rumbleExplosion(minecraft, sound);
        }
        if (soundName.contains("splash") && Config.rumbleFishing
            && minecraft.thePlayer.fishEntity != null
            && minecraft.thePlayer.fishEntity.getDistanceSq(sound.getXPosF(), sound.getYPosF(), sound.getZPosF())
                <= 36.0D) {
            float intensity = Config.rumbleIntensity;
            gamepadManager.playRumble(0.25F * intensity, 0.65F * intensity, 220, RumbleEffect.FISHING);
        }
    }

    private void rumbleExplosion(Minecraft minecraft, ISound sound) {
        double distance = Math
            .sqrt(minecraft.thePlayer.getDistanceSq(sound.getXPosF(), sound.getYPosF(), sound.getZPosF()));
        if (distance > 64.0D) {
            return;
        }
        float distanceScale = (float) Math.max(0.25D, 1.0D - distance / 64.0D);
        float intensity = Config.rumbleIntensity * distanceScale;
        gamepadManager.playRumble(0.90F * intensity, 0.55F * intensity, 500, RumbleEffect.EXPLOSION);
    }

    private void updateLowHealth(EntityClientPlayerMP player) {
        if (player.getHealth() <= 0.0F || player.getHealth() > LOW_HEALTH_THRESHOLD) {
            lowHealthPulseTicks = 0;
            return;
        }
        if (!Config.rumbleEnabled || !Config.rumbleLowHealth) {
            return;
        }
        boolean crossedThreshold = previousHealth > LOW_HEALTH_THRESHOLD;
        if (crossedThreshold || lowHealthPulseTicks <= 0) {
            float intensity = Config.rumbleIntensity;
            gamepadManager.playRumble(0.35F * intensity, 0.12F * intensity, 180, RumbleEffect.LOW_HEALTH);
            lowHealthPulseTicks = LOW_HEALTH_PULSE_INTERVAL_TICKS;
        } else {
            lowHealthPulseTicks--;
        }
    }

    private void detectDamage(EntityClientPlayerMP player) {
        boolean newHurt = player.hurtTime > 0 && previousHurtTime <= 0;
        float healthLost = Math.max(previousHealth - player.getHealth(), 0.0F);
        if (!newHurt && healthLost <= 0.0F) {
            return;
        }
        if (!Config.rumbleEnabled || !Config.rumbleDamage) {
            return;
        }

        float damageScale = InputMath.clamp(0.35F + healthLost / 12.0F, 0.35F, 1.0F) * Config.rumbleIntensity;
        gamepadManager.playRumble(0.75F * damageScale, 0.45F * damageScale, 280, RumbleEffect.DAMAGE);
    }

    private void updateMining(Minecraft minecraft) {
        boolean attackHeld = false;
        if (minecraft.gameSettings != null) {
            KeyBindingControllerAccessor attackBinding = (KeyBindingControllerAccessor) (Object) minecraft.gameSettings.keyBindAttack;
            attackHeld = attackBinding.gtnhcontroller$isPressed();
        }
        boolean blockTargeted = minecraft.objectMouseOver != null
            && minecraft.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
        boolean mining = Config.rumbleEnabled && Config.rumbleMining
            && minecraft.currentScreen == null
            && minecraft.inGameHasFocus
            && attackHeld
            && blockTargeted;
        if (!mining) {
            miningPulseTicks = 0;
            return;
        }

        if (miningPulseTicks <= 0) {
            float intensity = Config.rumbleIntensity;
            gamepadManager.playRumble(0.13F * intensity, 0.04F * intensity, 65, RumbleEffect.MINING);
            miningPulseTicks = MINING_PULSE_INTERVAL_TICKS;
        } else {
            miningPulseTicks--;
        }
    }
}
