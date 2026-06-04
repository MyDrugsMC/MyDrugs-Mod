> Snapshot generated from `scan/` and source review on 2026-06-03. Point-in-time report, not a contract. If this disagrees with source, source wins.

# Mood Audit

Generated from: `GAME_MOOD_BIBLE.md`, `GAMEPLAY_CONTRACTS.md`, `DRUG_SYSTEM.md`,
`MACHINES_PIPES_AND_RECIPES.md`, `RESOURCES_AND_DATAGEN.md`, direct source review of
`en_us.json`, `AddictionRecoveryFeedback.java`, `AddictionHudRenderer.java`,
`PsyMixerScreen.java`, `GasJeiUtil.java`, `AbstractNiceRecipeCategory.java`,
`DrugItem.java`, `CocainePowderPileBlock.java`.

Legend: `[confirmed]` = directly read from source. `[inferred]` = pattern derived from naming/structure.

Issue classes used:
- **mood mismatch** — content contradicts the intended emotional palette
- **missing feedback** — an effect or state has no readable signal
- **too generic** — language/design reads as any tech/survival mod, not `mydrugs`
- **too realistic / procedural** — names or descriptions that reference real-world drug procedure
- **too punishing** — consequence without legible cause or recovery path
- **too consequence-free** — power without readable cost
- **unclear fantasy** — the player cannot tell what the system is for

---

## Critical issues (player-facing policy or safety)

### MOOD-01 — Deferred opioid drug names are real-world and visible in lang

**Class:** too realistic / procedural  
**Evidence:** [confirmed]  
```
en_us.json:596  "mydrugs.addiction.drug.heroin": "Heroin"
en_us.json:597  "mydrugs.addiction.drug.morphine": "Morphine"
en_us.json:598  "mydrugs.addiction.drug.fentanyl": "Fentanyl"
en_us.json:604  "mydrugs.addiction.drug.benzodiazepine": "Benzodiazepine"
en_us.json:605  "mydrugs.addiction.drug.barbiturate": "Barbiturate"
en_us.json:600  "mydrugs.addiction.drug.ketamine": "Ketamine"
en_us.json:601  "mydrugs.addiction.drug.pcp": "PCP"
en_us.json:602  "mydrugs.addiction.drug.dxm": "DXM"
```

**Player-facing effect:** If any of these DrugIds ever reaches a player-visible context (addiction
HUD, tooltip, diary), the player sees verbatim real-world pharmaceutical and street drug names.
This contradicts the `GAME_MOOD_BIBLE` realism boundary: *"Use abstraction and fictionalization.
Avoid real-world synthesis, preparation, extraction, purification, dosing, or optimization."*
Naming precision is not synthesis, but `fentanyl` and `barbiturate` cross into medical/pharmacology
vocabulary that the design specifically wants to avoid.

**Recommended fix:**  
Replace all deferred and non-implemented drug names with fictional or abstracted labels before they
appear in any player-visible context. Examples:

| Current key value | Suggested direction |
|---|---|
| `"Fentanyl"` | `"Synthetic Opioid"` or mod-specific name |
| `"Heroin"` | `"Refined Extract"` or the fictional item name |
| `"Benzodiazepine"` | `"Calming Agent"` |
| `"Barbiturate"` | `"Sedative Compound"` |
| `"PCP"` | `"Dissociative Crystal"` |
| `"DXM"` | `"Cough Derivative"` (if ever used) |

The deferred opioid items (`morphine`, `heroin`, etc.) should carry fictional item names if they
exist in the registry at all. If these IDs are only in `DrugId` for future scaffolding, their
lang keys should not appear in `en_us.json` until the items are designed.

**Safety notes:** This is also a content safety issue. Real drug brand and street names in game
text are the most direct violation of the anti-goals listed in `AGENTS.md` § "Introduce real-world
drug procedure details." Treat this as Priority 0 for the lang file.

