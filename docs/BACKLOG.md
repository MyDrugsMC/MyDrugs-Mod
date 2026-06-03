# Curated Backlog

Keep this list high-level. Turn items into issues or feature specs before asking an agent to implement them.

## Project hygiene

- Add/maintain CI for `compileJava`, `test`, `validateCodeContracts`, and `validateResources`.
- Keep docs small and agent-useful.
- Confirm licensing/provenance for large sound/music assets before distribution.
- Continue moving generated JSON ownership into datagen providers.
- Keep all player-facing text in lang files.

## UX and clarity

- Make active effects readable in HUD/GUI/tooltips.
- Improve missing-requirement messages for knowledge, lifetime dose, rituals, and sanctuary modules.
- Normalize lang key families for menus, tooltips, statuses, and GUI messages.
- Ensure diary and guide have distinct voices.

## Drug/effect gameplay

- Tune durations using Minecraft-day feel rather than realism.
- Add/maintain shared manual-machine speed modifier helpers.
- Make stimulant crash readable and interesting.
- Keep psychedelics mechanically distinct from addictive overclock drugs.
- Expand alcohol only as courage/resistance/chaos, not pure buff.

## Machines, recipes, and pipes

- Keep machine descriptors and recipe displays aligned.
- Improve pipe GUI polish after logic is stable.
- Profile pipe transfer hot paths before broad rewrites.
- Add exact advancement hooks only at authoritative completion points.

## Recovery and diary

- Make sanctuary scoring clearer.
- Add actionable diary-guided recovery tasks.
- Expand recovery rewards that reduce late-game chores.
- Add reflective entries for discovery, overclocking, bad trips, recovery milestones, dimension entry, and boss preparation.

## Dimension and endgame

- Design safe first-entry area.
- Add symbolic regions and integration materials.
- Keep worldgen configurable and non-invasive.
- Design final boss around integration/freedom.

## Technical debt

- Continue enforcing `DrugUseService` as canonical consumption path.
- Review `DrugEffectRuntimeManager` lifecycle when touching runtime effects.
- Keep pipe contracts in `validateCodeContracts` current.
- Keep guide `@item` refs and resource validation green.
