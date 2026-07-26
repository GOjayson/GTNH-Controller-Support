package dev.gtnhcontroller.client.input;

final class SprintControl {

    enum Decision {
        UNCHANGED,
        START,
        STOP
    }

    private SprintControl() {}

    static Decision decide(ActivationMode mode, boolean controllerActive, boolean controllerOwned, boolean canStart) {
        if (mode == ActivationMode.PRESS) {
            if (controllerOwned) {
                return Decision.STOP;
            }
            return controllerActive && canStart ? Decision.START : Decision.UNCHANGED;
        }
        if (controllerActive) {
            if (canStart) {
                return Decision.START;
            }
            return controllerOwned ? Decision.STOP : Decision.UNCHANGED;
        }
        return controllerOwned ? Decision.STOP : Decision.UNCHANGED;
    }
}