---

## High-priority issues

### MOOD-02 — Drug effect tooltips are purely technical

**Class:** too generic, mood mismatch  
**Evidence:** [confirmed]  
```
en_us.json:1087  "tooltip.mydrugs.drug.effect": "%s: %ss, intensity %s"
en_us.json:1084  "tooltip.mydrugs.drug.category": "Category: %s"
en_us.json:1085  "tooltip.mydrugs.drug.addiction": "Addiction risk: %s"
```

**Player-facing effect:** The drug effect tooltip reads as pharmacokinetic data:
`Chromatic Dream: 120s, intensity 2.3`. This is the primary tooltip for every drug item,
and it describes the experience in engineering units rather than sensory or atmospheric language.
The mod's identity is "occult-industrial rather than generic tech," and "surreal but mechanically
readable." The current tooltip is neither surreal nor occult.

Advanced tooltips are gated (`tooltip.mydrugs.drug.advanced_hint`) but the primary tooltip
provides no atmospheric context — only a technical summary.

**Recommended fix:**  
Add a one-line flavor/sensory tooltip per drug type (or drug model) that runs before the effect
list. Examples aligned with identity:
- Cannabis: *"A stillness settles. The edges of things soften."*
- Cocaine: *"The world snaps into focus. Your pulse jumps to meet it."*
- LSD: *"Everything is looking back at you."*
- Meth: *"You feel like a machine running faster than it was built for."*

These should use `Component.translatable` keys like `tooltip.mydrugs.drug.flavor.<drug_id>`.
The technical block can stay for advanced tooltips; the flavor line should appear in normal mode.

**Safety notes:** Flavor text must remain atmospheric — no dosing, preparation, or procedural
language. One evocative sentence is the ceiling.

---

### MOOD-03 — No per-drug dose-level feedback messages

**Class:** missing feedback  
**Evidence:** [confirmed]  
```
en_us.json:495  "mydrugs.dose.drug.very_high_to_overdose": "Too much... everything feels wrong."
en_us.json:496  "mydrugs.dose.drug.overdose_to_very_high": "You're coming back from the edge."
en_us.json:497  "mydrugs.dose.drug.very_high_to_high": "The effects are subsiding."
```

Only three dose-transition messages exist — all generic, all at the ceiling of the dose arc.
There are no messages for the normal dose range: no feedback when effects first peak, no
per-drug sensory cue, and no message when a dose begins to fade on a substance that has a
recognizable crash (cocaine, meth).

**Player-facing effect:** A player who takes cocaine hears nothing distinctive as the effects hit.
A player in early meth overclock gets no atmospheric signal. The feedback channel exists but is
empty for the most important part of the risk/reward loop: the onset and normal arc.

**Recommended fix:**  
Add dose-transition messages for each DrugId at the `THRESHOLD → NOTABLE` and `NOTABLE → HIGH`
crossings. At minimum, add per-drug onset and crash messages:
- Coffee: *"You feel the familiar warmth kick in."* / *"The sharpness is already fading."*
- Cocaine: *"Your thoughts snap into tight focus."* / *"The crash comes fast."*
- Meth: *"Everything runs faster than it should."* / *"The silence afterward feels wrong."*
- Cannabis: *"A slow calm pulls at the edges of your thoughts."*
- LSD: *"Patterns begin to breathe."*
- Mushrooms: *"The world has become softer and stranger."*

Lang key structure: `mydrugs.dose.drug.<drug_id>.<transition>`. These should be hotbar messages
(action bar), not chat, to stay atmospheric without interrupting play.

**Safety notes:** All messages must be experiential/atmospheric. No preparation, route, or dosing
references.

---

### MOOD-04 — Recovery HUD labels are terse UI widgets, not in-world language

