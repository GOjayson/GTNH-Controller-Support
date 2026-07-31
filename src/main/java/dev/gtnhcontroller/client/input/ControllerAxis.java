package dev.gtnhcontroller.client.input;

import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_LEFTX;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_LEFTY;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTX;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTY;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER;

import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

/**
 * Keeps SDL3 constants out of Minecraft-facing classes, which must remain eligible for lwjgl3ify's LWJGL2 input
 * compatibility transformation.
 */
@Lwjgl3Aware
public enum ControllerAxis {

    LEFT_X(SDL_GAMEPAD_AXIS_LEFTX),
    LEFT_Y(SDL_GAMEPAD_AXIS_LEFTY),
    RIGHT_X(SDL_GAMEPAD_AXIS_RIGHTX),
    RIGHT_Y(SDL_GAMEPAD_AXIS_RIGHTY),
    LEFT_TRIGGER(SDL_GAMEPAD_AXIS_LEFT_TRIGGER),
    RIGHT_TRIGGER(SDL_GAMEPAD_AXIS_RIGHT_TRIGGER);

    final int sdlIndex;

    ControllerAxis(int sdlIndex) {
        this.sdlIndex = sdlIndex;
    }

    public boolean isTrigger() {
        return this == LEFT_TRIGGER || this == RIGHT_TRIGGER;
    }

    public String getDisplayName() {
        String[] words = name().split("_");
        StringBuilder displayName = new StringBuilder();
        for (String word : words) {
            if (displayName.length() > 0) {
                displayName.append(' ');
            }
            displayName.append(word.charAt(0));
            displayName.append(
                word.substring(1)
                    .toLowerCase(java.util.Locale.ROOT));
        }
        return displayName.toString();
    }
}
