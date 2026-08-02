package dev.gtnhcontroller.client.input;

/** Public Mixin bridge without exposing the reflection adapter itself as public API. */
public final class NeiKeyBindingAdapterAccess {

    private NeiKeyBindingAdapterAccess() {}

    public static boolean isVirtualKeyDown(String neiIdentifier) {
        return NeiKeyBindingAdapter.isVirtualKeyDown(neiIdentifier);
    }
}
