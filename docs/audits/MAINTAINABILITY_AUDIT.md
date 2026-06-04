> Snapshot generated from `scan/` and source review on 2026-06-03. Point-in-time report, not a contract. If this disagrees with source, source wins.

# Maintainability Audit

Generated from: `scan/client_server_violations.md`, `scan/component_literal_report.md`,
`scan/performance_hotspots.md`, `scan/todos.md`, `scan/validation.md`, and direct source review
of recently changed files and structural hot-zones.

Legend: `[confirmed]` = directly read from source. `[inferred]` = reasonable from naming/structure alone.

---

## Executive summary

The codebase is architecturally healthy in its most critical invariants:
- **Client/server boundary: PASS** (`scan/client_server_violations.md` reports zero violations).
- **Network hardening: good** — debug payloads require `hasPermissions(2)` + server config gate.
- **Drug consumption path: enforced** — `DrugUseService` is the canonical entry; items delegate correctly.
- **Pipe network: healthy** — dirty-flag rebuild, bounded BFS, WeakHashMap lifetime management.

Key maintenance risks are:
1. A cluster of open-field `PlayerAddictionStats` mutation scattered across domain boundaries.
2. A per-tick per-player chain that keeps growing without profiler markers.
3. `RecoveryRoomManager.getBestRoom()` scans up to 25×25×25 = 15 625 block reads on every cache miss.
4. A large and growing count of `Component.literal` in player-facing tooltips inside machine screens.
5. Several stub docs that agents and contributors are told to read but that are still empty.

---

## Priority 0 — Correctness and server safety

### Issue: PlayerAddictionStats is an all-public mutable struct

**Problem:**
Every field of `PlayerAddictionStats` is `public` (non-final where mutated), including `stressLevel`,
`overdoseDeathTimer`, `addictionSymptomsImmune`, `perDrug`, and the nested `temporaryEffects` object.
Any code anywhere on the server can mutate player addiction state directly without going through a
manager, silently bypassing rate limits, sync, and invariant checks.

**Evidence:** [confirmed]
- `PlayerAddictionStats.java` — all fields public, mutable.
- `AddictionDebugActionPayload.handleOnServer()` L44: `stats.perDrug.clear()` — direct field mutation.
- `ItemEffectHandler.applyDiary()` L39: `stats.temporaryEffects.diaryCalmUntil = now + ...` — direct nested field mutation.
- Many other callers in `AddictionManager`, `WithdrawalManager`, `StressManager`, etc.

**Files:**
- `addiction/data/PlayerAddictionStats.java`
- `addiction/data/TemporaryRecoveryEffects.java`
- `addiction/manager/ItemEffectHandler.java`
- `addiction/network/AddictionDebugActionPayload.java`

**Risk:**
Any future feature adding or reordering fields could silently break an invariant because there is no
mutation bottleneck to update. Serialization bugs from untracked field additions are also harder to
catch.

**Recommended fix:**
Do not expose `temporaryEffects` publicly for direct mutation. Add mutation methods to
`TemporaryRecoveryEffects` that encapsulate the dirty-flag pattern. `perDrug` can remain internal to
`PlayerAddictionStats` — expose it only through the existing safe accessor methods.

**Safe first step:**
Mark `temporaryEffects` and `badTrip` as package-private in `PlayerAddictionStats`. Fix the callers
in `addiction/*` first; this will immediately show any cross-package direct mutations that need to
become method calls.

**Validation:**
`./gradlew compileJava` must pass with no new errors after narrowing visibility.

---

### Issue: AddictionManager.consume(player, category, dose) resolves to a representative drug

**Problem:**
The `consume(player, DrugCategory, dose)` overload (line 49 of `AddictionManager`) resolves
`DrugId` via `DrugRegistry.getRepresentativeDrugId(category)`. Any call site using the category
overload records addiction on the *representative* drug, not the actual drug consumed. If a category
has multiple drugs, this conflates their addiction stats.

**Evidence:** [confirmed]
- `AddictionManager.java` L49-67 — category overload delegates to representative ID.
- `DrugRegistry.getRepresentativeDrugId()` — returns a single fallback per category.

