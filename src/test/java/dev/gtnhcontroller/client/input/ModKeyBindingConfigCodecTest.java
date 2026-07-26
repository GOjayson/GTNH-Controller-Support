package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class ModKeyBindingConfigCodecTest {

    @Test
    public void roundTripPreservesIdentifiersAndControllerBindings() {
        Map<String, String> bindings = new LinkedHashMap<String, String>();
        bindings.put(ModKeyBindingIdentifier.create("key.categories.ae2", "key.ae2.search", 1), "BUTTON:WEST");
        bindings.put(
            ModKeyBindingIdentifier.create("key.categories.backpack", "key.open_backpack", 1),
            "TRIGGER:LEFT_TRIGGER");

        assertEquals(bindings, ModKeyBindingConfigCodec.decode(ModKeyBindingConfigCodec.encode(bindings)));
    }

    @Test
    public void encodingIsStableAndSkipsUnboundEntries() {
        Map<String, String> bindings = new LinkedHashMap<String, String>();
        bindings.put("z", "BUTTON:NORTH");
        bindings.put("ignored", "NONE");
        bindings.put("a", "BUTTON:SOUTH");

        String[] first = ModKeyBindingConfigCodec.encode(bindings);
        String[] second = ModKeyBindingConfigCodec.encode(bindings);

        assertArrayEquals(first, second);
        assertEquals(2, first.length);
    }

    @Test
    public void corruptEntryDoesNotDiscardValidEntries() {
        Map<String, String> bindings = new LinkedHashMap<String, String>();
        bindings.put("valid", "BUTTON:EAST");
        String validEntry = ModKeyBindingConfigCodec.encode(bindings)[0];

        Map<String, String> decoded = ModKeyBindingConfigCodec
            .decode(new String[] { "*=BUTTON:SOUTH", "missing-delimiter", validEntry });

        assertEquals(1, decoded.size());
        assertEquals("BUTTON:EAST", decoded.get("valid"));
    }
}
