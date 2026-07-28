# Configuration

Most settings can be changed in-game under `Options -> Controls -> Controller...` and apply immediately.

The full configuration file is `config\gtnhcontroller.cfg`. Start the game once before editing it manually.

## Controller settings

The main screen contains:

- **Gameplay Controls:** enables or disables in-world controller input.
- **GUI Controls:** enables or disables controller input in menus.
- **Auto Jump:** automatically jumps over a one-block rise when there is enough clearance.
- **Gameplay Bindings:** edits the core in-world actions.
- **Modes:** edits accessibility activation behavior.
- **GUI Bindings:** edits cursor, click, navigation and scrolling actions.
- **Sensitivity:** edits movement response, camera sensitivity and cursor sensitivity.
- **Navigation:** edits directional target types, repeat timing and precision-cursor speed.
- **Minecraft & Mod:** maps controller inputs to registered Minecraft and mod keybindings.
- **Radial Menu:** assigns up to eight registered actions to radial directions.

## Accessibility activation modes

Swim supports:

- `HOLD`: rise while Jump is held.
- `TOGGLE`: press Jump once to keep rising and again to sink.

Sneak, Sprint, Attack and Use support:

- `HOLD`: follow the physical input.
- `TOGGLE`: latch on the first press and release on the next.
- `PRESS`: emit one game tick per physical press.

Latched actions reset whenever a menu opens, focus is lost, gameplay controls are disabled or the controller
disconnects.

## Sensitivity

Movement response, camera sensitivity and cursor sensitivity can be adjusted from 25% to 500%.

Movement Response shapes partial-stick input without reducing the maximum speed reached at full stick deflection.
Lower values provide finer low-speed control; higher values react more aggressively.

## Minecraft and mod bindings

The mod lists actions registered in Minecraft's normal `KeyBinding` registry. Search and category filtering are
available in-game.

Core actions already handled by the controller gameplay page are excluded to prevent two systems from driving the
same action. A red `!` marks an input shared with another gameplay binding.

Mods that read raw LWJGL keyboard state instead of registered keybindings may require dedicated compatibility code.

## Radial menu

Hold the configured Radial Action Menu input, aim with the right stick or D-pad and release to activate the selected
action once. Return the stick to the center or press Back to cancel.

Slots are empty by default. Missing actions from removed mods are displayed as missing and never silently replaced
with a similarly named action.

## Manual configuration reference

### Debug

- `showDebugOverlay`: append controller status, axes and pressed buttons to the F3 screen.

### Controller

- `rescanIntervalTicks`: how often to scan while no controller is connected.

### Controls

- `enableGameplayControls`: enable in-world mappings.
- `autoJump`: enable one-block-rise assistance.
- `autoSwim`: legacy migration value; new configurations use `activationModes.swim`.
- `moveSensitivity`: shape partial left-stick response.
- `lookSensitivity`: scale camera rotation speed.
- `moveDeadZone` and `lookDeadZone`: suppress stick drift.
- `moveCurveExponent` and `lookCurveExponent`: tune precision near stick center.
- `lookSpeed`: maximum camera rotation in degrees per second.
- `triggerThreshold`: trigger travel required before Attack or Use becomes active.
- `invertLookY`: invert vertical camera movement.

### Activation Modes

- `swim`: `HOLD` or `TOGGLE`.
- `sneak`, `sprint`, `attack`, `use`: `HOLD`, `TOGGLE` or `PRESS`.

### GUI

- `enableGuiControls`: enable controller input in menus.
- `cursorSensitivity`: scale the configured cursor speed.
- `cursorStick`: `LEFT` or `RIGHT`.
- `cursorDeadZone` and `cursorCurveExponent`: tune cursor drift and precision.
- `cursorSpeed`: maximum cursor speed in display pixels per second.
- `cursorAcceleration`: how gradually the cursor reaches its requested speed.
- `cursorDeceleration`: how quickly the cursor stops or reverses.

### GUI Navigation

- `enableButtonNavigation`: include visible enabled buttons as D-pad targets.
- `enableSlotNavigation`: include visible inventory slots as D-pad targets.
- `precisionCursorScale`: cursor-speed multiplier while Precision is held.
- `navigationInitialDelayMillis`: delay before held navigation begins repeating.
- `navigationRepeatIntervalMillis`: interval between repeated navigation or scrolling actions.

## Binding syntax

Manual bindings accept:

```text
BUTTON:SOUTH
TRIGGER:RIGHT_TRIGGER
BUTTON:LEFT_SHOULDER|BUTTON:DPAD_LEFT
NONE
```

The `|` character means either input. Names are case-insensitive.

Standard button names include `SOUTH`, `EAST`, `WEST`, `NORTH`, `BACK`, `GUIDE`, `START`, both stick clicks, both
shoulders, all D-pad directions, `TOUCHPAD`, `MISC1` through `MISC6`, and the four paddle names. Trigger names are
`LEFT_TRIGGER` and `RIGHT_TRIGGER`.

Changes made directly in the configuration file require a restart. Changes made through the in-game screens apply
immediately.