**Files:**
- `addiction/manager/AddictionManager.java`
- `core/drug/DrugRegistry.java`

**Risk:**
Any call site that passes a `DrugCategory` instead of the concrete `DrugModel` loses per-drug
tracking. This may be intentional for some legacy paths, but it should be explicit, not a silent
fallback.

**Recommended fix:**
Mark the category overload `@Deprecated` and annotate the usages so they can be migrated to
`consume(player, DrugModel, dose)` one by one.

**Safe first step:**
Add `@Deprecated` to the two category overloads. Run
`rg -n "AddictionManager.consume" src/main/java` to enumerate all call sites and decide which are
intentional.

**Validation:**
`./gradlew compileJava` — deprecation warnings in output confirm all call sites found.

---

### Issue: CocainePowderPileBlock instant consumption has no animation round-trip

**Problem:**
Two TODO comments in `CocainePowderPileBlock` explicitly note that the snorting gesture is missing:
- L159: `// TODO: Replace instant consumption with a client/server snorting animation.`
- L185: `// TODO: send a client payload to play a snorting animation; defer rail removal until completion.`

Currently the consumption fires instantly on right-click with no visual feedback sequence.

**Evidence:** [confirmed]
- `blocks/CocainePowderPileBlock.java` L159, L185.

**Files:**
- `blocks/CocainePowderPileBlock.java`

**Risk:**
No animation means the feedback contract (every effect needs readable feedback) is partially unmet
for this specific consumption path. Also, without an animation lock, the client can spam right-clicks
faster than the server validates consumption.

**Recommended fix:**
Add a server-authoritative cooldown timestamp. The client animation is a future enhancement; the
rate-limit fix is the correctness issue.

**Safe first step:**
Add a `lastConsumeTick` field to block state or to the player's attachment, and check it before
allowing consumption. This is independent of the animation.

**Validation:**
Manual in-game test: right-clicking multiple times rapidly should not produce multiple consumption
events faster than a configurable interval.

---

## Priority 1 — Architecture boundaries

### Issue: ItemEffectHandler lives in addiction/manager but owns recovery domain logic

**Problem:**
`ItemEffectHandler` is in the `addiction/manager` package but its methods directly drive
`RecoveryRoomManager`, `RecoverySessionManager`, `RecoveryProgressManager`, `IntegrationService`,
and `SafeZoneManager`. According to `ARCHITECTURE.md`, addiction and recovery are separate domains:
`addiction/*` owns "tolerance, withdrawal, addiction state, bad-trip state, sync snapshots" while
`recovery/*` owns "recovery blocks/items/sanctuary mechanics".

**Evidence:** [confirmed]
- `addiction/manager/ItemEffectHandler.java` imports: `RecoveryRoomManager`, `RecoverySessionManager`,
  `RecoveryProgressManager`, `SafeZoneManager`, `IntegrationService`, `SanctuaryModule`.

**Files:**
- `addiction/manager/ItemEffectHandler.java`

**Risk:**
Placing this logic in `addiction/*` makes it invisible to agents working on `recovery/*`. New recovery
items will be added to `recovery/item/`, but their effect application will need to go into a class
in `addiction/manager/` to access the pattern — this will not be obvious.

**Recommended fix:**
Move `ItemEffectHandler` to `recovery/` (e.g., `recovery/RecoveryItemEffectHandler`). It already
primarily serves recovery item consumption.

**Safe first step:**
Create `recovery/RecoveryItemEffectHandler.java` as a copy of `ItemEffectHandler`, verify
`compileJava` passes, then delete the original and update `ModAttachments` import.
Do not move until ready to test, as `AddictionManager` and headphone tick paths import it.

**Validation:**
After move: `rg -n "ItemEffectHandler" src/main/java` should find zero references in `addiction/*`.
`./gradlew compileJava` must pass.

---

### Issue: core/client/ contains client-only code in a shared package

