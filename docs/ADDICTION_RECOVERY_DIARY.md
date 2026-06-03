# Addiction, Recovery, and Diary

## Purpose

Explain addiction, tolerance, withdrawal, recovery, integration, and diary as related but separate server-authoritative domains, and name the real entry points so changes land in the right seam.

Each claim below is tagged `[confirmed]` (read from source), `[inferred]` (reasonable from naming/structure), or `[unknown]`. Source wins over this doc; report disagreements. For the full symbol/package dump see `../scan/symbols.md` and `../scan/packages.md`.

## Main packages

`[confirmed]` unless noted.

- `addiction/data` — persistent state records: `PlayerAddictionStats` (per-player), `DrugAddictionStats` (per-`DrugId`), `TemporaryRecoveryEffects`, `WithdrawalPhase`.
- `addiction/attachment` — `ModAttachments` (all `AttachmentType` registrations) and `PlayerIntegrationAttachment`.
- `addiction/manager` — the per-tick driver `AddictionManager`, `ItemEffectHandler` (recovery-item effects + headphones), `WithdrawalHintManager`, and `manager/state/*` (`SymptomManager`, `StressManager`, `StressDamageManager`, `BadTripManager`/`BadTripState`, `ResilienceManager`).
- `addiction/withdrawal` — `WithdrawalManager` (per-drug withdrawal-meter tick).
- `addiction/tolerance` — `ToleranceManager` (gain on use, decay on abstinence).
- `addiction/progression` — `RelapseManager` (relapse memory multiplier/decay).
- `addiction/config` — tunables: `AddictionConstants`, `DoseConstants`, `SymptomFlags`, `SymptomThresholds`.
- `addiction/explain` — player-facing reasoning/feedback: `AddictionStateExplainer`, `AddictionRecoveryFeedback`, `AddictionDangerReason`, `AddictionSuggestedAction`.
- `addiction/events` — game-bus subscribers, incl. `PlayerTickEvents` (the tick entry point) and clone/sleep/food/productive-action hooks.
- `addiction/network` — payloads (`DoseSyncPayload`, `HeadphonesStatePayload`, `AddictionClientSnapshotPayload`, `PersonalDiarySnapshotPayload`, bad-trip/cue/overlay payloads).
- `recovery/*` — environment + sanctuary: `PlayerRecoveryEnvironmentCache` + `PlayerEnvironmentSnapshot`, `RecoveryRoomManager`/`RecoveryRoomReport`/`RecoveryRoomTier`/`SanctuaryModule`, `SafeZoneManager`, `SocialReliefManager`, `RecoverySessionManager`, `SleepRecoveryManager`, plus recovery `block/*` and `item/*`.
- `diary/*` — `PlayerDiaryAttachment` (saved entries/blockers), `DiarySnapshotBuilder`, `DiaryEntryGenerator`, `DiaryBlocker`/`DiaryBlockerTypes`, `DiaryClarityService`, `IntegrationDiary`, `PersonalDiaryItem`, and `Diary*Dto` view models.
- `core/drug/integration/*` — `IntegrationService` (eligibility + unlock), `RecoveryProgressManager` (active-recovery accrual), `IntegratedTrait`/`IntegratedTraitManager`, `IntegrationRequirementType`/`IntegrationRequirementProfile`/`IntegrationRequirements`, `IntegrationCoreTier(s)`, `IntegrationConstants`, `CuratedDrugChain`.

## Main classes

`[confirmed]`, one line each:

