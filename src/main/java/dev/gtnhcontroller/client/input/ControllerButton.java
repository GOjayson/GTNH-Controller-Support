package dev.gtnhcontroller.client.input;

import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_BACK;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_DOWN;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_LEFT;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_RIGHT;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_EAST;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_GUIDE;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_PADDLE1;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_PADDLE2;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_STICK;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_MISC1;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_MISC2;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_MISC3;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_MISC4;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_MISC5;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_MISC6;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_NORTH;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_PADDLE1;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_PADDLE2;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_STICK;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_SOUTH;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_START;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_TOUCHPAD;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_WEST;

import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

/**
 * SDL's standardized gamepad buttons. Paddles and miscellaneous buttons only work when the controller and its SDL
 * mapping expose them as independent inputs.
 */
@Lwjgl3Aware
public enum ControllerButton {

    SOUTH(SDL_GAMEPAD_BUTTON_SOUTH),
    EAST(SDL_GAMEPAD_BUTTON_EAST),
    WEST(SDL_GAMEPAD_BUTTON_WEST),
    NORTH(SDL_GAMEPAD_BUTTON_NORTH),
    BACK(SDL_GAMEPAD_BUTTON_BACK),
    GUIDE(SDL_GAMEPAD_BUTTON_GUIDE),
    START(SDL_GAMEPAD_BUTTON_START),
    LEFT_STICK(SDL_GAMEPAD_BUTTON_LEFT_STICK),
    RIGHT_STICK(SDL_GAMEPAD_BUTTON_RIGHT_STICK),
    LEFT_SHOULDER(SDL_GAMEPAD_BUTTON_LEFT_SHOULDER),
    RIGHT_SHOULDER(SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER),
    DPAD_UP(SDL_GAMEPAD_BUTTON_DPAD_UP),
    DPAD_DOWN(SDL_GAMEPAD_BUTTON_DPAD_DOWN),
    DPAD_LEFT(SDL_GAMEPAD_BUTTON_DPAD_LEFT),
    DPAD_RIGHT(SDL_GAMEPAD_BUTTON_DPAD_RIGHT),
    MISC1(SDL_GAMEPAD_BUTTON_MISC1),
    RIGHT_PADDLE1(SDL_GAMEPAD_BUTTON_RIGHT_PADDLE1),
    LEFT_PADDLE1(SDL_GAMEPAD_BUTTON_LEFT_PADDLE1),
    RIGHT_PADDLE2(SDL_GAMEPAD_BUTTON_RIGHT_PADDLE2),
    LEFT_PADDLE2(SDL_GAMEPAD_BUTTON_LEFT_PADDLE2),
    TOUCHPAD(SDL_GAMEPAD_BUTTON_TOUCHPAD),
    MISC2(SDL_GAMEPAD_BUTTON_MISC2),
    MISC3(SDL_GAMEPAD_BUTTON_MISC3),
    MISC4(SDL_GAMEPAD_BUTTON_MISC4),
    MISC5(SDL_GAMEPAD_BUTTON_MISC5),
    MISC6(SDL_GAMEPAD_BUTTON_MISC6);

    final int sdlIndex;

    ControllerButton(int sdlIndex) {
        this.sdlIndex = sdlIndex;
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
