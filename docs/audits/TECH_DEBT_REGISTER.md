> Snapshot generated from `scan/` and source review on 2026-06-03. Point-in-time report, not a contract. If this disagrees with source, source wins.

# Technical Debt Register

See `docs/audits/MAINTAINABILITY_AUDIT.md` for full evidence and reasoning on each item.

Priority scale: **P0** = correctness/server safety · **P1** = architecture boundary · **P2** = duplication
**P3** = datagen/resource complexity · **P4** = package clarity · **P5** = performance · **P6** = mood/design

| ID | Priority | Area | Problem | Safe first step | Validation | Status |
|---|:---:|---|---|---|---|---|
| TD-001 | P0 | `addiction/data` | `PlayerAddictionStats` is an all-public mutable struct — anyone can mutate state bypassing managers and sync | Mark `temporaryEffects` package-private; fix callers in `addiction/*` only | `./gradlew compileJava` — no new errors | open |
| TD-002 | P0 | `addiction/manager` | `AddictionManager.consume(player, DrugCategory, dose)` silently falls back to representative drug, losing per-drug tracking | `@Deprecated` both category overloads; enumerate call sites with `rg` | `./gradlew compileJava` — deprecation warnings confirm all sites | open |
| TD-003 | P0 | `blocks` | `CocainePowderPileBlock` has no rate-limit on right-click consumption (TODO comments at L159, L185) | Add `lastConsumeTick` server-side guard before animation work | Manual test: rapid right-click must not produce multiple consumptions | open |
| TD-004 | P1 | `addiction/manager` | `ItemEffectHandler` lives in `addiction/manager` but owns recovery domain logic; tight coupling across domains | Create `recovery/RecoveryItemEffectHandler.java` as copy; verify compile; then migrate call sites | `rg -n "ItemEffectHandler" addiction/` returns zero after move | open |
| TD-005 | P1 | `core/client` | `core/client/` contains client-only classes in a package shared with server code | Confirm no server-side imports with `rg "core.client" --glob '!client/**'`; then move to `client/core/` | Dedicated-server import scan still passes | open |
| TD-006 | P1 | `core/drug/integration` | `RecoveryProgressManager` is in `core/drug/integration` but primarily drives recovery-arc logic; recovery domain agents won't find it | Add `package-info.java` to `core/drug/integration/` describing why it lives here | Docs only; no compile needed | open |
| TD-007 | P2 | `addiction/manager` | Headphone state changes send two payloads (`HeadphonesStatePayload` + `AddictionClientSnapshotPayload`) on every toggle | Audit which client fields each payload carries; check for duplication | Manual: toggle headphones; confirm single client update | open |
| TD-008 | P2 | `addiction/manager` | `AddictionManager.getGlobalSeverity()` iterates `perDrug` a second time; already computed inside `tickPlayer()` | Cache `lastGlobalSeverity` on `PlayerAddictionStats` (non-serialized field) | `./gradlew compileJava`; behavior unchanged | open |
| TD-009 | P2 | `addiction/manager` | `hasItem()` in `ItemEffectHandler` scans the full inventory on every headphone tick and toggle | Replace direct `hasItem` calls with `PlayerRecoveryEnvironmentCache.snapshot().hasHeadphones()` | `./gradlew compileJava`; headphone toggle still works | open |
| TD-010 | P3 | `menu/client` | `SteamCrackerScreen` L127-130 uses `Component.literal(label + " gas tank")` — player-facing untranslated label | Add `screen.mydrugs.ui.gas_tank` key to `en_us.json`; replace literal | Key present in lang file; UI renders translated string | open |
| TD-011 | P3 | `menu/client` | `PsyMixerScreen` L423: `Component.literal("Crude / Base / Perfect / Masterwork")` is untranslated ritual quality label | Add `screen.mydrugs.psy_mixer.quality_hint` to `en_us.json`; replace literal | Key present; visible in Psy Mixer UI | open |
| TD-012 | P3 | `fluids`, `gas` | Legacy 4-argument constructors in `FluidSpec` / `GasSpec` default `FluidRole`, `FluidPhase`, `Hazard` silently | `@Deprecated` on legacy constructors | `./gradlew compileJava` — warnings show remaining call sites | open |
| TD-013 | P4 | `psyche` | `psyche/*` (5 files) has no connected UI or game loop; attachment is persisted but system status is unclear | Add `package-info.java` with current status and what depends on the attachment | Docs only | open |
| TD-014 | P4 | `docs` | `docs/ADDICTION_RECOVERY_DIARY.md` and `docs/GAMEPLAY_CONTRACTS.md` addiction section are stub-empty | Fill from `AddictionManager` + `WithdrawalManager` source; 1–2 pages max | Docs only | open |
| TD-015 | P5 | `recovery` | `RecoveryRoomManager.getBestRoom()` scans up to 25³ = 15 625 blocks on every cache miss | Add diagnostics to count cache misses per player per minute before optimizing | Diagnostics confirm hot/cold path before investing in anchor-tracking refactor | open |
| TD-016 | P5 | `client/effects` | `ClientEventHandler.onClientTick()` chains 20+ tick calls with no profiler markers | Wrap the chain in `Profiler.get().push("mydrugs:client_tick")` / `pop()` | `F3+L` profiler shows `mydrugs:client_tick` in flame chart | open |

## Status values

- `open` — identified, not started
- `in-progress` — actively being worked
- `blocked` — waiting on prerequisite
- `done` — confirmed resolved
- `obsolete` — no longer applicable

## Ordering note

Items TD-010, TD-011, TD-016, TD-012 are safe cosmetic/annotation changes — do them first.
Items TD-001, TD-003, TD-008, TD-009 are correctness improvements with no behavioral change.
Items TD-004, TD-005 require careful migration — do not attempt without compile validation at each step.
Item TD-015 requires diagnostics before deciding on implementation.
