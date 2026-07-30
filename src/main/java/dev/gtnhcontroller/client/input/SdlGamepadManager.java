package dev.gtnhcontroller.client.input;

import static org.lwjgl.sdl.SDLError.SDL_GetError;
import static org.lwjgl.sdl.SDLGamepad.SDL_CloseGamepad;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_COUNT;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_LEFTX;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_LEFTY;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTX;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTY;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_COUNT;
import static org.lwjgl.sdl.SDLGamepad.SDL_GamepadConnected;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepadAxis;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepadButton;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepadName;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepadStringForButton;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepads;
import static org.lwjgl.sdl.SDLGamepad.SDL_OpenGamepad;
import static org.lwjgl.sdl.SDLStdinc.SDL_free;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.GTNHController;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

/**
 * Thin SDL3 device layer. Keeping SDL calls in one annotated class prevents lwjgl3ify from redirecting these
 * intentional LWJGL3 references to its LWJGL2 compatibility package.
 */
@Lwjgl3Aware
public final class SdlGamepadManager {

    private final int rescanIntervalTicks;
    private final short[] axes = new short[SDL_GAMEPAD_AXIS_COUNT];
    private final short[] previousAxes = new short[SDL_GAMEPAD_AXIS_COUNT];
    private final boolean[] buttons = new boolean[SDL_GAMEPAD_BUTTON_COUNT];
    private final boolean[] previousButtons = new boolean[SDL_GAMEPAD_BUTTON_COUNT];

    private int ticksUntilScan;
    private int gamepadInstanceId = -1;
    private long gamepad = NULL;
    private String gamepadName = "";

    public SdlGamepadManager(int rescanIntervalTicks) {
        this.rescanIntervalTicks = rescanIntervalTicks;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        if (gamepad != NULL && !SDL_GamepadConnected(gamepad)) {
            disconnect();
        }

        if (gamepad == NULL) {
            if (ticksUntilScan > 0) {
                ticksUntilScan--;
                return;
            }

            ticksUntilScan = rescanIntervalTicks;
            connectSelectedGamepad();
        }

        if (gamepad != NULL) {
            pollState();
        }
    }

    public boolean isConnected() {
        return gamepad != NULL;
    }

    public String getStatusLine() {
        if (isConnected()) {
            return "Connected: " + gamepadName;
        }
        return ControllerSelection.isAutomatic(Config.controllerSelection) ? "No gamepad detected"
            : "Waiting for selected controller";
    }

    public List<ControllerDevice> getAvailableGamepads() {
        List<ControllerDevice> devices = new ArrayList<ControllerDevice>();
        IntBuffer gamepads = SDL_GetGamepads();
        if (gamepads == null) {
            GTNHController.LOG.warn("SDL_GetGamepads failed: {}", SDL_GetError());
            return devices;
        }

        Map<String, Integer> occurrences = new HashMap<String, Integer>();
        try {
            int firstIndex = gamepads.position();
            for (int index = 0; index < gamepads.remaining(); index++) {
                int instanceId = gamepads.get(firstIndex + index);
                String name = readGamepadName(instanceId);
                if (name == null) {
                    continue;
                }
                int occurrence = nextOccurrence(occurrences, name);
                String displayName = occurrence == 1 ? name : name + " (" + occurrence + ")";
                devices.add(
                    new ControllerDevice(
                        ControllerSelection.createKey(name, occurrence),
                        displayName,
                        instanceId == gamepadInstanceId));
            }
        } finally {
            SDL_free(gamepads);
        }
        return devices;
    }

    public void selectController(String selectionKey) {
        Config.controllerSelection = ControllerSelection.isAutomatic(selectionKey) ? ControllerSelection.AUTOMATIC
            : selectionKey;
        Config.saveControllerSettings();
        disconnect();
        ticksUntilScan = 0;
        connectSelectedGamepad();
        if (gamepad != NULL) {
            pollState();
        }
    }

    public float getAxis(int axis) {
        return isValidAxis(axis) ? InputMath.normalizeSignedAxis(axes[axis]) : 0.0F;
    }

    public float getAxis(ControllerAxis axis) {
        return getAxis(axis.sdlIndex);
    }

    public float getTrigger(int axis) {
        return isValidAxis(axis) ? InputMath.normalizeTrigger(axes[axis]) : 0.0F;
    }

    public float getTrigger(ControllerAxis axis) {
        return getTrigger(axis.sdlIndex);
    }

    public boolean isButtonDown(int button) {
        return isValidButton(button) && buttons[button];
    }

    public boolean isButtonDown(ControllerButton button) {
        return isButtonDown(button.sdlIndex);
    }

    public boolean wasButtonPressed(int button) {
        return isValidButton(button) && buttons[button] && !previousButtons[button];
    }

    public boolean wasButtonPressed(ControllerButton button) {
        return wasButtonPressed(button.sdlIndex);
    }

    public boolean hasBindableInputDown(float triggerThreshold) {
        for (ControllerButton button : ControllerButton.values()) {
            if (isButtonDown(button)) {
                return true;
            }
        }
        return getTrigger(ControllerAxis.LEFT_TRIGGER) >= triggerThreshold
            || getTrigger(ControllerAxis.RIGHT_TRIGGER) >= triggerThreshold;
    }

