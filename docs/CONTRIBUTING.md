# Contributing Rules

These rules apply to humans and agents.

## Before editing

- Read `README.md` routing table.
- Read the relevant domain doc.
- Search with `rg` before opening many files.
- Keep the task scoped.

## During editing

- Follow existing package patterns.
- Preserve registry IDs and public data formats.
- Prefer small commits/diffs.
- Add or update tests when behavior is pure/JVM-testable.
- Keep generated resources generated.
- Keep player-facing text localized.

## Safety and content

- Do not add real-world drug synthesis, preparation, extraction, purification, dosing, or medical advice.
- Keep substance systems fictionalized and gameplay-oriented.
- Do not make recovery claims that sound like real treatment advice.

## Pull request checklist

- [ ] Changed files are focused.
- [ ] No unrelated refactors.
- [ ] Commands run are listed.
- [ ] Failures are honestly reported.
- [ ] Dedicated-server safety was checked if relevant.
- [ ] Resource/datagen outputs were reviewed if changed.
- [ ] Guide changes were intentional.
- [ ] Remaining risks/TODOs are explicit.
