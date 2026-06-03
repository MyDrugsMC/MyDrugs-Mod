# MyDrugs Docs — Agent-Ready Index

This folder is optimized for AI coding agents and human reviewers. It intentionally favors short, stable contracts over long brainstorming notes.

Project constants:

- Mod ID: `mydrugs`
- Minecraft: `1.21.10`
- NeoForge: `21.10.64`
- Java: `21`
- Build plugin: `net.neoforged.moddev`
- Main package: `org.mydrugs.mydrugs`

## How agents should use these docs

Do **not** read the whole folder by default. Pick the smallest document set for the task.

| Task type | Read first | Then read if needed |
|---|---|---|
| Any code change | `CODEBASE_MAP.md`, `AI_WORKFLOW.md`, `TESTING.md` | `ARCHITECTURE.md` |
| Drug behavior, dose, effects, addiction | `DRUG_SYSTEM.md` | `GAMEPLAY_DESIGN.md`, `RECOVERY_AND_INTEGRATION_DESIGN.md` |
| Client visuals, HUD, screens, shaders | `CLIENT_SERVER_SAFETY.md` | `NETWORKING.md`, `GAMEPLAY_DESIGN.md` |
| Payloads, menus, server requests | `NETWORKING.md` | `CLIENT_SERVER_SAFETY.md`, `TESTING.md` |
| Machines, recipes, pipes, menus | `MACHINES_PIPES_AND_RECIPES.md` | `RESOURCES_AND_DATAGEN.md` |
| JSON, lang, models, loot, tags, generated resources | `RESOURCES_AND_DATAGEN.md` | `GUIDE_AUTHORING.md` |
| Worldgen, Inner dimension, TerraBlender | `WORLDGEN_AND_DIMENSION.md` | `GAMEPLAY_DESIGN.md` |
| Recovery, diary, integration, late-game healing | `RECOVERY_AND_INTEGRATION_DESIGN.md` | `STORYLINE.md`, `VISION.md` |
| New feature planning | `FEATURE_TEMPLATE.md` | Relevant domain doc |
| Project direction | `VISION.md`, `ROADMAP.md`, `BACKLOG.md` | `STORYLINE.md` |
| Content safety | `SAFETY_AND_CONTENT_POLICY.md` | Relevant design doc |

## Hard project rules

1. Preserve dedicated-server safety.
2. Keep gameplay server-authoritative.
3. Keep common/server/client separation strict.
4. Route normal drug consumption through `core/drug/use/DrugUseService`.
5. Keep datagen/resource changes consistent.
6. Use localization for player-facing text.
7. Avoid real-world drug synthesis, preparation, extraction, dosing, or medical instruction.
8. Preserve existing registry IDs and save/network formats unless explicitly approved.
9. Prefer small, reviewable changes over broad rewrites.

## Guide file note

`progression_guide_pages.md` was copied byte-for-byte from the uploaded docs because the guide is user-edited. SHA-256 of the preserved guide content:

```text
503321a8153bb81e09cd75ffaee4239cb753918b024ff3233bed82d20aa5a1e9
```

## Files intentionally removed from the old docs folder

The previous folder contained large one-off notes and reports that are poor default agent context, including balancing dumps, old implementation plans, and narrow feature brainstorms. Their durable decisions were merged into the domain docs here. Raw ideation should live outside startup docs or in an issue tracker.
