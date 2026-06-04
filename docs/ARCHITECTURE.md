# Architecture Contracts

This document defines where code belongs and which boundaries agents must preserve.

## Architectural north star

The mod is a server-authoritative survival/progression mod with client-heavy presentation. The server owns state, progression, effects, machines, recipes, pipes, rituals, recovery, and dimension mechanics. The client renders and sends validated requests.

## Layer model

```text
MyDrugs.java bootstrap
  ├─ registries: blocks/items/menus/recipes/entities/components/fluids/sounds
  ├─ domain services: drug, addiction, recovery, diary, progression, dimension
  ├─ world systems: machines, recipes, pipes, energy, gas, fluids, worldgen
  ├─ networking: central payload registration + domain handlers
  └─ client: screens, HUD, overlays, shaders, particles, sounds
```

## Source of truth hierarchy

When code and docs disagree:

1. Existing runtime contracts and validation tasks win for current behavior.
2. `AGENTS.md` / `CLAUDE.md` wins for agent rules.
3. Domain docs in this folder define intended direction.
4. Generated resources are outputs, not design sources.
5. Old scattered comments are lower confidence than domain docs.

## Registration rules

- Keep registry IDs stable.
- Keep top-level `register(modEventBus)` call sites stable unless explicitly refactoring bootstrap.
- For new groups, prefer small spec records/domain registries over extending giant static sections.
- Do not silently rename registry IDs to match new names; use player-facing lang changes if old IDs must remain.

## Domain boundaries

| Domain | Belongs here | Does not belong here |
|---|---|---|
| `core/drug/*` | drug definitions, runtime drug effects, consumption, ritual drug data | addiction internals, recovery UI, client rendering |
| `addiction/*` | tolerance, withdrawal, addiction state, bad-trip state, sync snapshots | normal item consumption logic, recovery domain ownership |
| `recovery/*` | recovery blocks/items/sanctuary mechanics | addiction-only state machine internals |
| `diary/*` | diary data, snapshots, blockers, entries | unrelated progression gates |
| `client/*` | rendering, screens, HUD, shader/sound presentation | server gameplay decisions |
| `network/*` | shared payload definitions and central registration | feature logic that should live in a domain service |
| `blocks/*` | block/block-entity behavior | UI-only rendering logic |
| `menu/*` | server menu state and client screen pairing | long-running machine algorithms |
| `recipes/*` | recipe types, serializers, displays, recipe content | arbitrary machine-specific logic not represented as a recipe |
| `pipe/*` | transfer graph, filters, side configs, route/cache logic | machine recipe progression |
| `worldgen/*`, `dimension/*` | generation, dimension state, dimension content | client-only atmosphere rendering outside client packages |

## State ownership

- Server attachments/saved data own persistent player/world state.
- Data components own item-stack state.
- Client state holders mirror presentation state only.
- Screens should read menu state and send requests; they must not decide gameplay outcomes.
- Packet payloads should be small DTOs, not service containers.

## Compatibility and persistence

Ask before changing:

- registry IDs;
- data component codecs;
- network payload IDs/codecs;
- saved-data keys;
- recipe JSON schemas;
- public lang key naming patterns;
- guide page IDs;
- dimension IDs or biome IDs.

## Performance architecture

High-risk tick/frame areas:

- pipe networks and transfer caches;
- custom drug/addiction effects;
- hallucinations, overlays, aura scans;
- machines with inventory/fluid/gas scanning;
- Inner dimension world updates;
- PsyCurrent distribution.

Prefer dirty flags, bounded searches, cached topology, scheduled work, and explicit server authority.
