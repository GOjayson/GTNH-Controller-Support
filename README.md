# GTNH Controller Support

Accessibility-focused controller support for GregTech: New Horizons on Minecraft 1.7.10.

Version 1.0.0 is the first public release. It provides analog in-world controls plus a controller-driven mouse cursor
for inventories, menus, and machine interfaces. The on-screen keyboard remains experimental and is listed clearly
under Known issues.

## Current milestone

* Detect the first SDL3-compatible gamepad.
* Handle connection and disconnection without restarting Minecraft.
* Read standardized SDL gamepad axes, buttons, and analog triggers.
* Provide true analog 360-degree movement with radial dead zones and response curves.
* Control the camera with the right stick.
* Map jump, sneak, sprint, attack, use, hotbar selection, inventory, and pause.
* Configure every gameplay and GUI button action through named bindings.
* Discover registered Minecraft and mod key actions and expose them in a searchable, category-filtered controller
binding screen.
* Preserve duplicate registered actions as separate entries and warn when one controller input activates multiple
gameplay actions.
* Bind SDL-exposed rear paddles and miscellaneous controller buttons.
* Rebind gameplay and GUI actions from `Options -> Controls -> Controller...`, with changes applied immediately.
* Move a virtual GUI cursor, left-click, right-click, drag items, and send Escape from the controller.
* Select and launch vanilla singleplayer worlds with the virtual cursor.
* Dispatch controller clicks to BetterQuesting's custom quest-book panels and buttons.
* Accelerate and decelerate the GUI cursor for precise movement between small menu buttons and slots.
* Scroll vanilla lists and BetterQuesting panels from configurable controller actions.
* Navigate directionally between visible buttons and inventory slots with independently toggleable target types.
* Hold a configurable precision action to slow the GUI cursor for small targets.
* Configure navigation repeat timing and precision speed from the in-game controller settings.
* Open an eight-slot radial menu with the right stick or D-pad for registered Minecraft and mod actions without
assigning eight controller buttons.
* Configure every radial direction through a searchable, category-filtered action picker.
* Show an experimental controller-only on-screen keyboard without replacing or closing the active GUI.
* Optionally jump over one-block rises when moving into them.
* Choose Hold or Toggle behavior for swimming upward.
* Choose Hold, Toggle, or single-tick Press behavior independently for Sneak, Sprint, Attack, and Use.
* Adjust the partial-stick movement response, camera sensitivity, and GUI cursor sensitivity in-game from 25% to
500%, while preserving full movement speed at full stick deflection.
* Trigger vanilla's inventory-open achievement when the controller opens the inventory.
* Hide and reposition the operating-system cursor while the controller owns the virtual cursor, keeping direct mouse
queries and item tooltips aligned with it.
* Merge controller crouching directly into vanilla's movement input so it cannot be lost through key-state timing.
* Preserve physical mouse movement and hand control back to it as soon as it actually moves.
* Release held controller actions safely when the game loses focus, opens a GUI, or disconnects the controller.
* Append the controller name, live axis values, and pressed buttons to the F3 debug screen without cluttering normal
gameplay.
* Require lwjgl3ify 3.0.0 or newer so unsupported GTNH versions fail clearly.

Multiple saved controller profiles are not implemented yet.

## Known issues

* The on-screen keyboard opens, navigates, switches layouts, and sends Backspace, but printable characters still do
not enter focused text fields in the real GTNH 2.9 test pack. Several vanilla and NEI dispatch routes have been
attempted without fixing the runtime behavior. The feature remains included for further debugging, but it must not
be presented as working accessibility support yet.

## Default gameplay controls

SDL uses button positions, so the Xbox labels below also work sensibly with PlayStation, 8BitDo, and other
SDL-compatible controllers. An 8BitDo controller may display different letters in Switch mode, but the physical
positions remain consistent.

|Input|Action|
|-|-|
|Left stick|Analog movement|
|Right stick|Camera|
|South / Xbox A|Jump|
|East / Xbox B|Sneak|
|Left-stick click|Sprint|
|Right trigger|Attack or mine|
|Left trigger|Use item or place block|
|Left shoulder / D-pad left|Previous hotbar slot|
|Right shoulder / D-pad right|Next hotbar slot|
|North / Xbox Y|Open inventory|
|Start|Pause menu|
|Back / Xbox View|Hold radial action menu|