**Problem:**
`core/client/ClientState.java`, `core/client/shader/ClientShaderManager.java`, and
`core/client/shader/Shader.java` are under `core/` — a package shared with server-side code.
Although the `client_server_violations` scan passes today, placing client classes under `core/`
creates a risk that future agents add server-only code next to them and miss the boundary.

**Evidence:** [confirmed]
- `scan/tree.txt` — `core/client/` subtree exists alongside `core/drug/`, `core/Core.java`.

**Files:**
- `core/client/ClientState.java`
- `core/client/shader/ClientShaderManager.java`
- `core/client/shader/Shader.java`

**Risk:**
The package name implies these are available to all layers, but they are client-only. Any copy/paste
from `core/` to a server-side class would trigger a real dedicated-server crash.

**Recommended fix:**
Move these three files to `client/core/` or integrate them into `client/shaders/` and
`client/ClientStateHolder.java` respectively.

**Safe first step:**
Check all import sites:
`rg -n "core.client" src/main/java/org/mydrugs/mydrugs --glob '!client/**'`
If only client code imports them, the move is safe. Plan the move to `client/` before executing.

**Validation:**
After move: the dedicated-server import scan must still pass.

---

### Issue: RecoveryProgressManager lives under core/drug/integration

**Problem:**
`RecoveryProgressManager` tracks productive-action-driven detox and recovery progress for
integration eligibility. Its primary concern is recovery progression, not drug modeling.
Placing it under `core/drug/integration` creates a cross-domain call pattern: `recovery/*` classes
call into `core/drug/integration` for what is effectively a recovery-domain operation.

**Evidence:** [confirmed]
- `core/drug/integration/RecoveryProgressManager.java` — imports 8 classes from `recovery/*` and
  `addiction/*` and directly mutates `DrugAddictionStats`.
- `addiction/manager/ItemEffectHandler.java` imports `RecoveryProgressManager` from `core/drug/integration`.

**Files:**
- `core/drug/integration/RecoveryProgressManager.java`

**Risk:**
Low immediate risk; this is a clarity issue. Agents assigned to recovery work won't expect to find
recovery progress logic in `core/drug/integration`.

**Recommended fix:**
Consider a future move to `core/drug/integration/` → remaining (integration eligibility concern)
and `recovery/RecoveryProgressService` (recovery arc concern). This is a large change; defer until
the domains are otherwise stable.

**Safe first step:**
Add a package-level `package-info.java` comment to `core/drug/integration/` noting that
`RecoveryProgressManager` is an integration gate concern. This improves discoverability without code
change.

**Validation:**
No code change needed for the first step.

---

## Priority 2 — Duplicated or parallel systems

### Issue: Headphone state changes send two payloads to the client

**Problem:**
Every headphone state mutation in `ItemEffectHandler` calls both `syncHeadphones(player)` (sending
`HeadphonesStatePayload`) and `syncClientHud(player)` (sending `AddictionClientSnapshotPayload`).
Two separate packets are sent on each toggle, cycle, or set-playing call.

**Evidence:** [confirmed]
- `addiction/manager/ItemEffectHandler.java` L80: both `syncHeadphones(player)` and
  `syncClientHud(player)` called from `toggleHeadphones`.
- Same pattern in `cycleHeadphonesTrack`, `setHeadphonesPlaying`.

**Files:**
- `addiction/manager/ItemEffectHandler.java`

**Risk:**
Low correctness risk; the duplicate packet is extra bandwidth and client-side processing. The real
risk is that `HeadphonesStatePayload` and the HUD snapshot carry overlapping headphone state, which
could lead to ordering-dependent display bugs if packets arrive out of order.

**Recommended fix:**
Merge the headphone state into the HUD snapshot, removing the separate `HeadphonesStatePayload`
sync from `ItemEffectHandler`. Or suppress the HUD sync when the headphones payload already carries
the full state.

**Safe first step:**
Check which client-side fields are only populated by `HeadphonesStatePayload` vs. the snapshot.
Enumerate with `rg -n "HeadphonesStatePayload" src/main/java`.

**Validation:**
Manual test: toggle headphones; verify client HUD and sound both update once.

