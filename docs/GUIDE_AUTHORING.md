# Guide Authoring

The user owns guide content. Agents should only edit guide files when explicitly asked.

## Source and output

Source:

```text
docs/progression_guide_pages.md
```

Runtime output:

```text
src/main/resources/assets/mydrugs/guide/pages.json
```

## Agent rule

Do not rewrite the guide for style during unrelated code work. If a progression/content change needs a guide update, report that need and make the smallest relevant edit only if asked.

## Content style

- Keep it in-world and gameplay-focused.
- Prefer abstraction and fictional mechanics.
- Explain what the player can do, not real-world procedures.
- Use `@item` references only for valid item IDs.
- Avoid medical claims, dosing advice, or real-world drug preparation instructions.
- Keep spoilers controlled; use diary/progression gating where appropriate.

## Validation

After guide source changes:

```bash
./gradlew validateResources
```

If a guide generation script exists, run it before validation and review the generated `pages.json` diff.

## This zip

The `progression_guide_pages.md` file in this docs zip was preserved byte-for-byte from the uploaded docs.
