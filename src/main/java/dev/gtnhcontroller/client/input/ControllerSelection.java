package dev.gtnhcontroller.client.input;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class ControllerSelection {

    public static final String AUTOMATIC = "AUTO";

    private ControllerSelection() {}

    public static String createKey(String controllerName, int occurrence) {
        if (controllerName == null || controllerName.isEmpty()) {
            throw new IllegalArgumentException("Controller name must not be empty");
        }
        if (occurrence < 1) {
            throw new IllegalArgumentException("Controller occurrence must be positive");
        }
        String encodedName = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(controllerName.getBytes(StandardCharsets.UTF_8));
        return "NAME:" + encodedName + ":" + occurrence;
    }

    public static boolean isAutomatic(String selectionKey) {
        return selectionKey == null || AUTOMATIC.equalsIgnoreCase(selectionKey.trim());
    }

    public static boolean isValid(String selectionKey) {
        return isAutomatic(selectionKey) || selectionKey.matches("NAME:[A-Za-z0-9_-]+:[1-9][0-9]*");
    }
}
