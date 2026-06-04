#!/usr/bin/env python3
"""
update_project_brain.py

Coordinator for the `mydrugs` Project Brain.

Purpose:
- Run `tools/scan_project.py` to refresh factual scan files.
- Ensure required agent-facing docs exist.
- Create missing docs from safe templates.
- Optionally create/update `scan/validation.md` as a template.
- Print a clear next-step report for LLM or human follow-up.

Important:
- This script does NOT call an LLM.
- This script does NOT run Gradle by default.
- This script does NOT modify source code.
- This script does NOT overwrite existing docs unless `--overwrite-templates` is passed.
- This script should be safe to run frequently.

Usage:
    python tools/update_project_brain.py
    python tools/update_project_brain.py --scan-only
    python tools/update_project_brain.py --check-docs
    python tools/update_project_brain.py --no-scan
    python tools/update_project_brain.py --validation-template
    python tools/update_project_brain.py --overwrite-templates
    python tools/update_project_brain.py --dry-run

Recommended:
    Put this file at `tools/update_project_brain.py`.
    Put `scan_project.py` at `tools/scan_project.py`.
    Run from the repository root.
"""

from __future__ import annotations

import argparse
import datetime as _dt
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable


# ---------------------------------------------------------------------------
# Project Brain target files
# ---------------------------------------------------------------------------

REQUIRED_DOCS = [
    "docs/00_README_FOR_AGENTS.md",
    "docs/CODEBASE_MAP.md",
    "docs/ARCHITECTURE.md",
    "docs/GAME_MOOD_BIBLE.md",
    "docs/GAMEPLAY_CONTRACTS.md",
    "docs/DRUG_SYSTEM.md",
    "docs/ADDICTION_RECOVERY_DIARY.md",
    "docs/CLIENT_SERVER_SAFETY.md",
    "docs/NETWORKING.md",
    "docs/MACHINES_PIPES_AND_RECIPES.md",
    "docs/RESOURCES_AND_DATAGEN.md",
    "docs/WORLDGEN_AND_DIMENSION.md",
    "docs/TESTING.md",
    "docs/AI_WORKFLOW.md",
    "docs/MAINTAINABILITY_AUDIT.md",
    "docs/TECH_DEBT_REGISTER.md",
    "docs/RISK_HOTSPOTS.md",]

OPTIONAL_DOCS = [
    "docs/MOOD_AUDIT.md",
]

PROMPT_DOCS = [
    "docs/prompts/generate_codebase_map.md",
    "docs/prompts/generate_domain_doc.md",
    "docs/prompts/generate_maintainability_audit.md",
    "docs/prompts/generate_mood_audit.md",
    "docs/prompts/update_tech_debt_register.md",
    "docs/prompts/review_docs_for_agents.md",
]

EXPECTED_SCAN_FILES = [
    "scan/tree.txt",
    "scan/java_files.txt",
    "scan/resource_files.txt",
    "scan/generated_resource_files.txt",
    "scan/packages.md",
    "scan/symbols.md",
    "scan/registries.md",
    "scan/network_payloads.md",
    "scan/client_server_violations.md",
    "scan/component_literal_report.md",
    "scan/todos.md",
    "scan/resources.md",
    "scan/generated_vs_authored.md",
    "scan/localization.md",
    "scan/datagen.md",
    "scan/recipes.md",
    "scan/tags.md",
    "scan/loot_tables.md",
    "scan/models_and_blockstates.md",
    "scan/guide_references.md",
    "scan/performance_hotspots.md",
    "scan/risk_hotspots.md",
    "scan/dependencies.md",
    "scan/validation.md",
]

ROOT_FILES = [
    "AGENTS.md",
    "CLAUDE.md",
]


# ---------------------------------------------------------------------------
# Templates
# ---------------------------------------------------------------------------

def template_00_readme() -> str:
    return """# README for Agents

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
"""