---

### Issue: AddictionManager.getGlobalSeverity() duplicates the tickPlayer() severity loop

**Problem:**
`AddictionManager.tickPlayer()` (line 174-215) computes `globalSeverity` by iterating `perDrug`.
`AddictionManager.getGlobalSeverity(player)` (line 262-278) is a separate public method that
iterates `perDrug` again. Any caller outside `tickPlayer` triggers a second full pass.

**Evidence:** [confirmed]
- `AddictionManager.java` L174-215 and L262-278 — two independent loops over `perDrug`.
- `ItemEffectHandler.syncClientHud()` L248 calls `getGlobalSeverity(player)`, which is itself called
  from `AddictionManager.tickPlayer()` via the `SymptomManager.sync` path.

**Files:**
- `addiction/manager/AddictionManager.java`
- `addiction/manager/ItemEffectHandler.java`

**Risk:**
Mild per-tick cost today; significant if `perDrug` grows large for a player with many drug histories.

**Recommended fix:**
Cache the last-computed `globalSeverity` on `PlayerAddictionStats` as a non-persistent transient
field, updated by `tickPlayer()` and read by `getGlobalSeverity()`.

**Safe first step:**
Add `float lastGlobalSeverity` to `PlayerAddictionStats` (not serialized). Update it in
`tickPlayer()`. Have `getGlobalSeverity()` read it. Validate no logic depends on it being
freshly computed.

**Validation:**
`./gradlew compileJava`. Behavior should be unchanged.

---

### Issue: hasItem() does a full inventory scan on every headphone tick

**Problem:**
`ItemEffectHandler.hasItem(Inventory, Item)` (line 162-168) iterates every slot in the player's
inventory container. This is called on every `tickHeadphones` call (every server tick per player)
and every toggle/set-playing call.

**Evidence:** [confirmed]
- `addiction/manager/ItemEffectHandler.java` L130-140: `tickHeadphones` calls `snapshot().hasHeadphones()`.
- But `tickHeadphones` itself also calls `hasItem` via `PlayerRecoveryEnvironmentCache.snapshot(player).hasHeadphones()` — the cache wrapper.
- L63 in `toggleHeadphones`: `hasItem(player.getInventory(), ModItems.HEADPHONES.get())` is called directly.
- `setHeadphonesPlaying` (L106), `cycleHeadphonesTrack` (L93) also call `hasItem` directly.

**Files:**
- `addiction/manager/ItemEffectHandler.java`

**Risk:**
The inventory scan is O(inventory size). On large modpack servers with custom inventory expansions
this can be slow. `PlayerRecoveryEnvironmentCache` already caches the environment snapshot; using
the cache consistently would eliminate the direct calls.

**Recommended fix:**
Remove the direct `hasItem` calls from `toggleHeadphones`, `cycleHeadphonesTrack`, and
`setHeadphonesPlaying`. Instead, call `PlayerRecoveryEnvironmentCache.snapshot(player).hasHeadphones()`
so the result is cached at the cache TTL.

**Safe first step:**
Audit `PlayerRecoveryEnvironmentCache.snapshot()` to confirm `hasHeadphones()` is already computed
there, then replace the three direct `hasItem` calls. Do not change the underlying cache TTL.

**Validation:**
`./gradlew compileJava`. Manual test: headphone toggle with and without headphones in inventory.

---

## Priority 3 — Registry/resource/datagen complexity

### Issue: Machine screen tooltips use Component.literal for fluid/gas names

**Problem:**
`SteamCrackerScreen` (L127-130), `AbstractMachineScreen`, and every per-machine screen use
`Component.literal(label + " gas tank")` and similar constructs for player-visible tooltip labels.
The string `" gas tank"`, `" fluid tank"`, and quality tier labels like
`"Crude / Base / Perfect / Masterwork"` (PsyMixerScreen L423) are hardcoded English.

**Evidence:** [confirmed]
- `scan/component_literal_report.md` — `SteamCrackerScreen` L127: `Component.literal(label + " gas tank")`.
- `menu/client/PsyMixerScreen.java` L423: `return Component.literal("Crude / Base / Perfect / Masterwork");`