Drop and other ordinary Minecraft/mod actions remain unmapped by default. They can be assigned deliberately under
`Options -> Controls -> Controller... -> Minecraft \& Mod Bindings...`.

## Default GUI controls

|Input|Action|
|-|-|
|Right stick|Move the mouse cursor|
|South / Xbox A|Left-click and drag|
|West / Xbox X|Right-click and drag|
|East / Xbox B|Back / Escape|
|North / Xbox Y|Open or close the on-screen keyboard|
|D-pad|Navigate between visible buttons and inventory slots|
|Left / right shoulder|Scroll up / down in supported screens|
|Right-stick click|Precision cursor while held|

Set `cursorStick=LEFT` if the player controls the cursor more comfortably with the left stick. The physical mouse
continues to work and takes over as soon as it moves. Once the controller moves the cyan cursor, the operating-system
cursor is hidden and repositioned underneath it. This keeps vanilla, NEI, and mod GUIs that read the native mouse
position aligned with the visible controller cursor.

Sneak and GUI Back both use Xbox B by default, but they are context-separated: Sneak is read only with no GUI open,
and GUI Back is read only while a screen is open. They can still be rebound independently.

## Supported test target

Use a separate GTNH 2.9.0 beta/nightly test instance. GTNH 2.8.4 ships lwjgl3ify 2.1.x and does not provide the SDL3
API used by this mod.

Do not test an experimental mod in your only GTNH instance or your only copy of a world.

## Windows development setup

Install:

1. Git for Windows.
2. A full JDK 25 installation. A JRE alone is not enough.
3. IntelliJ IDEA Community or Ultimate.
4. Prism Launcher.
5. A separate GTNH 2.9.0 beta/nightly Java 25 instance for final pack testing.

For the first controller test, connect by USB and disable Steam Input for Minecraft. Steam Input can otherwise turn
the same controller action into a second mouse/keyboard action and make debugging results useless. Xbox controllers
and SDL-supported 8BitDo, PlayStation, Nintendo, and generic controllers can all work; SDL's standardized gamepad
mapping determines which extra/non-standard buttons are available.

## Prepare this project

Open PowerShell in the extracted project folder:

```powershell
git init
git add .
git commit -m "Prepare first public release"
git tag 1.0.0
.\\gradlew.bat setupDecompWorkspace
.\\gradlew.bat spotlessApply
.\\gradlew.bat build
```

The initial Git tag is required by the GTNH build scripts for version detection.

Then open the project folder in IntelliJ. Let IntelliJ import the Gradle project and set the Gradle JVM to JDK 25:

`File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JVM`

## Run the small development client

Plug in the controller before the first test, then run:

```powershell
.\\gradlew.bat runClient25
```

Do not use `runClient` for this milestone. `runClient25` adds the modern Java/lwjgl3ify runtime that contains SDL3.

Create or enter a disposable test world and press F3. The left debug panel should show:

* the detected controller name;
* left-stick and right-stick values;
* trigger values;
* the names of currently pressed buttons.

Close F3 and verify that no controller diagnostics remain on the normal gameplay screen.

Start in the main menu. Move the right stick and verify that the cyan virtual cursor moves smoothly in all four
directions, stops without jumping, and that A activates singleplayer/options buttons at that cursor location. The
normal operating-system cursor must disappear as soon as the controller takes ownership. Then test partial-speed
movement, diagonals, camera movement, crouching, both triggers, hotbar directions, opening the inventory, and pausing.
On a fresh player, verify that opening the inventory from the controller triggers the same achievement as pressing E.
Select a disposable world from the world list and verify that the Play Selected World button works. In the inventory,
BetterQuesting quest book, NEI, and at least one GTNH machine GUI, test slot highlights, item tooltips, normal clicks,
right-click half-stack behavior, and dragging an item between slots. Press B to close the screen. Move the physical
mouse and verify that it becomes visible and immediately takes over again. Also unplug the controller while holding an
action and verify that the action stops. Keyboard and mouse controls should continue to work alongside the controller.

