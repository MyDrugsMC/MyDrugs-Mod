# Prompt: Generate Domain Doc

You are documenting one domain of the `mydrugs` NeoForge mod.

Domain:

```text
<DOMAIN>
```

Read:

- `AGENTS.md`
- `docs/ARCHITECTURE.md`
- `docs/VISION.md` and `docs/GAMEPLAY_DESIGN.md` if gameplay-facing
- relevant scan files
- relevant source files

Generate:

```text
docs/<DOMAIN_DOC>.md
```

Use this structure:

1. Purpose
2. Main packages
3. Main classes
4. Data flow
5. Invariants
6. Extension points
7. Common mistakes
8. Validation
9. Related scan files
10. Open questions

Rules:

- Do not rewrite code.
- Do not invent behavior.
- Mark uncertainty explicitly.
- Preserve project safety rules.