def template_codebase_map() -> str:
    return """# Codebase Map

## Project summary

`mydrugs` is a Minecraft NeoForge mod for Minecraft 1.21.10, Java 21.

Design goal: a risk/reward survival mod about altered states, addiction, recovery, ritual crafting, dangerous power, psychotrope energy, machines, mutation, and strange exploration.

## Main entry points

TODO: Fill from `scan/symbols.md`, `scan/packages.md`, and source files.

## Package map

Use:

- `../scan/tree.txt`
- `../scan/packages.md`
- `../scan/java_files.txt`

TODO: Summarize major package areas.

## Major gameplay domains

TODO: Summarize drugs, addiction, recovery, diary, machines, pipes, gas/fluids, mutation, energy, progression, worldgen.

## Client-only systems

TODO: Summarize HUD, screens, shaders, hallucinations, sounds, input distortion, overlays.

## Server/common systems

TODO: Summarize server-authoritative systems and common domain logic.

## Registries

Use:

- `../scan/registries.md`

TODO: Summarize item/block/fluid/effect/menu/block-entity registries.

## Resources

Use:

- `../scan/resources.md`
- `../scan/localization.md`
- `../scan/generated_vs_authored.md`

TODO: Summarize hand-authored vs generated resources.

## Testing and validation

Use:

- `TESTING.md`
- `../scan/validation.md`

## High-risk areas

Use:

- `RISK_HOTSPOTS.md`
- `../scan/risk_hotspots.md`
- `../scan/performance_hotspots.md`

## Where agents should look first

- Overall structure: this file.
- Exact files/classes: `../scan/symbols.md`, `../scan/packages.md`.
- Exact resources: `../scan/resources.md`.
- Design/mood: `GAME_MOOD_BIBLE.md`, `GAMEPLAY_CONTRACTS.md`.
"""


def template_architecture() -> str:
    return """# Architecture

## Architectural principles

- Dedicated-server safety.
- Server-authoritative gameplay.
- Strict common/server/client separation.
- One canonical drug-consumption path.
- Small maintainable domain packages.
- Datagen/resource consistency.
- Localization for player-facing text.
- No real-world drug synthesis, preparation, or dosing instructions.

## Domain boundaries

TODO: Fill from `CODEBASE_MAP.md` and `../scan/packages.md`.

## Common/server/client split

Common/server code must not import client-only classes.

Use:

- `CLIENT_SERVER_SAFETY.md`
- `../scan/client_server_violations.md`

## Canonical consumption path

All drug consumption should go through:

```text
core/drug/use/DrugUseService
```

Item classes may delegate but should not directly apply effects, addiction, tolerance, overdose, dose, item consumption, or knowledge grants.

## Networking architecture

Use:

- `NETWORKING.md`
- `../scan/network_payloads.md`

Server-bound packets are validated requests, not trusted commands.

## Resource/datagen architecture

Use:

- `RESOURCES_AND_DATAGEN.md`
- `../scan/datagen.md`

Do not hand-edit generated JSON owned by datagen.

## Registries and definitions

Prefer small spec records and domain registries over giant static registry sections when adding new groups.

Preserve registry IDs.

## Save data, attachments, codecs

TODO: Fill from source and `../scan/risk_hotspots.md`.

## Extension patterns

TODO: Describe safe ways to add a drug, machine, payload, resource, or effect.

## Anti-patterns

- Parallel drug-consumption systems.
- Client logic in common/server packages.
- Unvalidated server-bound packets.
- Hand-edited generated resources.
- Player-facing `Component.literal` where localization is needed.
- Large unrelated refactors mixed with features.

## When to refactor

Prefer small migration passes:

1. isolate;
2. document;
3. migrate call sites;
4. compile;
5. remove dead code.
"""