    public String getNewBindableInput(float triggerThreshold) {
        for (ControllerButton button : ControllerButton.values()) {
            if (wasButtonPressed(button)) {
                return "BUTTON:" + button.name();
            }
        }
        if (wasTriggerPressed(ControllerAxis.LEFT_TRIGGER, triggerThreshold)) {
            return "TRIGGER:" + ControllerAxis.LEFT_TRIGGER.name();
        }
        if (wasTriggerPressed(ControllerAxis.RIGHT_TRIGGER, triggerThreshold)) {
            return "TRIGGER:" + ControllerAxis.RIGHT_TRIGGER.name();
        }
        return null;
    }

    public String getAxisLine() {
        return String.format(
            Locale.ROOT,
            "LS %.2f/%.2f  RS %.2f/%.2f  LT %.2f  RT %.2f",
            getAxis(SDL_GAMEPAD_AXIS_LEFTX),
            getAxis(SDL_GAMEPAD_AXIS_LEFTY),
            getAxis(SDL_GAMEPAD_AXIS_RIGHTX),
            getAxis(SDL_GAMEPAD_AXIS_RIGHTY),
            getTrigger(SDL_GAMEPAD_AXIS_LEFT_TRIGGER),
            getTrigger(SDL_GAMEPAD_AXIS_RIGHT_TRIGGER));
    }

    public String getButtonsLine() {
        StringBuilder pressed = new StringBuilder();

        for (int button = 0; button < buttons.length; button++) {
            if (!buttons[button]) {
                continue;
            }

            if (pressed.length() > 0) {
                pressed.append(", ");
            }

            String name = SDL_GetGamepadStringForButton(button);
            pressed.append(name == null ? Integer.toString(button) : name);
        }

        return pressed.length() == 0 ? "-" : pressed.toString();
    }

    private void connectSelectedGamepad() {
        IntBuffer gamepads = SDL_GetGamepads();
        if (gamepads == null) {
            GTNHController.LOG.warn("SDL_GetGamepads failed: {}", SDL_GetError());
            return;
        }

        try {
            Map<String, Integer> occurrences = new HashMap<String, Integer>();
            int firstIndex = gamepads.position();
            for (int index = 0; index < gamepads.remaining(); index++) {
                int instanceId = gamepads.get(firstIndex + index);
                long openedGamepad = SDL_OpenGamepad(instanceId);
                if (openedGamepad == NULL) {
                    GTNHController.LOG.warn("Could not open SDL gamepad {}: {}", instanceId, SDL_GetError());
                    continue;
                }

                String detectedName = SDL_GetGamepadName(openedGamepad);
                String name = detectedName == null ? "Unknown controller" : detectedName;
                int occurrence = nextOccurrence(occurrences, name);
                String selectionKey = ControllerSelection.createKey(name, occurrence);
                if (!ControllerSelection.isAutomatic(Config.controllerSelection)
                    && !Config.controllerSelection.equals(selectionKey)) {
                    SDL_CloseGamepad(openedGamepad);
                    continue;
                }

                gamepad = openedGamepad;
                gamepadInstanceId = instanceId;
                gamepadName = name;
                GTNHController.LOG.info("Connected SDL gamepad {} ({})", gamepadName, instanceId);
                return;
            }
        } finally {
            SDL_free(gamepads);
        }
    }

    private String readGamepadName(int instanceId) {
        if (instanceId == gamepadInstanceId && gamepad != NULL) {
            return gamepadName;
        }

        long openedGamepad = SDL_OpenGamepad(instanceId);
        if (openedGamepad == NULL) {
            GTNHController.LOG.warn("Could not inspect SDL gamepad {}: {}", instanceId, SDL_GetError());
            return null;
        }
        try {
            String detectedName = SDL_GetGamepadName(openedGamepad);
            return detectedName == null ? "Unknown controller" : detectedName;
        } finally {
            SDL_CloseGamepad(openedGamepad);
        }
    }

    private static int nextOccurrence(Map<String, Integer> occurrences, String name) {
        Integer previous = occurrences.get(name);
        int occurrence = previous == null ? 1 : previous.intValue() + 1;
        occurrences.put(name, Integer.valueOf(occurrence));
        return occurrence;
    }

    private void pollState() {
        System.arraycopy(axes, 0, previousAxes, 0, axes.length);
        System.arraycopy(buttons, 0, previousButtons, 0, buttons.length);

        for (int axis = 0; axis < axes.length; axis++) {
            axes[axis] = SDL_GetGamepadAxis(gamepad, axis);
        }
        for (int button = 0; button < buttons.length; button++) {
            buttons[button] = SDL_GetGamepadButton(gamepad, button);
        }
    }

    private boolean isValidAxis(int axis) {
        return axis >= 0 && axis < axes.length;
    }

    private boolean isValidButton(int button) {
        return button >= 0 && button < buttons.length;
    }

    private boolean wasTriggerPressed(ControllerAxis trigger, float threshold) {
        int axis = trigger.sdlIndex;
        return isValidAxis(axis) && InputMath.normalizeTrigger(axes[axis]) >= threshold
            && InputMath.normalizeTrigger(previousAxes[axis]) < threshold;
    }

    private void disconnect() {
        if (gamepad != NULL) {
            GTNHController.LOG.info("Disconnected SDL gamepad {}", gamepadName);
            SDL_CloseGamepad(gamepad);
        }
        gamepad = NULL;
        gamepadInstanceId = -1;
        gamepadName = "";

        for (int axis = 0; axis < axes.length; axis++) {
            axes[axis] = 0;
            previousAxes[axis] = 0;
        }
        for (int button = 0; button < buttons.length; button++) {
            buttons[button] = false;
            previousButtons[button] = false;
        }
    }
}
