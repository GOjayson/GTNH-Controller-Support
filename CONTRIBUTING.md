# Contributing

Bug reports, controller compatibility results, documentation improvements and focused code contributions are welcome.

## Reporting a bug

Search existing issues first. If the problem is new, include:

- exact GTNH version;
- Java version;
- controller make, model and connection method;
- affected screen, machine or action;
- expected and actual behavior;
- reproduction steps;
- `latest.log` and `fml-client-latest.log` when relevant.

Do not include world files, account tokens, server credentials or unrelated personal information.

## Controller compatibility reports

Useful reports include:

- whether SDL detects the controller;
- the displayed controller name under F3;
- face buttons, sticks, triggers, D-pad and stick clicks;
- whether rear buttons appear independently;
- controller firmware mode, such as Xbox, Switch or DirectInput;
- whether Steam Input or another remapper was enabled.

## Code changes

1. Create a branch from `main`.
2. Keep changes narrow and avoid unrelated formatting.
3. Add or update tests for input math, state machines and pure compatibility logic.
4. Run:

   ```powershell
   .\gradlew.bat spotlessApply
   .\gradlew.bat test
   .\gradlew.bat build
   ```

5. Test the normal JAR in a disposable GTNH 2.9.x instance.
6. Explain the affected controller, GUI or mod action in the pull request.

Minecraft 1.7.10 has many custom input paths. Prefer a narrow adapter for a proven incompatibility instead of global
fake keyboard or mouse state that could break other mods.

## On-screen keyboard

Printable character input remains unresolved. Investigations are welcome, but a proposed fix must be tested in the
real GTNH pack against at least:

- Creative Search;
- NEI Search;
- chat;
- server address entry;
- one naming or text-entry container.

Backspace, navigation and layout switching must continue to work.

## Documentation and media

Screenshots and videos should avoid exposing server addresses, usernames, private chat, world coordinates or other
personal information. Replace the files under `docs/media` and update the root README when real media is available.

See [Development](docs/DEVELOPMENT.md), [Testing](docs/TESTING.md) and
[Architecture](docs/ARCHITECTURE.md) for project-specific details.
