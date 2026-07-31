# Configuration

Most settings can be changed in-game under `Options -> Controls -> Controller support` and apply immediately.

The full configuration file is `config\gtnhcontroller.cfg`. Start the game once before editing it manually.

## Controller settings

The main screen contains:

- **Controller Setup & Test:** opens controller selection, calibration, live input testing, rumble configuration and
  profile import/export.
- **Gameplay Controls:** enables or disables in-world controller input.
- **GUI Controls:** enables or disables controller input in menus.
- **Auto Jump:** automatically jumps over a one-block rise when there is enough clearance.
- **Gameplay Bindings:** searches and edits the core in-world actions.
- **Accessibility modes:** edits accessibility activation behavior.
- **GUI Bindings:** searches and edits cursor, click, navigation and scrolling actions.
- **Sensitivity:** edits movement response, camera sensitivity and cursor sensitivity.
- **Deadzones:** separately edits movement, camera, cursor and trigger deadzones.
- **Axis Inversion:** independently inverts camera X/Y and GUI cursor X/Y.
- **Navigation:** edits directional target types, repeat timing and precision-cursor speed.
- **Minecraft & Mod Bindings:** maps controller inputs to registered Minecraft and mod keybindings.
- **Radial Menu:** assigns up to 24 registered actions or chat macros across Base, Hold LB and Hold RB pages and
  selects Hold or Toggle opening behavior.

## Controller profiles

Open `Controller Setup & Test -> Profile Import & Export`. Enter a name and choose `Export Current` to create a
shareable `.cfg` snapshot in `config/gtnhcontroller-profiles`. Copy another exported `.cfg` file into that folder,
reopen the screen and select it to import.

Profiles include controller bindings, sensitivities, deadzones, axis inversion, accessibility modes, navigation,
rumble, radial slots and chat macros. The physical controller selected on the current computer is deliberately
preserved during import. Core and Minecraft/mod bindings reload immediately.

Every import first creates a timestamped `before-import-*.cfg` safety backup in the same directory. Existing named
exports are never silently overwritten. Because macro message text is included, review macros before sharing a
profile publicly.

## Calibration and input testing

Open `Controller Setup & Test -> Calibration Wizard`. The first phase measures untouched stick/trigger drift for
three seconds. Put the controller on a flat surface and do not touch it. The second phase asks you to rotate both
sticks and fully press both triggers.

The result recommends movement, camera and cursor deadzones plus a trigger threshold. Nothing is changed until
`Apply Suggestions` is selected. If a control does not reach 75%, run the wizard again and make sure it is moved
through its full range.

`Test Controller Inputs` shows raw values, values after the current deadzones/curves, trigger threshold results, and
every standardized SDL button. A gray button is not exposed by the controller's SDL mapping; this distinction is
especially useful for rear paddles. Hold the configured GUI Back input for three seconds to leave the capture-only
test screen.

## Rumble

`Controller Setup & Test -> Rumble Feedback` contains:

- a master rumble switch;
- independent Damage, Explosions and Mining switches;
- a global 0–100% intensity;
- a Test Rumble button.

Rumble settings have no effect when the selected controller does not report SDL rumble support. Nearby explosions
scale by distance, mining uses a light pulse, and stronger effects temporarily take priority over mining feedback.

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
same action. A red `!` marks an input shared with another gameplay binding; hover it with the mouse or virtual cursor
to see every conflicting action by name.

Both Gameplay Bindings and Minecraft & Mod Bindings have Primary and Modifier layers. First bind the core
`Modifier Layer` action on the Primary gameplay page, then switch either editor to `Layer: Modifier` and add the
alternate mappings. The modifier is active only while held and defaults to `NONE`, so upgrading does not replace any
existing controls.

Mods that read raw LWJGL keyboard state instead of registered keybindings may require dedicated compatibility code.

## Radial menu

The radial settings screen offers two opening modes:

- `HOLD`: hold the configured Radial Action Menu input, aim with the right stick or D-pad and release to activate.
- `TOGGLE`: press once to open, choose an action, then press GUI Confirm. GUI Back or a second radial-menu press closes
  the menu without activating anything.

Hold Left Shoulder or Right Shoulder for the corresponding extra page; release the shoulder to return to Base. If
both shoulders are held, Base is used. Return the stick to the center or press Back to cancel in Hold mode.

Slots are empty by default. Missing actions from removed mods are displayed as missing and never silently replaced
with a similarly named action.

`Chat Macros` creates, edits and deletes named messages or commands. Choose `Chat Macros` while assigning a radial
slot to place one there. Selecting that slot sends its single configured line exactly once. Macro messages are
limited to 100 characters and cannot contain line breaks; there are no automatic triggers, delays, loops or
multi-command scripting.

## Manual configuration reference

### Debug

- `showDebugOverlay`: append controller status, axes and pressed buttons to the F3 screen.

### Controller

- `rescanIntervalTicks`: how often to scan while no controller is connected.
- `selected`: `AUTO` or the stable name/occurrence key selected by the in-game controller screen.

### Controls

- `enableGameplayControls`: enable in-world mappings.
- `autoJump`: enable one-block-rise assistance.
- `autoSwim`: legacy migration value; new configurations use `activationModes.swim`.
- `moveSensitivity`: shape partial left-stick response.
- `lookSensitivity`: scale camera rotation speed.
- `moveDeadZone` and `lookDeadZone`: suppress stick drift.
- `moveCurveExponent` and `lookCurveExponent`: tune precision near stick center.
- `lookSpeed`: maximum camera rotation in degrees per second.
- `triggerThreshold`: trigger deadzone before a trigger binding becomes active.
- `invertLookX` and `invertLookY`: independently invert camera movement.

### Accessibility Modes

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
- `invertCursorX` and `invertCursorY`: independently invert GUI cursor movement.

### Binding layers

- `bindings`: primary core gameplay and GUI bindings.
- `modifierBindings`: alternate core gameplay bindings used while Modifier Layer is held.
- `modBindings`: primary registered Minecraft/mod bindings.
- `modifierModBindings`: alternate registered Minecraft/mod bindings.

### GUI Navigation

- `enableButtonNavigation`: include visible enabled buttons as D-pad targets.
- `enableSlotNavigation`: include visible inventory slots as D-pad targets.
- `precisionCursorScale`: cursor-speed multiplier while Precision is held.
- `navigationInitialDelayMillis`: delay before held navigation begins repeating.
- `navigationRepeatIntervalMillis`: interval between repeated navigation or scrolling actions.

### Rumble

- `enabled`: master rumble switch.
- `damage`: damage feedback.
- `explosions`: nearby explosion feedback.
- `mining`: periodic block-mining feedback.
- `intensity`: global effect multiplier from `0.0` to `1.0`.

### Radial menu and chat macros

- `activationMode`: `HOLD` or `TOGGLE`.
- `entries`, `leftShoulderEntries` and `rightShoulderEntries`: encoded radial assignments.
- `chatMacros.entries`: encoded macro names and their single chat message or command.

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