**Files:**
- `menu/client/SteamCrackerScreen.java` L127-130
- `menu/client/PsyMixerScreen.java` L423
- Many other machine screens (see `scan/component_literal_report.md`)

**Risk:**
Players using non-English locales (or future translation contributors) will see English machine
tooltip labels for tank and progress identifiers. The `PsyMixerScreen` quality tier string is the
most visible: it appears in the main ritual UI.

**Recommended fix:**
Add lang keys for `screen.mydrugs.ui.gas_tank`, `screen.mydrugs.ui.fluid_tank`, and the four
quality tier names. Replace the literal strings. Dynamic *numeric* values (`amount + " mB"`) may
stay as literal; those are quantities, not labels.

**Safe first step:**
Fix `PsyMixerScreen` L423 first — replace with a `Component.translatable` call using an appropriate
key and add it to `en_us.json`. This is a one-line change per tier.

**Validation:**
`./gradlew compileJava`. Check `en_us.json` for the new keys. Manual UI test.

---

### Issue: Legacy constructors in FluidSpec and GasSpec have no migration plan

**Problem:**
`FluidSpec` and `GasSpec` both carry a documented legacy 4-argument constructor
("preserved so existing registration call sites compile"). There is no migration checklist or issue
tracking when these can be removed.

**Evidence:** [confirmed]
- `fluids/FluidSpec.java` L21: `/** Legacy constructor ... */`
- `gas/GasSpec.java` L19: `/** Legacy constructor ... */`
- `scan/todos.md` L121-128: both files noted.

**Files:**
- `fluids/FluidSpec.java`
- `gas/GasSpec.java`

**Risk:**
Low immediate risk. Long-term, the legacy constructors silently default `FluidRole`, `FluidPhase`,
and `Hazard` values that the audit/JEI display now relies on. Specs created with the legacy
constructor will appear with neutral metadata in any future audit that reads those fields.

**Recommended fix:**
Enumerate legacy call sites with `rg -n "new FluidSpec(" src/main/java` and
`rg -n "new GasSpec(" src/main/java`, then migrate one package at a time to the full constructor.
Remove the legacy constructors once all call sites are updated.

**Safe first step:**
Add `@Deprecated` to the legacy constructors to make future legacy additions visible in IDE warnings.

**Validation:**
`./gradlew compileJava` — deprecation warnings confirm all remaining legacy call sites.

---

## Priority 4 — Package clarity

### Issue: psyche/* appears to be an incomplete system

**Problem:**
`PsycheMapNodeCatalog`, `PsycheMapMilestones`, `PsycheMapManager`, `PlayerPsycheMapAttachment`,
`PsycheMapNodeDto` are all present, but there is no connected UI, guide page, or game loop driving
the psyche map. The `hasEarnedMemory()` check in `RecoveryProgressManager` (line 354) reads the
psyche map attachment but the map itself has no clear entry/exit point.

**Evidence:** [inferred]
- `scan/tree.txt` — `psyche/` has 5 files but no screen, no network payload, and no guide reference.
- `core/drug/integration/RecoveryProgressManager.java` L354: reads `PLAYER_PSYCHE_MAP` attachment.

**Files:**
- `psyche/` package (5 files)

**Risk:**
No functional risk today. Risk grows if new features start reading `psyche/` assuming it's complete.
The attachment is persisted — any future interpretation of old save data needs to know its current
state.

**Recommended fix:**
Either: (a) add a `// INCOMPLETE — pending Phase X` comment to `PsycheMapManager` and open a
backlog item, or (b) stub-complete the system with no-op methods to surface what's missing.

**Safe first step:**
Add a `package-info.java` to `psyche/` documenting current status: what is persisted, what is
not yet rendered, and what depends on it.

**Validation:**
No code change needed for the first step.

---

### Issue: Several canonical docs are TODO stubs

