# MyDrugs — Refactor TODO

Tracks work that remains after the maintainability refactor sessions.
Completed passes (A, B, C, D, D follow-up, E, F, G, H) and per-commit summaries
live in `git log --oneline`. This file is the **next-step queue**.

Ordering inside each pass is "do first" → "do last". Anything labelled
**security** or **safety** should jump the queue.

Status after current cleanup pass:

- Quick wins: AGENTS event-bus guidance, CI package-token fallback, and
  ClientEventHandler flattening are done. JEI RecipeType remains deferred until
  JEI publishes the stable replacement API.
- Pass G: complete. The explicitly listed player-facing literals are translated. Dynamic
  amount/fluid/name literals remain intentionally literal per AGENTS.md.
- Pass H: complete per `git log --oneline`; accessibility gates remain wired for shaders, camera shake, hallucinations, sounds, HUD, reduced motion, and screamers.
- Pass I: complete. validateResources is wired into Gradle/build/CI and writes
  docs/ASSET_TODO.md for missing artist assets.
- Pass J: complete. Added profiler sections, bounded rate-limiter cleanup, same-tick
  recovery query caches, machine-transfer spec caching, and amortized
  hallucination spawn raycasts.
- Pass K: complete. Legacy `effects/addiction` and `effects/payloads` Java
  sources were moved to addiction, core drug, recovery, diary, client effects,
  and network packages.
- Pass L: complete. Registry spec records and domain helpers now own recovery,
  mutation, pipe, crop, fluid, and gas groups while keeping top-level aliases
  and registry IDs stable.
- Pass M: complete. AGENTS, architecture, testing, asset manifest, and this
  TODO queue match the landed package/registry/validation changes.

---

## Quick wins that didn't fit in the previous sessions

- **AGENTS.md event-bus rule**: done. Client subscribers use `value = Dist.CLIENT`; NeoForge 21.10 auto-routes mod-bus and game-bus events.

- **CI workflow access to `mydrugsmc/mydrugs-core`**: the workflow assumes
  `secrets.GITHUB_TOKEN` can read that GitHub Packages dep. If the package
  is private and in a different org, swap to a PAT secret. Verify on first CI run.

- **ClientEventHandler flattening**: done. The class now lives at `client/effects/ClientEventHandler.java` and has no placeholder outer class.

- **`lang/en_us.json` vs `ModLangProvider`**: decided during Pass I. The hand-written `src/main/resources/assets/mydrugs/lang/en_us.json` remains canonical until datagen covers the full file.

- **JEI deprecation warnings**: every `*RecipeCategory.java` uses
  `mezz.jei.api.recipe.RecipeType` which is `@removal`-deprecated in JEI
  26.2.0.30. Migrate when JEI's replacement is stable.

---

## Pass G - Localization sweep

Done.

- Explicit player-facing literals from the remaining list were translated:
  guide navigation arrows, diary navigation/saving status, Psy Mixer unknown
  speaker prefix, and machine transfer side/world-side labels.
- Remaining `Component.literal(...)` hits are intentionally allowed buckets:
  debug/admin command output, dynamic numeric values, registered fluid/gas names,
  IDs, and width-truncated already-localized text.

Audit command:
```bash
rg -n 'Component\.literal\(' src/main/java/org/mydrugs/mydrugs
```

---

## Pass H - Accessibility and client-effect safety

Done in the previous Pass H commit and re-checked in this cleanup.

- Client visual/audio systems still respect `Config.CLIENT` toggles for shaders,
  camera shake, hallucinations, heartbeat/drug sounds, HUD visibility, compact
  HUD, reduced motion, and bad-trip screamers.
- Hallucination spawning remains behind `enableHallucinations` and now amortizes
  spawn raycasts across ticks.

---

## Pass I - Resource / datagen validation

Done.

- [x] JSON BOM scan for `.json` under `src/main/resources` and `src/generated/resources`.
- [x] Missing texture/model references are written to `docs/ASSET_TODO.md` with asset path, reference, expected use, and priority.
- [x] Registered blocks without blockstates fail validation.
- [x] Registered items without client item definitions/models fail validation.
- [x] Registered item/block lang coverage is validated against the canonical hand-written `en_us.json`.
- [x] Guide `@item` references in `docs/progression_guide_pages.md` resolve to registered items or blocks.
- [x] `src/generated/resources/.cache/` is explicitly gitignored.
- [x] Backup/checkpoint files such as `*~`, `*.bak`, `*.orig`, `.rej`, `.swp`, and `.swo` fail validation.
- [x] Lang source decision is documented: hand-written `src/main/resources/assets/mydrugs/lang/en_us.json` remains canonical until `ModLangProvider` covers the full file.

`validateResources` is wired into Gradle and CI/build.

---

## Pass J - Performance hardening

Done.

- [x] Pipe networks keep dirty-seed rebuilds and now precompute per-source output candidates for fluid/gas distribution.
- [x] `PayloadRateLimiter` removes logout state, periodically purges stale UUID entries, and exposes `trackedPlayerCount()` for future bounded-map audits.
- [x] `PsychedelicOreAuraClient` remains bounded by scan interval/radius/max aura constants and is covered by client profiling guidance.
- [x] `FakeEntityRenderController` remains gated by `enableHallucinations` and amortizes spawn raycasts across ticks.
- [x] `PlayerTickEvents` has `mydrugs:runtime` profiler sections around addiction, drug-effect runtime, lightning bottle, and mutation work.
- [x] `SocialReliefManager` and `SafeZoneManager` cache same-tick per-player queries.
- [x] `MachineTransferSpecs` caches specs by block entity type so supports/ports lookups do not rebuild specs on each query.
- [x] Pipe world ticking has a `mydrugs:world_tick` / `pipe_networks` profiler section.

