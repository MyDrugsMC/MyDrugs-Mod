# Development Setup

This project is a Minecraft NeoForge mod for Minecraft 1.21.10.

## Requirements

- Java 21
- The Gradle wrapper from this repository
- Network access to Maven Central, NeoForge, TerraBlender, JEI, and any configured repositories when dependencies are not cached

## Common commands

Linux/macOS:

```bash
./gradlew compileJava
./gradlew build
./gradlew runData
```

Windows PowerShell:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat build
.\gradlew.bat runData
```

Generated data is written to:

```text
src/generated/resources
```

## Editing the in-game guide

Edit:

```text
docs/progression_guide_pages.md
```

Then run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools\sync_progression_guide.ps1
```

Validate progression content:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools\validate_progression_content.ps1
```

More details are in `GUIDE_AUTHORING.md`.

## Credentials

Do not commit package credentials.

If GitHub Packages credentials are needed, provide them with either:

- environment variables: `GITHUB_ACTOR` and `GITHUB_TOKEN`;
- an untracked local Gradle properties file containing `gpr.user` and `gpr.key`.

Committed `gradle.properties` must not contain real tokens.
