# MyDrugs — Refactor TODO

Tracks work that remains after the maintainability refactor sessions.
Completed passes (A, B, C, D, D follow-up, E, F, G, H) and per-commit summaries
live in `git log --oneline`. This file is the **next-step queue**.

Ordering inside each pass is "do first" → "do last". Anything labelled
**security** or **safety** should jump the queue.

---

## Quick wins that didn't fit in the previous sessions

- **AGENTS.md**: drop the explicit `bus = EventBusSubscriber.Bus.MOD/GAME` rule —
  that attribute doesn't exist on `@EventBusSubscriber` in NeoForge 21.10.
  Source: `loader-10.0.32-sources.jar → net/neoforged/fml/common/EventBusSubscriber.java`
  has only `value()` (`Dist[]`) and `modid()`. Routing is automatic per
  `IModBusEvent`. Replace the rule with: *"every client-event subscriber
  must specify `value = Dist.CLIENT`"* and document the auto-routing.

- **CI workflow access to `mydrugsmc/mydrugs-core`**: the workflow assumes
  `secrets.GITHUB_TOKEN` can read that GitHub Packages dep. If the package
  is private and in a different org, swap to a PAT secret. Verify on first CI run.

- **`effects/addiction/client/ClientEventHandler.java`** is now empty except
  for the inner `Game` class (since the payload registration moved to
  `client/network/ClientPayloadHandlers`). Either inline `Game` to top level
  or rename the outer class to reflect its remaining scope. Defer to Pass K
  package split since it'll move anyway.

