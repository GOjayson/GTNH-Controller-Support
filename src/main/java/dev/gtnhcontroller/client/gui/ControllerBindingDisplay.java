package dev.gtnhcontroller.client.gui;

import java.util.Locale;

final class ControllerBindingDisplay {

    private ControllerBindingDisplay() {}

    static String format(String bindingSpecification) {
        if ("NONE".equalsIgnoreCase(bindingSpecification)) {
            return "Unbound";
        }

        StringBuilder display = new StringBuilder();
        String[] alternatives = bindingSpecification.split("\\|");
        for (String alternative : alternatives) {
            if (display.length() > 0) {
                display.append(" / ");
            }

            String[] parts = alternative.trim()
                .split(":", 2);
            display.append(parts.length == 2 ? formatInputName(parts[1]) : alternative);
        }
        return display.toString();
    }

    private static String formatInputName(String inputName) {
        String normalized = inputName.trim()
            .toUpperCase(Locale.ROOT);
        if (normalized.startsWith("DPAD_")) {
            return "D-pad " + toTitleCase(normalized.substring(5));
        }
        return toTitleCase(normalized);
    }

    private static String toTitleCase(String inputName) {
        StringBuilder display = new StringBuilder();
        String[] words = inputName.split("_");
        for (String word : words) {
            if (display.length() > 0) {
                display.append(' ');
            }
            if (!word.isEmpty()) {
                display.append(word.charAt(0));
                display.append(
                    word.substring(1)
                        .toLowerCase(Locale.ROOT));
            }
        }
        return display.toString();
    }
}
