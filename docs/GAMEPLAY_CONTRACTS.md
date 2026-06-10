# Gameplay Contracts

## Addiction, Dose, Recovery, and Diary

These contracts are enforced by the current flow through `DrugUseService`, `AddictionManager`,
`PlayerTickEvents`, `RecoveryProgressManager`, and `DiarySnapshotBuilder`.

### Consumption Contract

- Route every normal drug use through `DrugUseService`. Item, block, ritual, and command callers
  must not independently apply drug effects, addiction, tolerance, dose, overdose, item
  consumption, or knowledge.
- Preserve the order in `DrugUseService`: progression gate, resolved effective dose, runtime
  effects/cues, `AddictionManager.consume`, then knowledge and advancement handling.
- Pass a concrete `DrugModel`/`DrugId`. The category-only `AddictionManager.consume` overloads are
  deprecated representative-drug fallbacks and must not gain new normal-gameplay callers.
- Respect the existing configuration boundary: `AddictionManager.consume` returns immediately
  when addiction is disabled, so its tolerance, stress, dose, and integration-update calls also do
  not run.

### State and Persistence Contract

- `PlayerAddictionStats`, `DrugAddictionStats`, and `TemporaryRecoveryEffects` are authoritative
  server save state. Do not move their decisions to client screens or payload handlers.
- Keep per-drug persistence keyed by `DrugId.serializedName`, as implemented by
  `PlayerAddictionStats`; do not replace it with enum ordinals.
- Do not rename saved keys or alter the serialized shapes in `PlayerAddictionStats`,
  `DrugAddictionStats`, `TemporaryRecoveryEffects`, or `PlayerDiaryAttachment` without an explicit
  migration.
- Use `AddictionManager`, `WithdrawalManager`, `ToleranceManager`, `StressManager`,
  `DoseManager`, `RecoveryProgressManager`, and `IntegrationService` for state transitions.
  Public fields in the data classes do not make partial direct updates safe.

### Tick-Order Contract

- Keep `AddictionManager.tickPlayer` first in the server `PlayerTickEvents` chain. The current
  order then runs `DrugEffectRuntimeManager`, pending headphone input, `RecoverySessionManager`,
  `RecoveryRoomManager` particles, mutation, integrated traits, and inner-dimension handling.
- Within `AddictionManager`, preserve the sequence: headphone upkeep and one
  `PlayerRecoveryEnvironmentCache` snapshot; passive recovery; per-drug withdrawal/tolerance,
  addiction decay, relapse decay, and dose; global severity; stress, bad trip, stress damage,
  symptoms, overdose timer, hints; periodic sync.
- Reuse `PlayerRecoveryEnvironmentCache` in per-tick work. Do not add independent room, companion,
  or full-inventory scans to the hot path.
- Preserve the `addictionSymptomsImmune` early-return behavior in `AddictionManager` unless the
  gameplay contract is intentionally redesigned.

### Withdrawal, Dose, and Bad-Trip Contract

- `WithdrawalManager` owns movement of each withdrawal meter toward its contextual target and is
  the tick caller for `ToleranceManager.decay`.
- `DoseManager` owns timed dose contributions, dose-state transitions, overdose countdown, and
  dispatch to `DoseEffectManager`. Do not infer current dose from addiction or withdrawal.
- `StressManager` owns stress targeting; `StressDamageManager` owns survival-only overload damage;
  `SymptomManager` owns server symptoms and client symptom snapshots.
- `BadTripManager` owns bad-trip start/stop, severity, runtime symptoms, client sync, and
  `IntegrationService` setback recording. Do not start bad trips directly from client visuals or
  from an item class.
- `WithdrawalHintManager` presents server-selected guidance from authoritative state and cached
  environment data. Hints must not mutate the underlying addiction or recovery state.

### Recovery and Diary Contract

- Send productive recovery through `RecoveryProgressManager.onProductiveAction`. Its reckoning
  checks, detox floor, progress caps, room multiplier, and `IntegrationService.markEligible` call
  must not be bypassed.
- Passive support in `RecoveryProgressManager` must remain capped below completion. A recovery
  room, music, companions, or Resonator support alone cannot finish recovery progress.
- Keep `RecoverySessionManager` distinct from recovery progress: it owns the room
  arrival-ground-reflect-return sequence and grants temporary recovery momentum after return-to-work
  actions.
- Apply diary and recovery-item effects through `ItemEffectHandler`, which coordinates
  `TemporaryRecoveryEffects`, stress/withdrawal relief, recovery progress, session actions, and
  synchronization.
- `PlayerDiaryAttachment` is saved server state; `DiaryEntryGenerator` creates reflection;
  `DiarySnapshotBuilder` creates the client view. The client may render or submit validated text,
  but it must not calculate addiction, withdrawal, bad-trip, recovery, or blocker state.

### Confirmed Source Discrepancies

- The live `WithdrawalManager` package is `addiction/withdrawal`, not `addiction/manager`.
- The data classes expose mutable fields, so the manager-only rule is architectural rather than
  compiler-enforced.
- `ItemEffectHandler` currently sits under `addiction/manager` while coordinating recovery-domain
  behavior. Moving it is a separate refactor, not part of this contract.
