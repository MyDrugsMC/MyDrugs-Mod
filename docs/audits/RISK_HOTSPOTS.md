> Snapshot generated from `scan/` and source review on 2026-06-03. Point-in-time report, not a contract. If this disagrees with source, source wins.

# Risk Hotspots

Agents should treat code in this file with extra caution. Each section lists confirmed or
inferred risks with specific file references.

Sources: `scan/client_server_violations.md`, `scan/component_literal_report.md`,
`scan/performance_hotspots.md`, `scan/risk_hotspots.md`, direct source review.

Legend: `[confirmed]` = verified from source. `[inferred]` = structural reasoning.

---

## Server safety

**Status: PASS** — `scan/client_server_violations.md` reports zero forbidden client imports outside
`client/**`. This invariant must be preserved; run the dedicated-server scan before any commit that
touches `core/`, `addiction/`, `recovery/`, `dimension/`, or `pipe/`.

**Scan command:**
```bash
rg -n "import net\.minecraft\.client|import net\.neoforged\.neoforge\.client|import org\.mydrugs\.mydrugs\.client" \
   src/main/java/org/mydrugs/mydrugs --glob '!client/**'
```

**Known structural risk:** `core/client/` (3 files) places client-only code in a `core/` sub-package.
These files import `net.minecraft.client.*`. Any copy/paste from this package to server code would
create a real dedicated-server crash. See TD-005.

---

## Networking

**Debug payloads (server-bound):**
- `AddictionDebugActionPayload` — requires `hasPermissions(2)` AND `Config.SERVER.allowDebugActionPayloads`
  gate. Both checks are present. [confirmed]
- Do not relax these checks. Creative mode is not an authorization level on a shared server.

**Payload count:** `ModNetwork.java` registers payloads in 11 named groups, covering ~38 payload
types total. All server-bound payloads have inline `handleOnServer` methods. Review each new
server-bound payload against `AGENTS.md` networking rules before adding it.

**Rate limiting:** `PayloadRateLimiter` and `PayloadValidation` exist and are importable. Check
that new high-frequency server-bound payloads (drag, shake, ritual input) use them.

**Ritual authority:** `PsyMixerRitualEngine` on the server owns ritual phase and timing.
`PsyMixerRitualInputPayload` and `PsyMixerRitualActionPayload` are requests, not commands.
Do not add client-side state that could skip server validation.

---

## Persistence / codecs

**High-risk persistence files — do not change field names or order without a migration path:**

| File | What it persists | Risk if changed |
|---|---|---|
| `addiction/data/PlayerAddictionStats` | All addiction state per player | Player save data; field rename = corruption |
| `addiction/data/DrugAddictionStats` | Per-drug addiction metrics | Nested in above; same risk |
| `addiction/data/TemporaryRecoveryEffects` | Headphones, diary, tea, momentum timers | Nested in above |
| `core/drug/ritual/MixedDrugData` | Ritual drug item data component | Item stack data; codec change = item loss |
| `core/drug/ritual/RitualDrugEffectData` | Effect list inside MixedDrugData | Same |
| `dimension/InnerDimensionSavedData` | Inner dimension layout per player | Dimension regeneration on read failure |
| `pipe/filter/FilterAttachment` | Pipe filter config per block | Filter config lost on incompatible change |
| `recovery/RecoverySessionState` | Recovery session progress | Nested in PlayerAddictionStats |

**`IntegrationCoreTier`:** The legacy `mydrugs:integration_core` item maps to `CRUDE` (documented in
source). Do not change the ordinal mapping. See `core/drug/integration/IntegrationCoreTier.java`.

**`DrugId`:** `byNetworkId` / `bySerializedName` are the persistence-safe accessors. Never use
`.ordinal()` for persistence or networking.

**`MixedDrugData.STREAM_CODEC`:** Hand-written encode/decode (not `StreamCodec.composite`). Any
field addition must update both `encode()` and `decode()` together. See TD-012 area.

---

## Save data / attachments

**Player attachments registered in `ModAttachments`:**
- `PLAYER_ADDICTION` — `PlayerAddictionStats`
- `PLAYER_DIARY` — `PlayerDiaryAttachment`
- `PLAYER_INTEGRATION` — `PlayerIntegrationAttachment`
- `PLAYER_MUTATIONS` — `PlayerMutationsAttachment`
- `PLAYER_DRUG_EFFECTS` — `PlayerDrugEffectsAttachment`
- `PLAYER_PSYCHE_MAP` — `PlayerPsycheMapAttachment` *(system incomplete — see TD-013)*
- `PSY_KNOWLEDGE` — `PsyKnowledgeAttachment`
- `PSY_MIXER_MASTERY` — `PsyMixerMasteryAttachment`
- `DRUG_KNOWLEDGE` — `DrugKnowledgeAttachment`

The `PLAYER_PSYCHE_MAP` attachment is persisted but the system it serves is not yet fully wired.
Do not rely on its data being meaningful until the system is complete.

**World saved data:** `InnerDimensionSavedData`, `DrugPatentSavedData`.

---

## Per-tick / per-frame performance

### Server tick (per player, every tick)

`PlayerTickEvents.onPlayerTick()` runs the following chain every server tick per online player:

1. `AddictionManager.tickPlayer()` — iterates `perDrug` (all drug stats) twice (TD-008), calls
   `PlayerRecoveryEnvironmentCache.snapshot()`, `RecoveryRoomManager`, stress/bad-trip/symptom managers.
