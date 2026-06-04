# Codebase Map

This map is grounded in `scan/tree.txt`, `scan/packages.md`, `scan/resources.md`, and the confirmed source structure.
Use it for orientation before making changes. For exhaustive lists of registries, payloads, or resources, see the
`scan/` files directly.

> **Note:** `confirmed` = verified from source/scan. `inferred` = reasonable from naming/structure. Mark disagreements
> between this doc and the code as a finding, never silently patch one to match the other.

---

## Entry points

| File | Role |
|---|---|
| [`MyDrugs.java`](../src/main/java/org/mydrugs/mydrugs/MyDrugs.java) | Single `@Mod(MyDrugs.MODID)` bootstrap. |
| `MyDrugs.MODID` | `"mydrugs"` *(confirmed)* |
| `MyDrugs.DRUG_USE_SERVICE` | Shared `DrugUseService` — all drug consumption must go through this. |
| `build.gradle` | Java 21, NeoForge ModDev, TerraBlender, JEI, JUnit 5, custom validation tasks. |
| `gradle/validate-code-contracts.gradle` | Source-level architectural contract checks (client imports, DrugUseService usage, pipe contracts, external-tool safety). |
| `gradle/validate-resources.gradle` | JSON/resource/lang/guide/asset validation. |
| `Config.java` | Server, client, and startup config specs registered in bootstrap. |

---

## Bootstrap order in `MyDrugs`

*(confirmed from source)*

The constructor registers deferred registries and static runtime definitions in this broad order:

1. Common setup listener and entity attributes.
2. Blocks, block types (`ModBlockTypes`), items, block entities.
3. Recipes, recipe types, recipe displays.
4. Menus, entities, data components.
5. Fluids, fluid blocks/items, sound events.
6. Biome modifiers, Inner worldgen, POIs, villager professions.
7. Creative tabs, criteria triggers, attachments, crops.
8. `DrugRegistry.registerDrugs()`.
9. `RitualDrugRegistry.registerDefaults()`.
10. Client, server, and startup config specs.

`commonSetup` validates recipe/machine content, logs worldgen warnings, and registers TerraBlender regions/surface
rules when config allows.

---

## Package size snapshot

*(confirmed from `scan/tree.txt`)*

| Top-level package | Java files |
|---|---:|
| `client` | 183 |
| `blocks` | 121 |
| `menu` | 104 |
| `recipes` | 72 |
| `pipe` | 68 |
| `core` | 67 |
| `items` | 63 |
| `dimension` | 62 |
| `addiction` | 53 |
| `network` | 32 |
| `recovery` | 30 |
| `machine` | 23 |
| `diary` | 22 |
| `datagen` | 20 |
| `energy` | 16 |
| `gas` | 12 |
| `mutation` | 12 |
| `worldgen` | 12 |
| `advancement` | 11 |
| `fluids` | 10 |
| `entity` | 7 |
| `progression` | 6 |
| `events` | 5 |
| `psyche` | 5 |
| `commands` | 2 |
| `damage` | 1 |
| `sounds` | 1 |
| `util` | 1 |

---

## Package responsibilities

