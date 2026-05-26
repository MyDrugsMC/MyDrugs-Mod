# Feature: [Name]

## Goal

What should this add to the player experience?

Example: “Coffee should feel useful because it helps the player work faster with manual machines.”

Also state where the feature sits in the current story:

```text
Discovery / control / temptation / recovery / psychedelic opening / integration / dimension / boss / freedom
```

## Current problem

What is missing, confusing, broken, or not fun?

## Gameplay behavior

Describe what should happen in-game from the player’s perspective.

## Narrative and tone behavior

Describe what the feature says emotionally.

Ask:

- Does it support escape, control, recovery, integration, or freedom?
- Does it accidentally push the mod toward pure horror or moral panic?
- Does it create endgame chores?
- Does it make psychedelics into magic cures instead of integration openings?
- Does it respect the guide/diary voice split?

## Scope

### Included

- [ ] Item/block/effect/system included in this PR.
- [ ] GUI/HUD/JEI/guide updates if needed.
- [ ] Diary/Psyche Map updates if needed.
- [ ] Datagen/resources if needed.
- [ ] Accessibility/config updates if visuals or audio are intense.

### Not included

List tempting follow-ups that should not be done in this PR.

## Technical notes

Relevant classes, packages, registries, menus, payloads, events, or existing systems.

Mention whether the feature touches:

- `DrugUseService`;
- addiction/dose/runtime effect systems;
- recovery/integration systems;
- diary/guide systems;
- Resonator/dimension systems;
- worldgen;
- client-only visuals.

## Balance notes

Initial values, risks, and what should be adjusted after playtesting.

For drug/recovery features, include:

- benefit;
- cost;
- addiction/dose impact;
- recovery interaction;
- whether it is avoidable or optional.

## Safety/content notes

Keep descriptions fictional and gameplay-oriented.

Do not add:

- real-world synthesis procedures;
- real-world dosing instructions;
- medical advice;
- claims that a substance cures addiction or depression.

Psychedelic and ketamine-like systems should be framed as gameplay-inspired integration systems, not real-world guidance.

## Acceptance checklist

- [ ] Compiles with `./gradlew compileJava`.
- [ ] Works in singleplayer.
- [ ] Does not crash a dedicated server.
- [ ] No missing textures/models/lang keys.
- [ ] Datagen updated if relevant.
- [ ] JEI updated if relevant.
- [ ] In-game guide updated if relevant.
- [ ] Diary/Psyche Map updated if relevant.
- [ ] HUD/GUI feedback exists if the effect is invisible otherwise.
- [ ] Accessibility options respected for intense visuals/audio.
- [ ] Tested manually with steps written in the PR.
- [ ] No unrelated refactors mixed in.
- [ ] Feature still aligns with `VISION.md` and `STORYLINE.md`.

## Manual test steps

1. ...
2. ...
3. ...

## Follow-up ideas

- ...
