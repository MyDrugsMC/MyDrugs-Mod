# Contributing

MyDrugs is currently led by Asarix, with help from AI tools such as Codex and Claude. Assets are created by the project contributors, including The_Suicidaul for many textures and 3D models.

## Development expectations

- Follow `VISION.md`, `STORYLINE.md`, and `ROADMAP.md`.
- Use one branch per feature/theme.
- Keep PRs reviewable.
- Run compile checks before asking for review.
- Include manual test notes.
- Update JEI, guide pages, datagen, and lang keys when gameplay changes.
- Do not add new content that pushes the mod toward pure horror, moral panic, or constant endgame chores unless the vision docs are intentionally changed first.

## Code expectations

- Reuse existing systems before adding new ones.
- Keep server authority for gameplay decisions.
- Keep client-only code client-only.
- Avoid performance-heavy scans, per-frame allocations, or packet spam.
- Do not duplicate registries or parallel systems without a strong reason.
- Keep `DrugUseService` as the canonical path for drug consumption, progression, addiction, and custom effect application.
- Keep player-facing text in language files.

## Content safety

The mod can include fictionalized drug effects, addiction, withdrawal, rituals, recovery, psychedelics, ketamine-like endgame integration, and stylized processing, but docs, advancements, JEI text, and guide pages must not explain real-world drug synthesis or provide real-world procedural instructions.

Use wording like:

- ritual mixture;
- refined stimulant route;
- psychotrope research;
- volatile preparation;
- symbolic catalyst;
- gameplay transformation;
- integration window;
- recovery practice;
- somatic adaptation;
- resonance process.

Avoid wording that reads like a real lab procedure.

Do not include:

- real-world synthesis steps;
- dosage instructions;
- medical advice;
- claims that a substance cures addiction or depression;
- glamorized language that implies real-world use is safe.

The intended player-facing stance is:

> This is an 18+ fictional gameplay system inspired by altered states, risk, recovery, and integration. It is not real-world advice.

## Tone expectations

- The mod should not become “drugs are evil” horror.
- Addiction should be readable and recoverable.
- Psychedelics should be framed as insight/integration tools, not magic cures.
- Ketamine-like content should be serious, controlled, and recovery-gated.
- The ending should be positive and freedom-oriented.
- Bad trips and inner entities should be symbolic psychological challenges, not random punishment.

## Assets

Before release, asset provenance matters.

- Keep track of who made each major texture, model, sound, or music file.
- Do not include copyrighted music or sounds without permission.
- Avoid dumping large vanilla asset folders into the mod unless every override is intentional.
- Dimension and recovery assets should lean toward therapeutic surrealism: luminous fungi, soft impossible geometry, sky islands, recovery spaces, body regulation, and beautiful danger rather than gore or demon horror.

## Done means tested

A feature is done only when it passes the relevant checks in `TESTING.md`.
