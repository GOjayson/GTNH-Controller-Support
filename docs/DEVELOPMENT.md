# Development

## Supported development target

Develop and run regression tests against a separate GTNH 2.9.x instance. This remains the primary development and
testing target.

Other GTNH versions on Minecraft 1.7.10 may work with compatible dependencies. GTNH 2.8.4 has been reported working
after updating lwjgl3ify, Angelica and GTNHLib. Exact updated versions and the extent of testing were not supplied.
See the [community report](https://github.com/GOjayson/GTNH-Controller-Support/issues/3#issuecomment-5647667970).

Stock GTNH 2.8.4 includes lwjgl3ify 2.1.16 and does not provide the required SDL3 support. The reported working setup
used updated dependencies.

When investigating another GTNH version, record the complete dependency and runtime versions and follow the
[testing checklist](TESTING.md). Testing additional versions supplements regression testing on the primary target.

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
.\gradlew.bat build
```

`build` already runs the tests.

Do not commit generated directories such as `.gradle`, `build`, `run`, logs, saves or local configuration.

## Project layout

- `dev.gtnhcontroller`: mod entry point, configuration and proxies.
- `dev.gtnhcontroller.client.input`: SDL profiles, analog movement, actions and registered keybindings.
- `dev.gtnhcontroller.client.gui`: virtual cursor, navigation, scrolling, radial menu and settings screens.
- `dev.gtnhcontroller.mixins`: focused hooks for movement and GUI compatibility.
- `src/test`: input math, activation modes, cursor behavior, navigation, scrolling and codec tests.

See [ARCHITECTURE.md](ARCHITECTURE.md) for the design boundaries.

## Release process

1. Start each fix on a branch from an up-to-date `origin/main`. A separate worktree can keep other unfinished work
   in its existing folder:

   ```powershell
   git fetch origin
   git worktree add -b fix/short-description ..\GTNH-Controller-Support-fix origin/main
   cd ..\GTNH-Controller-Support-fix
   ```

2. Make the source changes, update `CHANGELOG.md`, run `spotlessApply build`, and test the resulting normal JAR in
   the separate GTNH instance.
3. Review and stage only the files for this fix, commit, and push the branch. Create a PR with base `main` and include
   `Fixes #N` for the relevant issue. Wait for its build checks to pass, review the diff, and merge the PR.
4. Wait for the build on the merged `main` commit to pass. Fetch it and inspect the commit to release:

   ```powershell
   git fetch origin
   if ($LASTEXITCODE -ne 0) { throw "Fetch failed; do not create a release tag." }
   $releaseCommit = (git rev-parse origin/main).Trim()
   if ($LASTEXITCODE -ne 0) { throw "Cannot resolve origin/main." }
   git log -1 --oneline $releaseCommit
   ```

   Confirm this is the merged commit whose build passed and that it contains the intended fix. `git fetch` updates
   `origin/main`; it does not advance the currently checked-out local branch. Always provide the verified commit to
   `git tag` explicitly.

5. Choose an unused patch version and create an annotated tag at that exact commit. For example, after merging the
   NEI inventory-click fix following `1.4.2`:

   ```powershell
   $releaseVersion = "1.4.3"
   git grep --quiet -F "ControllerMouseClickContext.dispatchCancelled(callback)" $releaseCommit -- src/main/java/dev/gtnhcontroller/client/gui/GuiController.java
   if ($LASTEXITCODE -ne 0) { throw "The selected commit is missing the NEI inventory-click fix." }
   git tag -a $releaseVersion $releaseCommit -m "GTNH Controller Support $releaseVersion"
   if ($LASTEXITCODE -ne 0) { throw "Tag creation failed; do not push or overwrite an existing tag." }
   git push origin "refs/tags/$releaseVersion"
   if ($LASTEXITCODE -ne 0) { throw "Tag push failed; check the error before continuing." }
   git ls-remote origin "refs/tags/$releaseVersion^{}"
   ```

   The final command must show the same commit as `$releaseCommit`. It resolves an annotated tag to the source
   commit, rather than showing the tag object's own ID.

6. Verify the new `Release tagged build` workflow. Download the normal JAR from that release and test that exact
   asset in a clean GTNH instance. Changes made after tagging require another release; rerunning an old workflow
   still builds its original commit.

Use a new patch version for changes to an already distributed release. If correcting an accidental, undistributed
tag, remove its obsolete release assets as well as the tag before rebuilding it from the intended commit.
