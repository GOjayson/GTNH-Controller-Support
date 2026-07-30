# Architecture

GTNH Controller Support is an original Minecraft 1.7.10 implementation built around the SDL3 API exposed by
lwjgl3ify.

## Why it is not a direct Controllable port

[Controllable](https://github.com/MrCrayfish/Controllable) is a useful UX reference for action mappings, cursor
behavior and radial menus. Its supported Minecraft versions use Forge and Minecraft APIs that do not exist in 1.7.10.
This project therefore implements those concepts specifically for GTNH rather than copying modern source.

## Input contexts

Gameplay and GUI control are intentionally separate:

- `GameplayController` handles movement, camera, core actions and safe release.
- `GuiController` owns the virtual cursor, clicks, navigation, scrolling and the keyboard overlay.
- `RadialMenuController` dispatches one registered action after radial selection.
- `ModKeyBindingController` maps SDL inputs to exact registered `KeyBinding` objects.

Separating contexts prevents a button such as Xbox B from triggering Sneak and GUI Back simultaneously.

`ControllerProfile` snapshots the modifier input before resolving other actions. Gameplay and registered keybindings
then read either the Primary or Modifier map for that tick. GUI actions intentionally remain on the Primary map so a
gameplay modifier cannot silently replace menu Confirm or Back.

Controller selection stores the SDL gamepad name plus its occurrence among identical names. `AUTO` retains the
original first-available behavior. Instance IDs are used only during the running session because SDL can assign new
ones after reconnection.

## Analog movement

Forge 1.7.10 predates the later `InputUpdateEvent`. A focused Mixin runs after vanilla calculates keyboard movement,
then merges the larger input magnitude on each movement axis. This keeps true analog movement while preserving
simultaneous keyboard input.

Sprint is applied after analog movement is known. Controller-owned sprint still obeys vanilla restrictions including
forward movement, hunger, collision, sneaking, item use, riding and blindness.

## Virtual cursor

The GUI controller stores cursor coordinates separately from the physical mouse. It injects the virtual position into
rendering and mouse callbacks, then mirrors it to the native cursor for older mod GUIs that read LWJGL coordinates
directly.

Physical mouse movement immediately returns ownership to the mouse.

## Compatibility adapters

Minecraft 1.7.10 does not expose universal APIs for every custom list, canvas or text field. The mod uses narrow
compatibility paths for:

- vanilla world and server lists;
- creative inventory scrolling;
- BetterQuesting canvas interaction;
- JourneyMap fullscreen dragging;
- Galacticraft celestial-map cursor rendering and zooming;
- focused vanilla, Creative and NEI text fields;
- exact registered keybinding dispatch;
- vanilla Screenshot and Fullscreen actions.

Unknown custom GUIs continue to receive the normal virtual cursor. Extra adapters are added only when a screen bypasses
the vanilla input paths. Optional-mod adapters use class names and reflection so those mods are not hard dependencies.

## Safety principles

- Held controller actions are released on menus, focus loss, disabled controls and disconnect.
- Missing mod actions are never replaced by approximate matches.
- Physical keyboard and mouse input remain available.
- Corrupt configuration entries fall back individually instead of invalidating the entire controller profile.
- Diagnostics stay behind F3 during normal gameplay.
