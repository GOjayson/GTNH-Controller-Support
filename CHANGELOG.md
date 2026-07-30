# Changelog

All notable changes to GTNH Controller Support are recorded here.

## 1.1.1

### Fixed

- Prioritized a focused NEI search field over Minecraft's Creative search field and added a direct Backspace fallback.

## 1.1.0

### Fixed

- Restored the virtual cursor on Galacticraft's celestial map even though that screen resets Minecraft's OpenGL
  projection after drawing.
- Added left-click dragging and shoulder-button zooming to the Galacticraft celestial map.
- Added controller dragging to JourneyMap's fullscreen map, including the normal follow-mode release and tile refresh.
- Added direct focused-field insertion fallbacks for the on-screen keyboard.
- Preserved Creative inventory search refreshes and NEI's normal text-change callback when using the on-screen
  keyboard.

### Changed

- Isolated optional Galacticraft and JourneyMap support behind reflection-based client adapters; neither mod is a
  compile-time or runtime requirement.

## 1.0.0

First public release.

### Added

- Direct controller sprint handling for Hold, Toggle and Press modes.
- Adaptive placement of the `Controller...` button beside the upper Controls options.
- Controller diagnostics on Minecraft's F3 debug screen.

### Changed

- Removed the duplicate Auto Swim setting; Swim is now configured only under `Modes...`.
- Moved the Java sources to the neutral `dev.gtnhcontroller` namespace.
- Cleaned release metadata and documentation for public distribution.

### Known issue

- The on-screen keyboard can navigate, switch layouts and send Backspace, but printable characters still do not reach
  focused text fields in the tested GTNH 2.9 environment.

## 0.13.0

- Added independent Hold, Toggle and Press activation modes for Sneak, Sprint, Attack and Use.
- Added Hold and surface-tolerant Toggle modes for swimming.
- Cleared latched actions whenever gameplay control stops, preventing Attack or Use from surviving a menu, focus
  loss or controller disconnect.
- Documented the unresolved printable on-screen keyboard issue.

## 0.12.4

- Sent Creative-search text directly to `GuiContainerCreative` so NEI could not consume it first.
- Sent NEI-search text to the focused NEI widget while preserving its normal text-change callback.
- Retained compatibility fallbacks for other containers and screens.

## 0.12.3

- Routed keyboard Confirm through the same press-and-repeat state machine as Backspace and navigation.
- Sent printable characters with their real LWJGL key code instead of `KEY_NONE`.

## 0.12.2

- Moved the virtual keyboard-dispatch interface outside the package reserved by Mixin, preventing a startup crash.

## 0.12.1

- Routed container text through GTNH NEI's `GuiContainerManager.keyTyped` path.
- Added virtual screen dispatch so overridden `keyTyped` implementations receive on-screen keyboard input.

## 0.12.0

- Added a controller on-screen keyboard overlay.
- Added keyboard navigation, Caps, symbols, Space, Backspace, Enter and Done controls.
- Raised movement, camera and cursor sensitivity ceilings from 200% to 500%.

## 0.11.1

- Added latched D-pad selection to the radial menu.
- Added explicit dispatch for vanilla Screenshot and Fullscreen actions, which Minecraft 1.7.10 handles through raw
  keyboard events instead of registered keybinding state.

## 0.11.0

- Added an eight-slot radial menu for registered Minecraft and mod actions.
- Added a searchable radial configuration screen.
- Stored language-independent binding identifiers and handled missing mod actions safely.
- Added a creative-inventory scrolling adapter for the Search tab and other scrollable creative tabs.

## 0.10.0

- Added rebindable GUI scrolling.
- Added optional directional navigation between buttons and inventory slots.
- Added a hold-to-slow precision cursor action.
- Added configurable initial and repeat timing.
- Added explicit scrolling adapters for vanilla `GuiSlot` lists and BetterQuesting canvases.

## 0.9.0

- Added a searchable and category-filtered controller editor for registered Minecraft and mod actions.
- Preserved duplicate keyboard codes and descriptions as separate registered actions.
- Added Forge key-input edge dispatch, immediate persistence and conflict markers.
- Changed movement sensitivity into a response curve that retains full speed at full stick deflection.

## 0.8.4

- Corrected native cursor Y positioning for lwjgl3ify's top-down window coordinates.
- Scaled cursor acceleration with higher sensitivity.
- Limited full-speed cursor coast to approximately 0.15 seconds.

## 0.8.3

- Tracked requested cursor warps separately from physical mouse readback.
- Prevented virtual and physical cursor ownership from oscillating after `Mouse.setCursorPosition`.

## 0.8.2

- Corrected inverse coordinate conversion so GUI positions map to the actual display edges.

## 0.8.1

- Added the missing `InputMath` import required to compile GUI-to-display coordinate conversion.

## 0.8.0

- Hid and synchronized the native cursor beneath the visible virtual cursor.
- Fixed tooltips and mod GUIs that query LWJGL mouse coordinates directly.
- Returned cursor ownership to the physical mouse when it moves.
- Merged controller Sneak directly into vanilla movement input.

## 0.7.0

- Kept Toggle Swim active through brief loss of water contact at the surface.
- Added in-game movement, camera and cursor sensitivity controls.
- Sent vanilla's inventory-status packet when the controller opens the inventory, restoring the achievement.

## 0.6.0

- Added compatibility for the vanilla world list and BetterQuesting canvas screens.
- Added persistent Auto Jump and Auto Swim settings.
- Added clearance checks for one-block Auto Jump.

## 0.5.0

- Added the controller settings entry under Minecraft's Controls screen.
- Added in-game capture for standardized SDL buttons, analog triggers and exposed rear paddles.
- Blocked the capture-opening input until release to prevent accidental double activation.

## 0.4.0

- Replaced unscaled native cursor calls with a scaled virtual GUI cursor.
- Unified hover, drawing, click, release and drag coordinates, including on the main menu.

## 0.3.1

- Replaced the unsafe `cursorSpeed=900` and `cursorCurveExponent=1.4` defaults with `420` and `1.8`.
- Preserved custom values that differed from the old defaults.
