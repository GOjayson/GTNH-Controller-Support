package dev.gtnhcontroller.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.gtnhcontroller.client.input.NeiKeyBindingAdapterAccess;

/** Makes virtual controller presses visible to the separate key registry in legacy NEI builds. */
@Pseudo
@Mixin(targets = "codechicken.nei.NEIClientConfig", remap = false)
public abstract class MixinNeiClientConfigControllerKeys {

    @Inject(
        method = "isKeyHashDown(Ljava/lang/String;)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = false,
        require = 0)
    private static void gtnhcontroller$useVirtualKey(String identifier, CallbackInfoReturnable<Boolean> callback) {
        if (NeiKeyBindingAdapterAccess.isVirtualKeyDown(identifier)) {
            callback.setReturnValue(Boolean.TRUE);
        }
    }
}
