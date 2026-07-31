package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import dev.gtnhcontroller.Config;

public class ControllerProfileConflictTest {

    @Test
    public void reportsTheExactCoreActionSharingAnInput() {
        String originalJump = Config.getBinding(ControllerAction.JUMP);
        String originalSprint = Config.getBinding(ControllerAction.SPRINT);
        try {
            Config.setBinding(ControllerAction.JUMP, "BUTTON:MISC6");
            Config.setBinding(ControllerAction.SPRINT, "BUTTON:MISC6");
            ControllerProfile profile = new ControllerProfile(null);

            List<ControllerAction> conflicts = profile
                .getConflictingActions(ControllerAction.JUMP, ControllerBindingLayer.PRIMARY);
            assertTrue(conflicts.contains(ControllerAction.SPRINT));
        } finally {
            Config.setBinding(ControllerAction.JUMP, originalJump);
            Config.setBinding(ControllerAction.SPRINT, originalSprint);
        }
    }

    @Test
    public void keepsGameplayAndGuiConflictContextsSeparate() {
        String originalJump = Config.getBinding(ControllerAction.JUMP);
        String originalConfirm = Config.getBinding(ControllerAction.GUI_CONFIRM);
        try {
            Config.setBinding(ControllerAction.JUMP, "BUTTON:SOUTH");
            Config.setBinding(ControllerAction.GUI_CONFIRM, "BUTTON:SOUTH");
            ControllerProfile profile = new ControllerProfile(null);

            List<ControllerAction> conflicts = profile
                .getConflictingActions(ControllerAction.JUMP, ControllerBindingLayer.PRIMARY);
            assertFalse(conflicts.contains(ControllerAction.GUI_CONFIRM));
        } finally {
            Config.setBinding(ControllerAction.JUMP, originalJump);
            Config.setBinding(ControllerAction.GUI_CONFIRM, originalConfirm);
        }
    }

    @Test
    public void modifierInputChecksActionsOnTheModifierLayer() {
        String originalModifier = Config.getBinding(ControllerAction.MODIFIER_LAYER);
        String originalModifierAttack = Config.getBinding(ControllerAction.ATTACK, ControllerBindingLayer.MODIFIER);
        try {
            Config.setBinding(ControllerAction.MODIFIER_LAYER, "TRIGGER:RIGHT_TRIGGER");
            Config.setBinding(ControllerAction.ATTACK, "TRIGGER:RIGHT_TRIGGER", ControllerBindingLayer.MODIFIER);
            ControllerProfile profile = new ControllerProfile(null);

            List<ControllerAction> conflicts = profile
                .getConflictingActions(ControllerAction.MODIFIER_LAYER, ControllerBindingLayer.PRIMARY);
            assertTrue(conflicts.contains(ControllerAction.ATTACK));
        } finally {
            Config.setBinding(ControllerAction.MODIFIER_LAYER, originalModifier);
            Config.setBinding(ControllerAction.ATTACK, originalModifierAttack, ControllerBindingLayer.MODIFIER);
        }
    }
}
