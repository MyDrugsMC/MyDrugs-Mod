# Recovery Sanctuary Phase 4 System Map

This note maps the existing recovery and sanctuary systems before Phase 4 changes.

## Room scanning

- `RecoveryRoomManager` owns Recovery Anchor scans and caches each anchor report for 60 ticks.
- Scans already flood-fill a bounded room, analyze boundary enclosure, count room contents, score comfort, and derive `RecoveryRoomTier`.
- Existing tiers are `NONE`, `FRAGILE_ROOM`, `RESTING_ROOM`, `SAFE_ROOM`, and `SANCTUARY`.
- The existing scan pass already visits nearby contents once, so sanctuary module detection should piggyback on that pass instead of adding per-tick searches.
- `RecoveryRoomReport` is the right status surface for detected modules because it already feeds anchor inspection, passive recovery, bad-trip pressure, and client ambience.

## Recovery effects

- `SleepRecoveryManager.onWakeUp` applies sleep withdrawal relief, stress relief, resilience, and recovery progress.
- `ItemEffectHandler` applies diary calm, headphones state, herbal tea, calming mixture, and sleeping aid effects.
- `StressManager` applies room stress target reduction and temporary recovery effects.
- `RecoveryProgressManager` converts productive and passive support actions into addiction recovery and integration progress.

## Diary and feedback

- `DiarySnapshotBuilder` builds the server snapshot for the personal diary screen.
- `PersonalDiarySnapshotPayload` carries the snapshot to the client.
- `PersonalDiaryScreen` already renders current state, recovery flags, unresolved blockers, memories, and integration pages.
- Room module summaries should be exposed through the diary snapshot rather than recalculated client-side.

## Bad trips and resilience

- `BadTripManager` owns bad-trip start, tick, symptom refresh, safe-room shelter feedback, and stop handling.
- `PlayerAddictionStats.resilience` is the existing resilience store, with mutations centralized in `ResilienceManager`.
- Bad-trip recovery feedback belongs in the stop path, before `BadTripState` is reset.
- Anti-farm state should live on `PlayerAddictionStats` because bad-trip state is reset and not serialized.

## Music and ambience

- Vanilla jukeboxes and the Recovery Jukebox are already counted as room music.
- `RecoveryJukeboxBlockEntity.isPlaying()` can distinguish active recovery music from a placed jukebox.
- `RecoveryRoomParticlesPayload` and `RecoveryRoomParticleClient` already provide room ambience and anchor highlighting.
- Module-specific ambience should extend the existing payload with compact flags and respect client particle density and reduced-motion settings.
