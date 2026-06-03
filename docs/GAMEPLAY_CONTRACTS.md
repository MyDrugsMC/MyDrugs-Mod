# Gameplay Contracts

## Core gameplay loop

TODO: Define the current loop from source and design docs.

## Risk/reward contract

Power should come with legible risk, cost, instability, or long-term consequence.

## Addiction/tolerance/withdrawal contract

TODO: Fill from source and `ADDICTION_RECOVERY_DIARY.md`.

## Recovery contract

Recovery is a first-class arc, not an addiction subfolder or a simple stat reset.

## Ritual contract

Rituals are server-authoritative, symbolic, and gameplay-focused.

Server owns ritual timing and judgement. Never trust client ritual phase/timing.

## Machine contract

Machines should be domain logic first, UI second. Screens render state and send validated requests.

## Progression contract

Progression changes should update the guide source manually or via the established guide generation process.

Do not rewrite `docs/progression_guide_pages.md` unless explicitly asked.

## Exploration contract

TODO: Define exploration expectations from worldgen/dimension systems.

## Feedback contract

Every effect should have readable feedback through at least one of:

- HUD;
- GUI;
- tooltip;
- sound;
- overlay;
- particles;
- guide text;
- JEI;
- screen state.

## Balance-change protocol

Do not change design or balance during maintainability work unless explicitly asked.

When proposing balance changes, mark them as design recommendations rather than bug fixes.