| Package | Responsibility | Key files / patterns | Main risks |
|---|---|---|---|
| `core/drug/*` | Drug models, categories, runtime effects, ritual formulas, dose/use service, integration tiers. | `DrugUseService` (canonical consumption path), `DrugRegistry`, `RitualDrugRegistry`, `IntegrationService`, `DoseManager`. | Parallel consumption paths, hidden progression grants, persistence bugs. |
| `core/drug/strategy/*` | Route-specific consumption strategies: eating, smoking, sniffing, injecting, drinking. | `ConsumptionStrategy` interface, `RouteEffectProfile`. | Never instantiate directly from items — delegate to `DrugUseService`. |
| `core/drug/ritual/*` | Mixed/ritual drug data, formulas, patents, effect combiner. | `MixedDrugData`, `RitualDrugEffectData`, `RitualDrugFormula`, `DrugPatentSavedData`. | Dynamic stack-based drugs use data components, not runtime registration. |
| `addiction/*` | Addiction, tolerance, withdrawal, stress, bad trip, recovery modifiers, sync payloads. | `AddictionManager`, `WithdrawalManager`, `ToleranceManager`, `BadTripManager`, `SymptomManager`, `PlayerAddictionStats`. | Per-tick performance, client/server authority, untestable state machines. |
| `client/*` | Screens, BERs, overlays, shaders, hallucinations, sounds, guide UI, client network handlers. | `ClientModEvents`, `ClientPayloadHandlers`, shader classes, `GuideBookScreen`. | Dedicated-server crash from client imports in common/server code. |
| `client/shaders/*` | Full-screen post-process effects for drug states. | 16 shader classes (AcidWarp, DrunkVision, WithdrawalTunnel, InnerAtmosphere, etc.), `ShaderManager`. | Performance; respect `reducedMotionMode`. |
| `client/effects/*` | HUD rendering, hallucinations, input interception, sound, overlay render. | `AddictionHudRenderer`, `ClientInputInterceptor`, `ClientSoundController`, `VisionEffectRenderer`. | Motion and sound distortions must respect accessibility toggles. |
| `client/recovery/music/*` | Custom music player, track import, YouTube/yt-dlp downloader, external tools. | `CustomMusicPlayer`, `TrackImportManager`, `YtDownloader`, `ExternalToolManager`. | External tool download from network — check manifest, sandboxed, user-initiated only. |
| `blocks/*` | Block/block-entity implementations, machine descriptors, multiblocks, crop blocks. | `ModBlocks`, `ModBlockEntities`, `ModBlockTypes` (machine type enum), `ModMachineContent` (descriptor registry), `MachineContentDescriptor`, `PsyMixerMultiblock`. | Logic in screens; mismatched menus/recipes/lang; side effects in render code. |
| `blocks/entity/*` | ~40 machine block entities + PsyMixer subsystem (13 classes). | `FormedPsyMixerCoreBlockEntity`, `PsyMixerRitualEngine`, `PsychotropeResonatorBlockEntity`. | Tick-heavy machines; server authority; never call client methods. |
| `blocks/crops/*` | 12 custom crop block types + `ModCrops` registry. | `CropSpec` record for farm definitions. | Growth rate balance; farmland compatibility. |
| `menu/*` | Server menus and client screens for ~30 machines. | `AbstractMachineMenu` base, `AbstractMachineScreen`, per-machine layout classes. | Trusting client input, missing `stillValid`, desync; screens must not own logic. |
| `recipes/*` | Recipe records, serializers, displays, runtime recipe validation. | `ModRecipeTypes`, `ModRecipeSerializers`, `ModRecipeContent`, per-machine recipe sub-packages. | Generated JSON drift; serializer/type/display mismatch; hand-editing generated JSON. |
| `pipe/*` | Item/fluid/gas transfer networks, side configs, filters, route cache, machine side attachment. | `PipeNetworkManager`, `PipeRouteCache`, `PipeTransferTicker`, `MachineTransferAttachment`, `PipeFilterConfig`. | Unbounded scans, duplicate handler transfers, unloaded endpoints. |
| `fluids/*` | Fluid types, spec records, roles, phases, hazards, bottle liquids. | `FluidSpec` record (id, color, viscosity, density, bucket), `FluidRole` enum, `FluidPhase` enum, `Hazard` enum, `ModFluids`, `DrugTintedFluidType`. | Do not hand-edit generated fluid JSON; update `ModFluids` + run `runData`. |
| `gas/*` | Custom gas system: `GasType`, `GasTank`, `IGasHandler`, JEI ingredient bridge. | `ModGases`, `GasSpec`, `CompositeGasHandler`. | Performance; gas capability registration order. |
| `energy/*` | PsyCurrent storage/distribution, machine upgrades, strain risks. | `PsyCurrentDistributor`, `PsyCurrentStorage`, `DistillateFuelRegistry`. | Per-tick scan cost; server authority; `StrainRisk` threshold validation. |
| `dimension/*` | Inner dimension blocks, terrain, persistence, region map, worldgen. | `InnerDimensionSystem`, `InnerChunkGenerator`, `InnerRegionMap`, `InnerDimensionSavedData`. | Non-deterministic generation; client-only code in common paths. |
| `worldgen/*` | TerraBlender region, surface rules, POIs, professions, worldgen config. | `ModRegions`, `ModSurfaceRules`, `ConfigurableAddFeaturesBiomeModifier`. | Invasive overworld generation; config must be respected. |
| `recovery/*` | Recovery items/blocks, sanctuary checks, session management, sleep recovery. | `RecoveryRoomManager`, `SanctuaryModuleDetector`, `RecoverySessionManager`, `SafeZoneManager`. | Must not make recovery punitive or gated behind arbitrary machines. |
| `diary/*` | Per-player diary state, snapshots, clarity service, memory entries. | `PlayerDiaryAttachment`, `DiarySnapshotBuilder`, `DiaryClarityService`, `DiaryEntryGenerator`. | Spoiler levels; no data component persisted without codec round-trip test. |
| `advancement/*` | Custom advancement criteria triggers, drug knowledge, knowledge attachments. | `ModCriteriaTriggers`, `DrugConsumedTrigger`, `PsyKnowledgeUnlockedTrigger`. | Trigger conditions must match server-authoritative events only. |
| `progression/*` | PSY knowledge gating, mastery attachment, progression unlock. | `PsyKnowledgeManager`, `DrugProgressionGate`. | Gate checks must run server-side. |
| `psyche/*` | Psyche map node catalog, milestones, DTO. | `PsycheMapNodeCatalog`, `PsycheMapMilestones`. | Not fully wired per last review; inferred from structure. |
| `mutation/*` | Genetic/mutation state, fragility events, sync payload. | `MutationManager`, `PlayerMutationsAttachment`, `GeneticProfileGenerator`. | Turning body progression into unrelated sci-fi power creep. |
| `network/*` | Centralized payload registration; machine transfer, psy mixer, ritual, drug visual, biome finder payloads. | `ModNetwork`, `ServerPayloadHandlers`, `PayloadRateLimiter`, `PayloadValidation`. | Never trust client ritual phase/timing; validate all server-bound packets. |
| `items/*` | Items, data components, creative tabs, bottles, rolling content, drug items. | `ModItems`, `ModDataComponents`, `ModCreativeTabs`, `DrugItem`, `MixedDrugItem`, `RollerItem`. | Items delegate to `DrugUseService`; direct drug effects in items are forbidden. |
| `datagen/*` | Providers for generated resources and snapshots. | `ModDataGenerators`, `ModBlockTagsProvider`, `ModItemTagsProvider`, `ModFluidBlockStateProvider`, `ModAdvancementProvider`, `VanillaRecipeSnapshotWriter`. | Hand-editing generated JSON; `ModLangProvider` was removed — localization is now hand-authored in `en_us.json`. |
| `commands/*` | `/mydrugs` admin and progression commands. | `ModCommands`, `ProgressionAdminCommands`. | Admin mutations require `hasPermissions(2)` + server config gate. |
| `entity/*` | Custom entities: `InnerDemonEntity`, `ShroomDefenderEntity`, `StonedCowEntity`, `StonedMooshroomEntity`. | `ModEntities`, `ModEntityAttributes`. | Spawn manager must be server-authoritative. |