def template_game_mood_bible() -> str:
    return """# Game Mood Bible

## Core fantasy

`mydrugs` is a survival risk/reward mod about dangerous power, altered states, addiction, recovery, ritual crafting, psychotrope energy, machines, mutation, and strange exploration.

## Emotional palette

- Temptation.
- Unease.
- Power with consequences.
- Ritual certainty.
- Industrial occultism.
- Fragile recovery.
- Strange discovery.
- Survival pressure.

## What the mod should feel like

- Powerful but dangerous.
- Surreal but mechanically readable.
- Occult-industrial rather than generic tech.
- Uncomfortable without becoming exploitative.
- Symbolic and fictionalized rather than procedural or realistic.

## What the mod should never feel like

- A meme drug mod.
- A real-world drug tutorial.
- A generic tech mod.
- A pure power fantasy without consequence.
- A punishment simulator without agency.
- A medical simulator.

## Power fantasy

Power should feel tempting, useful, and risky.

## Horror fantasy

Horror should come from instability, mutation, perception shifts, dependence, withdrawal, and the feeling that power has a cost.

## Recovery fantasy

Recovery should feel like a meaningful arc, not a simple stat reset.

## Ritual fantasy

Rituals should feel symbolic, strange, and gameplay-focused. They must not provide real-world procedural chemistry or drug preparation.

## Machine fantasy

Machines should feel dangerous, occult-industrial, and connected to the mod identity.

## Exploration fantasy

Exploration should feel strange, risky, and rewarding.

## Drug identity map

- Coffee: work, energy, early productivity.
- Tobacco: focus, precision, ritual steadiness.
- Cannabis: calm, stability, lowered threat perception.
- Stimulants/cocaine: overclock, dash, adrenaline.
- Crack: short burst, high risk.
- Meth: late-game overclock.
- Psychedelics: altered perception, ritual certainty.
- Alcohol: courage, resistance, chaos.
- Opioids: deferred until core loop is stable.

## Feedback language

Effects should be readable through HUD, GUI, tooltip, sound, overlay, particles, guide text, or JEI.

## Humor boundaries

Humor may exist, but addiction, withdrawal, overdose-like risk, and recovery should not be treated as throwaway jokes.

## Realism boundaries

Use abstraction and fictionalization. Avoid real-world synthesis, preparation, extraction, purification, dosing, or optimization.

## Accessibility boundaries

Respect client accessibility toggles such as reduced motion. Reduced motion should reduce aggressive visual motion without disabling gameplay.

## Examples of good player-facing text

TODO: Add project-specific examples.

## Examples of bad player-facing text

TODO: Add examples that are too realistic, too meme-like, or too generic.
"""


def template_gameplay_contracts() -> str:
    return """# Gameplay Contracts

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
"""


def template_domain(title: str, purpose: str, related_scans: list[str]) -> str:
    scans = "\n".join(f"- `{p}`" for p in related_scans)
    return f"""# {title}

## Purpose

{purpose}

## Main packages

TODO: Fill from `../scan/packages.md`.

## Main classes

TODO: Fill from `../scan/symbols.md`.

## Data flow

TODO: Describe the flow using source-confirmed details.

## Invariants

TODO: List rules that must not be broken.

## Extension points

TODO: Describe safe ways to add behavior in this domain.

## Common mistakes

TODO: List common agent mistakes for this domain.

## Validation

TODO: List narrow validation commands and manual checks.

## Related scan files

{scans if scans else "- TODO"}

## Open questions

- TODO
"""


def template_testing() -> str:
    return """# Testing

## Fast checks

```bash
./gradlew compileJava
```

## Datagen checks

Run after changing recipes, loot tables, tags, generated models, blockstates, advancements, generated language, or generated guide output.

```bash
./gradlew runData
```

## Full build

```bash
./gradlew build
```

## Dedicated-server safety

```bash
rg -n "import net\\.minecraft\\.client|import net\\.neoforged\\.neoforge\\.client|import org\\.mydrugs\\.mydrugs\\.client" src/main/java/org/mydrugs/mydrugs --glob '!client/**'
```

## Manual in-game checks

TODO: Add domain-specific manual checks.

## Domain-specific checks

TODO: Fill as systems stabilize.

## How to report validation

After code changes, report:

```text
Changed files:
- ...

Commands run:
- ...

Validation result:
- compileJava: pass/fail/not run with reason
- runData: pass/fail/not run with reason
- build: pass/fail/not run with reason

Dedicated-server safety:
- checked/not checked

Risks / TODOs:
- ...
```

## Known limitations

Do not claim compile, datagen, build, or in-game success unless the command/check was actually run.
"""


def template_ai_workflow() -> str:
    return """# AI Workflow

## Default agent workflow

```text
Investigate → Plan → Edit small slice → Validate → Summarize risks
```

## Read-only investigation

Use focused search and small file reads before editing.

Do not read every doc by default. Use `00_README_FOR_AGENTS.md` to route.

## Planning before edits

For non-trivial tasks, state:

- files likely to change;
- docs/scan files consulted;
- validation planned;
- risks or uncertainties.

## Editing rules

- Keep diffs small.
- Do not mix unrelated refactors with features.
- Preserve gameplay unless asked to change it.
- Preserve registry IDs and persistent formats.
- Prefer existing project patterns.

## Validation rules

Use `TESTING.md`.

Never claim success unless commands were actually run.

## Review rules

Review for:

- server safety;
- architecture boundaries;
- packet validation;
- datagen/resource drift;
- localization;
- performance;
- mood consistency;
- content safety.

## How to update docs

Update docs when architecture, domain behavior, workflow, or design contracts change.

## How to update scan

Run:

```bash
python tools/update_project_brain.py
```

## Forbidden behaviors

- Real-world drug synthesis, preparation, extraction, purification, dosing, or optimization instructions.
- Broad rewrites without a plan.
- Trusting client packets as commands.
- Hand-editing generated JSON owned by datagen.
- Claiming validation that was not run.

## Good prompt examples

TODO: Add examples.

## Bad prompt examples

TODO: Add examples.
"""


