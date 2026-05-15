# Architecture Overview

This is a compact map of the main systems. Keep detailed implementation notes in code comments or feature issues; this file should explain where future work should plug in.

## Canonical drug use path

All drug consumption should pass through `DrugUseService`.

```text
Drug item / fluid / block use
  -> DrugUseService
  -> route ConsumptionStrategy
  -> DrugEffectRuntimeManager.addEffect
  -> AddictionManager / DoseManager / knowledge gates
  -> DrugEffectSyncPayload
  -> client state and visuals
```

Do not apply effects, addiction, tolerance, or knowledge grants through separate ad-hoc paths unless there is a documented reason.

Current package boundaries:

- `org.mydrugs.mydrugs.core.drug.use` owns canonical drug use.
- `org.mydrugs.mydrugs.core.drug.dose` owns dose state and dose ticking.
- `org.mydrugs.mydrugs.core.drug.runtime` owns active custom drug effects.
- `org.mydrugs.mydrugs.addiction.*` owns addiction-only attachments, data, state managers, progression, withdrawal, tolerance, and addiction payloads.
- `org.mydrugs.mydrugs.recovery.*` owns recovery managers, blocks, and items.
- `org.mydrugs.mydrugs.diary` and `org.mydrugs.mydrugs.client.diary` own diary common and client UI code.
- `org.mydrugs.mydrugs.client.effects.*` owns client-only effect presentation: HUD, overlays, hallucinations, input distortion, sounds, and client payload handling.
- `org.mydrugs.mydrugs.network` owns common payload registration and shared payload records such as `DrugVisualPayload`.

## Custom effect system

The mod should use custom float-based effects, not vanilla potion effects.

Main responsibilities:

- `DrugEffect` stores effect type, duration, and intensity.
- `ConsumptionStrategy` adjusts duration, intensity, and dose for the route.
- `core.drug.runtime.DrugEffectRuntimeManager` owns server-side active custom effects.
- `DrugEffectSyncPayload` mirrors active effects to the client.
- `AddictionClientState` exposes client-readable effect and addiction state.

Client systems that may read synced effects:

- gamma/brightness controller;
- visual overlays;
- shader manager;
- input distortion;
- sound controller;
- HUD.

Server hooks that may read active effects:

- block break speed;
- movement modifiers;
- damage resistance/modification;
- vomit action;
- dose and withdrawal state transitions.

## Dose effects

```text
Addiction/dose tracking
  -> DoseManager
  -> DoseEffectManager
  -> DrugEffectRuntimeManager
```

`core.drug.dose.DoseEffectManager` should express high-dose effects through the same custom effect runtime. It should not add vanilla `MobEffectInstance`s.

Small speed/mining buffs in `DoseEffectManager` are intended to be float-based custom effects. Tune values through playtesting, but do not replace them with vanilla Speed/Haste.

## reducedMotionMode

`Config.CLIENT.reducedMotionMode` is an accessibility setting. It should reduce aggressive movement-heavy visuals without disabling gameplay.

Expected behavior:

- reduce or disable FOV/camera pulses;
- reduce shader duration or motion intensity;
- disable or soften stumble sway;
- reduce HUD/overlay shaking;
- keep gameplay effects, gamma boost, static overlays, sounds, addiction, dose logic, movement modifiers, and mining modifiers functional.

When adding a new camera, overlay, or shaking effect, check this setting.

## Psy knowledge and progression

Use `PsyKnowledgeManager` and stable `PsyKnowledgeKey` resource IDs for knowledge gates.

Do not use advancement progress as gameplay authority. Advancements can mirror knowledge, but recipes and machines should check the knowledge attachment/source of truth.

## Psy Anvil

The Psy Anvil is a no-GUI, server-authoritative progression gate for industrial components.

Expected behavior:

- right-click items onto the anvil to place a pack of nine items, no order needed;
- empty-hand right-click removes the last item;
- hammer interaction attempts crafting;
- recipe checks required knowledge;
- renderer displays placed items;
- JEI should show shaped inputs, output, tool note, and knowledge requirement.

Use the `mydrugs:psy_anvil` recipe type and keep recipe text abstract.

## Psy Mixer

The Psy Mixer should feel like a sacred primitive altar, not an industrial machine.

Expected concepts:

- small multiblock;
- formed model/renderer;
- floating item offerings;
- instability;
- mastery;
- golden zones / timing;
- religious messages;
- recipe requirements based on knowledge and lifetime dose.

Tobacco, cannabis, and psychedelics should interact with ritual precision/stability.

## Manual machine speed modifiers

Manual machine speed bonuses should be abstracted through a shared helper/service.

Do not hardcode “if coffee active” in every block entity. A helper should calculate a multiplier from active custom effects or drug states.

Affected candidates:

- Manual Coffee Pulper;
- Grinding Bowl / mortar;
- Fluid Filterer if manually operated;
- Distiller if manually operated;
- Sieve if manually operated;
- Stomp Crafter;
- Psy Mixer.

GUIs should show when a speed bonus is active.

## Psychotrope Generator

Psychotrope systems should not only generate energy. Long-term, psychotrope energy should power strange machines that create gameplay unavailable in vanilla Minecraft.

Current/future directions:

- generator multiblock;
- persistent area preview state;
- Psy Blueprint / ghost block building helper;
- future Dream Extractor, Reality Anchor, Psy Beacon, or Hallucination Trap.

## Pipes

The pipe system is staged and should stay server-authoritative.

Current architecture families:

- item pipes;
- fluid pipes;
- gas pipes.

Important rules:

- use NeoForge transfer handlers and transactions for item/fluid where possible;
- use the existing custom gas APIs for gas;
- keep side modes and filters explicit;
- avoid per-tick network rescans unless the network is dirty;
- do not add visual transfer effects before the logical transfer system is stable.

## Guide book

`docs/progression_guide_pages.md` is source content for the in-game guide. The generated output should not be edited by hand.

Use `GUIDE_AUTHORING.md` and the sync script when updating progression text.

## Datagen

Simple generated assets/data should come from datagen providers when possible:

- item models;
- blockstates;
- block models;
- loot tables;
- recipes;
- tags.

Keep hand-authored textures, sounds, complex models, guide source, and shaders in main resources.

`src/main/resources/assets/mydrugs/lang/en_us.json` is currently the canonical lang source. `ModLangProvider` is retained but not registered as a full replacement until it covers the complete hand-authored file.

Run `validateResources` after resource or datagen changes. It fails on BOM JSON, missing blockstates/item definitions/lang keys, unresolved guide `@item` references, backup files, and generated-cache hygiene. Missing texture/model references are written to `docs/ASSET_TODO.md` for artist follow-up rather than filled with placeholder art.

## Registries

Top-level registration remains stable through `ModItems.register(...)`, `ModBlocks.register(...)`, `ModCrops.register(...)`, and the existing fluid/gas registries. New grouped registries should use small records or domain helpers rather than adding another long static section.

Current examples:

- `ItemSpec` for small item definitions.
- `MachineSpec` for machine-like block definitions.
- `FluidSpec`, `GasSpec`, and `CropSpec` for resource families.
- `ModRecoveryItems`, `ModMutationItems`, `ModPipeItems`, `ModRecoveryBlocks`, and `ModPipeBlocks` as domain owners with compatibility aliases kept on `ModItems` and `ModBlocks`.

Registry IDs are not changed by moving a holder into a domain helper. Keep aliases when existing code expects `ModItems.X` or `ModBlocks.X`.