**Class:** mood mismatch, too generic  
**Evidence:** [confirmed]  
```
en_us.json:513  "message.mydrugs.recovery_feedback.calm": "+Calm"
en_us.json:514  "message.mydrugs.recovery_feedback.stress_down": "Stress ↓"
en_us.json:515  "message.mydrugs.recovery_feedback.recovery_plus": "Recovery +"
en_us.json:516  "message.mydrugs.recovery_feedback.room": "Room bonus"
en_us.json:517  "message.mydrugs.recovery_feedback.music": "+Music"
en_us.json:518  "message.mydrugs.recovery_feedback.diary": "+Diary"
en_us.json:519  "message.mydrugs.recovery_feedback.tea": "+Tea"
en_us.json:520  "message.mydrugs.recovery_feedback.sleep": "+Sleep"
```

Compare to the excellent session-level text in the same file:
```
en_us.json:543  "message.mydrugs.recovery_session.enter": "The room starts to hold."
en_us.json:546  "message.mydrugs.recovery_session.grounded_music": "Music gave the pressure a rhythm."
en_us.json:548  "message.mydrugs.recovery_session.grounded_tea": "Warmth settled the body."
en_us.json:550  "message.mydrugs.recovery_session.grounded_breathing": "Breathing steadied."
```

The recovery session messages are atmospheric and in-world. The recovery feedback messages are
UI labels (`+Music`, `+Tea`, `+Sleep`) that belong in a fitness app, not an occult survival mod.
Both use `player.displayClientMessage()` in the same hotbar slot.

**Player-facing effect:** After the excellent session text fires, subsequent recovery feedback
events replace it with widget labels that break immersion. The `GAME_MOOD_BIBLE` says "Recovery
should feel like a meaningful arc, not a simple stat reset." The labels make it feel like a
stat ticker.

**Recommended fix:**  
Replace the terse hotbar labels with short evocative phrases matched to the identity map:
- `+Calm` → `"Tension drains, slowly."` or `"Something loosens."`
- `Stress ↓` → `"The pressure has somewhere to go."`
- `Recovery +` → `"The work is counting."`
- `Room bonus` → `"The room is holding."`
- `+Music` → `"The sound gives you something to stand on."`
- `+Diary` → `"Writing made it lighter."`
- `+Tea` → `"Warmth, slowly."` or `"The warmth settles first."`
- `+Sleep` → `"Rest put something back."`

Keep the messages short — one sentence, five to six words. They fire in the hotbar during active
recovery, so they must be quick to read. But they should read like the world speaking, not a UI.

**Safety notes:** No changes to timing or trigger logic — only the string values.

---

### MOOD-05 — Machine statuses use generic tech-mod language

**Class:** too generic, mood mismatch  
**Evidence:** [confirmed]  
```
en_us.json:1152  "machine_status.mydrugs.idle": "Idle"
en_us.json:1153  "machine_status.mydrugs.running": "Running"
en_us.json:1154  "machine_status.mydrugs.missing_input_item": "Missing input item"
en_us.json:1155  "machine_status.mydrugs.missing_input_fluid": "Missing input fluid"
en_us.json:1156  "machine_status.mydrugs.missing_input_gas": "Missing input gas"
en_us.json:1158  "machine_status.mydrugs.no_matching_recipe": "No matching recipe"
en_us.json:1159  "machine_status.mydrugs.output_slot_full": "Output slot full"
```

**Player-facing effect:** Machines are supposed to feel "dangerous, occult-industrial, and
connected to the mod identity." The status display is the only in-world text the machine sends
to the player during operation. `"Idle"` and `"Running"` are indistinguishable from Applied
Energistics or Industrial Craft vocabulary.

**Recommended fix:**  
Rephrase the shared statuses to carry mod identity while preserving clarity. The meaning must
remain immediately readable — these are operational states, not flavor. One example direction:

| Current | Suggested direction |
|---|---|
| `"Idle"` | `"Dormant"` or `"Waiting"` |
| `"Running"` | `"Processing"` or `"Active"` |
| `"Missing input item"` | `"Feed the input slot"` or `"Input needed"` |
| `"No matching recipe"` | `"Unknown combination"` or `"Nothing to process"` |
| `"Output slot full"` | `"Output blocked"` |

Machine-specific statuses (if added in the future) can go further — e.g., the Aromatic Extractor
could show `"Extracting essence"` instead of `"Running"`. The shared status keys are the floor.

**Safety notes:** Do not break machine status enum serialization. These are display strings only;
the enum key names are stable.

---

### MOOD-06 — PsyMixerScreen quality tiers are a hardcoded literal with no mod identity

**Class:** too generic, missing feedback  
**Evidence:** [confirmed]  
```java
// PsyMixerScreen.java L423
return Component.literal("Crude / Base / Perfect / Masterwork");
```

This is the only place in the game that explains the ritual quality system to the player. It is
untranslatable and uses generic crafting vocabulary (`Masterwork` appears in many unrelated mods).

**Player-facing effect:** A player in the Psy Mixer — the core ritual system — sees a slash-
delimited list of four generic tier names as their only quality reference. The ritual identity of
the Psy Mixer (`GAME_MOOD_BIBLE`: *"Rituals should feel symbolic, strange, and gameplay-focused"*)
is absent from the quality hint.

**Recommended fix:**  
1. Add a translatable lang key: `screen.mydrugs.psy_mixer.quality_hint`.
2. Replace the literal with `Component.translatable("screen.mydrugs.psy_mixer.quality_hint")`.
3. Consider whether the four tier names themselves belong in the mod identity. `"Crude"` and
   `"Masterwork"` are from generic crafting. Alternative directions:
   - *Dim / Resonant / Clear / Aligned* — attunement language
   - *Raw / Refined / Pure / Integrated* — process language matching the drug arc
   - *Faint / Stable / Potent / Perfected* — intensity language

**Safety notes:** The tier names affect JEI display and any guide references. If renamed, update
`GAMEPLAY_DESIGN.md` and guide pages accordingly. Codec/serialization uses the enum not the label.

---

### MOOD-07 — Withdrawal has no onset message

**Class:** missing feedback, too punishing  
**Evidence:** [inferred from source scan]  
The withdrawal system exposes a HUD bar (`mydrugs.hud.withdrawal`) and symptom icons
(`mydrugs.hud.symptom.*`). There are no hotbar or chat messages that fire when withdrawal begins,
when it escalates, or what the player should do.

`AddictionRecoveryFeedback` sends messages *during* recovery actions but not for withdrawal onset
or escalation. The bad-trip system has 4 onset messages; withdrawal has none.

**Player-facing effect:** The player's withdrawal bar rises and symptom icons appear with no
atmospheric signal. The first indication that something is wrong is icons they may not yet
recognize. This is especially punishing for a new player who does not know what the icons mean.
The `GAMEPLAY_DESIGN.md` says *"No random sabotage without readable cause and recovery path."*
Unannounced withdrawal is a grey-zone case.

**Recommended fix:**  
Add at least one hotbar message when withdrawal first becomes significant (e.g., severity crossing
a first threshold). One variant per category is enough:
- Nicotinic: *"Your hands want something to hold."*
- Stimulant: *"The edges of things have gone dull."*
- Cannabinoid: *"A restlessness you can't name."*
- Psychedelic: *"The world feels flatter than it should."*

A companion recovery hint should fire with the onset message if `AddictionRecoveryFeedback` is
not already showing:
*"The guide has a recovery page. The diary knows your state."*

**Safety notes:** Withdrawal feedback messages must describe experience, not pharmacology. No
real withdrawal symptom names or medical descriptions.

---

### MOOD-08 — Overdose messages are clinical alert text

**Class:** mood mismatch  
**Evidence:** [confirmed]  
```
en_us.json:511  "message.mydrugs.overdose.use_antidote": "Overdose emergency. Use antidote."
en_us.json:512  "message.mydrugs.overdose.get_safe": "Overdose emergency. Get safe."
```

