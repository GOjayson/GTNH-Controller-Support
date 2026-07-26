package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ActivationModeStateTest {

    @Test
    public void configValuesAreCaseInsensitiveAndInvalidValuesUseTheFallback() {
        assertEquals(ActivationMode.HOLD, ActivationMode.parse("hold", ActivationMode.PRESS));
        assertEquals(ActivationMode.TOGGLE, ActivationMode.parse(" TOGGLE ", ActivationMode.HOLD));
        assertEquals(ActivationMode.PRESS, ActivationMode.parse("invalid", ActivationMode.PRESS));
        assertEquals(ActivationMode.HOLD, ActivationMode.parse(null, ActivationMode.HOLD));
        assertEquals(ActivationMode.TOGGLE, ActivationMode.HOLD.next());
        assertEquals(ActivationMode.PRESS, ActivationMode.TOGGLE.next());
        assertEquals(ActivationMode.HOLD, ActivationMode.PRESS.next());
    }

    @Test
    public void holdTracksThePhysicalInput() {
        ActivationModeState state = new ActivationModeState();

        assertTrue(state.update(ActivationMode.HOLD, true, true));
        assertTrue(state.update(ActivationMode.HOLD, true, false));
        assertFalse(state.update(ActivationMode.HOLD, false, false));
    }

    @Test
    public void toggleChangesOnlyOnPressEdges() {
        ActivationModeState state = new ActivationModeState();

        assertTrue(state.update(ActivationMode.TOGGLE, true, true));
        assertTrue(state.update(ActivationMode.TOGGLE, true, false));
        assertTrue(state.update(ActivationMode.TOGGLE, false, false));
        assertFalse(state.update(ActivationMode.TOGGLE, true, true));
    }

    @Test
    public void pressProducesOneTickPerPress() {
        ActivationModeState state = new ActivationModeState();

        assertTrue(state.update(ActivationMode.PRESS, true, true));
        assertFalse(state.update(ActivationMode.PRESS, true, false));
        assertFalse(state.update(ActivationMode.PRESS, false, false));
    }

    @Test
    public void resetAndModeChangesClearLatchedState() {
        ActivationModeState state = new ActivationModeState();

        assertTrue(state.update(ActivationMode.TOGGLE, true, true));
        state.reset();
        assertFalse(state.update(ActivationMode.TOGGLE, false, false));

        assertTrue(state.update(ActivationMode.TOGGLE, true, true));
        assertFalse(state.update(ActivationMode.HOLD, false, false));
        assertFalse(state.update(ActivationMode.TOGGLE, false, false));
    }
}
