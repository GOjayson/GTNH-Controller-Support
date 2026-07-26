package dev.gtnhcontroller.client.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A configurable action binding. Multiple inputs separated by {@code |} are alternatives, so either input activates
 * the action. Supported forms are {@code BUTTON:SOUTH}, {@code TRIGGER:RIGHT_TRIGGER}, and {@code NONE}.
 */
public final class ControllerBinding {

    private final List<ControllerButton> buttons;
    private final List<ControllerAxis> triggers;

    private ControllerBinding(List<ControllerButton> buttons, List<ControllerAxis> triggers) {
        this.buttons = Collections.unmodifiableList(buttons);
        this.triggers = Collections.unmodifiableList(triggers);
    }

    public static ControllerBinding parse(String specification) {
        if (specification == null) {
            throw new IllegalArgumentException("Binding must not be null");
        }

        String normalizedSpecification = specification.trim()
            .toUpperCase(Locale.ROOT);
        if (normalizedSpecification.isEmpty()) {
            throw new IllegalArgumentException("Binding must not be empty");
        }
        if ("NONE".equals(normalizedSpecification)) {
            return new ControllerBinding(
                Collections.<ControllerButton>emptyList(),
                Collections.<ControllerAxis>emptyList());
        }

        List<ControllerButton> buttons = new ArrayList<ControllerButton>();
        List<ControllerAxis> triggers = new ArrayList<ControllerAxis>();
        String[] alternatives = normalizedSpecification.split("\\|", -1);
        for (String alternative : alternatives) {
            String[] parts = alternative.trim()
                .split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid binding part: " + alternative);
            }

            String bindingType = parts[0].trim();
            String inputName = parts[1].trim();
            if (inputName.isEmpty()) {
                throw new IllegalArgumentException("Invalid binding part: " + alternative);
            }

            if ("BUTTON".equals(bindingType)) {
                buttons.add(parseButton(inputName));
            } else if ("TRIGGER".equals(bindingType)) {
                triggers.add(parseTrigger(inputName));
            } else {
                throw new IllegalArgumentException("Unknown binding type: " + bindingType);
            }
        }

        return new ControllerBinding(buttons, triggers);
    }

    public boolean isDown(SdlGamepadManager gamepadManager, float triggerThreshold) {
        for (ControllerButton button : buttons) {
            if (gamepadManager.isButtonDown(button)) {
                return true;
            }
        }
        for (ControllerAxis trigger : triggers) {
            if (gamepadManager.getTrigger(trigger) >= triggerThreshold) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return buttons.isEmpty() && triggers.isEmpty();
    }

    public boolean conflictsWith(ControllerBinding other) {
        if (other == null || isEmpty() || other.isEmpty()) {
            return false;
        }
        for (ControllerButton button : buttons) {
            if (other.buttons.contains(button)) {
                return true;
            }
        }
        for (ControllerAxis trigger : triggers) {
            if (other.triggers.contains(trigger)) {
                return true;
            }
        }
        return false;
    }

    int getButtonCount() {
        return buttons.size();
    }

    int getTriggerCount() {
        return triggers.size();
    }

    private static ControllerButton parseButton(String name) {
        try {
            return ControllerButton.valueOf(name);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown controller button: " + name, exception);
        }
    }

    private static ControllerAxis parseTrigger(String name) {
        final ControllerAxis axis;
        try {
            axis = ControllerAxis.valueOf(name);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown controller axis: " + name, exception);
        }

        if (!axis.isTrigger()) {
            throw new IllegalArgumentException("Axis is not a trigger: " + name);
        }
        return axis;
    }
}
