package dev.gtnhcontroller.client.input;

import net.minecraft.client.settings.KeyBinding;

/**
 * One exact Minecraft or mod key binding with a stable controller-configuration identifier.
 */
public final class RegisteredKeyBinding {

    private final KeyBinding keyBinding;
    private final String identifier;
    private final String categoryKey;
    private final String descriptionKey;
    private final String categoryName;
    private final String displayName;
    private final int occurrence;

    RegisteredKeyBinding(KeyBinding keyBinding, String identifier, String categoryKey, String descriptionKey,
        String categoryName, String displayName, int occurrence) {
        this.keyBinding = keyBinding;
        this.identifier = identifier;
        this.categoryKey = categoryKey;
        this.descriptionKey = descriptionKey;
        this.categoryName = categoryName;
        this.displayName = displayName;
        this.occurrence = occurrence;
    }

    KeyBinding getKeyBinding() {
        return keyBinding;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getDisplayName() {
        return occurrence > 1 ? displayName + " [" + occurrence + "]" : displayName;
    }
}
