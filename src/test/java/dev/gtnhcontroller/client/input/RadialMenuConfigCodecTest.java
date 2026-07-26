package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RadialMenuConfigCodecTest {

    @Test
    public void roundTripPreservesSlotOrderAndEmptySlots() {
        String[] identifiers = new String[RadialMenuConfigCodec.SLOT_COUNT];
        identifiers[0] = ModKeyBindingIdentifier.create("category.a", "action.up", 1);
        identifiers[3] = ModKeyBindingIdentifier.create("category.b", "action.down_right", 2);
        identifiers[7] = ModKeyBindingIdentifier.create("category.c", "action.up_left", 1);

        assertArrayEquals(identifiers, RadialMenuConfigCodec.decode(RadialMenuConfigCodec.encode(identifiers)));
    }

    @Test
    public void corruptEntriesDoNotDiscardValidSlots() {
        String[] identifiers = new String[RadialMenuConfigCodec.SLOT_COUNT];
        identifiers[2] = "valid";
        String validEntry = RadialMenuConfigCodec.encode(identifiers)[0];

        String[] decoded = RadialMenuConfigCodec
            .decode(new String[] { "-1=dmFsdWU", "8=dmFsdWU", "bad", "1=*", validEntry });

        assertEquals("valid", decoded[2]);
        assertEquals("", valueOrEmpty(decoded[1]));
    }

    @Test
    public void nullInputCreatesEightEmptySlots() {
        assertEquals(RadialMenuConfigCodec.SLOT_COUNT, RadialMenuConfigCodec.decode(null).length);
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
