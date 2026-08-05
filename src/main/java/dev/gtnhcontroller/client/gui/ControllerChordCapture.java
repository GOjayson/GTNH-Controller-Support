package dev.gtnhcontroller.client.gui;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Accumulates every controller input held during a binding capture and completes when all inputs are released. */
final class ControllerChordCapture {

    private final Set<String> capturedInputs = new LinkedHashSet<String>();
    private boolean started;

    String update(List<String> inputsDown) {
        if (!inputsDown.isEmpty()) {
            started = true;
            capturedInputs.addAll(inputsDown);
            return null;
        }
        return started ? specification() : null;
    }

    boolean hasStarted() {
        return started;
    }

    String displayValue() {
        return capturedInputs.isEmpty() ? "Press combination" : ControllerBindingDisplay.format(specification());
    }

    void reset() {
        capturedInputs.clear();
        started = false;
    }

    private String specification() {
        StringBuilder specification = new StringBuilder();
        for (String input : capturedInputs) {
            if (specification.length() > 0) {
                specification.append('+');
            }
            specification.append(input);
        }
        return specification.toString();
    }
}