**Problem:**
`docs/ADDICTION_RECOVERY_DIARY.md`, `docs/GAMEPLAY_CONTRACTS.md` (addiction section),
`docs/audits/MAINTAINABILITY_AUDIT.md` (prior version), `docs/audits/RISK_HOTSPOTS.md`, and
`docs/audits/TECH_DEBT_REGISTER.md` were listed in `00_README_FOR_AGENTS.md` as the first files to read,
but were empty or stub-filled. Agents reading them get no guidance and must re-derive context from
source.

**Evidence:** [confirmed]
- `scan/todos.md` — extensive TODO listings in all the above files.
- `docs/GAMEPLAY_CONTRACTS.md` L5, L13: `TODO: Fill from source`.

**Files:**
- `docs/ADDICTION_RECOVERY_DIARY.md`
- `docs/GAMEPLAY_CONTRACTS.md`
- `docs/GAME_MOOD_BIBLE.md` (examples section empty)

**Risk:**
Agents working on addiction or recovery will re-derive design intent from code, potentially
introducing changes that conflict with undocumented design intent. This is already happening since
`AGENTS.md` carries the same rules as `ADDICTION_RECOVERY_DIARY.md` was supposed to.

**Recommended fix:**
Fill `docs/ADDICTION_RECOVERY_DIARY.md` from `AGENTS.md` + direct source review.
Fill `docs/GAMEPLAY_CONTRACTS.md` addiction/tolerance section from `AddictionManager` and
`WithdrawalManager` source.

**Safe first step:**
Fill `docs/ADDICTION_RECOVERY_DIARY.md` first — it's the most referenced empty stub. Use
`AddictionManager.consume()` and `WithdrawalManager` as primary sources.

**Validation:**
No code change; docs only.

---

## Priority 5 — Performance risks

### Issue: RecoveryRoomManager.getBestRoom scans up to 15 625 blocks per cache miss

**Problem:**
`RecoveryRoomManager.getBestRoom(Level, BlockPos)` iterates every block in a
`DEFAULT_SCAN_RADIUS = 12` cube (25×25×25 = 15 625 positions) looking for `RECOVERY_ANCHOR` blocks.
The result is cached per-anchor for 60 ticks (3 seconds). Cache *misses* trigger a full world scan.

**Evidence:** [confirmed]
- `recovery/RecoveryRoomManager.java` L92-112: `BlockPos.betweenClosed()` with radius 12.
- `CACHE_TICKS = 60` — cache miss triggers full scan.
- `PlayerRecoveryEnvironmentCache.snapshot()` is called every tick; the cache is per-player, not
  per-anchor, so the 60-tick interval applies to the cached per-player snapshot.

**Files:**
- `recovery/RecoveryRoomManager.java`
- `recovery/PlayerRecoveryEnvironmentCache.java`

**Risk:**
A player walking near many recovery anchors (e.g., in a base with several rooms) can trigger
repeated 15K-block scans. Combined with the per-player per-tick call in `AddictionManager.tickPlayer`,
this could be the most expensive routine in the mod on heavily built-up servers.

**Recommended fix:**
Track placed `RECOVERY_ANCHOR` positions in a `SavedData` or `LevelChunkData`. The scan would then
iterate only anchors, not every block in the radius.

**Safe first step:**
Add `PipeNetworkDiagnostics`-style logging to the scan to measure how often cache misses happen
per minute per player. Only optimize after confirming this is hot in practice.

**Validation:**
Enable diagnostics; measure in a test world with 3+ recovery anchors and a single player.

---

### Issue: Client tick handler runs 20+ unguarded calls per frame

**Problem:**
`ClientEventHandler.onClientTick()` (lines 84-110) chains 20+ static `tick()` calls every client
tick with no profiler markers, no dirty-flag guards, and no conditional skipping.

**Evidence:** [confirmed]
- `client/effects/ClientEventHandler.java` L84-110: 20+ sequential `.tick()` calls.
- `scan/performance_hotspots.md` — lists all of them as tick candidates.

**Files:**
- `client/effects/ClientEventHandler.java`

**Risk:**
If any one tick method becomes allocating (e.g., the Inner soundscape scan or hallucination
controller), it adds to every frame budget invisibly. On lower-end hardware this compounds with
shader ticks.