---

## Pass K - Package split

Done. Landed package map:

```
org.mydrugs.mydrugs.addiction
org.mydrugs.mydrugs.addiction.attachment
org.mydrugs.mydrugs.addiction.data
org.mydrugs.mydrugs.addiction.manager
org.mydrugs.mydrugs.addiction.progression
org.mydrugs.mydrugs.addiction.withdrawal
org.mydrugs.mydrugs.addiction.tolerance
org.mydrugs.mydrugs.addiction.config

org.mydrugs.mydrugs.core.drug.dose
org.mydrugs.mydrugs.core.drug.runtime

org.mydrugs.mydrugs.recovery
org.mydrugs.mydrugs.recovery.block          (RecoveryAnchorBlock, TherapistDeskBlock)
org.mydrugs.mydrugs.recovery.item           (recovery items)

org.mydrugs.mydrugs.diary                   (DiaryEntry, DiaryEntryGenerator, ...)
org.mydrugs.mydrugs.client.diary            (PersonalDiaryScreen, DrugIconHelper)

org.mydrugs.mydrugs.client.effects.*        (hud / render / hallucination / sound / input / network)
org.mydrugs.mydrugs.network                 (shared payload records)
```

- `ClientEventHandler` now lives in `client.effects` and keeps only client
  event dispatch.
- `DrugVisualPayload` now lives in `network`.
- `rg -n "effects\.addiction|effects/payloads|org\.mydrugs\.mydrugs\.effects\.payloads" src/main/java`
  should return no Java source hits.

---

## Pass L - Registry records / specs

Done.

- [x] Introduced `ItemSpec`, `MachineSpec`, `FluidSpec`, `GasSpec`, and `CropSpec`.
- [x] Moved coherent registration ownership without changing registry IDs:
      recovery items/blocks, diary item, mutation items, pipe items/blocks,
      crop blocks/items, fluid specs, and gas specs.
- [x] Kept top-level `ModItems` and `ModBlocks` aliases and register call sites stable.
- [x] Kept one `MYDRUGS_TAB`; category tabs are still not justified by current UX.

Verify by `gradlew runData` and review generated-resource diffs. Registry holder
aliases preserve existing IDs.

---

## Pass M - Documentation

Done.

- [x] **AGENTS.md** — synced package map and registry-helper guidance.
- [x] **docs/ARCHITECTURE.md** — refreshed package boundaries and registry notes.
- [x] **docs/TESTING.md** — document the new `validateResources` task from
      Pass I; document the audit `rg` commands; document `PayloadRateLimiter`
      tuning constants.
- [x] **docs/AI_WORKFLOW.md** — no workflow change required.
- [x] **docs/ASSET_TODO.md** — generated/maintained by Pass I.
- [x] **Mod page** — no repo-side platform description file exists; keep
      `gradle.properties` `mod_description` as the current source until a
      CurseForge/Modrinth publishing config is added.

---

## Items deferred but worth tracking

- **JEI `RecipeType` migration** — every `*RecipeCategory.java` triggers a
  removal warning. JEI 26.2.0.30 marked the old API for removal; track JEI's
  replacement docs and migrate before a JEI bump breaks us.
- **`SyringeItem` mutation injection path** — currently not routed through
  `DrugUseService`. That's correct (it's mutation, not drug consumption),
  but if mutation grows its own canonical-service equivalent in the future,
  this should funnel through it.
- **`OverdoseAntidoteItem` direct `DoseManager.applyAntidote` call** — Pass E
  documented this as intentional. If the canonical-recovery side ever gets a
  `RecoveryService`, route through it.
- **`BiomeFinderSelectPayload`** — `InteractionHand.valueOf(name)` is fine
  for the two-element enum but if more hands are ever added this needs a
  whitelist instead of string parsing.
- **`StimulantDashClient`** — the key registration handler currently lives on
  a class annotated with `value = Dist.CLIENT` and NeoForge auto-routes. Kept
  as a single class for simplicity.

---

## Always-on audit commands

Run these before merging anything:

```bash
# Common code does not import client classes
rg -n "import net\.minecraft\.client|import net\.neoforged\.neoforge\.client|import org\.mydrugs\.mydrugs\.client" src/main/java/org/mydrugs/mydrugs --glob '!client/**'

# Player-facing literal strings (review each match against the AGENTS.md rule)
rg -n "Component\.literal\(" src/main/java/org/mydrugs/mydrugs

# Server-bound payload-handler surface (every match should validate ServerPlayer + clamp + stillValid where applicable)
rg -n "playToServer|\.ordinal\(|ByteBufCodecs\.STRING_UTF8" src/main/java/org/mydrugs/mydrugs

# Direct manager calls from item classes (must be DrugUseService for consumption)
rg -n "AddictionManager\.|DoseManager\.|DrugEffectRuntimeManager\." src/main/java/org/mydrugs/mydrugs/items src/main/java/org/mydrugs/mydrugs/recovery/item

# Build gates
./gradlew compileJava
./gradlew runData
./gradlew build
```