def template_maintainability_audit() -> str:
    return """# Maintainability Audit

## Executive summary

TODO: Generate using `../scan/*`, domain docs, and source review.

## Priority 0 — correctness and server safety

TODO

## Priority 1 — architecture boundaries

TODO

## Priority 2 — duplicated or parallel systems

TODO

## Priority 3 — registry/resource/datagen complexity

TODO

## Priority 4 — package clarity

TODO

## Priority 5 — performance risks

TODO

## Priority 6 — mood/design consistency

TODO

## Recommended refactor sequence

TODO

## Do-not-touch-without-tests areas

TODO

## Open questions

TODO

## Issue template

```md
## Issue: ...

Priority:
Evidence:
Files:
Risk:
Recommended fix:
Safe first step:
Validation:
```
"""


def template_tech_debt_register() -> str:
    return """# Technical Debt Register

| ID | Priority | Area | Problem | Safe first step | Validation | Status |
|---|---:|---|---|---|---|---|
| TD-001 | P0 | TODO | TODO | TODO | TODO | open |

## Status values

- `open`
- `in-progress`
- `blocked`
- `done`
- `obsolete`
"""


def template_risk_hotspots() -> str:
    return """# Risk Hotspots

This file summarizes areas agents should treat carefully.

Use generated candidates from:

- `../scan/risk_hotspots.md`
- `../scan/performance_hotspots.md`
- `../scan/network_payloads.md`
- `../scan/client_server_violations.md`
- `../scan/component_literal_report.md`

## Server safety

TODO

## Networking

TODO

## Persistence/codecs

TODO

## Save data/attachments

TODO

## Per-tick/per-frame performance

TODO

## Registries

TODO

## Datagen/resources

TODO

## Mood/content safety

TODO

## High-risk files

TODO
"""


def template_safety_policy() -> str:
    return """# Safety and Content Policy

## Project theme

`mydrugs` uses fictionalized drugs, altered states, addiction, recovery, ritual, dangerous power, mutation, and strange exploration as game themes.

## Allowed content

- Fictional mechanics.
- Abstract risk/reward systems.
- Status effects.
- Symbolic ritual crafting.
- Recovery systems.
- Player-facing fantasy language.
- Fictional machines and psychotrope energy.

## Disallowed content

Do not include real-world:

- drug synthesis instructions;
- preparation instructions;
- extraction or purification steps;
- dosing advice;
- optimization of intoxication;
- procedural chemistry;
- sourcing instructions.

## How to write abstract drug mechanics

Use symbolic, fictional, or gameplay-focused descriptions.

Prefer:

- instability;
- focus;
- overclock;
- calm;
- altered perception;
- withdrawal pressure;
- recovery progress.

Avoid procedural real-world descriptions.

## How to write guide text safely

Guide text should explain gameplay, not real-world chemistry or drug use.

## How to write recipes safely

Recipes should be fictionalized Minecraft mechanics and must not map to real-world preparation procedures.

## How to review risky content

Flag anything that sounds like a real-world instruction, recipe, procedure, dose, or optimization path.

## Examples

TODO: Add safe and unsafe examples from project context.
"""


def template_prompt_generate_codebase_map() -> str:
    return """# Prompt: Generate Codebase Map

You are documenting the `mydrugs` NeoForge mod for future LLM agents.

Read:

- `AGENTS.md`
- `docs/00_README_FOR_AGENTS.md`
- `scan/tree.txt`
- `scan/packages.md`
- `scan/registries.md`
- `scan/resources.md`
- `scan/network_payloads.md`

Generate or update:

- `docs/CODEBASE_MAP.md`

Rules:

- Do not invent systems.
- Distinguish confirmed facts from inferred structure.
- Keep this high-level.
- Link to scan files for exhaustive detail.
- Preserve project safety rules.
"""


