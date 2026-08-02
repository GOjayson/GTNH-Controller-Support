package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import codechicken.nei.KeyManager;
import codechicken.nei.KeyManager.KeyState;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.config.OptionKeyBind;
import codechicken.nei.config.OptionList;

public class NeiKeyBindingAdapterTest {

    @Before
    public void resetLegacyRegistry() {
        NEIClientConfig.OPTIONS.optionList.clear();
        KeyManager.keyStates.clear();

        OptionList guiOptions = new OptionList("keys.gui");
        guiOptions.optionList.add(new OptionKeyBind("gui.recipe", true));
        NEIClientConfig.OPTIONS.optionList.add(guiOptions);
        NEIClientConfig.OPTIONS.optionList.add(new OptionKeyBind("world.chunkoverlay", false));
        KeyManager.keyStates.put("world.chunkoverlay", new KeyState());
    }

    @Test
    public void discoversModifierAndStateBasedLegacyBindings() {
        List<NeiKeyBinding> bindings = new NeiKeyBindingAdapter().discoverBindings();

        assertEquals(2, bindings.size());
        NeiKeyBinding recipe = find(bindings, "gui.recipe");
        NeiKeyBinding chunkOverlay = find(bindings, "world.chunkoverlay");
        assertTrue(recipe.modifierAware);
        assertFalse(chunkOverlay.modifierAware);
        assertEquals(NeiKeyBindingIdentifier.create("recipe.recipe"), recipe.controllerIdentifier);
    }

    @Test
    public void updatesLegacyKeyStateWithoutAKeyboardEvent() {
        NeiKeyBindingAdapter adapter = new NeiKeyBindingAdapter();
        NeiKeyBinding chunkOverlay = find(adapter.discoverBindings(), "world.chunkoverlay");
        KeyState state = KeyManager.keyStates.get("world.chunkoverlay");

        adapter.update(chunkOverlay, false, true, null);
        assertTrue(state.down);
        assertTrue(state.held);
        assertFalse(state.up);

        adapter.update(chunkOverlay, true, false, null);
        assertFalse(state.down);
        assertFalse(state.held);
        assertTrue(state.up);
    }

    @Test
    public void exposesModifierAwareBindingsOnlyWhileControllerInputIsHeld() {
        NeiKeyBindingAdapter adapter = new NeiKeyBindingAdapter();
        NeiKeyBinding recipe = find(adapter.discoverBindings(), "gui.recipe");

        adapter.update(recipe, false, true, null);
        assertTrue(NeiKeyBindingAdapterAccess.isVirtualKeyDown("gui.recipe"));

        adapter.update(recipe, true, false, null);
        assertFalse(NeiKeyBindingAdapterAccess.isVirtualKeyDown("gui.recipe"));
    }

    private static NeiKeyBinding find(List<NeiKeyBinding> bindings, String identifier) {
        for (NeiKeyBinding binding : bindings) {
            if (identifier.equals(binding.neiIdentifier)) {
                return binding;
            }
        }
        throw new AssertionError("Missing NEI binding " + identifier);
    }
}
