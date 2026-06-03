# Machines, Pipes, and Recipes

This domain is large and easy to break. Keep server authority, recipe consistency, and transfer performance central.

## Package map

- `blocks/*` — blocks and block entities.
- `blocks/entity/*` — machine block entities and server logic.
- `menu/*` — menus and containers.
- `menu/client/*` — client screens.
- `recipes/*` — recipe records, serializers, displays, and per-machine recipe packages.
- `pipe/*` — pipe blocks, block entities, filters, network scanning, transfer logic.
- `fluids/*` — fluid specs, tags, roles, hazards.
- `gas/*` — gas specs, tanks, handlers, capabilities.
- `energy/*` — PsyCurrent energy, upgrades, distribution.

## Machine rule

Machine gameplay logic belongs in block entities/services, not screens. Screens render state and send validated requests.

When adding or changing a machine:

1. Block and block entity registration.
2. Menu and screen pairing.
3. Recipe type/serializer/display if recipe-driven.
4. Machine descriptor/content validation if applicable.
5. Lang keys for block, menu, tooltips, statuses.
6. Models/blockstates/item models through datagen if generated.
7. JEI/display integration if recipe is non-obvious.
8. Tests or validator updates if the machine becomes part of a contract.

## Recipe consistency

Recipe systems should keep type, serializer, display, content descriptor, and generated JSON aligned.

Do not hand-edit generated recipe JSON. Change the provider and run `runData`.

Prefer gameplay-fictional recipe names and abstractions over real-world chemistry steps.

## Pipe performance contracts

Pipe systems are tick-sensitive. Preserve these principles:

- dirty-driven rebuilds instead of full scans every tick;
- cached topology/routes;
- skip unloaded source/target endpoints;
- deduplicate transfer candidates by handler identity;
- bounded transfer budgets;
- diagnostic counters behind config;
- no stream-heavy route filtering in hot tick paths.

Relevant classes:

- `pipe/network/PipeNetworkManager.java`
- `pipe/network/PipeRouteCache.java`
- `pipe/network/ItemPipeNetworkLogic.java`
- `pipe/network/FluidPipeNetworkLogic.java`
- `pipe/network/GasPipeNetworkLogic.java`
- `pipe/network/PipeTransferTicker.java`

## Fluids and gas

- Use `FluidSpec` / `GasSpec` patterns for new definitions.
- Keep color, hazard, role, bucket, and tank behavior consistent.
- Validate client rendering for fluids/gases without leaking client imports into common packages.
- Gas/fluid transfer must handle partial insert/drain safely and avoid item duplication or loss.

## Status and localization

Machine statuses should use stable enum/status names and `machine_status.mydrugs.*` lang keys. Player-facing GUI text should use `Component.translatable`.

## Validation checklist

For machine/pipe/recipe changes, consider:

```bash
./gradlew compileJava
./gradlew test
./gradlew validateCodeContracts
./gradlew validateResources
./gradlew runData
```

Run the narrowest relevant subset first, then broader checks before finalizing a large change.
