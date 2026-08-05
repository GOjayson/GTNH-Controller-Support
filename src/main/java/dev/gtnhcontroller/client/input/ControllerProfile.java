package dev.gtnhcontroller.client.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;

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
    private List<ControllerBinding> gameplayPrimaryBindings = Collections.emptyList();
    private List<ControllerBinding> gameplayModifierBindings = Collections.emptyList();
    private List<ControllerBinding> guiPrimaryBindings = Collections.emptyList();
    private List<ControllerBinding> guiModifierBindings = Collections.emptyList();

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

        Map<ControllerAction, ControllerBinding> activeBindings = new EnumMap<ControllerAction, ControllerBinding>(
            ControllerAction.class);
        for (ControllerAction action : ControllerAction.values()) {
            if (action == ControllerAction.MODIFIER_LAYER) {
                continue;
            }
            ControllerBindingLayer layer = ControllerBindingLayer.select(modifierDown, action);
            ControllerBinding binding = bindingMap(layer).get(action);
            boolean down = connected && binding != null && binding.isDown(gamepadManager, Config.triggerThreshold);
            currentStates.put(action, Boolean.valueOf(down));
            activeBindings.put(action, binding);
        }

        if (!connected) {
            return;
        }
        for (ControllerAction action : ControllerAction.values()) {
            if (action == ControllerAction.MODIFIER_LAYER || !isDown(action)) {
                continue;
            }
            ControllerBinding binding = activeBindings.get(action);
            if (binding != null && binding.isSupersededBy(
                precedenceCandidates(action.guiAction, modifierDown),
                gamepadManager,
                Config.triggerThreshold)) {
                currentStates.put(action, Boolean.FALSE);
            }
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

    /**
     * Supplies registered Minecraft/mod bindings so chord precedence is resolved across both binding screens.
     */
    public void setSupplementalBindings(List<ControllerBinding> gameplayPrimary,
        List<ControllerBinding> gameplayModifier, List<ControllerBinding> guiPrimary,
        List<ControllerBinding> guiModifier) {
        gameplayPrimaryBindings = immutableCopy(gameplayPrimary);
        gameplayModifierBindings = immutableCopy(gameplayModifier);
        guiPrimaryBindings = immutableCopy(guiPrimary);
        guiModifierBindings = immutableCopy(guiModifier);
    }

    public boolean isBindingSuperseded(ControllerBinding binding, boolean guiContext) {
        if (binding == null || gamepadManager == null) {
            return false;
        }
        return binding.isSupersededBy(
            precedenceCandidates(guiContext, isModifierActive()),
            gamepadManager,
            Config.triggerThreshold);
    }

    public List<ControllerAction> getConflictingActions(ControllerAction subjectAction,
        ControllerBindingLayer displayedLayer) {
        ControllerBinding subject = bindingMap(displayedLayer).get(subjectAction);
        if (subject == null || subject.isEmpty()) {
            return Collections.emptyList();
        }

        ControllerBindingLayer candidateLayer = subjectAction == ControllerAction.MODIFIER_LAYER
            ? ControllerBindingLayer.MODIFIER
            : displayedLayer;
        List<ControllerAction> conflicts = new ArrayList<ControllerAction>();
        for (ControllerAction candidateAction : ControllerAction.values()) {
            if (candidateAction == subjectAction || candidateAction.guiAction != subjectAction.guiAction) {
                continue;
            }
            if (candidateLayer == ControllerBindingLayer.MODIFIER
                && candidateAction == ControllerAction.MODIFIER_LAYER) {
                continue;
            }
            ControllerBinding candidate = bindingMap(candidateLayer).get(candidateAction);
            if (subject.conflictsWith(candidate)) {
                conflicts.add(candidateAction);
            }
        }
        return Collections.unmodifiableList(conflicts);
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

    private List<ControllerBinding> precedenceCandidates(boolean guiContext, boolean modifierDown) {
        List<ControllerBinding> candidates = new ArrayList<ControllerBinding>();
        for (ControllerAction candidateAction : ControllerAction.values()) {
            if (candidateAction == ControllerAction.MODIFIER_LAYER || candidateAction.guiAction != guiContext) {
                continue;
            }
            ControllerBindingLayer layer = ControllerBindingLayer.select(modifierDown, candidateAction);
            ControllerBinding candidate = bindingMap(layer).get(candidateAction);
            if (candidate != null && !candidate.isEmpty()) {
                candidates.add(candidate);
            }
        }
        if (guiContext) {
            if (Minecraft.getMinecraft().currentScreen instanceof GuiContainer) {
                candidates.addAll(modifierDown ? guiModifierBindings : guiPrimaryBindings);
            }
        } else {
            candidates.addAll(modifierDown ? gameplayModifierBindings : gameplayPrimaryBindings);
        }
        return candidates;
    }

    private static List<ControllerBinding> immutableCopy(List<ControllerBinding> bindings) {
        return Collections.unmodifiableList(new ArrayList<ControllerBinding>(bindings));
    }
}