def template_prompt_generate_domain_doc() -> str:
    return """# Prompt: Generate Domain Doc

You are documenting one domain of the `mydrugs` NeoForge mod.

Domain:

```text
<DOMAIN>
```

Read:

- `AGENTS.md`
- `docs/ARCHITECTURE.md`
- `docs/GAME_MOOD_BIBLE.md` if gameplay-facing
- relevant scan files
- relevant source files

Generate:

```text
docs/<DOMAIN_DOC>.md
```

Use this structure:

1. Purpose
2. Main packages
3. Main classes
4. Data flow
5. Invariants
6. Extension points
7. Common mistakes
8. Validation
9. Related scan files
10. Open questions

Rules:

- Do not rewrite code.
- Do not invent behavior.
- Mark uncertainty explicitly.
- Preserve project safety rules.
"""


def template_prompt_generate_maintainability_audit() -> str:
    return """# Prompt: Generate Maintainability Audit

You are auditing maintainability for `mydrugs`.

Read:

- `AGENTS.md`
- `docs/CODEBASE_MAP.md`
- `docs/ARCHITECTURE.md`
- `docs/GAME_MOOD_BIBLE.md`
- `docs/GAMEPLAY_CONTRACTS.md`
- all relevant scan files

Generate or update:

- `docs/MAINTAINABILITY_AUDIT.md`
- `docs/TECH_DEBT_REGISTER.md`
- `docs/RISK_HOTSPOTS.md`

For each issue include:

```text
Problem:
Evidence:
Files:
Risk:
Recommended fix:
Safe first step:
Validation:
```

Rules:

- Prioritize small safe passes over large rewrites.
- Do not propose gameplay rebalance unless clearly marked as design recommendation.
- Preserve project safety rules.
"""


def template_prompt_generate_mood_audit() -> str:
    return """# Prompt: Generate Mood Audit

You are reviewing whether the codebase and docs preserve the intended mood of `mydrugs`.

Read:

- `docs/GAME_MOOD_BIBLE.md`
- `docs/GAMEPLAY_CONTRACTS.md`
- `docs/DRUG_SYSTEM.md`
- `docs/MACHINES_PIPES_AND_RECIPES.md`
- `docs/RESOURCES_AND_DATAGEN.md`
- relevant source/resource scans

Generate:

- `docs/MOOD_AUDIT.md` if desired, or update the mood section in `docs/MAINTAINABILITY_AUDIT.md`.

Classify issues as:

- mood mismatch;
- missing feedback;
- too generic;
- too realistic/procedural;
- too punishing;
- too consequence-free;
- unclear fantasy.

For each issue include:

```text
Evidence:
Player-facing effect:
Recommended fix:
Safety notes:
```
"""


def template_prompt_update_tech_debt_register() -> str:
    return """# Prompt: Update Tech Debt Register

You are updating `docs/TECH_DEBT_REGISTER.md`.

Read:

- `docs/MAINTAINABILITY_AUDIT.md`
- `docs/RISK_HOTSPOTS.md`
- relevant `scan/*` files
- recently changed source files, if any

Rules:

- Keep each debt item actionable.
- Prefer safe first steps.
- Do not add vague items.
- Do not mark items done unless evidence confirms they are done.
- Preserve IDs where possible.
"""


def template_prompt_review_docs_for_agents() -> str:
    return """# Prompt: Review Docs for Agents

You are reviewing the Project Brain docs for usefulness to future LLM agents.

Read:

- `AGENTS.md`
- `CLAUDE.md`
- `docs/00_README_FOR_AGENTS.md`
- all docs in `docs/`
- representative scan files

Check:

- Is the routing clear?
- Are docs too long?
- Are docs duplicating exhaustive scan details?
- Are any claims unsupported by source or scan?
- Are there stale or contradictory instructions?
- Are safety rules clear?
- Are maintainability recommendations actionable?

Output:

- concrete edits;
- files to change;
- stale sections;
- missing docs;
- risky ambiguity.
"""


