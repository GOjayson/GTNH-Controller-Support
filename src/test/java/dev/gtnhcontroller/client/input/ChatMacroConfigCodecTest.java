package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class ChatMacroConfigCodecTest {

    @Test
    public void roundTripPreservesNamesCommandsAndOrder() {
        Map<String, ChatMacro> macros = new LinkedHashMap<String, ChatMacro>();
        macros.put("first", new ChatMacro("first", "TPA accept", "/tpa accept"));
        macros.put("second", new ChatMacro("second", "Greeting", "Hi all | welcome"));

        Map<String, ChatMacro> decoded = ChatMacroConfigCodec.decode(ChatMacroConfigCodec.encode(macros));

        assertEquals(2, decoded.size());
        assertEquals(
            "TPA accept",
            decoded.get("first")
                .getName());
        assertEquals(
            "Hi all | welcome",
            decoded.get("second")
                .getMessage());
    }

    @Test
    public void corruptEntryDoesNotDiscardValidMacro() {
        Map<String, ChatMacro> macros = new LinkedHashMap<String, ChatMacro>();
        macros.put("valid", new ChatMacro("valid", "Hello", "Hello everyone"));
        String validEntry = ChatMacroConfigCodec.encode(macros)[0];

        Map<String, ChatMacro> decoded = ChatMacroConfigCodec
            .decode(new String[] { "bad", "invalid id|SGVsbG8|V29ybGQ", "id|*|*", validEntry });

        assertEquals(1, decoded.size());
        assertEquals(
            "Hello everyone",
            decoded.get("valid")
                .getMessage());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMultiLineMessages() {
        new ChatMacro("unsafe", "Unsafe", "first\nsecond");
    }

    @Test
    public void radialIdentifiersOnlyResolveForMacros() {
        ChatMacro macro = new ChatMacro("hello", "Hello", "Hello all");
        assertEquals("hello", ChatMacro.idFromRadialIdentifier(macro.getRadialIdentifier()));
        assertNull(ChatMacro.idFromRadialIdentifier("key.categories.gameplay|key.jump|1"));
    }
}
