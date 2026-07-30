# Testing checklist

Test in a disposable GTNH 2.9.x instance and keep backups of important worlds.

## Build

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat test
.\gradlew.bat build
```

Copy the normal JAR from `build\libs\` into the test instance. Never test only the development client before a public
release; test the exact release JAR as well.

## Detection and diagnostics

1. Connect the controller before starting the first test.
2. Enter a disposable world and press F3.
3. Verify the controller name, both sticks, both triggers and pressed button names.
4. Close F3 and verify that diagnostics disappear.
5. Disconnect and reconnect the controller without restarting Minecraft.
6. Connect a second controller, select each device in turn, and verify that only the selected device controls the game.
7. Return selection to Automatic and verify that the first available device reconnects.

## Main menu and virtual cursor

- Move the right stick in all four directions.
- Verify smooth acceleration, precise low-speed movement and a clean stop.
- Confirm that the native cursor disappears when the controller takes ownership.
- Open Singleplayer and select a world.
- Open Multiplayer and select a server.
- Press the configured Back action to close screens.
- Move the physical mouse and verify that it immediately takes ownership again.

## Gameplay

- Test partial-speed movement, full-speed movement and diagonals.
- Test camera movement at low and high sensitivity.
- Test Jump, Sneak, Sprint, Attack, Use, inventory and Pause.
- Bind Drop Item and verify that each controller press drops one selected item.
- Verify both hotbar directions.
- On a fresh player, open the inventory and verify the vanilla achievement.
- Hold an action while disconnecting the controller; the action must release.
- Repeat while opening a GUI and while removing game focus.

## Accessibility modes

Open `Controller... -> Modes...`.

- **Sneak, Attack and Use:** verify Hold, Toggle and Press.
- **Sprint:** move forward first. Hold and Toggle must start sprinting without a double-tap. Press should start
  vanilla sprint once.
- Verify that keyboard sprint and double-tap forward still work.
- Open a menu with Toggle actions active and confirm that all latches clear.

## Auto Jump and Swim

- Enable Auto Jump and walk directly into a one-block rise.
- Verify that the player jumps only when there is enough clearance.
- Set Swim to Hold and verify that the player rises only while Jump is held.
- Set Swim to Toggle and verify that one press rises and the next sinks.
- At the water surface, brief loss of water contact must not cancel Toggle mode.
- Climbing onto land or leaving the water must clear it.

## Inventories and mod GUIs

Test at least:

- vanilla inventory;
- Creative inventory;
- BetterQuesting quest book;
- NEI;
- JourneyMap fullscreen map;
- Galacticraft celestial map;
- one GTNH machine interface.

Verify slot highlighting, item tooltips, left-click, right-click half stacks, dragging and closing the screen.

For JourneyMap, hold GUI Confirm over the map, move the cursor and release. Verify that the map stays at the new
position and Follow is disabled. For Galacticraft, verify that the cursor remains visible, planets can be selected,
GUI Confirm can drag the map and both shoulder actions zoom it.

## Navigation and scrolling

- Use the D-pad in the main menu and an inventory.
- Verify one movement per initial press and delayed repeat while held.
- Disable Button Navigation and Slot Navigation independently.
- Hold the Precision Cursor action and verify the configured slowdown.
- Scroll vanilla world/server lists and BetterQuesting.
- In the Creative Search tab, enter enough results to require scrolling and verify both directions.

## Bindings

- Rebind a core gameplay action and a GUI action.
- Bind Modifier Layer to an unused trigger or exposed paddle.
- Add different core and mod actions on the Modifier page; verify primary actions stop while the modifier is held.
- Clear Modifier Layer and verify all primary controls continue working.
- Restart the game and verify persistence.
- Assign a rear paddle if SDL exposes one independently.
- Search for a GTNH mod action and bind it.
- Assign one input to two gameplay actions and verify conflict markers.
- Test two registered actions that share the same keyboard key; only the chosen action should activate.

## Radial menu

- Assign different registered actions to Base, Hold LB and Hold RB.
- Hold the radial-menu input, select with the right stick and release.
- While it is open, hold each shoulder and verify the page label and all eight assignments change.
- Hold both shoulders and verify the Base page is selected.
- Repeat with the D-pad.
- Return the stick to center before release and verify cancellation.
- Assign Screenshot and verify that exactly one screenshot is created.
- Remove a mod that owns an assigned action and verify that the slot becomes missing without activating another action.

## On-screen keyboard

Focus a text field with GUI Confirm before opening the keyboard. Verify that it:

- opens and closes without replacing the underlying screen;
- navigates and changes letter/symbol layouts;
- enters lowercase letters, capitals, digits, spaces and symbols;
- sends Backspace and Enter;
- immediately updates Creative inventory and NEI search results;
- while the Creative inventory is open, types into whichever of the NEI or vanilla Creative search fields is focused
  without changing the other field, including Backspace;
- enters text in chat, a server address and at least one item-naming screen.

## Release verification

1. Download the normal JAR from the GitHub Release.
2. Install it into a clean test instance.
3. Repeat detection, menu navigation, gameplay, one inventory, BetterQuesting and Sprint Toggle.
4. Confirm the mod version and filename do not contain `dirty`.
