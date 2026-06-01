# Roadmap

This roadmap is about playable milestones, not every idea in the Trello. Keep raw ideas in Trello. Move only clear, buildable work into GitHub Issues.

## Milestone 0 — Project control

Goal: make the project easier to steer before adding more content.

- Keep this docs folder clean.
- Add or maintain GitHub Actions build checks.
- Use one branch and one PR per feature/theme.
- Keep PRs small enough to review.
- Keep datagen and resources organized.
- Use `TESTING.md` before marking features done.
- Keep the healing/integration vision in `VISION.md`, `STORYLINE.md`, and `GAMEPLAY_DESIGN.md` as the review baseline.

Done when:

- A fresh clone can build.
- New work has a feature issue or prompt with acceptance criteria.
- The repo has no unclear random-notes docs that should be backlog items.

## Milestone 1 — Alpha gameplay spine

Goal: make the early/mid game loop understandable and fun.

Recommended PR order:

1. Rebalance drug durations around Minecraft time.
2. Add a shared manual machine speed modifier system.
3. Give coffee a clear work/energy identity.
4. Give tobacco a precision/mining/ritual focus identity.
5. Give cannabis a stress/stability/recovery-support identity.
6. Give cocaine/stimulants adrenaline and dash identity.
7. Add a Psy Mixer ritual drug helper/registry.
8. Add the first useful Psy Mixer ritual variants.
9. Update the in-game guide to explain the actual progression.
10. Playtest and rebalance.

Done when:

- The player can understand why coffee, tobacco, cannabis, cocaine, and alcohol are useful.
- The Psy Mixer has meaningful early recipes.
- HUD and GUIs explain active bonuses and risks.
- The diary exists as more than a stat screen and can direct the player to the next meaningful step.

## Milestone 2 — Risk, addiction, and readable consequence

Goal: make power dangerous but readable without turning the mod into punishment.

Work candidates:

- Better withdrawal balance and UI feedback.
- Better dose symptoms by category.
- Better stimulant crash behavior.
- Bad-trip prototype that is meaningful, recoverable, and accessible.
- More ritual failure outcomes.
- Better vomit, tremor, blur, stumble, and input-fail tuning.
- Diary entries that distinguish “useful tool” from “avoidance loop.”

Done when:

- The player can tell what is happening and why.
- Strong effects feel tempting but not free.
- Failures feel like consequences, not bugs.
- Addiction is avoidable, recoverable, and never a forced story railroad.

## Milestone 3 — Recovery Update

Goal: make recovery a real progression pillar instead of a side utility.

Work candidates:

- Maxable recovery / resilience progression.
- Therapy-like actions beyond talking to an NPC.
- Diary-guided recovery tasks.
- Diet and sleep bonuses.
- Exercise / training loop that improves withdrawal resistance and mood.
- Recovery Sanctuary upgrades and better scoring feedback.
- Music/headphones integration polish.
- Psychedelic preparation and post-trip integration steps.
- Clear separation between symptom relief and long-term recovery.

Done when:

- Recovery has multiple viable routes.
- The player can intentionally reduce addiction pressure and stabilize their character.
- Recovery progress unlocks concrete gameplay benefits.
- The diary can recommend recovery actions based on current state.

## Milestone 4 — Diary Update

Goal: make the diary the emotional and mechanical spine of the mod.

Work candidates:

- Diary objective tracking.
- Psyche Map rewrite around discovery → control → temptation → recovery → integration.
- Reflective entries that change tone as the player progresses.
- Entries for first addiction, first recovery room, first LSD, first bad trip, first integration, first dimension entry, first endgame recovery milestone.
- Diary pages that set the positive mood: “I fought my inner demons and won,” “I mistook movement for progress,” “The trip showed me where to begin.”
- Clear distinction between guide voice and diary voice.

Done when:

- The player can use the diary as a to-do list and a meaning-making tool.
- The diary explains why recovery matters without moralizing.
- The diary guides the player back toward LSD/mushrooms and integration after overclock/body-strain branches.

## Milestone 5 — Psychedelic Opening and Psychotrope Resonator

Goal: make LSD/mushrooms the bridge from machine mastery to healing/integration systems.

Work candidates:

- Rename/reframe Psychotrope Generator to Psychotrope Resonator where feasible.
- Unlock Resonator after LSD.
- Add deterministic Resonator states: Dormant, Stable, Lucid, Dream, Integration, Overstrained.
- Use Dream state for dimension entry/stabilization.
- Use Integration state for recovery materials and endgame healing recipes.
- Avoid hunger, cravings, random sabotage, or passive machine punishment.
- Add Dream Residue / Insight / Resonance resources.
- Update textures from tech generator to ritual-technical healing device.

Done when:

- The Resonator has a clear purpose beyond energy.
- It supports recovery, dimension access, and positive endgame tools.
- It does not monopolize gameplay or create constant chores.

## Milestone 6 — Inner Dimension Update

Goal: make the dimension a beautiful manifestation of the player's inside.

Work candidates:

- Sky-island dimension entry through Resonator Dream state.
- Safe first-entry area.
- Symbolic biomes: Memory Grove, Fungal Choir, Quiet Lake, Fracture Fields, Craving Hollows, Inner Sun.
- Mystical Ores that replace amethyst in advanced integration recipes.
- Plants, trees, structures, and mobs that communicate psychological states.
- Helpful/passive entities, not only enemies.
- Accessibility settings for dimension visuals, fog, flashing, and motion.

Done when:

- The dimension is beautiful first and dangerous second.
- The player wants to return there.
- Dimension materials clearly support recovery, Resonator upgrades, and endgame freedom.

## Milestone 7 — Ketamine-like Integration Update

Goal: add a careful endgame clinical-inspired recovery tool without turning it into a casual power drug.

Work candidates:

- Fictionalized ketamine-like integration route.
- Requires Recovery Sanctuary, diary progress, or therapy-like structure.
- Creates a short integration window where recovery actions are stronger.
- Long cooldown and no ordinary recreational buff identity.
- Supports severe addiction recovery without replacing all recovery work.
- Guide text and disclaimer avoid medical/dosing/procedural claims.

Done when:

- The system feels serious, controlled, and recovery-oriented.
- It helps addiction/integration gameplay without becoming a new abuse loop.

## Milestone 8 — Somatic Adaptation / DNA Review

Goal: reframe body progression so it fits the recovery/integration mood.

Work candidates:

- Rename DNA-facing concepts where possible.
- Review all body stats and effects.
- Replace pure “bigger stats” with resilience/regulation/adaptation effects.
- Make body progression support recovery, safe trips, dimension traversal, and endgame freedom.
- Keep old registry IDs if needed; change player-facing text first.
- Add diary entries that question whether body optimization is solving the real problem.

Done when:

- Body systems no longer feel like unrelated sci-fi.
- The player understands the difference between over-optimizing the body and integrating the self.

## Milestone 9 — Boss and Positive Ending

Goal: conclude the mod with integration, not moral panic.

Work candidates:

- Boss concept: The Loop, The False Cure, The Craving, The Hollow Protector, or The Unintegrated Self.
- Fight uses addiction state, recovery level, diary progress, Resonator, dimension materials, and endgame armor.
- Final phase emphasizes integration/transformation, not only killing.
- Rewards reduce chores and increase freedom.
- Post-boss dimension state becomes calmer/brighter/safer.

Done when:

- The ending is positive.
- The player feels freer afterward.
- The final message is not “don’t do drugs,” but “do not confuse escape with healing.”
