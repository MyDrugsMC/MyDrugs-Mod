# In-Game Guide Authoring

The MyDrugs Field Guide content is edited in Markdown and synced into generated game resources.

## Workflow

1. Edit:

```text
docs/progression_guide_pages.md
```

2. Sync:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools\sync_progression_guide.ps1
```

3. Validate:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools\validate_progression_content.ps1
```

4. Build or run data if needed:

```powershell
.\gradlew.bat compileJava
```

## Source and output

Source:

```text
docs/progression_guide_pages.md
```

Generated output:

```text
src/main/resources/assets/mydrugs/guide/pages.json
```

Do not edit the generated JSON by hand.

## Page format

A page starts with `# Title`. Pages are separated by a line containing only:

```markdown
---
```

Example:

```markdown
# Coffee First

Coffee is the first discovery branch.

> [GOAL] Brew coffee and gain Caffeine Knowledge.

---

# Tobacco

Process tobacco before consuming it.
```

## Supported elements

```text
# Title                    Page title
## Heading                 Section heading
plain text                 Body paragraph
> [TIP] text               Green tip callout
> [WARN] text              Warning callout
> [GOAL] text              Goal callout
@title text                Large centered title
@link target|label         Clickable link to another page
@item namespace:item_id    Item icon and item name
***                        Horizontal separator
---                        Page break
```

## Writing rules

- Keep paragraphs short.
- Use goals to tell the player what to do next.
- Use warnings for mistakes that block progression.
- Keep exact ingredients and quantities in JEI when possible.
- Use the guide to explain order, purpose, and concepts.
- Do not write real-world drug synthesis instructions.
- Use fictional, gameplay, ritual, or Minecraft-style wording.
