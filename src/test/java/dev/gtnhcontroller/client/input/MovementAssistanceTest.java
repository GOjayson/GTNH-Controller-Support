package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MovementAssistanceTest {

    @Test
    public void controllerSneakMergesWithKeyboardSneak() {
        assertTrue(MovementAssistance.mergeSneak(true, false));
        assertTrue(MovementAssistance.mergeSneak(false, true));
        assertFalse(MovementAssistance.mergeSneak(false, false));
    }

    @Test
    public void autoJumpRequiresMovementCollisionGroundAndClearance() {
        assertTrue(MovementAssistance.shouldAutoJump(true, true, true, true, false, false, false, true));
        assertFalse(MovementAssistance.shouldAutoJump(true, false, true, true, false, false, false, true));
        assertFalse(MovementAssistance.shouldAutoJump(true, true, false, true, false, false, false, true));
        assertFalse(MovementAssistance.shouldAutoJump(true, true, true, true, false, false, false, false));
    }

    @Test
    public void autoJumpStaysOffInLiquidsWhileSneakingOrRiding() {
        assertFalse(MovementAssistance.shouldAutoJump(true, true, true, true, true, false, false, true));
        assertFalse(MovementAssistance.shouldAutoJump(true, true, true, true, false, true, false, true));
        assertFalse(MovementAssistance.shouldAutoJump(true, true, true, true, false, false, true, true));
    }

    @Test
    public void autoSwimTogglesAndSurvivesBriefSurfaceContact() {
        assertTrue(MovementAssistance.updateAutoSwim(true, true, false, true, false, true));
        assertTrue(MovementAssistance.updateAutoSwim(true, true, false, false, true, true));
        assertTrue(MovementAssistance.updateAutoSwim(true, false, false, false, true, true));
        assertFalse(MovementAssistance.updateAutoSwim(true, false, false, false, true, false));
    }

    @Test
    public void autoSwimStopsOnSecondPressLandOrDisable() {
        assertFalse(MovementAssistance.updateAutoSwim(true, true, false, true, true, true));
        assertFalse(MovementAssistance.updateAutoSwim(true, false, true, false, true, true));
        assertFalse(MovementAssistance.updateAutoSwim(false, true, false, false, true, true));
        assertFalse(MovementAssistance.updateAutoSwim(true, false, false, true, false, true));
    }
}
