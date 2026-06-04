# AI Agent Workflow

Use this file to keep coding agents predictable.

## Default loop

1. **Orient** with `CODEBASE_MAP.md` and the relevant domain doc.
2. **Search narrowly** with `rg`.
3. **Read small files** around the target symbols.
4. **State the plan** for non-trivial changes.
5. **Edit minimal files**.
6. **Run the narrowest relevant validation**.
7. **Report changed files, commands, validation, risks.**

## Before and during editing

These rules apply to humans and agents.

Before editing:

- Read the `docs/00_README_FOR_AGENTS.md` routing table.
- Read the relevant domain doc.
- Search with `rg` before opening many files.
- Keep the task scoped.

During editing:

- Follow existing package patterns.
- Preserve registry IDs and public data formats.
- Prefer small commits/diffs.
- Add or update tests when behavior is pure/JVM-testable.
- Keep generated resources generated.
- Keep player-facing text localized.

## Good agent tasks

Good:

```text
Fix server validation for `CycleMachineTransferSidePayload`.
Constraints: do not change packet ID or menu public behavior. Add/adjust tests if a safe JVM test exists. Run `compileJava` and the dedicated-server import grep.
```

Bad:

```text
Improve the machine system.
```

## Roles

Use different agent roles mentally, even if one tool performs all steps.

| Role | Allowed actions | Forbidden actions |
|---|---|---|
| Explorer | read/search/map code, identify risks | edit files |
| Implementer | apply agreed narrow diff | broad cleanup, design changes |
| Test agent | add/update focused tests | change behavior to make tests pass unless requested |
| Reviewer | inspect diff for regressions, security, style, docs | rewrite entire solution |
| Migration agent | mechanical call-site migration | opportunistic feature changes |

## Plan-first threshold

Plan before editing when the task touches:

- networking;
- persistence/codecs;
- registries;
- client/server boundaries;
- drug consumption/addiction/recovery;
- datagen or generated resources;
- pipe performance;
- worldgen/dimension behavior;
- more than three production files.

## Scope discipline

One task should have one reviewable theme:

- network hardening;
- event-bus cleanup;
- addiction package split;
- datagen validation;
- client/server safety;
- resource/language audit;
- pipe performance cleanup;
- one machine/recipe feature.

Do not combine unrelated refactors with features.

## Required response after code edits

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

Never claim compile, datagen, build, or in-game success unless the command/test actually ran.

## Pull request checklist

- [ ] Changed files are focused.
- [ ] No unrelated refactors.
- [ ] Commands run are listed.
- [ ] Failures are honestly reported.
- [ ] Dedicated-server safety was checked if relevant.
- [ ] Resource/datagen outputs were reviewed if changed.
- [ ] Guide changes were intentional.
- [ ] Remaining risks/TODOs are explicit.

## When to stop and ask

Ask before:

- adding dependencies;
- changing registry IDs;
- changing saved/network formats;
- changing gameplay balance beyond the task;
- deleting old systems instead of first migrating call sites;
- adding real-world drug instructions;
- disabling validators/tests to make a build pass.
