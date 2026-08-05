# GTNH Controller Support

[![Latest release](https://img.shields.io/github/v/release/GOjayson/GTNH-Controller-Support)](https://github.com/GOjayson/GTNH-Controller-Support/releases/latest)
[![Build and test](https://github.com/GOjayson/GTNH-Controller-Support/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/GOjayson/GTNH-Controller-Support/actions/workflows/build-and-test.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Accessibility-focused controller support for GregTech: New Horizons on Minecraft 1.7.10.

I originally built this mod for my nephew, who has a muscle condition that makes a traditional keyboard and mouse
difficult to use. It is shared publicly in the hope that it makes GTNH accessible to more players.

> This is an independent community project, not an official GTNH mod.
>
> GTNH Controller Support is client-only. Do not install it on a dedicated server.

## Demo

<!--
After uploading the video, replace the placeholder below with a real thumbnail and wrap the image in a link to the
YouTube video.
-->

![Gameplay demo video coming soon](docs/media/demo-video-placeholder.svg)

## Screenshots

| Controller settings | Virtual cursor |
| --- | --- |
| ![Controller settings menu](docs/media/Contoller_Settings_Menu.png) | ![Virtual cursor in a Minecraft interface](docs/media/Virtual-cursor.png) |
| Radial menu | Mod bindings |
| ![Controller radial menu](docs/media/Radial_Menu.png) | ![Minecraft and mod bindings menu](docs/media/Mod-Bindings-menu.png) |

## Features

- True analog movement and camera control with separate movement, camera, cursor and trigger deadzones.
- Independent camera X/Y and GUI cursor X/Y inversion.
- In-game controller selection when more than one SDL gamepad is connected.
- Guided calibration with live drift detection, range checks and suggested deadzones/trigger thresholds.
- Full controller test screen for sticks, triggers, buttons, paddles and miscellaneous SDL inputs.
- Controller-driven cursor for menus, inventories, machine interfaces and BetterQuesting.
- Controller panning and zooming for JourneyMap and Galacticraft's celestial map.
- Rebindable gameplay and GUI actions, including SDL-exposed triggers, paddles and miscellaneous buttons.
- Multi-button chords such as `LB+A`, with custom-binding-aware controller prompts in menus.
- Primary and hold-modifier binding layers for core, Minecraft and mod actions.
- Searchable core, Minecraft and mod bindings with exact conflict details.
- Separate NEI key bindings appear alongside Minecraft and mod actions, including legacy GTNH NEI builds.
- Three eight-direction radial pages for up to 24 frequently used mod actions.
- Selectable Hold or Toggle radial-menu opening behavior.
- Editable one-shot chat and command macros assignable to radial slots.
- In-game controller profile import/export for backups and sharing.
- Directional GUI navigation, configurable scroll acceleration and a precision-cursor mode.
- On-screen keyboard for focused vanilla, Creative inventory and NEI text fields.
- Optional Auto Jump and Hold/Toggle swimming assistance.
- Hold, Toggle and Press activation modes for Sneak, Sprint, Attack and Use.
- Optional active-mode HUD, large high-contrast cursor and cursor trail.
- Optional configurable rumble for damage, explosions, mining, fishing bites and low health.
- Battery display and an exportable SDL/controller compatibility report when supported by the driver.
- Safe hand-off between controller, mouse and keyboard.
- Live controller diagnostics on the F3 debug screen.

## Requirements

- GregTech: New Horizons 2.9.x
- Minecraft 1.7.10
- lwjgl3ify 3.0.0 or newer
- The Java version recommended by the GTNH 2.9 launcher configuration

GTNH 2.8.4 ships an older lwjgl3ify version without the SDL3 API required by this mod.

## Installation

1. Open the [latest release](https://github.com/GOjayson/GTNH-Controller-Support/releases/latest).
2. Download the normal mod JAR. Do not download a `dev` or `sources` JAR.
3. Place the JAR in the `mods` folder of a separate GTNH 2.9.x instance.
4. Connect the controller and start the game.
5. Open `Options -> Controls -> Controller support` to configure it.
6. Open `Controller Setup & Test -> Calibration Wizard` before fine-tuning deadzones manually.

Use a test instance and back up important worlds before adding an experimental community mod.

## Supported controllers

The mod uses SDL3's standardized gamepad API through lwjgl3ify. Xbox, PlayStation, 8BitDo, Nintendo and many generic
controllers can work when SDL recognizes them as gamepads.

Rear buttons are independently bindable only when the controller firmware exposes them as paddles or miscellaneous
buttons. Some controllers make rear buttons duplicate ordinary buttons, which software cannot distinguish.

Rumble is also hardware and driver dependent. The setup screen reports whether SDL says the selected controller
supports it; unsupported controllers continue to work without haptics.

Battery reporting is optional. Wireless controllers may show a percentage and charging state, while many wired,
XInput and remapped devices report no battery information. This never affects input.

For the first test, use USB and disable Steam Input, DS4Windows, JoyToKey and similar remapping software. A second
mapper can cause every controller action to fire twice.

## Default controls

SDL uses physical button positions, so the Xbox labels below map sensibly to other controller layouts.

### Gameplay

| Input | Action |
| --- | --- |
| Left stick | Analog movement |
| Right stick | Camera |
| South / Xbox A | Jump |
| East / Xbox B | Sneak |
| Left-stick click | Sprint |
| Right trigger | Attack or mine |
| Left trigger | Use item or place block |
| Unbound | Drop selected item |
| Left shoulder / D-pad left | Previous hotbar slot |
| Right shoulder / D-pad right | Next hotbar slot |
| North / Xbox Y | Open inventory |
| Start | Pause menu |
| Back / Xbox View | Open radial menu (Hold mode by default) |
| Unbound | Hold modifier layer |

### Menus and inventories

| Input | Action |
| --- | --- |
| Right stick | Move the virtual cursor |
| South / Xbox A | Left-click and drag |
| North / Xbox Y | Quick-move the hovered inventory stack |
| West / Xbox X | Right-click and drag |
| East / Xbox B | Back / Escape |
| Back / Xbox View | Open or close the on-screen keyboard |
| D-pad | Navigate buttons and inventory slots |
| Left / right shoulder | Scroll up / down |
| Right-stick click | Precision cursor while held |

All core actions can be rebound. Quick Move Stack is listed under GUI Controller Bindings, while Drop Item is listed
under Gameplay Bindings. Configure Modifier Layer there, then
use the `Layer: Primary/Modifier` button in either binding editor to assign a second set of actions. LT, RT and
independently exposed rear paddles can all be used as the modifier.

To create a chord, select a binding, hold every required input together, then release the full combination. For
example, holding LB and A records `LB+A`. A larger active chord takes precedence over a plain component binding, so
`A` and `LB+A` can perform different actions.

Additional Minecraft and mod actions can be assigned under `Controller support -> Minecraft & Mod Bindings` or placed
on any of the three radial pages. Hold LB or RB while the radial menu is open to access the two extra pages. Radial
Menu settings also provide Hold and Toggle opening modes plus editable one-shot chat macros such as `/tpa accept` or
`Hi all`.

Controller profiles are managed under
`Controller support -> Controller Setup & Test -> Profile Import & Export`. Exported `.cfg` files are stored in
`config/gtnhcontroller-profiles` and can be copied to another GTNH instance. Review chat macros before sharing a
profile because their text is included.

## Accessibility modes

Open `Controller support -> Accessibility modes` to configure:

- **Hold:** active only while the controller input is held.
- **Toggle:** press once to activate and again to deactivate.
- **Press:** emits one game tick per physical press.

Swim supports Hold and Toggle. Sneak, Sprint, Attack and Use support all three modes. Latched actions clear when a
menu opens, focus is lost, gameplay controls are disabled or the controller disconnects.

The same screen can enable controller prompts, the active-mode HUD, a large cursor and a cursor trail.

## Known limitations

- Imported profiles are manual global snapshots; automatic per-device profile switching and merged adaptive-controller
  inputs are not implemented yet.
- Calibration recommends one value per physical stick; movement and cursor can still be adjusted separately afterward.
- Custom text widgets and mod GUIs that bypass Minecraft's normal input APIs may still need dedicated compatibility
  code.
- Controller battery percentages depend on SDL, the connection type and the operating-system driver and may be
  unavailable or approximate.

Please report compatibility problems through
[GitHub Issues](https://github.com/GOjayson/GTNH-Controller-Support/issues) and include the controller model, exact GTNH
version, affected screen or action, and relevant logs.

## Documentation

- [Configuration reference](docs/CONFIGURATION.md)
- [Development setup](docs/DEVELOPMENT.md)
- [Testing checklist](docs/TESTING.md)
- [Architecture notes](docs/ARCHITECTURE.md)
- [Troubleshooting](docs/FAQ.md)
- [Version history](CHANGELOG.md)
- [Contributing](CONTRIBUTING.md)

## Credits

- Built from the [GTNewHorizons 1.7.10 example-mod template](https://github.com/GTNewHorizons/ExampleMod1.7.10).
- Uses SDL3 through [lwjgl3ify](https://github.com/GTNewHorizons/lwjgl3ify).
- Controller UX was informed by [MrCrayfish's Controllable](https://github.com/MrCrayfish/Controllable), but this is
  an original Minecraft 1.7.10 implementation rather than a source port.
- Inspired by the accessibility request in
  [GTNH issue #23030](https://github.com/GTNewHorizons/GT-New-Horizons-Modpack/issues/23030).

## License

[MIT](LICENSE) © 2026 Jayson Gorissen
