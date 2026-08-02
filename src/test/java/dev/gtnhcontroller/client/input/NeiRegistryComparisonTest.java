package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class NeiRegistryComparisonTest {

    @Test
    public void identicalLegacySnapshotsDoNotForceARebuild() {
        NeiKeyBinding first = legacy("gui.recipe", true);
        NeiKeyBinding same = legacy("gui.recipe", true);

        assertTrue(
            ModKeyBindingController.sameNeiRegistry(Collections.singletonList(first), Collections.singletonList(same)));
    }

    @Test
    public void identifierTypeAndOrderChangesAreDetected() {
        NeiKeyBinding recipe = legacy("gui.recipe", true);
        NeiKeyBinding usage = legacy("gui.usage", true);

        assertFalse(
            ModKeyBindingController.sameNeiRegistry(
                Collections.singletonList(recipe),
                Collections.singletonList(legacy("gui.recipe", false))));
        assertFalse(
            ModKeyBindingController.sameNeiRegistry(Arrays.asList(recipe, usage), Arrays.asList(usage, recipe)));
    }

    private static NeiKeyBinding legacy(String identifier, boolean modifierAware) {
        return new NeiKeyBinding(
            identifier,
            "nei.options.keys.gui",
            "nei.options.keys." + identifier,
            true,
            modifierAware,
            null);
    }
}
