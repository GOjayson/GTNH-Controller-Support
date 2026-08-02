package dev.gtnhcontroller.client.input;

import net.minecraft.client.settings.KeyBinding;

/** One action discovered from either NEI's legacy option tree or its newer KeyBinding map. */
final class NeiKeyBinding {

    final String neiIdentifier;
    final String controllerIdentifier;
    final String categoryKey;
    final String descriptionKey;
    final boolean legacy;
    final boolean modifierAware;
    final KeyBinding keyBinding;

    NeiKeyBinding(String neiIdentifier, String categoryKey, String descriptionKey, boolean legacy,
        boolean modifierAware, KeyBinding keyBinding) {
        this.neiIdentifier = neiIdentifier;
        this.controllerIdentifier = NeiKeyBindingIdentifier.create(neiIdentifier);
        this.categoryKey = categoryKey;
        this.descriptionKey = descriptionKey;
        this.legacy = legacy;
        this.modifierAware = modifierAware;
        this.keyBinding = keyBinding;
    }

    boolean sameRegistryEntry(NeiKeyBinding other) {
        return other != null && neiIdentifier.equals(other.neiIdentifier)
            && controllerIdentifier.equals(other.controllerIdentifier)
            && legacy == other.legacy
            && modifierAware == other.modifierAware
            && keyBinding == other.keyBinding;
    }
}
