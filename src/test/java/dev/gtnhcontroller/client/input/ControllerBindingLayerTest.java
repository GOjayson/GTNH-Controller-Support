package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ControllerBindingLayerTest {

    @Test
    public void modifierAppliesOnlyToGameplayActions() {
        assertEquals(ControllerBindingLayer.MODIFIER, ControllerBindingLayer.select(true, ControllerAction.DROP_ITEM));
        assertEquals(ControllerBindingLayer.PRIMARY, ControllerBindingLayer.select(true, ControllerAction.GUI_CONFIRM));
    }

    @Test
    public void modifierBindingAlwaysReadsFromPrimaryLayer() {
        assertEquals(
            ControllerBindingLayer.PRIMARY,
            ControllerBindingLayer.select(true, ControllerAction.MODIFIER_LAYER));
    }

    @Test
    public void releasedModifierUsesPrimaryLayer() {
        assertEquals(ControllerBindingLayer.PRIMARY, ControllerBindingLayer.select(false, ControllerAction.ATTACK));
    }
}