- `PlayerAddictionStats` — per-player attachment: `EnumMap<DrugId, DrugAddictionStats> perDrug` plus global `geneticFactor`, `resilience`, `stressLevel`, `temporaryEffects`, `recoverySession`, `badTrip`, hint/overdose bookkeeping; `ValueIOSerializable`.
- `DrugAddictionStats` — per-drug: `addictionValue`, `baseWithdrawalMeter`, `tolerance`, `lastUseTime`, `relapseMemory`, `peakHistoricalAddiction`, `lifetimeDoseConsumed`, `integrationStage`, `recoveryProgress`, clean-streak counters, `doseContributions`/`lastDoseState`.
- `ModAttachments` — registers `PLAYER_ADDICTION` (not `copyOnDeath`), `PLAYER_DIARY` and `PLAYER_INTEGRATION` (`copyOnDeath`), and `PLAYER_DRUG_EFFECTS` (intentionally dropped on death).
- `AddictionManager` — `consume(ResolvedDrugUse)` applies a dose; `tickPlayer(ServerPlayer)` runs the whole per-tick addiction/recovery/symptom pass; exposes `getGlobalSeverity`, `getDominantDrugId/Category`.
- `WithdrawalManager.tickDrug(...)` — moves `baseWithdrawalMeter` toward a context-weighted target and applies recovery; calls `ToleranceManager.decay`.
- `ToleranceManager` — `onUse` raises tolerance, `decay` lowers it after abstinence (>200 ticks), both scaled by recovery room + config.
- `ItemEffectHandler` — applies herbal tea / calming mixture / sleeping aid / diary / headphones effects and headphone tick/sync.
- `PlayerRecoveryEnvironmentCache` — per-player, server-thread `WeakHashMap` cache of `PlayerEnvironmentSnapshot` (room, safe-zone, companions, recovery inventory) to avoid re-scanning every tick.
- `RecoveryProgressManager` — single funnel `onProductiveAction(player, ActionKind, weight)`; accrues per-drug `recoveryProgress` and active detox for drugs "in reckoning"; `tickPassiveSupport` for capped passive sources.
- `IntegrationService` — `evaluate`/`canIntegrate`, `markEligible` (NONE→ELIGIBLE), `tryIntegrate` (safe, re-validates), `forceIntegrateUnsafe` (admin/debug), plus clean-streak/reflection/bad-trip hooks.
- `IntegratedTrait` — enum mapping a curated `DrugId` to permanent `EffectEcho`s; `IntegratedTraitManager` applies/sync traits.
- `PlayerDiaryAttachment` — saved entries (cap 2048) + recent blockers (cap 16), write cooldown 1200 ticks, server-side custom-content sanitizer.
- `DiarySnapshotBuilder` — server-side builder that assembles `PersonalDiarySnapshotPayload` from addiction/diary/mastery/psyche/integration state for the client screen.

## Data flow

Consumption (request → state), `[confirmed]`:

1. An item delegates: `DrugItem`/`BangItem` call `DrugUseService.consume(...)`/`consumeStack(...)` with a `DrugUseSource` (`ITEM`, `BANG`, `BOTTLE`, `SYRINGE`, `PSYCHOTROPE`, `COMMAND`, …). Item classes do **not** mutate stats themselves.
2. `DrugUseService.consume` runs the progression gate, computes `effectiveDose` (strategy + meth-purity modifiers), applies runtime effects/cues/visuals, then calls `AddictionManager.consume(ResolvedDrugUse)`. It also records knowledge-gate diary blockers and grants knowledge.
3. `AddictionManager.consumeEffective` (gated by `Config.SERVER.addictionEnabled`) updates the drug's `DrugAddictionStats`: addiction gain (`AddictionMath` × genetic/relapse/mutation modifiers), `lastUseTime`, `lifetimeDoseConsumed`; `ToleranceManager.onUse`; relief lowers `baseWithdrawalMeter`; `peakHistoricalAddiction`; clean-streak integration hooks; `StressManager` relief; `DoseManager.onConsume`; then `IntegrationService.afterDrugStatsUpdated` (which may mark ELIGIBLE).

Per-tick (server), `[confirmed]`:

1. `PlayerTickEvents.onPlayerTick(PlayerTickEvent.Post)` (game bus, `@EventBusSubscriber`, `ServerPlayer` only) calls `AddictionManager.tickPlayer`.
2. `tickPlayer` takes **one** `PlayerRecoveryEnvironmentCache.snapshot(player)` (room/safe-zone/companions/inventory) and threads it through. Every 20 ticks it runs `RecoveryProgressManager.tickPassiveSupport`.
3. For each tracked `DrugId`: `WithdrawalManager.tickDrug` (→ `ToleranceManager.decay`), addiction decay, `RelapseManager.decay`, `DoseManager.tickDrug`, and severity accumulation; empty drug stats are pruned.
4. Global severity feeds `StressManager`, `BadTripManager`, `StressDamageManager`, `SymptomManager.applyServerSymptoms`, `DoseManager.tickOverdoseTimer`, and `WithdrawalHintManager`. Every 20 ticks `SymptomManager.sync` + `sendDoseSync` push client snapshots.

