# Feature Template

Use this before asking an agent to build a non-trivial feature.

## Title

`<short feature name>`

## Goal

What player problem or project problem does this solve?

## Player-facing behavior

- What does the player see?
- What does the player do?
- What feedback confirms success/failure?
- What risks or costs exist?

## Technical scope

Likely files/packages:

- `...`

Expected new/changed systems:

- registry: yes/no
- data component: yes/no
- network payload: yes/no
- saved data/attachment: yes/no
- recipe/datagen: yes/no
- lang/resource changes: yes/no
- client rendering/screen: yes/no

## Constraints

- Preserve registry IDs unless approved.
- Keep server authoritative.
- Keep client code under `client/*`.
- Use `Component.translatable` for player-facing text.
- Avoid real-world procedural drug/medical instruction.

## Acceptance criteria

- [ ] Behavior works on server authority.
- [ ] UI/tooltip/guide feedback is readable.
- [ ] Relevant validation commands pass or failures are documented.
- [ ] Dedicated-server safety checked if side-sensitive.
- [ ] Risks/TODOs are listed.

## Validation plan

```bash
./gradlew compileJava
./gradlew test
./gradlew validateCodeContracts
./gradlew validateResources
./gradlew runData
./gradlew build
```

List only the commands that are actually relevant and run.
