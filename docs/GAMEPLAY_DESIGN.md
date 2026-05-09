# Gameplay Design

## Core loop

```text
Grow or find a substance
→ process it through primitive or industrial tools
→ consume it, ritualize it, or feed it into psychotrope systems
→ gain useful effects with visible consequences
→ unlock knowledge, recipes, machines, or risky opportunities
→ stabilize or upgrade the result through mastery
```

## Drug identity targets

Every substance should have:

- one clear advantage;
- one clear danger;
- one reason to use it in a specific situation;
- visible feedback in HUD, GUI, sound, particles, overlay, or guide text.

### Coffee / caffeine

Role: work, energy, early-game productivity.

Should affect:

- manual machine speed;
- mild mining speed or movement speed;
- small aggressive camera sway/jitter at higher intensity;
- no huge combat power.

Coffee should be useful often, but not overpowered.

### Tobacco / nicotine

Role: focus, precision, ritual steadiness.

Should affect:

- reduced tremor when tremor exists;
- larger Psy Mixer golden zones while active;
- strong custom mining speed through “precision”, not vanilla Haste;
- an aloe vera mixture that reaches roughly a Haste II-like mining feel through the custom mining-speed system.

Presence of tobacco matters more than dose for ritual golden zones. Dose may affect mining speed slightly.

### Cannabis / cannabinoids

Role: calm, stability, lowered threat perception.

Should affect:

- stress reduction;
- reduced coffee tremor;
- increased Psy Mixer ritual stability;
- less movement of golden zones and less golden-zone size variation while active;
- optionally lower aggressive mob detection distance if technically feasible and performant.

Cannabis should make rituals calmer, not just visually different.

### Cocaine / stimulants

Role: overclock, speed, adrenaline.

Should affect:

- manual machine speed through the same abstraction as coffee;
- dash ability;
- adrenaline surge when taking damage;
- temporary increases to mining speed, movement speed, and damage during adrenaline;
- red/vein screen feedback during adrenaline.

Cocaine already has high addiction risk. Add more punishment only if playtesting shows it is too strong despite addiction, crash, heartbeat, tremor, and dose risk.

### Crack

Role: explosive, short-lived, risky stimulant meta option.

Possible identity:

- shorter but much stronger adrenaline window;
- instant “burst” effect after use;
- strong dash/attack/mining spike;
- harsh crash and dose instability;
- useful when the player wants immediate power, worse for sustained work.

Crack should not just be “cocaine but worse”. It should be tempting for burst combat or emergency escape.

### Meth

Role: endgame overclock drug.

Possible identity:

- long duration;
- strong manual machine speed;
- strong mining/movement/combat buffs;
- better Psy Mixer variants because of complex composition;
- high dose/addiction danger;
- endgame recipes should be unusually rewarding.

Meth should feel like a late-game cocaine upgrade, not a simple duplicate.

### Psychedelics: LSD and mushrooms

Role: altered perception and ritual certainty.

Should affect:

- ore aura through blocks, optimized client-side;
- Psy Mixer golden zone becomes full or nearly full, making rituals much easier;
- Psy Blueprint becomes unnecessary for multiblock previews while active;
- future rift/dream island mechanics.

The ore aura must be optimized. Avoid scanning large volumes every frame.

### Alcohol

Role: courage, resistance, chaos.

Should affect:

- mild resistance or custom damage reduction;
- courage/stress reduction;
- slightly increased melee confidence/damage if balanced;
- stumble, blur, input fail, vomit, and coma at high dose.

Alcohol should be useful but messy.

### Opioids

Deferred for now. Do not expand opioid systems until the existing core content is more stable.

## Ritual design rules

- The Psy Mixer is a sacred primitive altar, not a machine.
- Rituals should use religious/mystical messaging.
- Instability must be readable.
- Mastery should make rituals faster or safer.
- Tobacco improves precision.
- Cannabis improves stability.
- Psychedelics can make success much easier, but may create other risks later.

## Manual machine speed abstraction

Manual machines should use a shared speed modifier helper instead of each machine checking drugs directly.

Affected systems:

- Manual Coffee Pulper
- Grinding Bowl / mortar
- Fluid Filterer if manual interaction exists
- Distiller if manual interaction exists
- Sieve
- Stomp Crafter
- Psy Mixer

GUI screens should clearly show when a coffee/cocaine/meth/manual-speed bonus is active.

## Future content hooks

Ideas that need more design before implementation:

- `inner_demon_remains` from bad-trip creatures.
- Alcohol ritual mix that greatly improves resistance.
- Sneak three times quickly to open a temporary rift.
- Dream island in the void with mobs and ores for endgame Psy Mixer recipes.
- Crack meta identity and meth endgame variants.