Use the D-pad in the main menu and an inventory. It should move once per press, then repeat only after the configured
delay when held. Disable Button Navigation and Slot Navigation independently under `Navigation...` and verify that
each target type disappears while analog cursor movement keeps working. Hold right-stick click and verify that cursor
movement slows immediately. Use the shoulder buttons to scroll the singleplayer/server lists and BetterQuesting.
Open creative inventory, select the Search tab, enter a query with more than 45 results, and verify both shoulder
buttons move its item grid and scrollbar in the expected direction.

The on-screen keyboard is retained as an experimental feature. Confirm that it still opens, navigates, changes
layouts, backspaces, and closes without crashing the underlying GUI. Printable text entry is a recorded unresolved
GTNH compatibility issue and is not a release criterion for version 1.0.0.

Assign several registered actions under `Controller... -> Radial Menu...`. In a world, hold Back/Xbox View, aim at
each assigned direction with the right stick or D-pad, and release. The chosen action must receive one clean press.
Return the stick to the center before selecting anything and verify that releasing the menu button cancels. A tapped
D-pad direction stays selected so simultaneous release cannot lose the input; press B to cancel it. Assign Screenshot
to a radial slot and directly to a D-pad direction; both paths must save one screenshot and display the normal
confirmation message. Missing actions from removed mods must be shown as missing and must never activate another
binding.

Enable Auto Jump in `Options -> Controls -> Controller...`, walk directly into a one-block rise, and verify that the
player jumps only when there is enough space above it. Set Swim to Hold and verify that the player rises only while
Jump is held. Set Swim to Toggle, tap Jump once to keep rising, and tap it again to stop rising and sink. At the
surface, verify that brief loss of water contact does not cancel Toggle mode. Climbing onto land or genuinely leaving
the water must still clear it.

Open `Controller... -> Modes...` and test Hold, Toggle, and Press for Sneak, Sprint, Attack, and Use. Hold must follow
the physical input, Toggle must latch on one press and release on the next, and Press must emit only one game tick.
For Sprint, move forward before activating the assigned input. Hold and Toggle should start sprinting directly
without requiring a double-tap; Press should start Minecraft's normal sprint state once. Releasing Hold or disabling
Toggle should stop controller-owned sprinting, while keyboard sprint and double-tap forward should continue to work.
Open a menu while every Toggle mode is active and verify that all latched actions release immediately. Repeat by
disconnecting the controller and by removing game focus.

Open `Options -> Controls -> Controller... -> Minecraft \& Mod Bindings...`. Search for at least one GTNH mod action,
filter to its category, assign a controller input, and verify that the exact action activates in a world. Assign the
same input to a second gameplay action and verify that both rows show a red conflict marker. Clear one row and verify
that its mapping is removed immediately and remains removed after restarting the game. Also test two registered
actions that share a keyboard key; controller activation must affect only the selected action.

Connection, disconnection, Mixin errors, and SDL errors are recorded in `run\\client\\logs\\latest.log`.

## Configuration

Open `Options -> Controls`. The `Controller...` button sits beside the upper option controls on wide screens, leaving
GTNH's crowded Reset/Done footer unchanged; narrow screens place it on a separate row above that footer. Open it to
enable or disable controller input and edit gameplay or GUI bindings.
Select a binding, release the input used to select it, then press the new controller button or trigger. `Clear`
unbinds one action and `Reset Defaults` restores the current page's gameplay or GUI actions. Changes are saved and
used immediately; restarting Minecraft is not required.

Open `Modes...` to configure Swim, Sneak, Sprint, Attack, and Use activation behavior. Hold follows the physical
controller input. Toggle latches on the first press and releases on the next. Press emits one game tick per physical
press and is mainly useful for Attack or Use. Swim supports Hold and Toggle only. Latched actions reset whenever a
menu opens, game focus is lost, gameplay controls are disabled, or the controller disconnects.

Open `Minecraft \& Mod Bindings...` to assign controller inputs to the actions registered in Minecraft's normal
key-binding registry. Focus the search box and press the on-screen keyboard binding, or use a physical keyboard.
Click the category button to filter the list. Core actions already handled by the analog gameplay page, such as jump,
sneak, attack, use, and inventory, are excluded to avoid two controller systems driving the same action. A red `!`
marks an input shared with another gameplay binding. This works for mods that use Minecraft `KeyBinding` state or
Forge key-input events. A mod that bypasses the registry and reads raw LWJGL keyboard state directly needs a dedicated
compatibility adapter.