---

## Notable spec/descriptor patterns

*(confirmed from source and scan)*

The project uses small spec records for group registration to avoid giant static registry sections:

```java
record FluidSpec(String id, int color, int viscosity, int density, boolean bucket,
                 FluidPhase phase, FluidRole role, Hazard... hazards)
record GasSpec(String id, int color, boolean toxic, boolean flammable)
record MachineSpec(String id, MachineTier tier, boolean manual, int tankCapacity)
record CropSpec(String id, Supplier<Block> factory)
record ItemSpec(String id, Supplier<Item> factory)
```

`ModMachineContent` holds machine content descriptors (`MachineContentDescriptor`) that associate block types
with their menu, screen, block entity, and recipe type. `ModBlockTypes` enumerates machine type identifiers.
These descriptor tables are validated at startup by `commonSetup`.

---

## Drug system invariants

*(from `AGENTS.md` and `core/drug/`)*

1. All drug consumption must go through `DrugUseService.consume(...)`.
2. Item classes may delegate — they must never directly apply effects, addiction, tolerance, overdose, dose, or knowledge grants.
3. Dynamic stack-based drugs use data components (`ModDataComponents`), not runtime item registration.
4. Recovery and diary are first-class domains; do not conflate them with addiction.
5. Opioids are deferred — do not expand until core loop is stable.

Drug identity quick map:

Canonical drug identities and risk language: see `docs/GAMEPLAY_DESIGN.md`. Do not restate them here.

---

## Networking invariants

*(from `AGENTS.md` and `network/`)*