**Player-facing effect:** During one of the most high-stakes moments in the mod — overdose risk —
the player sees text that reads like a medical emergency protocol. The word "emergency" is clinical,
not atmospheric. This does not match the horror/instability palette of the mod: *"Horror should
come from instability, mutation, perception shifts, dependence, withdrawal."*

**Recommended fix:**  
Replace with messages that convey instability and urgency without medical terminology:
- `"use_antidote"`: *"Everything is wrong. The antidote — now."*
- `"get_safe"`: *"You are too far out. Find ground."* or *"Get somewhere safe. Now."*

The urgency must survive the change. These fire in a critical state so they must be short
and readable under panic. But they can still sound like the world, not a manual.

**Safety notes:** Both messages are hotbar (action bar). Keep them single-line and short.
Do not remove urgency in the edit.

---

## Medium-priority issues

### MOOD-09 — HUD symptom names are one-word clinical labels

**Class:** too generic, mood mismatch  
**Evidence:** [confirmed]  
```
en_us.json:569  "mydrugs.hud.symptom.confusion": "Confusion"
en_us.json:570  "mydrugs.hud.symptom.fragility": "Fragility"
en_us.json:571  "mydrugs.hud.symptom.vision": "Vision"
en_us.json:572  "mydrugs.hud.symptom.hallucination": "Hallucination"
en_us.json:573  "mydrugs.hud.symptom.stress": "Stress"
en_us.json:574  "mydrugs.hud.symptom.dissociation": "Dissociation"
en_us.json:575  "mydrugs.hud.symptom.fatigue": "Fatigue"
en_us.json:576  "mydrugs.hud.symptom.intrusive_thoughts": "Intrusive Thoughts"
en_us.json:577  "mydrugs.hud.symptom.insomnia": "Insomnia"
```

**Player-facing effect:** These labels appear as icon tooltips in the HUD symptom column. They
are technically accurate DSM-adjacent terms. `"Dissociation"` and `"Intrusive Thoughts"` are
clinical vocabulary that the mod's abstraction policy should soften. The symptom names are also
inconsistent in register — `"Vision"` is abstract while `"Hallucination"` is clinical; `"Fatigue"`
is a medical term while `"Insomnia"` is a diagnosis label.

**Recommended fix:**  
Rephrase toward experiential, gameplay-readable descriptions. Keep it short — these are icon
labels. Example directions:
- `"Dissociation"` → `"Detachment"` or `"Drift"`
- `"Intrusive Thoughts"` → `"Noise"` or `"Loop"`
- `"Insomnia"` → `"Sleepless"` or `"Wired"`
- `"Hallucination"` → `"Visions"` or keep as is (context-appropriate here)

The label is the tooltip title; one or two words is correct. Just shift away from DSM.

**Safety notes:** No gameplay change. Icon → label mapping must stay stable; only the lang value
changes.

---

### MOOD-10 — Effect type names have mixed register

**Class:** too generic, mood mismatch  
**Evidence:** [confirmed]  
```
en_us.json:1129  "effect_type.mydrugs.nausea": "Nausea"
en_us.json:1130  "effect_type.mydrugs.slowness": "Slowness"
en_us.json:1134  "effect_type.mydrugs.fog": "Fog"
en_us.json:1151  "effect_type.mydrugs.ore_fortune": "Ore Fortune"
— alongside —
en_us.json:1131  "effect_type.mydrugs.chromatic_dream": "Chromatic Dream"
en_us.json:1132  "effect_type.mydrugs.acid_warp": "Acid Warp"
en_us.json:1133  "effect_type.mydrugs.void_pulse": "Void Pulse"
en_us.json:1137  "effect_type.mydrugs.melt_reality": "Melt Reality"
en_us.json:1138  "effect_type.mydrugs.velvet_echo": "Velvet Echo"
en_us.json:1142  "effect_type.mydrugs.quantum_flower": "Quantum Flower"
en_us.json:1143  "effect_type.mydrugs.cosmic_tunnel": "Cosmic Tunnel"
```

