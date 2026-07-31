package dev.gtnhcontroller.client.input;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ChatMacroConfigCodec {

    private static final String SEPARATOR = "|";

    private ChatMacroConfigCodec() {}

    public static Map<String, ChatMacro> decode(String[] serializedEntries) {
        Map<String, ChatMacro> macros = new LinkedHashMap<String, ChatMacro>();
        if (serializedEntries == null) {
            return macros;
        }

        for (String serializedEntry : serializedEntries) {
            if (serializedEntry == null) {
                continue;
            }
            String[] fields = serializedEntry.split("\\|", -1);
            if (fields.length != 3) {
                continue;
            }
            try {
                String name = decodeText(fields[1]);
                String message = decodeText(fields[2]);
                ChatMacro macro = new ChatMacro(fields[0], name, message);
                macros.put(macro.getId(), macro);
            } catch (IllegalArgumentException ignored) {
                // Ignore one corrupt macro while retaining every other valid entry.
            }
        }
        return macros;
    }

    public static String[] encode(Map<String, ChatMacro> macros) {
        List<String> entries = new ArrayList<String>();
        if (macros == null) {
            return new String[0];
        }
        for (ChatMacro macro : macros.values()) {
            if (macro != null) {
                entries.add(
                    macro.getId() + SEPARATOR
                        + encodeText(macro.getName())
                        + SEPARATOR
                        + encodeText(macro.getMessage()));
            }
        }
        return entries.toArray(new String[entries.size()]);
    }

    private static String encodeText(String value) {
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeText(String value) {
        return new String(
            Base64.getUrlDecoder()
                .decode(value),
            StandardCharsets.UTF_8);
    }
}
