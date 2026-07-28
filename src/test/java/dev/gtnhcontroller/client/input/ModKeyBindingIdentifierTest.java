package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class ModKeyBindingIdentifierTest {

    @Test
    public void duplicateDescriptionsReceiveDifferentIdentifiers() {
        String first = ModKeyBindingIdentifier.create("key.categories.ae2", "key.ae2.search", 1);
        String second = ModKeyBindingIdentifier.create("key.categories.ae2", "key.ae2.search", 2);

        assertNotEquals(first, second);
        assertEquals(
            ModKeyBindingIdentifier.base("key.categories.ae2", "key.ae2.search") + '\u001F' + "1",
            first);
    }

    @Test(expected = IllegalArgumentException.class)
    public void occurrenceMustBePositive() {
        ModKeyBindingIdentifier.create("category", "description", 0);
    }
}
