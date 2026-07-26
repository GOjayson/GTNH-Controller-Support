package dev.gtnhcontroller.mixins;

import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.gtnhcontroller.client.input.GameplayController;

/**
 * Adds analog controller values after vanilla reads the keyboard. Forge 1.7.10 does not yet expose the
 * InputUpdateEvent used by newer controller mods.
 */
@Mixin(MovementInputFromOptions.class)
public abstract class MixinMovementInputFromOptions extends MovementInput {

    @Inject(method = "updatePlayerMoveState", at = @At("RETURN"))
    private void gtnhcontroller$applyAnalogMovement(CallbackInfo callbackInfo) {
        GameplayController.applyAnalogMovement(this);
    }
}