**Recommended fix:**
Wrap the chain in `Profiler.get().push("mydrugs:client_tick")` / `pop()` with per-subsystem
push/pop markers, mirroring the server pattern in `PlayerTickEvents`.

**Safe first step:**
Add the outer profiler marker first (one line). Subsystem markers can be added iteratively.

**Validation:**
Open the vanilla profiler (`F3+L`) in-game and verify `mydrugs:client_tick` appears in the chart.

---

## Priority 6 — Mood/design consistency

### Issue: PsyMixerScreen shows quality tiers as untranslated English literals

**Problem:**
`PsyMixerScreen` L423 returns
`Component.literal("Crude / Base / Perfect / Masterwork")` as the quality tier hint. This is
visible in the main ritual crafting UI and is the only place the quality naming is explained to the
player.

**Evidence:** [confirmed]
- `scan/component_literal_report.md` — `menu/client/PsyMixerScreen.java` L423.

**Files:**
- `menu/client/PsyMixerScreen.java` L423

**Risk:**
Low runtime risk; high localization/design risk. The quality names are part of the mod's ritual
identity (see `GAME_MOOD_BIBLE.md` — "ritual certainty"). Having them hard-coded in English prevents
translation and makes them invisible to localization tests.

**Recommended fix:**
Add `screen.mydrugs.psy_mixer.quality_hint` to `en_us.json`. Replace the literal with
`Component.translatable("screen.mydrugs.psy_mixer.quality_hint")`.

**Safe first step:**
Add the lang key first. Replace the literal. One-line change.

**Validation:**
`./gradlew compileJava`. Confirm key in `en_us.json`.

---

## Recommended refactor sequence

1. Fix `PsyMixerScreen` quality tier literal (5 min, zero risk).
2. Fix `SteamCrackerScreen` tank tooltip literals (15 min, zero risk).
3. Add profiler markers to `ClientEventHandler.onClientTick()` (10 min, zero risk).
4. Add `@Deprecated` to legacy `FluidSpec` / `GasSpec` constructors (5 min, zero risk).
5. Narrow `PlayerAddictionStats.temporaryEffects` visibility and fix callers in `addiction/*` only.
6. Replace direct `hasItem` calls with `PlayerRecoveryEnvironmentCache` in `ItemEffectHandler`.
7. Cache `globalSeverity` in `PlayerAddictionStats` to eliminate the second loop.
8. Move `ItemEffectHandler` to `recovery/` (after test coverage is in place).
9. Move `core/client/` to `client/` (after confirming no server-side imports).

Steps 1–4 are cosmetic/documentation with no behavioral risk.
Steps 5–9 require compile and manual verification.

---

## Do-not-touch-without-tests areas

- `PlayerAddictionStats.serialize()` / `deserialize()` — any field rename or reorder breaks saves.
- `PipeNetworkManager.tick()` / `PipeNetworkScanner.scan()` — pipe network topology.
- `DrugUseService` — canonical consumption path.
- `MixedDrugData.CODEC` / `STREAM_CODEC` — item data component codec, save-breaking if changed.
- `IntegrationCoreTier` — legacy `integration_core` item must still map to `CRUDE`.
- `RecoveryRoomManager.scanRoom()` — any change to scan logic alters all existing recovery room
  tier evaluations.

---

## Open questions

1. **`psyche/*`** — is the psyche map system deferred, abandoned, or in active development?
   The attachment is persisted; the answer affects whether old saves need migration.

2. **`SteamCrackerScreen` `label + " gas tank"` / `" fluid tank"`** — are these debug tooltips
   or player-facing? If debug, mark them `@Debug` or wrap in a config gate.

3. **`DrugCategory` overload of `AddictionManager.consume()`** — is this used intentionally
   anywhere, or is it a legacy entry point that should be removed?

4. **`PlayerRecoveryEnvironmentCache`** — what is its actual TTL and how is it invalidated?
   Confirm the headphone check is cached and not re-computed every tick.
