package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class NeiKeyBindingIdentifierTest {

    @Test
    public void createsAProviderScopedIdentifier() {
        assertEquals("NEI\u001Fworld.chunkoverlay", NeiKeyBindingIdentifier.create("world.chunkoverlay"));
        assertNotEquals(
            ModKeyBindingIdentifier.create("nei.options.keys.world", "nei.options.keys.world.chunkoverlay", 1),
            NeiKeyBindingIdentifier.create("world.chunkoverlay"));
    }

    @Test
    public void legacyAndModernNamesShareConfigurationIdentifiers() {
        assertEquals(NeiKeyBindingIdentifier.create("recipe.recipe"), NeiKeyBindingIdentifier.create("gui.recipe"));
        assertEquals(
            NeiKeyBindingIdentifier.create("bookmark.hide"),
            NeiKeyBindingIdentifier.create("gui.hide_bookmarks"));
        assertEquals(NeiKeyBindingIdentifier.create("copy.identifier"), NeiKeyBindingIdentifier.create("gui.copy_id"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyIdentifiersAreRejected() {
        NeiKeyBindingIdentifier.create("  ");
    }
}
