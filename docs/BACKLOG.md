# Curated Backlog

This is not the full idea dump. Use Trello for raw ideas. This file keeps the main project follow-ups that should become GitHub Issues when they are ready.

## Priority 0 — project hygiene

- Add or maintain GitHub Actions build workflow.
- Keep docs focused and remove stale one-off notes.
- Audit `assets/minecraft` and keep only intentional vanilla overrides.
- Confirm licensing/provenance for large `.ogg` sound/music files before distribution.
- Continue moving simple generated JSON into datagen providers.

## Priority 1 — Alpha gameplay spine

- Rebalance drug durations using Minecraft-day time scaling.
- Add shared manual machine speed modifier helper.
- Apply coffee/stimulant speed bonuses to manual machines.
- Show active manual speed bonuses in GUIs.
- Add coffee camera sway/jitter at higher dose intensity.
- Add tobacco precision mining effect through custom mining speed.
- Add aloe vera plant and tobacco/aloe mixture.
- Add tobacco golden-zone ritual bonus.
- Add cannabis stress reduction if missing.
- Add cannabis ritual stability behavior.
- Add stimulant adrenaline and dash.
- Add red/vein adrenaline overlay.
- Add first Psy Mixer ritual-drug helper/registry.
- Add first useful Psy Mixer recipes.

## Priority 2 — clarity and UX

- Keep HUD compact and readable.
- Make invisible effects visible through icons, GUI labels, overlays, or guide text.
- Update guide pages whenever progression changes.
- Ensure JEI displays all core non-obvious recipe systems, including Psy Anvil and Psy Mixer.
- Improve missing-requirement messages for knowledge, lifetime dose, and rituals.

## Priority 3 — risk and consequence

- Tune addiction rates after playtesting.
- Make stimulant crash readable and interesting.
- Tune vomit, tremor, blur, stumble, and input fail.
- Add bad-trip creature prototype.
- Add bad-trip drops such as `inner_demon_remains` only after creature behavior is designed.
- Add alcohol courage/resistance/chaos pass.

## Priority 4 — unique exploration

- Add optimized psychedelic ore aura.
- Let psychedelic effects reveal missing multiblock blocks without Psy Blueprint.
- Prototype temporary rift opening later, not before the core loop is stable.
- Design dream-island gameplay before implementing a dimension.

## Technical follow-ups from old notes

- Add missing `machine_recipe_completed` advancement hooks only at exact once-per-recipe completion points.
- Add a pipe transfer criterion when there is one authoritative transfer-completion point.
- Add dedicated fluid/gas container-filled criteria only if there is a shared server-side filling method.
- Keep pipe GUI polish and transfer visuals as later work after logic is stable.
