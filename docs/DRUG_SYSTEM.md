# Drug System Contracts

This is the highest-risk gameplay domain. Preserve the canonical path.

## Canonical consumption path

Normal drug consumption must go through:

```text
core/drug/use/DrugUseService
```

Rules:

- Item classes may delegate to `DrugUseService`.
- Item classes must not directly apply effects, addiction, tolerance, overdose, dose, item consumption, or knowledge grants.
- Do not create parallel consumption systems.
- Dynamic stack-based drugs use data components, not runtime item registration.

## Main classes

| Class/package | Purpose |
|---|---|
| `DrugRegistry` | registers base `DrugModel` definitions and psychotrope values. |
| `DrugModel` | drug category, effects, addiction rate, tuning profile. |
| `DrugId`, `DrugCategory` | stable drug/category enums. Avoid ordinal persistence. |
| `DrugUseService` | resolves stack drugs, validates knowledge/progression, consumes, applies dose/addiction/runtime effects. |
| `DrugStackResolver` | maps item stacks and data components to resolved drug uses. |
| `ConsumptionStrategy` and route strategies | route-specific behavior: smoking, drinking, eating, sniffing, injecting. |
| `DoseManager`, `DoseEffectManager` | dose accumulation and dose-driven effect behavior. |
| `DrugEffectRuntimeManager` | active runtime effects and lifecycle persistence. |
| `RitualDrugRegistry`, `MixedDrugData` | ritual/mixed drug definitions and stack data. |
| `AddictionManager` | addiction state updates; should not be called as a parallel normal-consumption gateway. |

## Data component rule

Stack-specific drug state belongs in data components, especially:

- `MIXED_DRUG_DATA`;
- `ROLLED_CONTENT`;
- `BOTTLE_CONTENT`;
- `PURITY`;
- other bounded components in `items/data/ModDataComponents.java`.

Persistent and network codecs must have matching bounds.

## Adding or changing a base drug

1. Update `DrugRegistry` definitions.
2. Keep identity aligned with `GAMEPLAY_DESIGN.md`.
3. Add/adjust visual, HUD, tooltip, sound, particle, guide, or GUI feedback.
4. Ensure addiction and dose implications are intentional.
5. Update progression/knowledge gates only through the progression domain.
6. Add/update tests if behavior is pure enough for JVM tests.
7. Run `compileJava`, relevant tests, and `validateCodeContracts` for architecture-sensitive changes.

## Adding an effect type

1. Define the effect type in the core effect domain.
2. Add runtime/server behavior where needed.
3. Add client presentation only under `client/*`.
4. Add sync payloads only if presentation needs them.
5. Add localization/guide/UI explanation.
6. Respect reduced-motion/accessibility toggles.

## Drug identity map

- Coffee: work, energy, early productivity.
- Tobacco: focus, precision, ritual steadiness.
- Cannabis/hash: calm, stability, lowered threat perception, ritual stability.
- Cocaine/stimulants: short overclock, dash/adrenaline, readable crash/risk.
- Crack: violent short burst, high risk.
- Meth: late-game overclock, high consequence.
- Psychedelics: altered perception, ritual certainty, ore/structure perception, recovery/integration hooks.
- Alcohol: courage/resistance/chaos.
- Opioids: deferred; do not expand until core loop is stable unless explicitly requested.

## Forbidden patterns

Do not add:

- real-world synthesis, preparation, extraction, purification, or dosing instructions;
- normal consumption logic outside `DrugUseService`;
- client-authoritative drug outcomes;
- hidden knowledge grants in unrelated systems;
- addictive escalation that is mandatory for basic progression;
- unbounded per-tick scans for visual/drug effects.