Open `Sensitivity...` to adjust movement response, camera sensitivity, and cursor sensitivity in 25% steps from 25%
to 500%. Movement Response changes partial-stick precision or aggressiveness without reducing the speed reached at
full stick deflection.

To enter text without a physical keyboard, first focus the target text field with the controller cursor and press the
On-screen Keyboard binding (North/Xbox Y by default). D-pad moves between keys, South/A types the selected key,
East/B backspaces, West/X toggles Caps, and North/Y closes the keyboard. The `#+=`/`ABC` key switches between letters
and all common ASCII punctuation. Space, Back, Enter, and Done are also available as selectable keys. Because this is
an overlay rather than a replacement screen, inventories and mod containers remain open while typing.

Open `Navigation...` to toggle directional button and inventory-slot targets, adjust precision cursor speed, and set
the initial repeat delay and repeat interval. GUI bindings for all four navigation directions, scrolling, and
precision remain independently rebindable under `GUI Bindings...`.

Open `Radial Menu...` to assign up to eight registered Minecraft or mod key actions. Hold the Radial Action Menu
binding in a world, aim with the right stick or D-pad, and release to activate the selected action once. Releasing
with no direction selected cancels. D-pad selections stay latched after the direction is released; press B to cancel
one. Slots are empty by default so the mod never guesses which GTNH actions matter to the player. The menu-opening
input itself is rebindable under `Gameplay Bindings...`.

The full tuning options remain in `config\\gtnhcontroller.cfg`. Run the game once before editing it. The controls
section contains:

* `enableGameplayControls`: disable all gameplay mappings while retaining optional F3 diagnostics;
* `autoJump`: automatically jump when controller movement encounters a one-block rise with enough clearance;
* `autoSwim`: legacy migration value; new configurations should use `activationModes.swim`;
* `moveSensitivity`: shape partial left-stick response while preserving full movement at full stick deflection;
* `lookSensitivity`: scale the configured controller camera speed;
* `moveDeadZone` and `lookDeadZone`: suppress stick drift;
* `moveCurveExponent` and `lookCurveExponent`: increase these for more precision near the center;
* `lookSpeed`: maximum camera rotation in degrees per second;
* `triggerThreshold`: how far a trigger must be pulled before attack/use is held;
* `invertLookY`: invert vertical camera movement.

The Activation Modes section contains `swim`, `sneak`, `sprint`, `attack`, and `use`. Swim accepts `HOLD` or
`TOGGLE`; the four action modes accept `HOLD`, `TOGGLE`, or `PRESS`.

The GUI section contains:

* `enableGuiControls`: disable controller input in menus without disabling gameplay controls;
* `cursorSensitivity`: scale the configured GUI cursor speed;
* `cursorStick`: select `LEFT` or `RIGHT`;
* `cursorDeadZone` and `cursorCurveExponent`: tune cursor drift and precision;
* `cursorSpeed`: set the maximum cursor speed in display pixels per second;
* `cursorAcceleration`: control how gradually the cursor reaches the requested speed;
* `cursorDeceleration`: control how quickly the cursor stops or reverses.

The GUI Navigation section contains:

* `enableButtonNavigation`: include visible, enabled GUI buttons as D-pad navigation targets;
* `enableSlotNavigation`: include visible inventory slots as D-pad navigation targets;
* `precisionCursorScale`: cursor-speed multiplier while the precision action is held;
* `navigationInitialDelayMillis`: delay before a held navigation or scroll action starts repeating;
* `navigationRepeatIntervalMillis`: interval between repeated navigation or scroll actions.

The Radial Menu section stores the eight selected registered-action identifiers. Use the in-game picker instead of
editing these encoded values manually.

Version 0.3.1 automatically replaces the broken 0.3.0 default values `cursorSpeed=900` and
`cursorCurveExponent=1.4` with the safer defaults `420` and `1.8`. Custom values that differ from those old defaults
are preserved.

Version 0.4.0 replaced the original unscaled `Mouse.setCursorPosition` calls with a scaled virtual cursor. This is
important under lwjgl3ify: GUI hover events, `GuiScreen.drawScreen`, clicks, releases, and drags now all receive the
same virtual coordinates, including on the main menu. The existing cursor speed settings keep their display-pixel
units.