**Player-facing effect:** `"Chromatic Dream"`, `"Velvet Echo"`, and `"Cosmic Tunnel"` are
evocative and match mod identity. `"Nausea"`, `"Slowness"`, `"Fog"`, and `"Ore Fortune"`
are either vanilla-adjacent or utility-plain. The effect name column in the HUD and tooltip is
where the player reads what a drug does to them. A mixed register weakens both the evocative
names and the plain ones.

**Recommended fix:**  
Rename the plain/vanilla-adjacent effect types:
- `"Nausea"` → `"Revulsion"` or `"Sickness"` (still readable but distinct from vanilla)
- `"Slowness"` → `"Weight"` or `"Lead Limbs"` (if this is the overdose/withdrawal variant)
- `"Fog"` → `"Murk"` or `"Haze"` (mod already uses `Iridescent Haze`; `"Fog"` is bland)
- `"Ore Fortune"` → `"Aura Sight"` or `"Deep Read"` (matches the perception fantasy)

**Safety notes:** Effect type names are used in HUD rendering and tooltip display. They are not
codec-serialized IDs — only the enum name is persistent. The lang value change is safe.

---

### MOOD-11 — Drug route tooltips use clinical/realistic terminology

**Class:** too realistic / procedural  
**Evidence:** [confirmed]  
```
en_us.json:1093  "tooltip.mydrugs.drug.route.injecting": "Route: Injecting"
en_us.json:1094  "tooltip.mydrugs.drug.route.sniffing": "Route: Sniffing"
en_us.json:1092  "tooltip.mydrugs.drug.route.bang": "Route: Bang"
```

**Player-facing effect:** `"Injecting"` and `"Sniffing"` are real-world route-of-administration
terms. The `GAME_MOOD_BIBLE` says the mod should be *"fictionalized rather than procedural or
realistic."* Naming the route in clinical or street terms is a low-level policy violation and also
a clarity issue — new players may not know what `"Bang"` means.