TEMPLATES = {
    "docs/00_README_FOR_AGENTS.md": template_00_readme,
    "docs/CODEBASE_MAP.md": template_codebase_map,
    "docs/ARCHITECTURE.md": template_architecture,
    "docs/GAME_MOOD_BIBLE.md": template_game_mood_bible,
    "docs/GAMEPLAY_CONTRACTS.md": template_gameplay_contracts,
    "docs/DRUG_SYSTEM.md": lambda: template_domain(
        "Drug System",
        "Explain canonical drug consumption, dose/runtime effects, drug identities, item delegation, feedback, and extension rules.",
        ["../scan/packages.md", "../scan/symbols.md", "../scan/registries.md", "../scan/component_literal_report.md"],
    ),
    "docs/ADDICTION_RECOVERY_DIARY.md": lambda: template_domain(
        "Addiction, Recovery, and Diary",
        "Explain addiction, tolerance, withdrawal, recovery, and diary as related but separate domains.",
        ["../scan/packages.md", "../scan/symbols.md", "../scan/risk_hotspots.md"],
    ),
    "docs/CLIENT_SERVER_SAFETY.md": lambda: template_domain(
        "Client/Server Safety",
        "Explain dedicated-server safety, client-only boundaries, forbidden imports, and event subscriber rules.",
        ["../scan/client_server_violations.md", "../scan/packages.md"],
    ),
    "docs/NETWORKING.md": lambda: template_domain(
        "Networking",
        "Explain payload registration, server-bound request validation, client-bound presentation, codecs, and permission gates.",
        ["../scan/network_payloads.md", "../scan/risk_hotspots.md"],
    ),
    "docs/MACHINES_PIPES_AND_RECIPES.md": lambda: template_domain(
        "Machines, Pipes, and Recipes",
        "Explain machine logic, pipe transfer, recipes, menus/screens separation, and performance risks.",
        ["../scan/packages.md", "../scan/recipes.md", "../scan/performance_hotspots.md"],
    ),
    "docs/RESOURCES_AND_DATAGEN.md": lambda: template_domain(
        "Resources and Datagen",
        "Explain generated vs hand-authored resources, localization, models, blockstates, loot tables, tags, recipes, and runData expectations.",
        [
            "../scan/resources.md",
            "../scan/generated_vs_authored.md",
            "../scan/localization.md",
            "../scan/datagen.md",
            "../scan/models_and_blockstates.md",
            "../scan/tags.md",
            "../scan/loot_tables.md",
        ],
    ),
    "docs/WORLDGEN_AND_DIMENSION.md": lambda: template_domain(
        "Worldgen and Dimension",
        "Explain worldgen, dimension, biome/feature placement if present, and generation risks.",
        ["../scan/packages.md", "../scan/resources.md", "../scan/risk_hotspots.md"],
    ),
    "docs/TESTING.md": template_testing,
    "docs/AI_WORKFLOW.md": template_ai_workflow,
    "docs/MAINTAINABILITY_AUDIT.md": template_maintainability_audit,
    "docs/TECH_DEBT_REGISTER.md": template_tech_debt_register,
    "docs/RISK_HOTSPOTS.md": template_risk_hotspots,
    "docs/prompts/generate_codebase_map.md": template_prompt_generate_codebase_map,
    "docs/prompts/generate_domain_doc.md": template_prompt_generate_domain_doc,
    "docs/prompts/generate_maintainability_audit.md": template_prompt_generate_maintainability_audit,
    "docs/prompts/generate_mood_audit.md": template_prompt_generate_mood_audit,
    "docs/prompts/update_tech_debt_register.md": template_prompt_update_tech_debt_register,
    "docs/prompts/review_docs_for_agents.md": template_prompt_review_docs_for_agents,
}


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

@dataclass
class ActionReport:
    scanned: bool
    scan_exit_code: int | None
    created: list[str]
    skipped_existing: list[str]
    missing_required_docs: list[str]
    missing_scan_files: list[str]
    missing_root_files: list[str]
    warnings: list[str]


def today() -> str:
    return _dt.date.today().isoformat()


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8", errors="replace")


def write_text(path: Path, text: str, dry_run: bool) -> None:
    if dry_run:
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text.rstrip() + "\n", encoding="utf-8")


def file_is_empty_or_template(path: Path) -> bool:
    if not path.exists():
        return False
    text = read_text(path)
    return "TODO" in text and len(text.strip()) < 5000


def create_from_template(
    root: Path,
    rel_path: str,
    *,
    overwrite: bool,
    dry_run: bool,
) -> tuple[bool, str]:
    path = root / rel_path
    template_fn = TEMPLATES.get(rel_path)

    if template_fn is None:
        return False, f"No template registered for {rel_path}"

    if path.exists() and not overwrite:
        return False, "exists"

    write_text(path, template_fn(), dry_run=dry_run)
    return True, "created" if not path.exists() or not dry_run else "would create"


