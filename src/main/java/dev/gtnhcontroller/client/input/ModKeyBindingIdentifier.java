package dev.gtnhcontroller.client.input;

/**
 * Builds a language-independent identifier for a registered Minecraft key binding. The occurrence suffix keeps
 * broken or unusual mods that register the same category and description more than once from colliding.
 */
public final class ModKeyBindingIdentifier {

    private static final char SEPARATOR = '\u001F';

    private ModKeyBindingIdentifier() {}

    public static String base(String categoryKey, String descriptionKey) {
        return safe(categoryKey) + SEPARATOR + safe(descriptionKey);
    }

    public static String create(String categoryKey, String descriptionKey, int occurrence) {
        if (occurrence < 1) {
            throw new IllegalArgumentException("Occurrence must be at least one");
        }
        return base(categoryKey, descriptionKey) + SEPARATOR + occurrence;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
