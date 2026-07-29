# Troubleshooting and FAQ

## No gamepad is detected

- Confirm that Windows sees it through `joy.cpl`.
- Connect by USB for the first test.
- Close Steam Input, DS4Windows, JoyToKey, reWASD and similar remappers.
- Restart the development client after confirming that the controller is visible to Windows.
- Press F3 and check whether GTNH Controller Support reports a device.

## Every press happens twice

Another mapper is probably translating the same controller input into mouse or keyboard input. Disable Steam Input,
DS4Windows, JoyToKey, reWASD and launcher controller mappings.

## A rear button cannot be assigned independently

SDL can expose paddles and miscellaneous buttons, but some controller firmware makes a rear button duplicate an
ordinary face button. Software cannot separate two inputs when the controller reports the same button for both.

Check the pressed-button line under F3. If both physical buttons show the same SDL name, change the controller's
firmware mode or mapping.

## The cursor moves while untouched

Increase `cursorDeadZone` gradually, for example from `0.15` to `0.20`. Do not hide severe hardware drift with an
enormous dead zone; calibrate or replace the controller.

## Two cursors are visible

Check `latest.log` for a native-cursor warning. The mod still synchronizes hover coordinates if the operating system
refuses the transparent cursor, but the extra pointer may remain visible.

## A click works in vanilla but not in a specific mod GUI

Some custom screens bypass Minecraft's mouse callback path. Record:

- the exact mod and screen or machine name;
- the GTNH version;
- which controller action was used;
- whether the cyan cursor and tooltip are correctly positioned;
- the relevant `latest.log`.

That screen may need a narrow compatibility adapter.

## Scrolling fails in one screen

Minecraft 1.7.10 has no universal GUI scroll callback. The mod includes adapters for vanilla lists, BetterQuesting,
Creative inventory and Galacticraft's celestial map. Record the exact unsupported screen so it can be investigated.

## A registered mod action ignores the controller

The mod probably reads raw LWJGL keyboard state instead of consuming its registered `KeyBinding`. Global fake
operating-system keys would create cross-mod conflicts, so these cases require a dedicated compatibility adapter.

## A radial slot says `Missing action`

The mod that registered that action is absent or changed its binding identity. Reassign or clear the slot. The
controller mod deliberately refuses to activate a similarly named action.

## The on-screen keyboard does not type characters

Version 1.1.0 added focused-field input for vanilla, Creative inventory and NEI text boxes. Select the field with GUI
Confirm before opening the keyboard. If only one custom mod field fails, that mod probably uses its own text widget and
may need a compatibility adapter.

Include the exact screen, GTNH version and `latest.log` in the report.

## Minecraft does not start

Confirm:

- the instance is GTNH 2.9.x;
- lwjgl3ify is 3.0.0 or newer;
- the normal mod JAR was installed instead of the `dev` or `sources` JAR;
- the launcher uses the Java version required by the GTNH instance.

Attach `logs\latest.log` and `logs\fml-client-latest.log` when reporting startup failures.

## `spotlessJavaCheck` fails

Run:

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat build
```

Review and commit the formatting changes.

## Gradle says the repository has no version

The GTNH build scripts derive versions from Git tags. Create a commit and semantic-version tag before building a
release:

```powershell
git add .
git commit -m "Prepare release"
git tag 1.1.0
```