def validation_template() -> str:
    return f"""# Validation

Last updated: {today()}

This file records actual validation commands.

`update_project_brain.py` does not run Gradle by default and does not claim validation success.

## Commands

| Command | Status | Notes |
|---|---:|---|
| `./gradlew compileJava` | not run | |
| `./gradlew runData` | not run | |
| `./gradlew build` | not run | |
| dedicated-server import scan | not run | |

## Dedicated-server safety command

```bash
rg -n "import net\\.minecraft\\.client|import net\\.neoforged\\.neoforge\\.client|import org\\.mydrugs\\.mydrugs\\.client" src/main/java/org/mydrugs/mydrugs --glob '!client/**'
```

## Notes

- Update this file manually after actually running commands.
- Do not claim compile, datagen, build, or in-game success unless the check was actually run.
"""


def ensure_validation_template(root: Path, *, overwrite: bool, dry_run: bool) -> bool:
    path = root / "scan/validation.md"
    if path.exists() and not overwrite:
        return False
    write_text(path, validation_template(), dry_run=dry_run)
    return True


def run_scan_project(root: Path, dry_run: bool, quiet: bool) -> int:
    script = root / "tools/scan_project.py"

    if not script.exists():
        print("WARNING: `tools/scan_project.py` not found. Scan step skipped.", file=sys.stderr)
        return 127

    if dry_run:
        if not quiet:
            print(f"[dry-run] Would run: {sys.executable} {script.relative_to(root)} --root {root}")
        return 0

    cmd = [sys.executable, str(script), "--root", str(root)]
    if quiet:
        cmd.append("--quiet")

    result = subprocess.run(cmd, cwd=root)
    return result.returncode


def check_missing(root: Path, paths: Iterable[str]) -> list[str]:
    return [p for p in paths if not (root / p).exists()]


def check_progression_guide(root: Path) -> str | None:
    path = root / "docs/progression_guide_pages.md"
    if not path.exists():
        return "`docs/progression_guide_pages.md` is missing. If you intentionally keep the guide elsewhere, update docs routing."
    return None


def print_section(title: str, items: list[str], *, empty: str = "none") -> None:
    print(f"\n{title}")
    if items:
        for item in items:
            print(f"- {item}")
    else:
        print(f"- {empty}")


def build_report(
    root: Path,
    *,
    run_scan: bool,
    create_missing: bool,
    create_prompts: bool,
    overwrite_templates: bool,
    validation_template_flag: bool,
    dry_run: bool,
    quiet: bool,
) -> ActionReport:
    created: list[str] = []
    skipped_existing: list[str] = []
    warnings: list[str] = []
    scanned = False
    scan_exit_code: int | None = None

    guide_warning = check_progression_guide(root)
    if guide_warning:
        warnings.append(guide_warning)

    if run_scan:
        scanned = True
        scan_exit_code = run_scan_project(root, dry_run=dry_run, quiet=quiet)
        if scan_exit_code != 0:
            warnings.append(f"`tools/scan_project.py` exited with code {scan_exit_code}.")

    if validation_template_flag:
        did_write = ensure_validation_template(
            root,
            overwrite=overwrite_templates,
            dry_run=dry_run,
        )
        if did_write:
            created.append("scan/validation.md")
        else:
            skipped_existing.append("scan/validation.md")

    if create_missing:
        targets = list(REQUIRED_DOCS)
        if create_prompts:
            targets += PROMPT_DOCS

        for rel_path in targets:
            path = root / rel_path
            if path.exists() and not overwrite_templates:
                skipped_existing.append(rel_path)
                continue

            ok, status = create_from_template(
                root,
                rel_path,
                overwrite=overwrite_templates,
                dry_run=dry_run,
            )
            if ok:
                created.append(rel_path)
            elif status == "exists":
                skipped_existing.append(rel_path)
            else:
                warnings.append(status)

    missing_required_docs = check_missing(root, REQUIRED_DOCS)
    missing_scan_files = check_missing(root, EXPECTED_SCAN_FILES)
    missing_root_files = check_missing(root, ROOT_FILES)

    return ActionReport(
        scanned=scanned,
        scan_exit_code=scan_exit_code,
        created=created,
        skipped_existing=skipped_existing,
        missing_required_docs=missing_required_docs,
        missing_scan_files=missing_scan_files,
        missing_root_files=missing_root_files,
        warnings=warnings,
    )


