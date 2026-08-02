package dev.gtnhcontroller.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.gtnhcontroller.client.input.NeiKeyBindingAdapterAccess;

/** Makes controller presses visible to GUI actions in newer NEI builds that poll raw keyboard state. */
@Pseudo
@Mixin(targets = "codechicken.nei.KeyManager", remap = false)
public abstract class MixinNeiKeyManagerControllerKeys {

    @Inject(
        method = { "isKeyDown(Ljava/lang/String;)Z", "isHashDown(Ljava/lang/String;)Z" },
        at = @At("HEAD"),
        cancellable = true,
        remap = false,
        require = 0)
    private static void gtnhcontroller$useVirtualKey(String identifier, CallbackInfoReturnable<Boolean> callback) {
        if (NeiKeyBindingAdapterAccess.isVirtualKeyDown(identifier)) {
            callback.setReturnValue(Boolean.TRUE);
        }
    }

    @Inject(
        method = "isHashDown(Ljava/lang/String;I)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = false,
        require = 0)
    private static void gtnhcontroller$useVirtualModifiedKey(String identifier, int modifiers,
        CallbackInfoReturnable<Boolean> callback) {
        if (NeiKeyBindingAdapterAccess.isVirtualKeyDown(identifier)) {
            callback.setReturnValue(Boolean.TRUE);
        }
    }
}