2. `DrugEffectRuntimeManager.tickServer()` — iterates active drug effects.
3. `LightningBottleManager.tick()` — unclear cost; depends on lightning bottle presence.
4. `HeadphonesItem.tickPendingClick()` — lightweight.
5. `RecoverySessionManager.tick()` — session state machine.
6. `RecoveryRoomManager.tickPlayerParticles()` — may send a packet.
7. `MutationManager.tickPlayer()` — mutation state.
8. `IntegratedTraitManager.tickPlayer()` — integration trait tick.
9. Inner dimension check + `InnerDemonSpawnManager.tickInnerAmbient()`.

**Critical:** `AddictionManager.tickPlayer()` calls `PlayerRecoveryEnvironmentCache.snapshot()` once,
but `WithdrawalHintManager.tick()` may call it again. Verify the cache TTL is not reset per-call.

**`RecoveryRoomManager.getBestRoom()`** can scan 25³ = 15 625 blocks on cache miss. See TD-015.
The cache is 60 ticks (3 seconds) per anchor. Multiple anchors multiply the scan cost.

### Server tick (per level tick)

`InnerOverlayQueue.onLevelTick()` and `InnerScarHealer` run on every level tick. These affect the
Inner Dimension level. If the Inner Dimension is not loaded, these are no-ops.

`PipeNetworkManager.tick()` runs per `ServerLevel` tick. It rebuilds dirty seeds only when
`dirtySeeds` is non-empty — this is a correct dirty-flag pattern. Watch for cases where
`markDirty()` is called spuriously (on read-only operations).

### Client tick (per frame)

`ClientEventHandler.onClientTick()` runs 20+ tick calls every client frame with no profiler
markers and no conditional skip. See TD-016. Key expensive candidates:

- `InnerSoundscapeController.tick()` — builds three audio layers.
- `FakeEntityRenderController.tick()` — hallucination entity management.
- `ShaderManager.onClientTick()` — ticks all registered shaders.
- `PsychedelicOreAuraClient.onClientTick()` — aura scan, bounded but per-frame.

---

## Registries

**Registry IDs must be stable.** Changing any registry ID breaks existing worlds, pipes, fluid
networks, recipes, and item data components.

**Do not hand-edit generated JSON** in `src/generated/resources/`. Always update the datagen
provider and run `./gradlew runData`.

**`ModMachineContent` / `MachineContentDescriptor`** — new machine descriptor system. The `commonSetup`
validation checks that every descriptor resolves its block entity, menu, recipe type, and serializer.
If adding a new descriptor, all four must be registered before `commonSetup` runs.

---

## Datagen / resources

**Hand-authored vs. generated split:**
- `src/main/resources/` — hand-authored (blockstates for pipes, models, guide pages, recipes,
  lang, loot tables). Edit freely.
- `src/generated/resources/` — datagen output. Never hand-edit.

**`ModLangProvider` was removed.** All localization is now exclusively in `en_us.json`.
Datagen no longer generates lang entries. Any new item, block, or message needs a manual entry in
`src/main/resources/assets/mydrugs/lang/en_us.json`.

**Localization test:** The JUnit localization audit checks that every translatable key used in code
exists in `en_us.json`. Run `./gradlew test` after adding new `Component.translatable` calls.

**Guide pages:** `src/main/resources/assets/mydrugs/guide/pages.json` is the runtime source.
`docs/progression_guide_pages.md` is the authoritative edit source. Do not edit `pages.json`
directly; update the markdown and regenerate.

---

## Mood / content safety

**Never add** real-world synthesis, preparation, extraction, purification, dosing, or optimization
steps to: recipes, guide pages, diary entries, JEI recipe displays, tooltip text, or commands.

**Recovery must not be punitive.** Do not add mechanics that permanently punish recovery attempts or
make the recovery arc mandatory for progression outside the curated integration chain.

**Accessibility:** All visual distortion effects must respect `Config.CLIENT.reducedMotionMode`.
Before adding a new overlay or shader effect, confirm it reads this config and reduces motion
without disabling gameplay feedback.

**Humor boundaries:** `DiaryEntryGenerator` text and guide entries may have dry flavor, but
overdose risk, bad trips, and withdrawal should not be played for laughs.

---

## High-risk files

Files that are disproportionately risky to edit without full test coverage:

| File | Why risky |
|---|---|
| `addiction/data/PlayerAddictionStats.java` | Save data; codec changes corrupt saves; all-public mutation surface |
| `recovery/RecoveryRoomManager.java` | World scan logic; tier evaluation affects gameplay balance |
| `core/drug/use/DrugUseService.java` | Canonical drug consumption path; any change ripples to all drug items |
| `core/drug/integration/IntegrationService.java` | Integration eligibility and unlock; tied to save data and guide progression |
| `core/drug/ritual/MixedDrugData.java` | Item data component codec; change = item data loss |
| `pipe/network/PipeNetworkManager.java` | Pipe network topology; bugs cause silent transfer failures |
| `pipe/network/PipeNetworkScanner.java` | BFS scan; any change to connectivity logic affects all pipe layouts |
| `dimension/inner/InnerDimensionSystem.java` | Dimension generation; non-deterministic changes corrupt existing inner worlds |
| `blocks/entity/psy_mixer/PsyMixerRitualEngine.java` | Server-authoritative ritual; any phase/timing change affects live rituals |
| `network/ModNetwork.java` | All payload registrations; payload ID change = protocol break |