def print_report(report: ActionReport, *, scan_only: bool, check_docs: bool) -> None:
    print("\nProject Brain update report")

    print("\nScan")
    if report.scanned:
        print(f"- scan_project.py exit code: {report.scan_exit_code}")
    else:
        print("- scan not run")

    print_section("Created / updated from templates", report.created)
    print_section("Skipped because existing", report.skipped_existing)

    if not scan_only:
        print_section("Missing required docs", report.missing_required_docs)
        print_section("Missing root instruction files", report.missing_root_files)
    else:
        print("\nDoc checks skipped because --scan-only was used.")

    print_section("Missing expected scan files", report.missing_scan_files)

    if report.warnings:
        print_section("Warnings", report.warnings)

    print("\nRecommended next steps")
    next_steps: list[str] = []

    if report.missing_root_files:
        next_steps.append("Create or update `AGENTS.md` and `CLAUDE.md` so agents know how to route into docs/ and scan/.")
    if report.missing_required_docs:
        next_steps.append("Review missing docs, then rerun with default settings to create templates or write them manually.")
    if report.missing_scan_files:
        next_steps.append("Ensure `tools/scan_project.py` exists and rerun `python tools/update_project_brain.py`.")
    if not report.missing_scan_files:
        next_steps.append("Ask an LLM to update `docs/CODEBASE_MAP.md` using `scan/tree.txt`, `scan/packages.md`, and `scan/registries.md`.")
        next_steps.append("Ask an LLM to update domain docs using the matching `scan/*` files and source slices.")
        next_steps.append("Ask an LLM to update `MAINTAINABILITY_AUDIT.md`, `TECH_DEBT_REGISTER.md`, and `RISK_HOTSPOTS.md` using scan evidence.")
    next_steps.append("Run Gradle validation manually when code/resources change and record results in `scan/validation.md`.")

    for i, step in enumerate(next_steps, start=1):
        print(f"{i}. {step}")

    if check_docs:
        print("\nCheck-docs mode complete.")


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Coordinate Project Brain scans, doc templates, and next-step reporting."
    )

    parser.add_argument(
        "--root",
        default=".",
        help="Repository root. Default: current directory.",
    )
    parser.add_argument(
        "--scan-only",
        action="store_true",
        help="Only run scan_project.py and report scan status. Do not create doc templates.",
    )
    parser.add_argument(
        "--no-scan",
        action="store_true",
        help="Do not run scan_project.py.",
    )
    parser.add_argument(
        "--check-docs",
        action="store_true",
        help="Only check required docs/scan files. Implies --no-scan and does not create templates.",
    )
    parser.add_argument(
        "--prompts",
        action="store_true",
        help="Also create missing docs/prompts/*.md prompt templates.",
    )
    parser.add_argument(
        "--validation-template",
        action="store_true",
        help="Create scan/validation.md template if missing.",
    )
    parser.add_argument(
        "--overwrite-templates",
        action="store_true",
        help="Overwrite existing template docs. Use carefully.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print what would happen without writing files or running scan_project.py.",
    )
    parser.add_argument(
        "--quiet",
        action="store_true",
        help="Pass --quiet to scan_project.py and reduce scan output.",
    )

    return parser.parse_args(argv)


def main(argv: list[str]) -> int:
    args = parse_args(argv)

    root = Path(args.root).resolve()
    if not root.exists():
        print(f"ERROR: root does not exist: {root}", file=sys.stderr)
        return 2

    if args.check_docs:
        run_scan = False
        create_missing = False
    else:
        run_scan = not args.no_scan
        create_missing = not args.scan_only

    report = build_report(
        root,
        run_scan=run_scan,
        create_missing=create_missing,
        create_prompts=args.prompts,
        overwrite_templates=args.overwrite_templates,
        validation_template_flag=args.validation_template,
        dry_run=args.dry_run,
        quiet=args.quiet,
    )

    print_report(report, scan_only=args.scan_only, check_docs=args.check_docs)

    # Non-zero only for operational failure, not for missing docs.
    if report.scan_exit_code not in (None, 0):
        return report.scan_exit_code
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