Recovery/sanctuary, `[confirmed]`: gameplay events (mining, crops, machine output, sleep, food, therapy, diary, recovery items) call `RecoveryProgressManager.onProductiveAction`, which burns addiction and fills `recoveryProgress` toward the integration gate; recovery rooms (`RecoveryRoomManager` tiers/modules) multiply accrual and item effects but cannot complete recovery alone.

Diary, `[confirmed]`: server code calls `diary.recordBlocker(type, gameTime)` at gates and appends entries; `DiarySnapshotBuilder.build` reads addiction + diary + integration state to send `PersonalDiarySnapshotPayload`. The client screen sends `SubmitPersonalDiaryEntryPayload` as a request, sanitized and cooldown-checked server-side.

## Invariants

Must not break (`[confirmed]` unless noted):

- **One consumption path.** Normal consumption goes item → `DrugUseService` → `AddictionManager.consume`. Do not create a parallel path; do not apply effects/addiction/tolerance/dose from item classes.
- **Server authority.** All of `PlayerAddictionStats`/`DrugAddictionStats`, recovery, integration, and diary state live on the server (`ServerPlayer` attachments). The client only receives snapshots (`DoseSyncPayload`, `SymptomManager.sync`/`AddictionClientSnapshotPayload`, `HeadphonesStatePayload`, `PersonalDiarySnapshotPayload`, integration sync). Server-bound packets (diary submit, headphone toggles) are validated requests, never trusted commands.
- **Who mutates stats.** `PlayerAddictionStats` is mutated only by the addiction/recovery/integration managers (`AddictionManager`, `WithdrawalManager`, `ToleranceManager`, `RelapseManager`, `RecoveryProgressManager`, `IntegrationService`, `ItemEffectHandler`, `StressManager`/`BadTripManager`). Outside code should call those, not poke fields.
- **recoveryProgress is earned.** It is advanced only by `RecoveryProgressManager` from real productive actions; idle time and passive sources are capped and never complete it (`[confirmed]` via the class contract).
- **Integration is re-validated at the boundary.** `tryIntegrate` re-checks eligibility before unlocking; `forceIntegrateUnsafe` is admin/debug only. Eligibility = peak exposure + low current addiction + recovery progress + lifetime dose + (for psychedelics) clean-dose streak / reflections / safe uses / no recent bad trip.
- **Config gates.** Respect `Config.SERVER.addictionEnabled`, `withdrawalEnabled`, and the gain/decay/severity multipliers.
- **Persistence stability.** Drugs are keyed by `DrugId.serializedName()` (not ordinal); dose contributions, diary entries (2048), and recent blockers (16) are bounded. Keep matching bounds when adding fields.
- **Death policy `[inferred]`.** Diary and integration attachments are `copyOnDeath`; acute drug effects are intentionally dropped; `PlayerAddictionStats` is not `copyOnDeath` and is handled via `PlayerAddictionStats.copyFrom(...)` (death-aware) — see `addiction/events/CloneEvents` (not read here).

## Extension points

Real seams (`[confirmed]` unless noted):

- **Add a withdrawal symptom.** Extend `manager/state/SymptomManager` (server apply + the `sync` snapshot) and the thresholds in `addiction/config/SymptomThresholds`/`SymptomFlags`; put any visuals/audio under `client/*`. `[inferred]` — `SymptomManager` internals not fully read.
- **Add a recovery reward/action.** Add a value to `RecoveryProgressManager.ActionKind` (with `baseWeight`, `canCompleteRecovery`, `progressCap`, `nextStageWork`) and call `RecoveryProgressManager.onProductiveAction(player, kind, weight)` from the relevant event. Room/module multipliers flow automatically.
- **Add a diary blocker.** Add a constant to `DiaryBlockerTypes`, call `playerDiary.recordBlocker(type, gameTime)` at the gate, and surface it through `DiarySnapshotBuilder`. Existing types: `KNOWLEDGE_GATE`, `MUSHROOM_GATE`, `MACHINE_GENERIC`, `BODY_TOO_LOUD`.
- **Add an integration requirement.** Extend the `IntegrationRequirementProfile`/`IntegrationRequirements` for the drug and add the matching field+check in `IntegrationService.evaluate(...)`/`EligibilityResult`; choose the `IntegrationRequirementType` (`ADDICTION_RECOVERY` or `CLEAN_PSYCHEDELIC_STREAK`). For a new reward, add an `IntegratedTrait` mapping the source `DrugId` to its `EffectEcho`s. `[inferred]` — per-drug profile wiring in `IntegrationRequirements` not read in detail.

