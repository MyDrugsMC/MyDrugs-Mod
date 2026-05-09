# Contributing

MyDrugs is currently led by Asarix, with help from AI tools such as Codex and Claude. Assets are created by the project contributors, including The_Suicidaul for many textures and 3D models.

## Development expectations

- Follow `VISION.md` and `ROADMAP.md`.
- Use one branch per feature/theme.
- Keep PRs reviewable.
- Run compile checks before asking for review.
- Include manual test notes.
- Update JEI, guide pages, datagen, and lang keys when gameplay changes.

## Code expectations

- Reuse existing systems before adding new ones.
- Keep server authority for gameplay decisions.
- Keep client-only code client-only.
- Avoid performance-heavy scans, per-frame allocations, or packet spam.
- Do not duplicate registries or parallel systems without a strong reason.

## Content safety

The mod can include fictionalized drug effects, addiction, withdrawal, rituals, and stylized processing, but docs, advancements, JEI text, and guide pages must not explain real-world drug synthesis or provide real-world procedural instructions.

Use wording like:

- ritual mixture;
- refined stimulant route;
- psychotrope research;
- volatile preparation;
- symbolic catalyst;
- gameplay transformation.

Avoid wording that reads like a real lab procedure.

## Assets

Before release, asset provenance matters.

- Keep track of who made each major texture, model, sound, or music file.
- Do not include copyrighted music or sounds without permission.
- Avoid dumping large vanilla asset folders into the mod unless every override is intentional.

## Done means tested

A feature is done only when it passes the relevant checks in `TESTING.md`.
