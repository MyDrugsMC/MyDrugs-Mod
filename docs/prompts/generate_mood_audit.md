# Prompt: Generate Mood Audit

You are reviewing whether the codebase and docs preserve the intended mood of `mydrugs`.

Read:

- `docs/VISION.md`
- `docs/GAMEPLAY_DESIGN.md`
- `docs/DRUG_SYSTEM.md`
- `docs/MACHINES_PIPES_AND_RECIPES.md`
- `docs/RESOURCES_AND_DATAGEN.md`
- relevant source/resource scans

Generate:

- `docs/audits/MOOD_AUDIT.md` if desired, or update the mood section in `docs/audits/MAINTAINABILITY_AUDIT.md`.

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
