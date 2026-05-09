# AI Workflow

Use AI as an implementation assistant, not as the project manager. The project direction comes from `VISION.md`, `ROADMAP.md`, and feature issues.

## Basic rules

1. One feature/theme = one branch.
2. One prompt = one PR whenever possible.
3. Do not mix refactor + feature unless the feature requires it.
4. Every PR must compile.
5. Every PR must include manual test notes.
6. Large features must be split into passes.
7. Never let AI delete systems without explaining why.
8. After every AI change, inspect:
   - registries;
   - client/server separation;
   - datagen;
   - resources;
   - networking;
   - performance;
   - dedicated-server safety.

## Branch naming

Use clear names:

```text
feature/coffee-machine-speed
feature/tobacco-precision
fix/shader-source-channels
refactor/manual-machine-speed-helper
docs/project-roadmap
```

## PR size rule

A PR is too big if you cannot summarize it in one sentence.

Good:

```text
Add shared manual machine speed modifiers and coffee GUI feedback.
```

Bad:

```text
Rebalance drugs, add aloe vera, add crack effects, fix HUD, change datagen, update Psy Mixer.
```

## Prompt structure for Codex/Claude

Use this format:

```text
Context:
- What project this is.
- Existing systems to reuse.
- What must not be changed.

Goal:
- One clear feature or bugfix.

Implementation requirements:
- Concrete behavior.
- Relevant classes.
- Client/server rules.
- Datagen/resource requirements.

Acceptance checklist:
- Compile.
- In-game behavior.
- Dedicated server.
- No missing assets.
```

## Checkpoints vs PRs

Commits can be messy checkpoints while working. PRs should be clean decisions.

```text
Commit = save point
PR = reviewable feature
Merge = official project state
```

## When to stop AI work

Stop and inspect manually if:

- the AI edits many unrelated packages;
- it duplicates existing systems;
- it touches generated files without updating datagen;
- it references client classes from common/server code;
- it adds real-world synthesis details;
- it claims a system works without a compile/test result.
