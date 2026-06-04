# Gameplay Design

The mod is a risk/reward survival progression mod about altered states, dangerous power, addiction, recovery, ritual crafting, mutation, machines, and strange exploration.

## Design promise

Power should feel tempting, useful, and costly. Recovery should feel meaningful, not like a punishment timer. Psychedelic and recovery systems should point toward integration and freedom, not shock value.

## Core loops

### 1. Discovery loop

The player finds strange plants, substances, machines, rituals, and guide/diary hints. Early systems should teach through safe feedback and small risks.

### 2. Overclock loop

The player uses stimulants, machines, and PsyCurrent systems to accelerate work. This loop creates high output but higher instability, crash, or addiction pressure.

### 3. Ritual/perception loop

Psychedelic systems reveal structures, ritual certainty, ores, dimension access, or symbolic mechanics. The value is not raw DPS; it is perception and transformation.

### 4. Recovery/integration loop

The player builds safety, uses diary feedback, recovers from consequences, and converts insight into stable long-term benefits.

### 5. Inner dimension loop

The player enters a symbolic inner landscape for materials, insight, danger, and eventual integration. It should be beautiful first and dangerous second.

## Drug identities

| Identity | Gameplay role | Risk language |
|---|---|---|
| Coffee | early productivity, manual work, focus | jitter, mild tremor, heartbeat |
| Tobacco | precision, steadiness, ritual timing | dependency pressure, ritual habit |
| Cannabis/hash | calm, stability, reduced threat perception | slowed movement, altered perception |
| Cocaine/stimulants | short overclock, dash, adrenaline | crash, tremor, heartbeat, impulsivity |
| Crack | explosive short burst | high instability and input failure |
| Meth | late-game overclock | severe crash/consequence profile |
| Psychedelics | perception, ritual certainty, ore/structure/dimension insight | bad trips if unsafe; not addictive power drugs |
| Alcohol | courage, resistance, chaos | stumble, input fail, nausea/vomit |
| Opioids | deferred | do not expand before core loop is stable |

## Feedback rule

Every effect must be readable through at least one channel:

- HUD;
- GUI status;
- tooltip;
- sound;
- overlay;
- particles;
- guide text;
- JEI or recipe display;
- diary entry;
- advancement/criteria feedback.

Invisible power is bad design unless mystery is the explicit point and the guide eventually explains it.

## Balance principles

- Addiction should be avoidable and recoverable.
- Recovery should reduce chores over time, not add endless chores.
- High risk should buy unique utility, not only bigger numbers.
- Psychedelics should not become generic stat boosters.
- Machines should support the fantasy without turning into real-world procedural chemistry.
- Balance constants should be explained by player experience, not arbitrary realism.

## Balance-change protocol

- Do not change design or balance during maintainability work unless explicitly asked.
- When proposing balance changes, mark them as design recommendations rather than bug fixes.

## Progression guide

Progression changes should update the guide source manually or via the established guide generation process. Do not rewrite `docs/progression_guide_pages.md` unless explicitly asked.

## Anti-goals

- No moral panic tone.
- No real-world procedural drug instructions.
- No mandatory addiction for progression.
- No random sabotage without readable cause and recovery path.
- No client-authoritative gameplay effects.
- No broad power creep that erases survival risk.
