package dev.gtnhcontroller.client.input;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class RadialMenuConfigCodec {

    public static final int SLOT_COUNT = 8;

    private static final char ENTRY_SEPARATOR = '=';

    private RadialMenuConfigCodec() {}

    public static String[] decode(String[] serializedEntries) {
        String[] identifiers = new String[SLOT_COUNT];
        if (serializedEntries == null) {
            return identifiers;
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
                int slot = Integer.parseInt(serializedEntry.substring(0, separatorIndex));
                String identifier = new String(
                    Base64.getUrlDecoder()
                        .decode(serializedEntry.substring(separatorIndex + 1)),
                    StandardCharsets.UTF_8);
                if (slot >= 0 && slot < SLOT_COUNT && !identifier.isEmpty()) {
                    identifiers[slot] = identifier;
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore one corrupt slot without discarding the other configured actions.
            }
        }
        return identifiers;
    }

    public static String[] encode(String[] identifiers) {
        List<String> serializedEntries = new ArrayList<String>();
        if (identifiers == null) {
            return new String[0];
        }

        int finalSlot = Math.min(identifiers.length, SLOT_COUNT);
        for (int slot = 0; slot < finalSlot; slot++) {
            String identifier = identifiers[slot];
            if (identifier == null || identifier.isEmpty()) {
                continue;
            }

            String encodedIdentifier = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(identifier.getBytes(StandardCharsets.UTF_8));
            serializedEntries.add(slot + String.valueOf(ENTRY_SEPARATOR) + encodedIdentifier);
        }
        return serializedEntries.toArray(new String[serializedEntries.size()]);
    }
}
