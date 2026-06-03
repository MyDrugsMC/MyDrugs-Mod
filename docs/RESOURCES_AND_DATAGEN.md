# Resources, Datagen, Localization, and Assets

Resources are part of the architecture. Treat generated resources as outputs, not hand-authored design.

## Resource roots

- Hand-authored resources: `src/main/resources`
- Generated resources: `src/generated/resources`
- Generated cache: `src/generated/resources/.cache`
- Mod metadata templates: `src/main/templates`
- Guide source: `docs/progression_guide_pages.md`

## Datagen rule

If changing recipes, loot tables, tags, generated models, blockstates, advancements, or generated snapshots:

1. Change the provider in `src/main/java/org/mydrugs/mydrugs/datagen`.
2. Run `./gradlew runData`.
3. Review generated output.
4. Run `./gradlew validateResources` when resources are touched.

Do not hand-edit generated JSON owned by datagen.

## Localization

Every player-facing item, block, menu, message, guide entry, tooltip, status, and GUI label needs localization.

Use:

```java
Component.translatable("...")
```

Use `Component.literal` only for debug text or dynamic numeric output that has no stable lang key.

Common key families:

- `item.mydrugs.*`
- `block.mydrugs.*`
- `container.mydrugs.*`
- `gui.mydrugs.*`
- `tooltip.mydrugs.*`
- `status.mydrugs.*`
- `machine_status.mydrugs.*`

## JSON rules

- UTF-8 without BOM.
- No backup/checkpoint files in repo.
- Do not commit `src/generated/resources/.cache` unless intentionally tracked.
- Keep generated and hand-authored resources separated.

## Guide sync

`docs/progression_guide_pages.md` is the source for:

```text
src/main/resources/assets/mydrugs/guide/pages.json
```

When progression changes:

1. Update `progression_guide_pages.md` if the user wants guide changes.
2. Regenerate guide output if a script exists.
3. Validate all `@item` IDs.
4. Keep guide text gameplay-focused and abstract.
5. Do not include real-world procedural chemistry or dosing instructions.

## Asset TODO behavior

`validateResources` writes `docs/ASSET_TODO.md` as a report of missing or placeholder assets. Treat it as generated/diagnostic. Do not use it as a design source.

## Review checklist

- Are lang keys present?
- Are generated resources regenerated from providers?
- Are hand-authored resources in the right root?
- Are guide refs valid?
- Are all missing assets either fixed or recorded?
- Did `validateResources` run after resource changes?
