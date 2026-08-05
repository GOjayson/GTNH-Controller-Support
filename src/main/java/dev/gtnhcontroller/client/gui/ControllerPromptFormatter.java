package dev.gtnhcontroller.client.gui;

import java.util.Locale;

final class ControllerPromptFormatter {

    private ControllerPromptFormatter() {}

    static String format(String specification) {
        if (specification == null || "NONE".equalsIgnoreCase(specification)) {
            return "Unbound";
        }
        StringBuilder result = new StringBuilder();
        String[] alternatives = specification.split("\\|");
        for (String alternative : alternatives) {
            if (result.length() > 0) {
                result.append('/');
            }
            String[] chord = alternative.split("\\+");
            for (int index = 0; index < chord.length; index++) {
                if (index > 0) {
                    result.append('+');
                }
                result.append(formatInput(chord[index]));
            }
        }
        return result.toString();
    }

    private static String formatInput(String specification) {
        String[] parts = specification.trim()
            .toUpperCase(Locale.ROOT)
            .split(":", 2);
        String name = parts.length == 2 ? parts[1] : parts[0];
        if ("SOUTH".equals(name)) return "A";
        if ("EAST".equals(name)) return "B";
        if ("WEST".equals(name)) return "X";
        if ("NORTH".equals(name)) return "Y";
        if ("LEFT_SHOULDER".equals(name)) return "LB";
        if ("RIGHT_SHOULDER".equals(name)) return "RB";
        if ("LEFT_TRIGGER".equals(name)) return "LT";
        if ("RIGHT_TRIGGER".equals(name)) return "RT";
        if ("LEFT_STICK".equals(name)) return "LS";
        if ("RIGHT_STICK".equals(name)) return "RS";
        if ("START".equals(name)) return "Menu";
        if ("BACK".equals(name)) return "View";
        if (name.startsWith("DPAD_")) return "D-" + name.substring(5);
        if (name.startsWith("LEFT_PADDLE")) return "LP" + name.substring("LEFT_PADDLE".length());
        if (name.startsWith("RIGHT_PADDLE")) return "RP" + name.substring("RIGHT_PADDLE".length());
        return name.replace('_', ' ');
    }
}
