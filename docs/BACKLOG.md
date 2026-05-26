# Curated Backlog

This is not the full idea dump. Use Trello for raw ideas. This file keeps the main project follow-ups that should become GitHub Issues when they are ready.

## Priority 0 — project hygiene

- Add or maintain GitHub Actions build workflow.
- Keep docs focused and remove stale one-off notes.
- Audit `assets/minecraft` and keep only intentional vanilla overrides.
- Confirm licensing/provenance for large `.ogg` sound/music files before distribution.
- Continue moving simple generated JSON into datagen providers.
- Keep all player-facing text in lang files.
- Keep the current vision aligned with `VISION.md` and `STORYLINE.md`.

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
- Ensure LSD and mushrooms are mechanically distinct from addictive escalation drugs.

## Priority 2 — clarity and UX

- Keep HUD compact and readable.
- Make invisible effects visible through icons, GUI labels, overlays, or guide text.
- Update guide pages whenever progression changes.
- Ensure JEI displays all core non-obvious recipe systems, including Psy Anvil and Psy Mixer.
- Improve missing-requirement messages for knowledge, lifetime dose, and rituals.
- Normalize `container.mydrugs.*` for menu titles and `gui.mydrugs.*` / `tooltip.mydrugs.*` / `status.mydrugs.*` for GUI text.
- Ensure the diary and guide have different voices.

## Priority 3 — risk, addiction, and consequence

- Tune addiction rates after playtesting.
- Make stimulant crash readable and interesting.
- Tune vomit, tremor, blur, stumble, and input fail.
- Make bad trips meaningful and recoverable, not random horror.
- Replace or reframe pure “inner demon” drops with psychological/integration fragments if needed.
- Add alcohol courage/resistance/chaos pass.
- Ensure addiction is avoidable and recoverable; do not force addiction as mandatory progression.

## Priority 4 — Recovery Update

- Make recovery maxable through a resilience/integration progression.
- Add therapy-like actions beyond talking to an NPC.
- Add diary-guided recovery tasks.
- Add diet/nutrition recovery bonuses.
- Add exercise/training recovery bonuses.
- Expand sleep and safe-room mechanics.
- Make Recovery Sanctuary scoring clearer.
- Add psychedelic preparation and integration actions.
- Add recovery rewards that reduce endgame chores.

## Priority 5 — Diary Update

- Make the diary central to objectives.
- Add entries for discovery, addiction pressure, stimulant overclocking, LSD, mushrooms, bad trips, recovery milestones, Resonator unlock, dimension entry, boss preparation.
- Add Psyche Map progression around the new story arc.
- Add reflective lines that set the positive mood.
- Let the diary guide the player back to LSD/mushrooms and recovery after the meth/overclock branch.

## Priority 6 — Psychotrope Resonator Update

- Rename/reframe Psychotrope Generator to Psychotrope Resonator in player-facing text.
- Unlock after LSD.
- Define deterministic states: Dormant, Stable, Lucid, Dream, Integration, Overstrained.
- Use Dream state for dimension entry.
- Use Integration state for recovery resources and endgame healing tools.
- Avoid hunger, cravings, random sabotage, and passive punishment.
- Add Resonance/Insight/Dream Residue resources.
- Update textures toward ritual-technical healing machinery.

## Priority 7 — Dimension Update

- Design the sky-island dimension as the player's inner landscape.
- Add safe first-entry zone.
- Add symbolic biomes: Memory Grove, Fungal Choir, Quiet Lake, Fracture Fields, Craving Hollows, Inner Sun.
- Add Mystical Ores as amethyst replacement in advanced integration recipes.
- Add plants, trees, passive guides, symbolic hostile mobs, and beautiful structures.
- Ensure worldgen is configurable and non-invasive.

## Priority 8 — Ketamine-like endgame integration

- Add fictionalized ketamine-like treatment route only after recovery systems are strong.
- Require Recovery Sanctuary, diary progress, or therapy-like setup.
- Use it as an integration window, not a recreational buff.
- Add long cooldown and careful guide text.
- Avoid dosage, medical advice, or procedural real-world details.

## Priority 9 — ADN / Somatic Adaptation review

- Review all ADN names and player-facing text.
- Reframe from sci-fi gene optimization to somatic adaptation / body regulation.
- Review stats: favor resilience, grounding, withdrawal resistance, safe trips, dimension traversal, and recovery.
- Keep registry IDs if needed, but update lang keys and guide tone.
- Make body progression a questioned branch, not the final answer.

## Priority 10 — Boss and positive ending

- Design boss as the Loop / False Cure / Craving / Hollow Protector / Unintegrated Self.
- Make final phase integration-focused.
- Use diary, recovery, dimension, Resonator, addiction state, and endgame armor.
- Rewards should make the player freer, not add chores.
- Post-boss world/dimension should become calmer, safer, and more open.

## Technical follow-ups from old notes

- Fix Psy Receptacle and wire soft-locks through expensive recipes or recovery commands.
- Add useful automated tests.
- Double-check build files.
- Fix hash knowledge inconsistency.
- Double-check whole pipe system and performance.
- Fix DistillerBlockEntity status.
- Fix `DrugEffectRuntimeManager` persistence/lifecycle.
- Enforce `DrugUseService` as the canonical architecture.
- Add missing `machine_recipe_completed` advancement hooks only at exact once-per-recipe completion points.
- Add a pipe transfer criterion when there is one authoritative transfer-completion point.
- Add dedicated fluid/gas container-filled criteria only if there is a shared server-side filling method.
- Keep pipe GUI polish and transfer visuals as later work after logic is stable.
