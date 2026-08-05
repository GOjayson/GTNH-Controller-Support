package dev.gtnhcontroller.client.input;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.Tags;

public final class ControllerDiagnosticsExporter {

    private ControllerDiagnosticsExporter() {}

    public static File export(Minecraft minecraft, SdlGamepadManager gamepadManager,
        ModKeyBindingController modKeyBindingController) throws IOException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ROOT).format(new Date());
        File reportDirectory = new File(minecraft.mcDataDir, "controller-reports");
        if (!reportDirectory.isDirectory() && !reportDirectory.mkdirs()) {
            throw new IOException("Could not create " + reportDirectory.getAbsolutePath());
        }
        File reportFile = new File(reportDirectory, "gtnh-controller-compatibility-" + timestamp + ".txt");
        Writer writer = new OutputStreamWriter(new FileOutputStream(reportFile), "UTF-8");
        try {
            writer.write(buildReport(minecraft, gamepadManager, modKeyBindingController, new Date()));
        } finally {
            writer.close();
        }
        return reportFile;
    }

    static String buildReport(Minecraft minecraft, SdlGamepadManager gamepadManager,
        ModKeyBindingController modKeyBindingController, Date generatedAt) {
        StringBuilder report = new StringBuilder();
        line(report, "GTNH Controller Support compatibility report");
        line(report, "Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT).format(generatedAt));
        line(report, "Mod version: " + Tags.VERSION);
        line(report, "Minecraft version: 1.7.10");
        line(
            report,
            "Active GUI: " + (minecraft.currentScreen == null ? "None"
                : minecraft.currentScreen.getClass()
                    .getName()));
        line(report, "");
        line(report, "Controller");
        line(report, "Status: " + gamepadManager.getStatusLine());
        line(report, "Name: " + gamepadManager.getGamepadName());
        line(report, "SDL instance ID: " + gamepadManager.getGamepadInstanceId());
        line(
            report,
            "Battery: " + gamepadManager.getBatteryStatus()
                .getDisplayText());
        line(report, "Rumble supported: " + gamepadManager.supportsRumble());
        line(report, "SDL mapping: " + gamepadManager.getGamepadMapping());
        line(report, "Reported inputs: " + gamepadManager.getCapabilityLine());
        line(report, "Live axes: " + gamepadManager.getAxisLine());
        line(report, "Pressed buttons: " + gamepadManager.getButtonsLine());
        line(report, "");
        line(report, "Core bindings");
        for (ControllerAction action : ControllerAction.values()) {
            line(report, action.configKey + " = " + Config.getBinding(action, ControllerBindingLayer.PRIMARY));
            if (!action.guiAction && action != ControllerAction.MODIFIER_LAYER) {
                line(
                    report,
                    action.configKey + " [modifier] = " + Config.getBinding(action, ControllerBindingLayer.MODIFIER));
            }
        }
        line(report, "");
        line(report, "Minecraft, mod and NEI bindings");
        List<RegisteredKeyBinding> registeredBindings = modKeyBindingController.getRegisteredBindings();
        for (RegisteredKeyBinding binding : registeredBindings) {
            String primary = Config.getModKeyBinding(binding.getIdentifier(), ControllerBindingLayer.PRIMARY);
            String modifier = Config.getModKeyBinding(binding.getIdentifier(), ControllerBindingLayer.MODIFIER);
            if (!"NONE".equalsIgnoreCase(primary) || !"NONE".equalsIgnoreCase(modifier)) {
                line(report, binding.getCategoryName() + " / " + binding.getDisplayName());
                line(report, "  identifier = " + binding.getIdentifier());
                line(report, "  primary = " + primary);
                line(report, "  modifier = " + modifier);
            }
        }
        line(report, "");
        line(report, "Settings");
        line(report, "Gameplay controls: " + Config.enableGameplayControls);
        line(report, "GUI controls: " + Config.enableGuiControls);
        line(
            report,
            "Movement/camera/cursor sensitivity: " + Config.moveSensitivity
                + "/"
                + Config.lookSensitivity
                + "/"
                + Config.cursorSensitivity);
        line(
            report,
            "Movement/camera/cursor deadzone: " + Config.moveDeadZone
                + "/"
                + Config.lookDeadZone
                + "/"
                + Config.cursorDeadZone);
        line(report, "Trigger threshold: " + Config.triggerThreshold);
        line(
            report,
            "Scroll acceleration: " + Config.scrollAccelerationEnabled
                + " at "
                + Config.scrollAccelerationMultiplier
                + "x");
        line(report, "Rumble: " + Config.rumbleEnabled + " at " + Config.rumbleIntensity);
        return report.toString();
    }

    private static void line(StringBuilder report, String value) {
        report.append(value)
            .append(System.lineSeparator());
    }
}