- **`lang/en_us.json` vs `ModLangProvider`**: the json at
  `src/main/resources/assets/mydrugs/lang/en_us.json` is hand-written (1.7k
  lines), and `ModLangProvider` also generates lang into
  `src/generated/resources` (~311 entries). Two sources of truth = drift risk.
  Decide one canonical source during Pass I (probably "generate everything,
  delete the hand-written file"). Until then, lang keys added in code must be
  added to BOTH or only to the hand-written json.

- **JEI deprecation warnings**: every `*RecipeCategory.java` uses
  `mezz.jei.api.recipe.RecipeType` which is `@removal`-deprecated in JEI
  26.2.0.30. Migrate when JEI's replacement is stable.

---

## Pass G — localization sweep (partial; ~155 of 163 occurrences remain)

Sweep landed only on the clear-cut player-facing strings. The rest fall into
three buckets:

1. **Acceptable per AGENTS.md (dynamic numeric or registered-name values
   wrapped in literal):** `"x / y mB"`, `getFluidName(...)`, gas display
   names, fluid amounts. Leave as-is unless we add a structured
   `Component.translatable("ui.amount", current, max, unit)` later.

2. **Debug/admin command output (`commands/ModCommands.java`)**: allowed
   per AGENTS.md.

3. **Player-facing but still literal — should be translated:**
   - `client/guide/GuideBookScreen.java` lines 103, 106 — `"<"`, `">"`. Arrow
     glyphs; low priority. Translate if a community wants RTL arrows.
   - `effects/addiction/client/diary/PersonalDiaryScreen.java` line 213 — `"..."`
     status spinner. Use `Component.translatable("screen.mydrugs.diary.saving")`
     or `Component.empty()`.
   - `blocks/entity/FormedPsyMixerCoreBlockEntity.java` lines 765–768 — `"["`,
     `"???"`, `"] "` formatting around a translated drug name. Re-author as a
     single `screen.mydrugs.psy_mixer.unknown_drug` translatable.
   - `menu/client/AbstractMachineScreen.java` line 364 — transfer-side button
     label (`sideButton.label()` returns a literal). Should map to
     `gui.mydrugs.transfer_side.*` keys per side.

Audit command:
```bash
rg -n 'Component\.literal\(' src/main/java/org/mydrugs/mydrugs
```

---

## Pass I — Resource / datagen validation

Not started. AGENTS.md acceptance checks:

- [ ] **JSON BOM scan**: write a Gradle task or script that fails the build if
      any `.json` under `src/main/resources` or `src/generated/resources` has
      a UTF-8 BOM. None known yet; previous WIP commits may have introduced one.
- [ ] **Missing-texture manifest**: walk every blockstate and item model under
      the `mydrugs` namespace; for each referenced texture path that does not
      exist, write an entry into `docs/ASSET_TODO.md` (or
      `docs/asset_manifest_missing.md`) with: asset path, referenced by,
      expected use, priority. Do **not** generate placeholder art.
- [ ] **Registered blocks without blockstate**: same idea — flag, don't fix.
- [ ] **Registered items without item definition/model**: same.
- [ ] **Lang coverage**: every registered user-facing item/block/menu/message
      key has an `en_us` translation. Hint: the same audit can compare
      `ModItems`/`ModBlocks` entries against `en_us.json` keys.
- [ ] **Guide `@item` references**: ensure every `@item:...` reference in
      `docs/progression_guide_pages.md` resolves to a registered item/block.
- [ ] **Generated-cache hygiene**: confirm `src/generated/resources/.cache`
      is gitignored (it appears untracked in current git status, which is
      correct — codify the gitignore so a future contributor's IDE does not
      stage it).
- [ ] **Backup files**: scan for `*~`, `*.bak`, `*.orig` and similar accidental
      checkpoints. None found at session end, but worth automating.
- [ ] **Lang source consolidation**: decide between `ModLangProvider` (datagen)
      and the hand-written `en_us.json`. If keeping both, document which keys
      live where.

Add a Gradle task `validateResources` that runs the above and is called by
the `build` task in `.github/workflows/ci.yml`.

---

## Pass J — Performance hardening

Not started. Focus areas from AGENTS.md:

- [ ] **Pipe network rescans**: the TODO about precomputed source/output sets
      (see `pipe/network/`) is still open. Either implement dirty-flag-based
      caching or convert it into a tracked design issue with clear cost
      estimates.
- [ ] **`PayloadRateLimiter` UUID map cleanup audit**: cleanup hook is wired
      to `PlayerLoggedOutEvent`; verify under a long-running server that the
      map stays bounded.
- [ ] **`PsychedelicOreAuraClient` per-frame scan**: bounded already
      (`SCAN_INTERVAL_TICKS = 32`, `SCAN_RADIUS = 20`, `MAX_AURAS = 48`) —
      profile to confirm.
- [ ] **`FakeEntityRenderController` hallucination ticking**: now gated by
      `enableHallucinations` (Pass H). Still walks `MAX_SPAWN_TRIES = 24`
      raycasts per spawn attempt — consider amortizing across ticks.
- [ ] **Addiction / dose / withdrawal ticking** in `PlayerTickEvents`:
      `AddictionManager.tickPlayer` + `DrugEffectRuntimeManager.tickServer`
      run every tick per player. Profile under load (8+ players); throttle to
      every-other-tick if hot.
- [ ] **Companion / safe-zone / social-relief checks**: `SocialReliefManager`
      and `SafeZoneManager` are queried by `AddictionDebugOpenPayload.from`
      and likely on tick. Cache per-tick.
- [ ] **Machine transfer scans**: `MachineTransferAttachments` is queried in
      several places per tick. Verify the supports/ports lookup is cached.

Add `runtime` and `world-tick` profiler events around these so future
regressions are visible in `--profile`.

---

## Pass K — Package split (effects/addiction)

Not started. This is the largest pass. Target package map from AGENTS.md:

```
org.mydrugs.mydrugs.addiction
org.mydrugs.mydrugs.addiction.attachment
org.mydrugs.mydrugs.addiction.data
org.mydrugs.mydrugs.addiction.manager
org.mydrugs.mydrugs.addiction.progression
org.mydrugs.mydrugs.addiction.withdrawal
org.mydrugs.mydrugs.addiction.tolerance
org.mydrugs.mydrugs.addiction.config

org.mydrugs.mydrugs.core.drug.dose          (was effects/addiction/dose)
org.mydrugs.mydrugs.core.drug.runtime       (was effects/addiction/manager/effect)

org.mydrugs.mydrugs.recovery                (was effects/addiction/manager/recovery)
org.mydrugs.mydrugs.recovery.block          (RecoveryAnchorBlock, TherapistDeskBlock)
org.mydrugs.mydrugs.recovery.item           (recovery items)

org.mydrugs.mydrugs.diary                   (DiaryEntry, DiaryEntryGenerator, ...)
org.mydrugs.mydrugs.client.diary            (PersonalDiaryScreen, DrugIconHelper)

org.mydrugs.mydrugs.client.effects.*        (hud / render / hallucination / sound / input)
```

Procedure for each move:
1. Move the file.
2. Update the `package` line and any imports.
3. `rg` for the old fully-qualified name everywhere and update.
4. Verify with `gradlew compileJava` before the next move.
5. Squash into one PR per top-level package boundary, so review is sane.

Migration order (least-coupled first):
1. `recovery.item.*` items (5 classes).
2. `diary.*` server-side + `client.diary.*` UI (~6 + 2 classes).
3. `addiction.config`, `addiction.data`, `addiction.attachment`.
4. `core.drug.dose` and `core.drug.runtime` extractions.
5. `addiction.manager.*` consolidation (largest).
6. `client.effects.*` reshuffle.

Block: `effects/addiction/client/ClientEventHandler` ends up empty after
Pass D follow-up; inline or rename during this pass.

Also expected to relocate during Pass K:
- `effects/payloads/DrugVisualPayload.java` — consolidate under
  `client/effects/` or `network/`.

---

## Pass L — Registry records / specs

Not started. From AGENTS.md:

- [ ] Introduce small records: `ItemSpec`, `MachineSpec`, `FluidSpec`,
      `GasSpec`, `CropSpec`.
- [ ] Move coherent groups out of huge files **without changing registry IDs**:
      - recovery items (now in `effects/addiction/item/`)
      - mutation items (in `items/`)
      - pipe items (in `pipe/item/`)
      - machine blocks (in `blocks/`)
      - crop blocks / items (in `blocks/crops/`)
      - fluid / gas definitions
- [ ] Split `ModItems` and `ModBlocks` into sub-classes per domain. Keep the
      top-level `register(modEventBus)` call site stable.
- [ ] Creative tabs by category once the item count justifies it (currently
      one `MYDRUGS_TAB`).

Verify by `gradlew runData` showing zero changes to generated JSON — that
proves registry IDs are unchanged.

---

## Pass M — Documentation

Not started. Update after Passes I–L land:

- [ ] **AGENTS.md** — sync the package map to whatever Pass K produced.
      Drop the `bus =` rule (see "Quick wins" above).
- [ ] **docs/ARCHITECTURE.md** — refresh the canonical drug-use diagram and
      the package responsibility table.
- [ ] **docs/TESTING.md** — document the new `validateResources` task from
      Pass I; document the audit `rg` commands; document `PayloadRateLimiter`
      tuning constants.
- [ ] **docs/AI_WORKFLOW.md** — only if the workflow changes. Likely no edit
      needed.
- [ ] **docs/ASSET_TODO.md** — generated/maintained by Pass I.
- [ ] **Mod page** (CurseForge / Modrinth) — `mod_description` was filled in
      `gradle.properties`; if either platform expects a separate description
      file, mirror it.

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
- **`Component.literal("...")` spinner in `PersonalDiaryScreen`** — should
  probably become a small animated translatable.
- **`StimulantDashClient`** — the key registration handler currently lives on
  a class annotated with `bus` (well, with `value = Dist.CLIENT`) and
  NeoForge auto-routes. Kept as a single class for simplicity. If Pass K
  splits client/effects/input, move it there.

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
rg -n "AddictionManager\.|DoseManager\.|DrugEffectRuntimeManager\." src/main/java/org/mydrugs/mydrugs/items src/main/java/org/mydrugs/mydrugs/effects/addiction/item

# Build gates
./gradlew compileJava
./gradlew runData
./gradlew build
```
