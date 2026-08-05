package dev.gtnhcontroller.client.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A configurable action binding. {@code +} joins inputs which must be held together, while {@code |} separates
 * alternatives. Examples: {@code BUTTON:SOUTH}, {@code BUTTON:LEFT_SHOULDER+BUTTON:SOUTH}, and
 * {@code BUTTON:WEST|TRIGGER:RIGHT_TRIGGER}.
 */
public final class ControllerBinding {

    private final List<Chord> alternatives;
    private final int buttonCount;
    private final int triggerCount;

    private ControllerBinding(List<Chord> alternatives, int buttonCount, int triggerCount) {
        this.alternatives = Collections.unmodifiableList(alternatives);
        this.buttonCount = buttonCount;
        this.triggerCount = triggerCount;
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
            return new ControllerBinding(Collections.<Chord>emptyList(), 0, 0);
        }

        List<Chord> alternatives = new ArrayList<Chord>();
        int buttonCount = 0;
        int triggerCount = 0;
        String[] alternativeSpecifications = normalizedSpecification.split("\\|", -1);
        for (String alternativeSpecification : alternativeSpecifications) {
            String trimmedAlternative = alternativeSpecification.trim();
            if (trimmedAlternative.isEmpty()) {
                throw new IllegalArgumentException("Binding contains an empty alternative");
            }

            Set<BindingInput> chordInputs = new LinkedHashSet<BindingInput>();
            String[] inputSpecifications = trimmedAlternative.split("\\+", -1);
            for (String inputSpecification : inputSpecifications) {
                BindingInput input = parseInput(inputSpecification.trim());
                if (!chordInputs.add(input)) {
                    throw new IllegalArgumentException("Binding chord contains the same input twice: " + input);
                }
                if (input.button != null) {
                    buttonCount++;
                } else {
                    triggerCount++;
                }
            }
            alternatives.add(new Chord(chordInputs));
        }

        return new ControllerBinding(alternatives, buttonCount, triggerCount);
    }

    public boolean isDown(SdlGamepadManager gamepadManager, float triggerThreshold) {
        for (Chord chord : alternatives) {
            if (chord.isDown(gamepadManager, triggerThreshold)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return alternatives.isEmpty();
    }

    /**
     * Two bindings conflict only when they contain the same complete chord. A plain A binding and LB+A are safe to
     * use together because the more-specific active chord suppresses the plain binding.
     */
    public boolean conflictsWith(ControllerBinding other) {
        if (other == null || isEmpty() || other.isEmpty()) {
            return false;
        }
        for (Chord chord : alternatives) {
            if (other.alternatives.contains(chord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true when every currently active alternative in this binding is a strict subset of an active chord in
     * the supplied bindings. This gives combinations predictable precedence over their component buttons.
     */
    public boolean isSupersededBy(Iterable<ControllerBinding> otherBindings, SdlGamepadManager gamepadManager,
        float triggerThreshold) {
        boolean foundActiveAlternative = false;
        for (Chord subject : alternatives) {
            if (!subject.isDown(gamepadManager, triggerThreshold)) {
                continue;
            }
            foundActiveAlternative = true;
            if (!isSuperseded(subject, otherBindings, gamepadManager, triggerThreshold)) {
                return false;
            }
        }
        return foundActiveAlternative;
    }

    int getButtonCount() {
        return buttonCount;
    }

    int getTriggerCount() {
        return triggerCount;
    }

    int getAlternativeCount() {
        return alternatives.size();
    }

    private static boolean isSuperseded(Chord subject, Iterable<ControllerBinding> otherBindings,
        SdlGamepadManager gamepadManager, float triggerThreshold) {
        for (ControllerBinding otherBinding : otherBindings) {
            if (otherBinding == null) {
                continue;
            }
            for (Chord candidate : otherBinding.alternatives) {
                if (candidate.isStrictSupersetOf(subject) && candidate.isDown(gamepadManager, triggerThreshold)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static BindingInput parseInput(String specification) {
        String[] parts = specification.split(":", 2);
        if (parts.length != 2 || parts[1].trim()
            .isEmpty()) {
            throw new IllegalArgumentException("Invalid binding part: " + specification);
        }

        String bindingType = parts[0].trim();
        String inputName = parts[1].trim();
        if ("BUTTON".equals(bindingType)) {
            return BindingInput.forButton(parseButton(inputName));
        }
        if ("TRIGGER".equals(bindingType)) {
            return BindingInput.forTrigger(parseTrigger(inputName));
        }
        throw new IllegalArgumentException("Unknown binding type: " + bindingType);
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

    private static final class Chord {

        private final Set<BindingInput> inputs;

        private Chord(Set<BindingInput> inputs) {
            this.inputs = Collections.unmodifiableSet(new LinkedHashSet<BindingInput>(inputs));
        }

        private boolean isDown(SdlGamepadManager gamepadManager, float triggerThreshold) {
            for (BindingInput input : inputs) {
                if (!input.isDown(gamepadManager, triggerThreshold)) {
                    return false;
                }
            }
            return !inputs.isEmpty();
        }

        private boolean isStrictSupersetOf(Chord other) {
            return inputs.size() > other.inputs.size() && inputs.containsAll(other.inputs);
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Chord && inputs.equals(((Chord) object).inputs);
        }

        @Override
        public int hashCode() {
            return inputs.hashCode();
        }
    }

    private static final class BindingInput {

        private final ControllerButton button;
        private final ControllerAxis trigger;

        private BindingInput(ControllerButton button, ControllerAxis trigger) {
            this.button = button;
            this.trigger = trigger;
        }

        private static BindingInput forButton(ControllerButton button) {
            return new BindingInput(button, null);
        }

        private static BindingInput forTrigger(ControllerAxis trigger) {
            return new BindingInput(null, trigger);
        }

        private boolean isDown(SdlGamepadManager gamepadManager, float triggerThreshold) {
            return button != null ? gamepadManager.isButtonDown(button)
                : gamepadManager.getTrigger(trigger) >= triggerThreshold;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof BindingInput)) {
                return false;
            }
            BindingInput other = (BindingInput) object;
            return button == other.button && trigger == other.trigger;
        }

        @Override
        public int hashCode() {
            return button != null ? button.hashCode() : trigger.hashCode();
        }

        @Override
        public String toString() {
            return button != null ? "BUTTON:" + button.name() : "TRIGGER:" + trigger.name();
        }
    }
}
