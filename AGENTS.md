# AGENTS.md

## Project

`mydrugs` is a Minecraft NeoForge mod for Minecraft `1.21.10`, Java 21, using `net.neoforged.moddev`.

Design goal: a risk/reward survival mod about altered states, addiction, recovery, ritual crafting, dangerous power, psychotrope energy, machines, mutation, and strange exploration.

Canonical docs, read only when relevant:

* `docs/VISION.md` — tone, fantasy, pillars, anti-goals.
* `docs/GAMEPLAY_DESIGN.md` — drug identities, rituals, machine abstraction.
* `docs/ARCHITECTURE.md` — system boundaries.
* `docs/progression_guide_pages.md` — in-game guide source.
* `docs/TESTING.md` — compile, datagen, manual checks.
* `docs/AI_WORKFLOW.md` — PR/workflow expectations.
* Codebase : `docs/CODEBASE_MAP.md`


When docs clearly define intent, prefer them over generated files or scattered legacy code.

## Core invariants

Prioritize:

1. Dedicated-server safety.
2. Server-authoritative gameplay.
3. Strict common/server/client separation.
4. One canonical drug-consumption path.
5. Small maintainable domain packages.
6. Datagen/resource consistency.
7. Localization for player-facing text.
8. No real-world drug synthesis, preparation, or dosing instructions.
9. Preserve gameplay unless explicitly asked to change design/balance.

## Work protocol

Before editing, inspect only the relevant area. Use focused `rg` and small file reads. Avoid huge registries, generated JSON, and unrelated docs unless needed.

```bash
rg -n "class|record|enum|interface" src/main/java/org/mydrugs/mydrugs/<target-package>
rg -n "TODO|FIXME|Component\.literal|@EventBusSubscriber|playToServer|registerPayload" src/main/java/org/mydrugs/mydrugs
rg -n "<id_or_class_or_payload>" src/main/java src/main/resources src/generated/resources docs
```

Keep work scoped:

* Do not mix unrelated refactors with features unless asked.
* One task should produce one reviewable theme, such as network hardening, event-bus cleanup, addiction package split, datagen validation, client/server safety, resource/language audit, or pipe performance cleanup.
* Do not delete messy systems immediately. First isolate, document, migrate call sites, compile, then remove dead code.
* If a change is too large, make safe passes in the same branch and leave a migration checklist.

## Architecture rules

### Drugs, dose, runtime effects

Canonical consumption path: `core/drug/use/DrugUseService`.

Rules:

* All drug consumption must go through `DrugUseService`.
* Dynamic stack-based drugs use data components, not runtime item registration.
* Item classes may delegate; they must not directly apply effects, addiction, tolerance, overdose, dose, item consumption, or knowledge grants.
* Do not create parallel consumption systems.

Relevant packages:

* Drug domains: `core/drug`, `core/drug/effect`, `core/drug/strategy`, `core/drug/use`, `core/drug/ritual`, `core/drug/runtime`, `core/drug/dose`.
* Addiction-only domains: `addiction/attachment`, `addiction/data`, `addiction/manager`, `addiction/progression`, `addiction/withdrawal`, `addiction/tolerance`, `addiction/config`.
* Recovery and diary are first-class domains: `recovery/*`, `diary/*`.

### Client-only presentation

Client visuals, HUD, screens, shaders, hallucinations, sound, and input distortion belong under `client/*`.

No common/server class may import:

```text
net.minecraft.client.*
net.neoforged.neoforge.client.*
org.mydrugs.mydrugs.client.*
```

Client event subscribers must use `value = Dist.CLIENT`.

NeoForge 21.10 routes `@EventBusSubscriber` automatically: `IModBusEvent` to mod bus, others to game bus. Do not add `bus = ...`.

### Machines and world systems

Keep domains separate: `machine/*`, `recipes/*`, `pipe/*`, `gas/*`, `fluids/*`, `mutation/*`, `energy/*`, `progression/*`, `worldgen/*`.

Screens render state and send validated requests only. Do not put machine logic in menu screens.

## Networking

Centralize payloads in:

* `network/ModNetwork.java`
* `network/ServerPayloadHandlers.java`
* `client/network/ClientPayloadHandlers.java`

Server-bound packets are requests, not commands. Validate:

* `ServerPlayer`
* open menu and `menuId`
* `menu.stillValid(player)`
* menu-owned block position
* held item, hand, stack, or capability state
* numeric bounds; reject `NaN`, infinities, negative work, oversized values
* spammy actions by rate limit
* admin/debug mutations with `player.hasPermissions(2)` plus server config gate

Server owns ritual timing/judgement. Never trust client phase/timing.

Client-bound packets are for visuals, sounds, HUD snapshots, screens, and presentation state. Register client handlers only in client-side handler registration.

Avoid enum ordinal codecs for persistent/networked data unless documented stable IDs exist. Prefer named IDs or explicit `byId`.

## Registries and definitions

For new groups, prefer small spec records and domain registries over giant static sections in `ModItems`, `ModBlocks`, `ModFluids`, or `ModBlockEntities`.

Examples:

```java
record ItemSpec(String id, Supplier<Item> factory) {}
record MachineSpec(String id, MachineTier tier, boolean manual, int tankCapacity) {}
record FluidSpec(String id, int color, int viscosity, int density, boolean bucket) {}
record GasSpec(String id, int color, boolean toxic, boolean flammable) {}
```

Keep top-level `register(modEventBus)` call sites stable and preserve registry IDs. Organize creative tabs by category as item count grows.

## Resources, datagen, localization

* Generated resources: `src/generated/resources`.
* Hand-authored resources: `src/main/resources`.
* Do not hand-edit generated JSON owned by datagen.
* If changing recipes, loot tables, tags, generated models, blockstates, or advancements, update the provider and run `runData`.
* Do not commit `src/generated/resources/.cache` unless intentionally tracked.
* JSON must be UTF-8 without BOM.
* Every user-facing item, block, menu, message, guide entry, and tooltip needs localization.
* Use `Component.translatable` for player-facing text. `Component.literal` is only for debug or dynamic numeric output.
* Missing art should be a TODO, asset manifest entry, or clearly marked placeholder.

## Guide and progression

`docs/progression_guide_pages.md` is the source for `src/main/resources/assets/mydrugs/guide/pages.json`.

When progression changes:

1. Update `docs/progression_guide_pages.md`.
2. Regenerate guide output if a script exists.
3. Validate all `@item` IDs.
4. Keep guide text abstract/gameplay-focused.
5. Do not include real-world procedural chemistry instructions.

## Drug identity rules

Follow `docs/GAMEPLAY_DESIGN.md`.

Canonical drug identities and risk language: see `docs/GAMEPLAY_DESIGN.md`. Do not restate them here.

Every effect needs readable feedback through HUD, GUI, tooltip, sound, overlay, particles, guide text, or JEI.

Respect `Config.CLIENT` accessibility toggles. `reducedMotionMode` should reduce aggressive visual motion without disabling gameplay.

## Performance

Be careful with per-tick/per-frame systems: pipes, custom effects, addiction/dose/withdrawal, visual overlays, ore aura scans, hallucinations, machine transfer/network scans.

Prefer dirty flags, cached topology, bounded searches, scheduled work, and server authority. Avoid unbounded scans every tick/frame.

## Testing

Run the narrowest relevant check first:

```bash
./gradlew compileJava
./gradlew runData
./gradlew build
```

If the Gradle wrapper is missing or not executable, fix/report that before claiming success.

Dedicated-server safety check:

```bash
rg -n "import net\.minecraft\.client|import net\.neoforged\.neoforge\.client|import org\.mydrugs\.mydrugs\.client" src/main/java/org/mydrugs/mydrugs --glob '!client/**'
```

Useful refactor checks:

```bash
rg -n "@EventBusSubscriber\(modid = MyDrugs\.MODID\)" src/main/java/org/mydrugs/mydrugs
rg -n "Component\.literal\(" src/main/java/org/mydrugs/mydrugs
rg -n "\.ordinal\(|ByteBufCodecs\.STRING_UTF8|playToServer|registerPayload" src/main/java/org/mydrugs/mydrugs
```

## Required final response after code changes

Every response after code changes must include:

```text
Changed files:
- ...

Commands run:
- ...

Validation result:
- compileJava: pass/fail/not run with reason
- runData: pass/fail/not run with reason
- build: pass/fail/not run with reason

Dedicated-server safety:
- checked/not checked

Risks / TODOs:
- ...
```

Do not claim compile, datagen, build, or in-game success unless the command/test was actually run.