- Payloads registered in `ModNetwork`, handled in `ServerPayloadHandlers` and `client/network/ClientPayloadHandlers`.
- Server-bound = requests; validate `ServerPlayer`, open menu, `stillValid`, item/cap state, numeric bounds, rate.
- Client-bound = presentation state; visuals, sounds, HUD snapshots.
- Never use enum ordinals for persistent/networked data; prefer named IDs or `byId` maps.
- `PayloadRateLimiter` and `PayloadValidation` are shared utilities — use them.

---

## Client/server safety rule

No class outside `client/*` may import:

```
net.minecraft.client.*
net.neoforged.neoforge.client.*
org.mydrugs.mydrugs.client.*
```

Client event subscribers must carry `value = Dist.CLIENT` on `@EventBusSubscriber`.
NeoForge 21.10 routes `IModBusEvent` to mod bus automatically — do not add `bus =`.

Validation command:

```bash
rg -n "import net\.minecraft\.client|import net\.neoforged\.neoforge\.client|import org\.mydrugs\.mydrugs\.client" \
    src/main/java/org/mydrugs/mydrugs --glob '!client/**'
```

---

## Test snapshot

*(confirmed from scan, ~39 JUnit test files)*

| Test group | Files |
|---|---:|
| `audit` | 7 |
| `core` | 8 |
| `dimension` | 11 |
| `blocks` | 2 |
| `energy` | 3 |
| `commands` | 1 |
| `guide` | 1 |
| `items` | 1 |
| `mutation` | 1 |
| `progression` | 1 |
| `recovery` | 1 |
| `validation` | 1 |
| `worldgen` | 1 |

Key test roles:

- **Registry snapshot tests** guard public IDs against accidental renames.
- **Recipe/machine descriptor tests** guard menu/serializer/recipe-type/lang consistency.
- **Localization tests** guard all `Component.translatable` keys against `en_us.json`.
- **Component codec tests** guard data component round-trips.
- **Gradle source-scanning validators** guard client imports, `DrugUseService` usage, pipe contracts, external-tool safety.

> JUnit cannot bootstrap Minecraft (no FML loader). All tests use source-scanning + `registry_snapshot.json`.

---

## Source/resource roots

| Root | Purpose |
|---|---|
| `src/main/java/org/mydrugs/mydrugs` | All mod Java source |
| `src/test/java/org/mydrugs/mydrugs` | JUnit audit/unit tests |
| `src/test/resources` | Test resources (`registry_snapshot.json`, etc.) |
| `src/main/resources` | Hand-authored assets, lang, blockstates, guide, recipes, loot tables |
| `src/generated/resources` | Datagen output — do not hand-edit |
| `src/main/templates` | Source templates (version injection) |
| `docs/progression_guide_pages.md` | Authoritative source for `src/main/resources/assets/mydrugs/guide/pages.json` |

---

## Fast orientation commands

```bash
# Find all class/record/enum/interface declarations in a package
rg -n "class|record|enum|interface" src/main/java/org/mydrugs/mydrugs/<target-package>

# Find TODOs, literals, event subscribers, payloads
rg -n "TODO|FIXME|Component\.literal|@EventBusSubscriber|playToServer|registerPayload" src/main/java/org/mydrugs/mydrugs

# Find a specific ID, class, or payload across all source+resources
rg -n "<id_or_class>" src/main/java src/main/resources src/generated/resources docs

# Check dedicated-server safety
rg -n "import net\.minecraft\.client|import net\.neoforged\.neoforge\.client|import org\.mydrugs\.mydrugs\.client" \
    src/main/java/org/mydrugs/mydrugs --glob '!client/**'

# Find all DrugUseService call sites
rg -n "DrugUseService|DRUG_USE_SERVICE" src/main/java/org/mydrugs/mydrugs

# Verify no enum ordinals in network/persistent data
rg -n "\.ordinal\(\)" src/main/java/org/mydrugs/mydrugs
```

Avoid broad reads of generated JSON, huge registries, or unrelated docs until the relevant package is identified.

---

## Related scan files

For exhaustive detail, see:

| Scan file | Contents |
|---|---|
| `scan/tree.txt` | Full file tree of `src/main/java` and `src/main/resources` |
| `scan/packages.md` | Flat package-to-file index |
| `scan/registries.md` | Registry IDs snapshot |
| `scan/resources.md` | Hand-authored and generated resource index |
| `scan/network_payloads.md` | All registered network payload types |