Version 0.5.0 adds the controller settings entry under the vanilla Controls screen. Rebinding listens directly to
SDL3, so standardized buttons, analog triggers, and SDL-exposed rear paddles can all be captured. The input used to
open or finish capture must be released before normal GUI actions resume, preventing accidental double activation.

Version 0.6.0 adds explicit input adapters for vanilla's singleplayer world list and BetterQuesting's canvas-based
screens. It also adds persistent Auto Jump and Auto Swim switches to the in-game controller settings. Auto Jump
checks the player-sized space above the obstacle before jumping. Auto Swim resets when the player leaves water,
controller gameplay is disabled, or the controller disconnects.

Version 0.7.0 keeps Auto Swim active through brief water-contact flicker at the surface while still clearing it on
land or after ten dry ticks. It adds persistent movement, camera, and GUI cursor sensitivity controls under
`Options -> Controls -> Controller... -> Sensitivity...`. Controller inventory opening now sends vanilla's
inventory-status packet before displaying the screen, so the inventory achievement is awarded normally.

Version 0.8.0 hides the native cursor and synchronizes its display-pixel position with the scaled controller cursor.
This fixes item hover and tooltip paths that query LWJGL directly instead of using `GuiScreen.drawScreen` coordinates.
Physical mouse movement restores the native cursor and takes ownership back. Controller Sneak is also merged directly
into `MovementInput` after vanilla reads the keyboard, while GUI Back remains a separate screen-only action.

Version 0.8.1 adds the missing `InputMath` import required to compile the GUI-to-display coordinate conversion.

Version 0.8.2 corrects the inverse coordinate conversion so clamped GUI positions map to the actual display edges
while every intermediate display coordinate still maps back to the intended GUI pixel.

Version 0.8.3 tracks requested native-cursor warps separately from physical mouse readback. It ignores the short
delayed coordinate changes caused by `Mouse.setCursorPosition`, preventing controller ownership from oscillating
between the virtual and physical cursor while still restoring physical mouse control after the warp settles.

Version 0.8.4 sends native cursor Y positions using lwjgl3ify's top-down window coordinates, so native hover follows
the visible cursor vertically instead of moving in the opposite direction. Cursor acceleration now scales with higher
sensitivity, and deceleration guarantees a maximum full-speed coast of approximately 0.15 seconds.

Version 0.9.0 adds a searchable and category-filtered controller editor for every registered Minecraft and mod key
action not already owned by the core controller pages. Mappings target the exact `KeyBinding` object, so duplicate
keyboard key codes and duplicate action names are kept separate, and Forge key-input listeners receive controller
edges. Dynamic mappings are saved immediately, conflicts are marked in red, and corrupt saved entries are ignored
individually. Movement sensitivity is now a response curve: lower values provide finer partial-stick control, higher
values react more strongly, and every setting still reaches full movement at full stick deflection.

Version 0.10.0 adds rebindable GUI scrolling, optional directional navigation between buttons, optional inventory-slot
navigation, a hold-to-slow precision cursor action, and adjustable initial/repeat timing. Scrolling uses explicit
adapters for vanilla `GuiSlot` lists and BetterQuesting canvases because Minecraft 1.7.10 has no universal GUI scroll
callback; unsupported custom screens retain normal analog cursor control.

Version 0.11.0 adds an eight-slot radial action menu for registered Minecraft and mod keybindings. Hold Back/Xbox
View, aim with the right stick, and release to emit one exact keybinding press; returning the stick to center cancels.
The searchable radial configuration screen stores language-independent binding identifiers and safely exposes
missing actions after a mod is removed. This version also adds a dedicated creative-inventory scrolling adapter,
fixing shoulder scrolling in the Search tab and other scrollable creative tabs.

Version 0.11.1 adds latched D-pad selection to the radial menu and explicitly dispatches vanilla's Screenshot and
Fullscreen actions. Minecraft 1.7.10 handles those two actions from raw keyboard events instead of consuming their
registered `KeyBinding` state, so an ordinary controller keybinding pulse could never activate them.

Version 0.12.0 adds a modal controller on-screen keyboard that injects normal text, Backspace, Space, and Enter events
into the currently open screen without replacing it. This preserves NEI searches, chat, server addresses, item naming,
inventories, and mod-container state. The keyboard action is independently rebindable under GUI Bindings, and the
movement-response, camera, and cursor sensitivity ceilings are raised from 200% to 500%.