## Common mistakes

Concrete failure modes implied by the code:

- Mutating `PlayerAddictionStats`/`DrugAddictionStats` fields directly from item/event code — bypasses tolerance, integration eligibility hooks, and client sync. `[confirmed]`
- Adding a second consumption path instead of routing through `DrugUseService`/`AddictionManager.consume`. `[confirmed]`
- Calling `RecoveryRoomManager.getBestRoom(...)` (an expensive anchor-cube scan) per tick instead of reusing `PlayerRecoveryEnvironmentCache.snapshot(player)` — `tickPlayer` deliberately snapshots once and threads it. `[confirmed]`
- Per-tick full-inventory scans. `ItemEffectHandler.hasItem(...)` walks the whole inventory; `tickHeadphones` now reads the cached `snapshot.hasHeadphones()`. New hot-path checks should use the cache, not fresh scans. `[confirmed]`
- Double-sync payloads. Headphone toggles send `HeadphonesStatePayload` **and** `syncClientHud(...)`; overlapping headphone/HUD state is known overlap (tech-debt TD-007). Don't add a third path. `[confirmed]`
- Advancing `recoveryProgress` from idle/passive sources past their caps, or completing integration through passive support. `[confirmed]`
- Using `IntegrationService.forceIntegrateUnsafe` (or `integrate` without re-check) in normal gameplay instead of `tryIntegrate`. `[confirmed]`
- Forgetting `Config.SERVER` gates or feeding `NaN`/negative dose into `consume`. `[inferred]`

## Validation

Narrowest checks for this domain:

- JVM tests (pure logic): `src/test/java/org/mydrugs/mydrugs/core/drug/integration/` — `IntegrationEligibilityTest`, `ActiveRecoveryTest`, `CleanStreakSpacingTest`, `PsychedelicReckoningTest`, `IntegratedTraitTest`, `IntegrationCoreTierTest`; `recovery/SanctuaryModuleDetectorTest`; `blocks/entity/psy_mixer/RecoveryRitualLogicTest`; `mutation/*`. `[confirmed: tests exist]`
- `./gradlew compileJava`
- `./gradlew test --tests '*Integration*' --tests '*Recovery*'` (scope to the touched area)
- `./gradlew validateCodeContracts` — guards progression, localization, and server-safety source contracts.
- Dedicated-server safety grep: no `net.minecraft.client.*` / `neoforge.client.*` / `mydrugs.client.*` imports in this common/server code.

Note: this is JVM-only logic where read here; `tickPlayer`, room scans, and packet sync need a running client/server to verify end to end. No checks above were run while writing this doc.

## Related scan files

- `../scan/packages.md`
- `../scan/symbols.md`
- `../scan/risk_hotspots.md`

## Open questions

- `PlayerAddictionStats.copyFrom(...)` caller and exact death/clone semantics — `addiction/events/CloneEvents` not read.
- `SymptomManager` symptom set and `SymptomThresholds`/`SymptomFlags` exact mapping — not read in detail.
- `RecoveryRoomManager` scoring, tier thresholds, and `SanctuaryModuleDetector` rules — only observed via call sites.
- Per-drug numbers in `IntegrationRequirements`/`IntegrationRequirementProfile`, and the role of `IntegrationCoreTier(s)` / `IntegrationMaterials` in the Psy-Mixer/Resonator route — headers/usage only.
- `DoseManager`/`DoseConstants` overdose timing and `AddictionMath` formulas — referenced but not read.
- `IntegratedTraitManager.tickPlayer` trait-application detail and fragility-immunity handling — observed via tick entry only.
