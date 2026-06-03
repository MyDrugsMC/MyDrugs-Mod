# Worldgen and Inner Dimension

Worldgen must be configurable, stable, and non-invasive. The Inner dimension should support the mod fantasy without breaking ordinary worlds.

## Packages

- `worldgen/*` — TerraBlender region, surface rules, POIs, villager professions, config.
- `worldgen/biomes/*` — biome region and surface behavior.
- `dimension/*` — dimension IDs, saved data, dimension services, blocks.
- `dimension/inner/*` — Inner dimension terrain, regions, chunks, overlays, scar healing, atmosphere-related systems.
- `dimension/inner/worldgen/*` — Inner dimension worldgen registration.
- `client/*Inner*` — client atmosphere, sky, particles, soundscape, overlays.

## Config-first rule

Worldgen must respect `Config.WORLDGEN_SPEC` / `WorldgenConfig` gates. If adding invasive overworld content, make it configurable and default-safe.

## TerraBlender

`MyDrugs.commonSetup` registers the overworld TerraBlender region and surface rules only when config allows it. Do not bypass this with unconditional registration.

## Inner dimension design

The Inner dimension is a symbolic inner landscape. It should be:

- beautiful first and dangerous second;
- readable through landmarks, regions, color, sound, and guide/diary hints;
- linked to recovery/integration and ritual systems;
- accessible through late progression, not random early-world noise;
- safe from flashing/motion overload via client accessibility config.

## Technical risks

- generation nondeterminism;
- per-chunk performance cost;
- client-only atmosphere imports in common dimension code;
- unstable saved data formats;
- too much overworld biome invasion;
- inaccessible visuals for reduced-motion players.

## Testing focus

Relevant tests include Inner terrain/noise/region/overlay/scar/persistence tests and worldgen config tests. After changes, run the relevant JVM tests plus `compileJava`; run client/server manually if generation behavior changed.
