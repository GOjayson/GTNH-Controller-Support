package dev.gtnhcontroller.client.input;

import java.util.EnumMap;
import java.util.Map;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.GTNHController;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Resolves configured action bindings and takes one stable action-state snapshot per client tick.
 */
public final class ControllerProfile {

    private final SdlGamepadManager gamepadManager;
    private final Map<ControllerAction, ControllerBinding> bindings = new EnumMap<ControllerAction, ControllerBinding>(
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
            boolean down = gamepadManager.isConnected() && bindings.get(action)
                .isDown(gamepadManager, Config.triggerThreshold);
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
        ControllerBinding parsedBinding = ControllerBinding.parse(bindingSpecification);
        Config.setBinding(action, bindingSpecification);
        Config.saveControllerSettings();
        bindings.put(action, parsedBinding);
        resetState(action);
    }

    public void resetBindings(boolean guiBindings) {
        for (ControllerAction action : ControllerAction.values()) {
            if (action.guiAction == guiBindings) {
                Config.setBinding(action, action.defaultBinding);
            }
        }
        Config.saveControllerSettings();
        reloadBindings();
    }

    public void reloadBindings() {
        bindings.clear();
        for (ControllerAction action : ControllerAction.values()) {
            register(action, Config.getBinding(action));
            resetState(action);
        }
    }

    private void resetState(ControllerAction action) {
        currentStates.put(action, Boolean.FALSE);
        previousStates.put(action, Boolean.FALSE);
    }

    private void register(ControllerAction action, String configuredBinding) {
        try {
            bindings.put(action, ControllerBinding.parse(configuredBinding));
        } catch (IllegalArgumentException exception) {
            GTNHController.LOG.error(
                "Invalid controller binding '{}' for {}. Falling back to '{}'.",
                configuredBinding,
                action,
                action.defaultBinding,
                exception);
            bindings.put(action, ControllerBinding.parse(action.defaultBinding));
        }
    }
}
