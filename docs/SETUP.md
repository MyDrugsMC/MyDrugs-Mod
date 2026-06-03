# Setup

## Requirements

- Java 21.
- Gradle wrapper from the repository.
- Internet access for dependency resolution when caches are cold.

## Project versions

From `gradle.properties`:

- Minecraft: `1.21.10`
- NeoForge: `21.10.64`
- Parchment Minecraft: `1.21.10`
- Parchment mappings: `2025.10.12`
- JEI: `26.2.0.30`
- TerraBlender: `1.21.10-21.10.0.0`

## Common commands

```bash
./gradlew compileJava
./gradlew test
./gradlew validateCodeContracts
./gradlew validateResources
./gradlew runData
./gradlew build
./gradlew runClient
./gradlew runServer
```

## GitHub Packages credentials

`build.gradle` can read optional GitHub Packages credentials from:

- Gradle properties: `gpr.user` / `gpr.key` in untracked local Gradle properties;
- environment variables: `GITHUB_ACTOR` / `GITHUB_TOKEN`.

Do not commit credentials.

## Wrapper note

The validator expects `gradlew` to use LF line endings. Keep `.gitattributes` stable.
