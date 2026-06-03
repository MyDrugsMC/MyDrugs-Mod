# Worldgen Config Notes

This project exposes worldgen behavior through startup/worldgen config. Keep worldgen changes configurable and conservative.

## Principles

- Do not force invasive overworld generation when config disables it.
- Keep TerraBlender registration behind config gates.
- Keep surface rules behind config gates.
- Log useful warnings when config combinations are suspicious.
- Avoid hidden dependencies on client rendering for common worldgen code.

## Areas to inspect

- `Config.java`
- `worldgen/WorldgenConfig.java`
- `worldgen/biomes/ModRegions.java`
- `worldgen/biomes/ModSurfaceRules.java`
- `dimension/inner/worldgen/ModInnerWorldgen.java`

## Validation

Run at least:

```bash
./gradlew compileJava
./gradlew test --tests '*WorldgenConfigTest'
```

For generation behavior, manual world creation/server testing is still required.
