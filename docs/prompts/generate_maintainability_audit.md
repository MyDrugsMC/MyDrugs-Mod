# Prompt: Generate Maintainability Audit

You are auditing maintainability for `mydrugs`.

Read:

- `AGENTS.md`
- `docs/CODEBASE_MAP.md`
- `docs/ARCHITECTURE.md`
- `docs/GAME_MOOD_BIBLE.md`
- `docs/GAMEPLAY_CONTRACTS.md`
- all relevant scan files

Generate or update:

- `docs/audits/MAINTAINABILITY_AUDIT.md`
- `docs/audits/TECH_DEBT_REGISTER.md`
- `docs/audits/RISK_HOTSPOTS.md`

For each issue include:

```text
Problem:
Evidence:
Files:
Risk:
Recommended fix:
Safe first step:
Validation:
```

Rules:

- Prioritize small safe passes over large rewrites.
- Do not propose gameplay rebalance unless clearly marked as design recommendation.
- Preserve project safety rules.
