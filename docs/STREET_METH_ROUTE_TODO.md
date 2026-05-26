# Street Meth Route — Implementation Status

Branch: `feature/street-meth-route`

## What landed

| Phase | Work |
| --- | --- |
| 1 | `PURITY` `DataComponentType<Float>` + helper + tooltip + grinding preservation |
| 2 | Ephedra crop block + `EPHEDRA_CUTTINGS` placeable item + worldgen (desert / savanna / badlands) |
| 3 | Phosphate ore (+ deepslate) + worldgen + raw_phosphorus → reactive_phosphorus smelt |
| 4 | Reduction Still machine (block / BE / menu / screen / recipe type / serializer / JEI category). Auto-tick low-power, water-bucket solvent |
| 5 | MixingVat cook recipe (ephedra_extract + reactive_phosphorus + crude_reactant_cake + HCl → crude_meth_slurry). Evaporation Tray recipe (crude_meth_slurry → meth_shard with purity 0.15..0.38) |
| 6 | `MethPurityModifiers` + DrugUseService/AddictionManager plumbing. Applies dose/intensity/duration/tolerance/stress/contaminant modifiers only for smoked meth |
| 7 | Telegraphed cook fume hazard (poison + nausea radius + fire on adjacent flammables). Respects `enableCookHazards` + `cookHazardIntensity` server config |
| 8 | Regression test `OverclockedGateTest` — gate keys on form, not purity |
| 9 | Lang entries in `en_us.json`, status messages, this document |

## Outstanding art / polish work

These are the placeholder textures/models needed before shipping. Minecraft will render the magenta/black missing-texture pattern in their place today.

### Items (need `assets/mydrugs/textures/item/<name>.png` + matching `models/item/<name>.json`)
- `ephedra_cuttings`
- `raw_phosphorus`
- `reactive_phosphorus`
- `ephedra_extract`
- `spent_plant_pulp`
- `crude_reactant_cake`

### Blocks (need block model + blockstate + textures)
- `phosphate_ore` + `deepslate_phosphate_ore`
- `ephedra_crop` (age 0..7 stages, like other CropBlock crops)
- `reduction_still` (single block, faces FACING)

### Fluids
- `crude_meth_slurry` — `FluidEntry` is registered with placeholder colour `0xFFA08850`; texture (still + flow) needs to follow the existing fluid asset pattern. `crude_meth_slurry_bucket` item model is auto-generated.

### Reduction Still screen background
- Currently uses procedural `drawWindow` / `drawPanel` calls inherited from `AbstractMachineScreen`. Functional but bare. A proper GUI texture would match the polish of `ManualCoffeePulper` etc.

## Tuning notes (constants, not literal numbers in code)

- `Purity.STREET_MAX = 0.40F`, `Purity.CUT_MAX = 0.75F` — bands shown in tooltip
- `MethPurityModifiers` — all six coefficients per spec, in named constants
- `StreetCookFumeHazard.VENT_BASE_CHANCE_PER_SECOND = 0.06`, radius 3.5, fire chance 0.20 per adjacent face
- `ReductionStillRecipe` defaults `cuttings_per_batch=16`, `work=600` ticks
- `EvaporationTrayRecipe` purity range for street shards: `0.15..0.38`
- Phosphate ore: vein size 5, 6 veins/chunk, Y `-32..64` (configurable)
- Ephedra spawn rate: `1 / 80` chunks (sparser than lavender at `1 / 24`)

## Server config additions

- `mydrugs.gameplay.enableCookHazards` (bool, default true)
- `mydrugs.gameplay.cookHazardIntensity` (double 0..4, default 1)
- `mydrugs.worldgen.enableEphedra` + `ephedraSpawnRate`
- `mydrugs.worldgen.enablePhosphateOre` + vein-size / per-chunk / height range

## Verification

- `./gradlew compileJava` — green
- `./gradlew test` — green (includes new `OverclockedGateTest`)
- `./gradlew runData` — green; new biome modifiers, configured/placed features, loot tables, fluid blockstate / bucket item all emitted to `src/generated/resources/`

## Architectural note: `ModLangProvider` is not wired

`ModDataGenerators.gatherData` does not call `event.createProvider(ModLangProvider::new)`. New lang keys go into `src/main/resources/assets/mydrugs/lang/en_us.json` directly. Worth registering ModLangProvider in `ModDataGenerators` so future contributors can add lang via the provider class instead of the JSON.
