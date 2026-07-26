package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SprintControlTest {

    @Test
    public void holdAndToggleStartWhileActiveAndStopWhenReleased() {
        assertEquals(SprintControl.Decision.START, SprintControl.decide(ActivationMode.HOLD, true, false, true));
        assertEquals(SprintControl.Decision.START, SprintControl.decide(ActivationMode.TOGGLE, true, true, true));
        assertEquals(SprintControl.Decision.STOP, SprintControl.decide(ActivationMode.HOLD, false, true, true));
        assertEquals(SprintControl.Decision.STOP, SprintControl.decide(ActivationMode.TOGGLE, false, true, true));
    }

    @Test
    public void controllerOwnedSprintStopsWhenSprintConditionsFail() {
        assertEquals(SprintControl.Decision.STOP, SprintControl.decide(ActivationMode.HOLD, true, true, false));
        assertEquals(SprintControl.Decision.UNCHANGED, SprintControl.decide(ActivationMode.HOLD, true, false, false));
    }

    @Test
    public void pressStartsOnceWithoutTakingOwnership() {
        assertEquals(SprintControl.Decision.START, SprintControl.decide(ActivationMode.PRESS, true, false, true));
        assertEquals(SprintControl.Decision.UNCHANGED, SprintControl.decide(ActivationMode.PRESS, false, false, true));
    }
}
