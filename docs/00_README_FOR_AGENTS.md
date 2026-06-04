# README for Agents

This folder is the curated Project Brain for `mydrugs`. It is the single router for the docs folder.

Use this file as the router after reading `AGENTS.md`.

Project constants:

- Mod ID: `mydrugs`
- Minecraft: `1.21.10`
- NeoForge: `21.10.64`
- Java: `21`
- Build plugin: `net.neoforged.moddev`
- Main package: `org.mydrugs.mydrugs`

## First files to read

For most tasks:

1. `AGENTS.md`
2. `CLAUDE.md` if using Claude Code
3. `docs/00_README_FOR_AGENTS.md`
4. `docs/CODEBASE_MAP.md`

## Choose docs by task

Do **not** read the whole folder by default. Pick the smallest document set for the task.

| Task type | Read first | Then read if needed |
|---|---|---|
| Any code change | `CODEBASE_MAP.md`, `AI_WORKFLOW.md`, `TESTING.md` | `ARCHITECTURE.md` |
| Mood, design, tone | `VISION.md`, `GAMEPLAY_DESIGN.md`, `STORYLINE.md` | `RECOVERY_AND_INTEGRATION_DESIGN.md` |
| Drug behavior, dose, effects | `DRUG_SYSTEM.md` | `GAMEPLAY_DESIGN.md`, `RECOVERY_AND_INTEGRATION_DESIGN.md` |
| Addiction, recovery, diary | `ADDICTION_RECOVERY_DIARY.md`, `DRUG_SYSTEM.md`, `RECOVERY_AND_INTEGRATION_DESIGN.md` | `STORYLINE.md`, `VISION.md` |
| Client visuals, HUD, screens, shaders | `CLIENT_SERVER_SAFETY.md` | `NETWORKING.md`, `GAMEPLAY_DESIGN.md` |
| Payloads, menus, server requests | `NETWORKING.md` | `CLIENT_SERVER_SAFETY.md`, `TESTING.md` |
| Machines, recipes, pipes, menus | `MACHINES_PIPES_AND_RECIPES.md` | `RESOURCES_AND_DATAGEN.md` |
| JSON, lang, models, loot, tags, generated resources | `RESOURCES_AND_DATAGEN.md` | `GUIDE_AUTHORING.md` |
| Worldgen, Inner dimension, TerraBlender | `WORLDGEN_AND_DIMENSION.md` | `GAMEPLAY_DESIGN.md` |
| New feature planning | `FEATURE_TEMPLATE.md` | Relevant domain doc |
| Project direction | `VISION.md`, `ROADMAP.md`, `BACKLOG.md` | `STORYLINE.md` |
| Maintainability | `audits/MAINTAINABILITY_AUDIT.md`, `audits/TECH_DEBT_REGISTER.md`, `audits/RISK_HOTSPOTS.md` | `audits/MOOD_AUDIT.md` |
| Content safety | `SAFETY_AND_CONTENT_POLICY.md` | Relevant design doc |

## Guide file note

`progression_guide_pages.md` was copied byte-for-byte from the uploaded docs because the guide is user-edited. SHA-256 of the preserved guide content:

```text
503321a8153bb81e09cd75ffaee4239cb753918b024ff3233bed82d20aa5a1e9
```

## Files intentionally removed from the old docs folder

The previous folder contained large one-off notes and reports that are poor default agent context, including balancing dumps, old implementation plans, and narrow feature brainstorms. Their durable decisions were merged into the domain docs here. Raw ideation should live outside startup docs or in an issue tracker.

## Source of truth order

1. Explicit user request
2. Source code
3. Datagen providers and generated outputs
4. Curated docs
5. Generated scan files
6. Comments or legacy code

When docs and code disagree, report the disagreement instead of guessing.

## Answer discipline

When answering project questions, distinguish:

- confirmed from source;
- confirmed from docs;
- inferred;
- recommendation;
- unknown.

## Safety rule

The project may use fictional drugs, altered states, addiction, recovery, ritual, dangerous power, and strange exploration as themes.

Do not provide real-world drug synthesis, preparation, extraction, purification, dosing, or optimization instructions.