**Recommended fix:**  
Replace with interaction-language:
- `"Route: Sniffing"` → `"Method: Rail"` or `"Use: Rail"` (the game already uses "rail" as an
  item name, so it's in-fiction vocabulary)
- `"Route: Injecting"` → `"Method: Syringe"` (referencing the item, not the act)
- `"Route: Bang"` → `"Method: Bang"` is acceptable since Bang is an in-game item name; add a
  guide explanation for players who do not know it
- `"Route: Smoking"` and `"Route: Eating"` are fine as-is — no clinical connotation

**Safety notes:** Route enum names are stable; only lang values change.

---

### MOOD-12 — Gas/fluid names in JEI come from ID strings, not localized names

**Class:** too generic, unclear fantasy  
**Evidence:** [confirmed]  
```java
// GasJeiUtil.java L70
lines.add(Component.literal(displayName(id)));
// AbstractNiceRecipeCategory.java L168
gasId == null ? ui("empty") : Component.literal(GasJeiUtil.displayName(gasId)),
```
`displayName(id)` derives the display string from the registry ID path (`psychotrope_gas` →
`"Psychotrope Gas"`), bypassing the localization system.

**Player-facing effect:** Gas and fluid names in JEI panels are always English and always derived
from registry ID casing conventions. Names like `"Psychotrope Gas"` work, but less evocative IDs
become bland machine-readable strings. This also blocks translation entirely for JEI gas/fluid
entries.

**Recommended fix:**  
Add lang keys for gas and fluid display names (e.g., `gas.mydrugs.psychotrope_gas`). Replace
`GasJeiUtil.displayName()` with a lookup that checks the lang key and falls back to the ID-derived
name. This is a two-part change: lang keys + lookup.

**Safety notes:** The registry ID and lang key are separate; no serialization impact.

---

### MOOD-13 — PsyMixerScreen ritual action list is a Component.literal from enum names

**Class:** too generic, missing feedback  
**Evidence:** [confirmed]  
```java
// PsyMixerScreen.java L188
: Component.literal(recipe.availableRitualActions().stream()
    .map(PsyMixerRitualAction::serializedName)
    .limit(4)
    .reduce((a, b) -> a + ", " + b).orElse(""));
```

**Player-facing effect:** Ritual actions in the Psy Mixer display their serialized enum name
(e.g., `"rotate_clockwise"`, `"hold_still"`) rather than a localized, evocative label. These
are the player's gesture cues during a ritual — the moment of "ritual certainty" that the
`GAME_MOOD_BIBLE` calls central. Seeing raw enum names breaks the ritual fantasy.

**Recommended fix:**  
Add lang keys for each `PsyMixerRitualAction` (e.g., `ritual_action.mydrugs.rotate_clockwise`
→ `"Turn clockwise"` or `"Spiral inward"`). Replace `serializedName()` with a translatable
component lookup. Even neutral English labels are better than enum names.

**Safety notes:** `serializedName()` is the codec ID; do not change it. Only the display label
changes.

---

## Low-priority issues

### MOOD-14 — CocainePowderPileBlock consumption has no feedback round-trip

**Class:** missing feedback  
**Evidence:** [confirmed] — also documented in `MAINTAINABILITY_AUDIT.md` §Priority 0  
The snorting gesture TODO comments are at `CocainePowderPileBlock.java` L159 and L185.
No animation, no atmospheric message, no server cooldown.

**Player-facing effect:** The cocaine rail is a set-piece interaction — a powder line on a surface
that the player right-clicks to consume. It should be one of the most kinetic interactions in the
mod. Currently it fires instantly with no signal to the player other than the rail block
disappearing and the drug effect applying.

**Recommended fix:**  
Short-term (no animation needed): add a server cooldown and fire a hotbar message on consumption
that aligns with cocaine's identity (`"Your focus narrows to a point."` or similar). Long-term:
implement the animation round-trip per the existing TODO.

**Safety notes:** Do not describe the act of consumption in the message — only the experiential
effect.

---

### MOOD-15 — `GAME_MOOD_BIBLE.md` examples sections are empty

**Class:** unclear fantasy  
**Evidence:** [confirmed]  
```
GAME_MOOD_BIBLE.md:88  ## Examples of good player-facing text
GAME_MOOD_BIBLE.md:89  TODO: Add project-specific examples.
GAME_MOOD_BIBLE.md:91  ## Examples of bad player-facing text
GAME_MOOD_BIBLE.md:92  TODO: Add examples that are too realistic, too meme-like, or too generic.
```

**Player-facing effect:** None directly. Indirectly, agents and contributors writing player-facing
text have no concrete examples to calibrate against. The bad-trip messages, session messages, and
mutation messages are the best existing examples — but they are not referenced. New text (drug
dose messages, withdrawal messages) will drift toward generic or technical if the mood bible gives
no concrete anchors.

**Recommended fix:**  
Fill both sections now that the mod has strong examples. Suggested good examples (from current
`en_us.json`):
- *"Something in your mind turns against you."* — atmospheric, not clinical
- *"The room held. So did you."* — earned, terse, in-world
- *"Your borrowed genetics destabilize."* — fictional, readable

Suggested bad examples (from current `en_us.json`):
- *"Overdose emergency. Use antidote."* — clinical protocol language
- *"+Music"* — UI widget, not world text
- *"Recovery +"* — fitness app label

**Safety notes:** Docs-only change. No code impact.

---

### MOOD-16 — `tooltip.mydrugs.manual_energy` uses vanilla-adjacent label

**Class:** too generic  
**Evidence:** [confirmed]  
```
en_us.json:437  "tooltip.mydrugs.manual_energy": "Manual Boost"
```

**Player-facing effect:** The PsyCurrent energy system has a distinct identity. `"Manual Boost"`
is the label for a player interaction with a machine — it should carry that identity. Compare
with the mod's best machine-adjacent text.

**Recommended fix:**  
`"Manual Boost"` → `"Psychotrope Input"` or `"Manual Charge"`. Small but consistent.

**Safety notes:** One key, one value. No code change needed.

---

## Summary table

| ID | Class | Severity | Effort |
|---|---|---|---|
| MOOD-01 | too realistic / procedural | Critical | Low (lang only) |
| MOOD-02 | too generic, mood mismatch | High | Medium (new lang keys + tooltip code) |
| MOOD-03 | missing feedback | High | Medium (new lang keys + dose event wiring) |
| MOOD-04 | mood mismatch, too generic | High | Low (lang only) |
| MOOD-05 | too generic | High | Low (lang only) |
| MOOD-06 | too generic, missing feedback | High | Low (one literal → translatable + lang) |
| MOOD-07 | missing feedback | High | Medium (new messages + trigger in WithdrawalManager) |
| MOOD-08 | mood mismatch | Medium | Low (lang only) |
| MOOD-09 | too generic | Medium | Low (lang only) |
| MOOD-10 | too generic | Medium | Low (lang only) |
| MOOD-11 | too realistic | Medium | Low (lang only) |
| MOOD-12 | too generic | Medium | Medium (lang keys + lookup helper) |
| MOOD-13 | too generic | Medium | Low–Medium (lang keys + display lookup) |
| MOOD-14 | missing feedback | Low | Low (hotbar message) + Medium (animation, later) |
| MOOD-15 | unclear fantasy | Low | Low (docs only) |
| MOOD-16 | too generic | Low | Low (lang only) |

---

## Recommended fix sequence

Safe lang-only fixes first (no code change, no compile needed):

1. **MOOD-01** — Replace deferred opioid/CNS drug names with fictional/abstract labels in `en_us.json`.
2. **MOOD-04** — Replace recovery feedback hotbar labels with short evocative phrases.
3. **MOOD-05** — Rephrase machine status strings.
4. **MOOD-08** — Rephrase overdose alert messages.
5. **MOOD-09** — Rephrase HUD symptom names away from clinical vocabulary.
6. **MOOD-10** — Rename plain/vanilla effect types.
7. **MOOD-11** — Rename drug route tooltip values.
8. **MOOD-15** — Fill `GAME_MOOD_BIBLE.md` examples sections.
9. **MOOD-16** — Rename `manual_energy` label.

Lightweight code changes (one-liners):

10. **MOOD-06** — Replace `PsyMixerScreen` L423 literal with `Component.translatable`.
11. **MOOD-13** — Replace ritual action `serializedName()` display with translatable lookup.

Medium effort:

12. **MOOD-02** — Add per-drug flavor tooltip line; add `DrugTooltipBuilder` flavor slot.
13. **MOOD-03** — Add dose-transition messages for each DrugId; wire into dose manager.
14. **MOOD-07** — Add withdrawal onset message; wire into `WithdrawalManager`.
15. **MOOD-12** — Add gas/fluid lang keys; replace `GasJeiUtil.displayName()` lookup.
16. **MOOD-14** — Add hotbar message for cocaine rail consumption; add server cooldown.

---

## Do-not-touch constraints

- `DrugId` serialized names — these are save-stable. Only lang *values* change, never keys.
- `PsyMixerRitualAction.serializedName()` — codec ID, must not change.
- Machine status enum names — codec-stable. Only lang values change.
- `MixedDrugData.CODEC` — save-breaking if changed.
- `effect_type.mydrugs.*` key names — lang keys are stable IDs. Only values change.