Version 0.12.1 fixes on-screen characters not reaching NEI or Creative search. Container input now follows GTNH NEI's
own `GuiContainerManager.keyTyped` route, so NEI input handlers receive the event before the underlying container.
Screens outside NEI use a virtual-dispatch Mixin so chat, server fields, and other overridden `keyTyped` methods receive
the same input instead of accidentally calling only the base `GuiScreen` implementation.

Version 0.12.2 moves the virtual keyboard-dispatch interface outside the package reserved by Mixin. Version 0.12.1
placed that ordinary runtime interface under the configured Mixin package, which caused Mixin to reject its direct
class load and abort Minecraft before the window appeared.

Version 0.12.3 routes the keyboard's Confirm action through the same press-and-repeat state machine already proven by
Backspace and directional navigation. Printable characters now carry their real LWJGL key code instead of
`KEY\_NONE`, preserving the character while satisfying GTNH and mod text fields that inspect both event values.

Version 0.12.4 sends Creative-search text directly to `GuiContainerCreative` so NEI cannot consume it first. For NEI
searches, it calls the currently focused NEI widget's text handler, preserving the normal `onTextChange` callback and
immediately refreshing the item filter. Other container and screen types retain the existing compatibility fallbacks.

Version 0.13.0 adds independent Hold, Toggle, and Press activation modes for Sneak, Sprint, Attack, and Use. Swim can
use normal Hold behavior or the existing surface-tolerant Toggle behavior. Toggle state is deliberately cleared when
gameplay control stops, preventing a latched Attack or Use action from surviving a menu, focus loss, or disconnect.
The on-screen keyboard remains included but is now explicitly documented as unresolved because printable characters
still fail in the real GTNH 2.9 pack even though navigation, layout switching, and Backspace work.

Version 1.0.0 is the first public release. It removes the duplicate Auto Swim setting so Swim is configured only
under `Modes...`, moves the Controls-screen entry away from GTNH's crowded bottom row, and puts live controller
diagnostics behind F3. Sprint Hold and Toggle now drive the player's sprint state directly after analog movement is
calculated while retaining vanilla hunger, collision, sneaking, item-use, riding, and blindness restrictions. The
Java namespace is project-neutral and contains no company branding. The known printable on-screen keyboard issue
remains documented for later investigation.

The bindings section accepts one of these forms:

```text
BUTTON:SOUTH
TRIGGER:RIGHT\_TRIGGER
BUTTON:LEFT\_SHOULDER|BUTTON:DPAD\_LEFT
NONE
```

The `|` character means “either input.” Button and trigger names are case-insensitive. Standard button names include
`SOUTH`, `EAST`, `WEST`, `NORTH`, `BACK`, `GUIDE`, `START`, `LEFT\_STICK`, `RIGHT\_STICK`, both shoulders, all four
D-pad directions, `TOUCHPAD`, `MISC1` through `MISC6`, and `LEFT\_PADDLE1`, `LEFT\_PADDLE2`, `RIGHT\_PADDLE1`, and
`RIGHT\_PADDLE2`. Trigger names are `LEFT\_TRIGGER` and `RIGHT\_TRIGGER`.

Rear buttons can only be assigned independently if SDL reports them as paddles or miscellaneous buttons. Many 8BitDo
firmware modes instead make rear buttons duplicate another controller button; software cannot distinguish duplicated
inputs. Check the controller lines on the F3 debug screen before relying on a paddle binding.

Bindings changed directly in the configuration file still require a restart. Changes made through the in-game
controller screen apply immediately. F3 controller diagnostics can be disabled separately with `showDebugOverlay`.

## Test inside the real GTNH pack

Build the jar:

```powershell
.\\gradlew.bat build
```

Copy the normal mod jar from `build\\libs\\` into the `mods` folder of the separate GTNH 2.9.0 test instance. Do not copy
a sources or dev jar.

Launch the test instance with Java 25, enter a disposable test world, and verify the F3 diagnostics again.

## Troubleshooting

