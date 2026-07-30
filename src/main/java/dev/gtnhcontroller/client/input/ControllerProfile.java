package dev.gtnhcontroller.client.input;

import java.util.EnumMap;
import java.util.Map;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.GTNHController;

/**
 * Resolves configured action bindings and takes one stable action-state snapshot per client tick.
 */
public final class ControllerProfile {

    private final SdlGamepadManager gamepadManager;
    private final Map<ControllerAction, ControllerBinding> primaryBindings = new EnumMap<ControllerAction, ControllerBinding>(
        ControllerAction.class);
    private final Map<ControllerAction, ControllerBinding> modifierBindings = new EnumMap<ControllerAction, ControllerBinding>(
        ControllerAction.class);
    private final Map<ControllerAction, Boolean> currentStates = new EnumMap<ControllerAction, Boolean>(
        ControllerAction.class);
    private final Map<ControllerAction, Boolean> previousStates = new EnumMap<ControllerAction, Boolean>(
        ControllerAction.class);

    public ControllerProfile(SdlGamepadManager gamepadManager) {
        this.gamepadManager = gamepadManager;

        for (ControllerAction action : ControllerAction.values()) {
            currentStates.put(action, Boolean.FALSE);
            previousStates.put(action, Boolean.FALSE);
        }
        reloadBindings();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        for (ControllerAction action : ControllerAction.values()) {
            previousStates.put(action, currentStates.get(action));
        }

        boolean connected = gamepadManager.isConnected();
        boolean modifierDown = connected && primaryBindings.get(ControllerAction.MODIFIER_LAYER)
            .isDown(gamepadManager, Config.triggerThreshold);
        currentStates.put(ControllerAction.MODIFIER_LAYER, Boolean.valueOf(modifierDown));

        for (ControllerAction action : ControllerAction.values()) {
            if (action == ControllerAction.MODIFIER_LAYER) {
                continue;
            }
            ControllerBindingLayer layer = ControllerBindingLayer.select(modifierDown, action);
            ControllerBinding binding = bindingMap(layer).get(action);
            boolean down = connected && binding != null && binding.isDown(gamepadManager, Config.triggerThreshold);
            currentStates.put(action, Boolean.valueOf(down));
        }
    }

    public boolean isDown(ControllerAction action) {
        return currentStates.get(action)
            .booleanValue();
    }

    public boolean wasPressed(ControllerAction action) {
        return isDown(action) && !previousStates.get(action)
            .booleanValue();
    }

    public void setBinding(ControllerAction action, String bindingSpecification) {
        setBinding(action, bindingSpecification, ControllerBindingLayer.PRIMARY);
    }

    public void setBinding(ControllerAction action, String bindingSpecification, ControllerBindingLayer layer) {
        ControllerBinding parsedBinding = ControllerBinding.parse(bindingSpecification);
        Config.setBinding(action, bindingSpecification, layer);
        Config.saveControllerSettings();
        bindingMap(layer).put(action, parsedBinding);
        resetState(action);
    }

    public void resetBindings(boolean guiBindings) {
        resetBindings(guiBindings, ControllerBindingLayer.PRIMARY);
    }

    public void resetBindings(boolean guiBindings, ControllerBindingLayer layer) {
        for (ControllerAction action : ControllerAction.values()) {
            if (action.guiAction == guiBindings
                && !(layer == ControllerBindingLayer.MODIFIER && action == ControllerAction.MODIFIER_LAYER)) {
                String defaultBinding = layer == ControllerBindingLayer.PRIMARY ? action.defaultBinding : "NONE";
                Config.setBinding(action, defaultBinding, layer);
            }
        }
        Config.saveControllerSettings();
        reloadBindings();
    }

    public void reloadBindings() {
        primaryBindings.clear();
        modifierBindings.clear();
        for (ControllerAction action : ControllerAction.values()) {
            register(action, ControllerBindingLayer.PRIMARY, Config.getBinding(action));
            if (!action.guiAction && action != ControllerAction.MODIFIER_LAYER) {
                register(
                    action,
                    ControllerBindingLayer.MODIFIER,
                    Config.getBinding(action, ControllerBindingLayer.MODIFIER));
            }
            resetState(action);
        }
    }

    public boolean isModifierActive() {
        return isDown(ControllerAction.MODIFIER_LAYER);
    }

    private void resetState(ControllerAction action) {
        currentStates.put(action, Boolean.FALSE);
        previousStates.put(action, Boolean.FALSE);
    }

    private void register(ControllerAction action, ControllerBindingLayer layer, String configuredBinding) {
        try {
            bindingMap(layer).put(action, ControllerBinding.parse(configuredBinding));
        } catch (IllegalArgumentException exception) {
            String fallback = layer == ControllerBindingLayer.PRIMARY ? action.defaultBinding : "NONE";
            GTNHController.LOG.error(
                "Invalid {} controller binding '{}' for {}. Falling back to '{}'.",
                layer,
                configuredBinding,
                action,
                fallback,
                exception);
            bindingMap(layer).put(action, ControllerBinding.parse(fallback));
        }
    }

    private Map<ControllerAction, ControllerBinding> bindingMap(ControllerBindingLayer layer) {
        return layer == ControllerBindingLayer.MODIFIER ? modifierBindings : primaryBindings;
    }
}
