# Addiction, Recovery, and Diary

This document describes the current server implementation. The primary entry points are
`DrugUseService`, `AddictionManager`, `PlayerTickEvents`, `RecoveryProgressManager`, and
`DiarySnapshotBuilder`.

## Authoritative State

`PlayerAddictionStats` is the saved per-player root. It owns per-drug `DrugAddictionStats`, global
stress and resilience, `TemporaryRecoveryEffects`, `RecoverySessionState`, bad-trip state, overdose
timers, and hint cooldowns. `DrugAddictionStats` stores addiction, withdrawal, tolerance, last use,
relapse memory, dose contributions, historical exposure, and integration/recovery counters for one
serialized `DrugId`.

`TemporaryRecoveryEffects` persists timed diary calm, calming mixture, headphones, thought
suppression, sleep support, prepared tea, and recovery momentum. `BadTripManager` uses
`BadTripState` as acute runtime state; `PlayerAddictionStats.deserialize` and `copyFrom` reset it
rather than restoring an active trip.

These values are server-owned. Client presentation comes from payloads built or sent by
`SymptomManager`, `DoseManager`, and `DiarySnapshotBuilder`.

## Canonical Consumption Path

Normal consumption must enter through `DrugUseService`.

1. `DrugUseService.consumeStack` resolves stack-backed drug models and consumption strategies.
2. `DrugUseService.consume` runs `DrugProgressionGate` before applying anything. A blocked use
   records a diary blocker and returns.
3. For an allowed use, `DrugUseService` calculates the strategy- and purity-adjusted effective
   dose, applies runtime drug effects and route cues through `DrugEffectRuntimeManager`, then calls
   `AddictionManager.consume(ResolvedDrugUse)`.
4. `AddictionManager` updates the concrete drug's `DrugAddictionStats`: addiction, last-use time,
   lifetime dose, relapse memory, withdrawal relief, historical peak, tolerance via
   `ToleranceManager`, stress via `StressManager`, dose contributions via `DoseManager`, and
   integration eligibility via `IntegrationService`.
5. `DrugUseService` grants knowledge and advancement credit only after the use has passed the
   progression gate.

The category-only `AddictionManager.consume` overloads are deprecated because they replace the
concrete drug with a representative `DrugId`. They are not a valid normal-consumption path.

`Config.SERVER.addictionEnabled` gates the whole `AddictionManager.consume` update, including its
call to `DoseManager.onConsume`. `DoseManager` separately checks `overdoseEnabled` when accepting
dose contributions.

## Tick Flow

`PlayerTickEvents.onPlayerTick` runs only for `ServerPlayer` during `PlayerTickEvent.Post`. Its
order is:

1. `AddictionManager.tickPlayer`
2. `DrugEffectRuntimeManager.tickServer`
3. `LightningBottleManager.tick`
4. `HeadphonesItem.tickPendingClick`
5. `RecoverySessionManager.tick`
6. `RecoveryRoomManager.tickPlayerParticles`
7. `MutationManager.tickPlayer`
8. `IntegratedTraitManager.tickPlayer`
9. Inner-dimension checks through `InnerDimensionService`

Inside `AddictionManager.tickPlayer`, order is also significant:

1. `ItemEffectHandler.tickHeadphones`
2. One `PlayerRecoveryEnvironmentCache.snapshot` for room, safe-zone, companions, food, diary, and
   headphones
3. `RecoveryProgressManager.tickPassiveSupport` every 20 ticks
4. Per tracked drug: `WithdrawalManager.tickDrug`, passive addiction decay,
   `RelapseManager.decay`, `DoseManager.tickDrug`, severity accumulation, then empty-state pruning
5. Global severity calculation
6. `StressManager.tick`, `BadTripManager.tick`, `StressDamageManager.tick`,
   `SymptomManager.applyServerSymptoms`, `DoseManager.tickOverdoseTimer`, then
   `WithdrawalHintManager.tick`
7. `SymptomManager.sync` and dose sync every 20 ticks

When `PlayerAddictionStats.addictionSymptomsImmune` is true, `AddictionManager` stops bad trips,
clears server symptoms, performs the periodic sync, and returns before the normal stress, overdose,
damage, and hint steps.

## Addiction, Withdrawal, Dose, and Bad Trips

`ToleranceManager.onUse` raises tolerance from dose and category configuration. Its decay is called
by `WithdrawalManager` only after more than 200 ticks of abstinence and is accelerated by valid
recovery-room multipliers.

`WithdrawalManager` moves each drug's withdrawal meter toward a target derived from addiction,
abstinence phase, night, stress, companions, safe-zone state, combat, resilience, and server
configuration. Sleeping, companions, calm relief, safe zones, and `RecoveryRoomManager` multipliers
increase recovery from that meter.

`DoseManager` stores timed `DoseContribution` values, resolves alcohol or drug dose states, sends
state-transition messages, starts and advances the overdose death timer, and calls
`DoseEffectManager`. `DoseEffectManager` refreshes category-specific runtime effects for non-normal
dose states.

`StressManager` derives a moving stress target from withdrawal severity, dose state, combat, time,
companions, food, health, recovery context, and temporary supports. `StressDamageManager` applies
stress-overload damage once per second only in survival. `SymptomManager` applies server-side
fragility/fatigue and builds the client symptom snapshot.

`BadTripManager` considers psychedelic, cannabinoid, dissociative, and stimulant drugs only when
their `DoseManager` state is `VERY_HIGH` or `OVERDOSE`. Stress and dose pressure determine start,
severity, and stop hysteresis. Starting a trip records the psychedelic setback through
`IntegrationService`; recovery rooms reduce pressure and symptom intensity.

## Recovery and Diary

`PlayerRecoveryEnvironmentCache` is the shared server cache for recovery-room scans, companions,
and relevant inventory checks. Hot tick paths should reuse its `PlayerEnvironmentSnapshot`.
`RecoveryRoomManager` supplies withdrawal, tolerance, addiction, stress, and bad-trip modifiers;
its particle tick is presentation only.

`RecoveryProgressManager.onProductiveAction` is the funnel for active recovery. It reduces
addiction and advances `recoveryProgress` only for drugs in their reckoning window. Passive music,
room, companion, and Resonator support have explicit caps and cannot complete recovery.
`RecoverySessionManager` separately runs the room sequence from arrival through grounding,
reflection, and return-to-work momentum.

`ItemEffectHandler` applies diary, headphones, tea, calming mixture, and sleeping-aid effects. It
updates `TemporaryRecoveryEffects`, stress/withdrawal, `RecoveryProgressManager`, and
`RecoverySessionManager`, then synchronizes presentation.

`PlayerDiaryAttachment` stores bounded entries and recent blockers, sanitizes custom text, and
enforces the write cooldown. `DiaryEntryGenerator` creates deterministic reflective entries from
server state. `DiarySnapshotBuilder` constructs the client DTO from addiction, dose, bad-trip,
recovery-room, integration, and diary state; the client does not calculate those states.

## Source Findings

- The implementation class is `addiction/withdrawal/WithdrawalManager`, not
  `addiction/manager/WithdrawalManager`.
- `PlayerAddictionStats` and `DrugAddictionStats` still expose many mutable fields. Manager-only
  mutation is therefore a required calling convention, not a Java-enforced boundary.
- Recovery item behavior currently lives in `addiction/manager/ItemEffectHandler` even though
  recovery is otherwise represented by `recovery/*` managers. This document records that source
  reality without endorsing a package move.