* `No matching toolchain`: IntelliJ/Gradle cannot find JDK 25. Point the Gradle JVM to the JDK, not a JRE.
* `spotlessJavaCheck FAILED`: run `.\\gradlew.bat spotlessApply`, then build again.
* `This mod requires lwjgl3ify 3.0.0`: the instance is GTNH 2.8.4 or otherwise uses lwjgl3ify 2.x.
* `No gamepad detected`: connect by USB first, close DS4Windows/Steam Input, restart the dev client, and check Windows
Game Controllers (`joy.cpl`) to prove Windows sees the device.
* Every press acts twice: another mapper such as Steam Input, DS4Windows, JoyToKey, or reWASD is also producing input.
* Two cursors are visible after moving the controller cursor: check `latest.log` for a native-cursor warning. The mod
keeps hover coordinates synchronized even when the operating system refuses a transparent cursor.
* The cursor still moves without touching the stick: increase `cursorDeadZone` gradually, for example from `0.15` to
`0.20`. Do not hide severe drift with an enormous dead zone; calibrate or replace the controller instead.
* A configured binding is rejected: check `latest.log`; the invalid action falls back to its safe default.
* A controller click works in vanilla and BetterQuesting but not a specific mod GUI: record the exact GUI or machine
name. Exotic screens may bypass vanilla's mouse callback path and require another compatibility adapter.
* Shoulder scrolling does nothing in a specific mod GUI: 1.7.10 has no universal scroll callback. Version 0.10.0
supports vanilla lists and BetterQuesting, and version 0.11.0 adds creative inventory; record the exact screen so it
can receive a compatibility adapter.
* A radial slot says `Missing action`: the mod that registered that exact keybinding is absent or changed it. Reassign
or clear the slot; silently activating a similarly named action would be unsafe.
* A registered mod action still ignores both radial and direct controller mappings: the mod probably reads raw LWJGL
keyboard state instead of its registered `KeyBinding`. Record the exact mod and action; it needs a narrow
compatibility adapter because globally faking operating-system keys would create unsafe cross-mod conflicts.
* On-screen keyboard Backspace works but characters do not: this is a confirmed unresolved GTNH 2.9 compatibility
issue, not a setup mistake. Record the exact screen and logs for future GitHub investigation.
* Gradle says the repository has no version: create the initial commit and `git tag 1.0.0`.

## Why this is not a direct Controllable port

[Controllable](https://github.com/MrCrayfish/Controllable) is a valuable UX and architecture reference, especially
its action mappings, cursor behavior, slot attraction, and radial menus. Its oldest published source is for Minecraft
1.12.2, however, and relies on Forge and Minecraft APIs that do not exist in 1.7.10. That source release is also
GPL-licensed. This project therefore uses an original implementation and GTNH's existing SDL3 backend rather than
copying Controllable source. The cursor follows the same high-level virtual-mouse design: keep controller coordinates
separately, inject them into the GUI render path, and invoke mouse callbacks at those coordinates. Version 0.8.0 also
mirrors the virtual position to LWJGL for older mod GUIs that bypass the normal render parameters.

Forge 1.7.10 predates the `InputUpdateEvent` used by newer controller mods. A focused client Mixin runs immediately
after vanilla calculates keyboard movement, adds the larger controller value on each axis, and preserves simultaneous
keyboard input.

## Implementation roadmap

1. SDL3 device detection, live diagnostics, dead zones, curves, and automated input math tests.
2. Analog in-world movement, camera, core actions, and safe release of held input.
3. Configurable action bindings with accessible defaults and SDL paddle support.
4. GUI cursor movement, click, drag, right-click, back behavior, and physical-mouse coexistence.
5. An in-game binding editor with live SDL3 button, trigger, and paddle capture.
6. Compatibility adapters for vanilla lists and BetterQuesting, plus optional Auto Jump and Auto Swim.
7. Searchable, category-filtered controller mappings for registered Minecraft and mod key actions.
8. GUI scrolling, directional button navigation, inventory-slot navigation, and a precision cursor mode.
9. A low-input radial menu for GTNH's unusually large keybind set.
10. Controller-friendly on-screen text entry.
11. Per-action accessibility activation modes with safe latch release.
12. Named controller profiles.
13. Multiple-controller input merging for split or adaptive-control setups.
14. Accessibility testing, multiplayer safety, cross-platform testing, packaging, and an upstream proposal.

The mod does not assign every GTNH keybind to a fixed controller chord. Players opt into direct bindings or radial
slots for the actions they need, while future contexts and profiles can make the unusually large action set easier to
reach.

