package dev.gtnhcontroller.client.gui;

/**
 * Lets the normal GUI controller pause click/back handling while a settings screen captures a raw controller input.
 */
public interface ControllerInputCaptureScreen extends ControllerConfigurationScreen {

    boolean isCapturingControllerInput();
}
