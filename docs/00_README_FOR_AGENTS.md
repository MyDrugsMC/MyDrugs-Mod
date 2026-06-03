# README for Agents

This folder is the curated Project Brain for `mydrugs`.

Use this file as the router after reading `AGENTS.md`.

## First files to read

For most tasks:

1. `AGENTS.md`
2. `CLAUDE.md` if using Claude Code
3. `docs/00_README_FOR_AGENTS.md`
4. `docs/CODEBASE_MAP.md`

## Choose docs by task

| Task | Read |
|---|---|
| Overall architecture | `CODEBASE_MAP.md`, `ARCHITECTURE.md` |
| Mood/design | `GAME_MOOD_BIBLE.md`, `GAMEPLAY_CONTRACTS.md` |
| Drug mechanics | `DRUG_SYSTEM.md` |
| Addiction/recovery/diary | `ADDICTION_RECOVERY_DIARY.md` |
| Networking | `NETWORKING.md`, `../scan/network_payloads.md` |
| Client/server safety | `CLIENT_SERVER_SAFETY.md`, `../scan/client_server_violations.md` |
| Machines/pipes/recipes | `MACHINES_PIPES_AND_RECIPES.md` |
| Resources/datagen/localization | `RESOURCES_AND_DATAGEN.md`, relevant `../scan/*` files |
| Worldgen/dimension | `WORLDGEN_AND_DIMENSION.md` |
| Testing | `TESTING.md`, `../scan/validation.md` |
| Maintainability | `MAINTAINABILITY_AUDIT.md`, `TECH_DEBT_REGISTER.md`, `RISK_HOTSPOTS.md` |
| Safety/content policy | `SAFETY_AND_CONTENT_POLICY.md` |

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
