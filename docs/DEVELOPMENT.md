# Development

## Supported development target

Develop and test against a separate GTNH 2.9.x instance. GTNH 2.8.4 uses lwjgl3ify 2.1.x and does not expose the SDL3
API required by this mod.

## Windows prerequisites

Install:

1. Git for Windows.
2. A full JDK 25 installation.
3. IntelliJ IDEA Community or Ultimate.
4. Prism Launcher.
5. A separate GTNH 2.9.x Java 25 test instance.

## Prepare the project

Clone the repository:

```powershell
git clone https://github.com/GOjayson/GTNH-Controller-Support.git
cd GTNH-Controller-Support
```

The GTNH build scripts derive the project version from Git tags. A source archive without Git metadata needs a local
commit and tag before producing a correctly versioned release build.

Set the Gradle JVM in IntelliJ to JDK 25:

`File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JVM`

Prepare and build:

```powershell
.\gradlew.bat setupDecompWorkspace
.\gradlew.bat spotlessApply
.\gradlew.bat build
```

The normal mod JAR is written to `build\libs\`. Do not distribute the `dev` or `sources` JAR.

## Development client

Connect a controller and run:

```powershell
.\gradlew.bat runClient25
```

Use `runClient25`, not the legacy `runClient` task. The modern runtime contains lwjgl3ify and SDL3.

Connection, Mixin and SDL errors are written to `run\client\logs\latest.log`.

## Code quality

Before committing:

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat test
.\gradlew.bat build
```

Do not commit generated directories such as `.gradle`, `build`, `run`, logs, saves or local configuration.

## Project layout

- `dev.gtnhcontroller`: mod entry point, configuration and proxies.
- `dev.gtnhcontroller.client.input`: SDL profiles, analog movement, actions and registered keybindings.
- `dev.gtnhcontroller.client.gui`: virtual cursor, navigation, scrolling, radial menu and settings screens.
- `dev.gtnhcontroller.mixins`: focused hooks for movement and GUI compatibility.
- `src/test`: input math, activation modes, cursor behavior, navigation, scrolling and codec tests.

See [ARCHITECTURE.md](ARCHITECTURE.md) for the design boundaries.

## Release process

1. Make sure `main` builds successfully.
2. Update `CHANGELOG.md`.
3. Commit the release.
4. Create an annotated semantic-version tag, for example:

   ```powershell
   git tag -a 1.0.1 -m "GTNH Controller Support 1.0.1"
   git push origin 1.0.1
   ```

5. Verify the GitHub release workflow.
6. Download the normal JAR from the created release and test that exact asset in a clean GTNH instance.

Never reuse a published release tag. Fixes after `1.0.0` belong in `1.0.1`.
