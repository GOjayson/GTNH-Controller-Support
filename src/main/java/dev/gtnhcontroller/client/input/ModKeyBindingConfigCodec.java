package dev.gtnhcontroller.client.input;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores dynamic key-binding identifiers in one Forge configuration string list. The identifier is encoded so mod
 * translation keys and categories cannot interfere with the entry delimiter.
 */
public final class ModKeyBindingConfigCodec {

    private static final char ENTRY_SEPARATOR = '=';

    private ModKeyBindingConfigCodec() {}

    public static Map<String, String> decode(String[] serializedEntries) {
        Map<String, String> bindings = new LinkedHashMap<String, String>();
        if (serializedEntries == null) {
            return bindings;
        }

        for (String serializedEntry : serializedEntries) {
            if (serializedEntry == null) {
                continue;
            }

            int separatorIndex = serializedEntry.indexOf(ENTRY_SEPARATOR);
            if (separatorIndex <= 0 || separatorIndex == serializedEntry.length() - 1) {
                continue;
            }

            try {
                String identifier = new String(
                    Base64.getUrlDecoder()
                        .decode(serializedEntry.substring(0, separatorIndex)),
                    StandardCharsets.UTF_8);
                String binding = serializedEntry.substring(separatorIndex + 1);
                if (!identifier.isEmpty() && !binding.isEmpty()) {
                    bindings.put(identifier, binding);
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore one corrupt entry without discarding every other saved mod binding.
            }
        }
        return bindings;
    }

    public static String[] encode(Map<String, String> bindings) {
        List<String> identifiers = new ArrayList<String>(bindings.keySet());
        Collections.sort(identifiers);

        List<String> serializedEntries = new ArrayList<String>();
        for (String identifier : identifiers) {
            String binding = bindings.get(identifier);
            if (identifier == null || identifier.isEmpty()
                || binding == null
                || binding.isEmpty()
                || "NONE".equalsIgnoreCase(binding)) {
                continue;
            }

            String encodedIdentifier = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(identifier.getBytes(StandardCharsets.UTF_8));
            serializedEntries.add(encodedIdentifier + ENTRY_SEPARATOR + binding);
        }
        return serializedEntries.toArray(new String[serializedEntries.size()]);
    }
}
