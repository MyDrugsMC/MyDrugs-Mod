# Gameplay Design

## Core loop

```text
Find or grow a substance
→ process it with primitive or industrial tools
→ use it for work, ritual, recovery, or insight
→ gain visible benefits and visible costs
→ unlock knowledge, machines, mixes, and inner-world access
→ stabilize through recovery and integration
→ return stronger and freer
```

## Substance role families

Substances should be designed by **role**, not only by effect.

| Family | Main role | Main risk | Narrative meaning |
|---|---|---|---|
| Functional substances | work, focus, calm, productivity | tolerance, dependency, crash if overused | useful tools |
| Escalation substances | power, overclock, escape, intensity | addiction, strain, withdrawal, risky mixes | false mastery |
| Psychedelics | insight, perception, ritual clarity, inner-world access | bad set/setting, overwhelm, poor integration | healing opening |
| Recovery tools | grounding, repair, stability, return | slow, requires effort | real freedom |
| Somatic adaptation | embodied resilience | over-optimization, instability if abused | body learns to regulate |

## Drug identity targets

Every substance should have:

- one clear advantage;
- one clear danger or limitation;
- one reason to use it in a specific situation;
- visible feedback in HUD, GUI, sound, particles, overlay, guide text, or diary text;
- a role in either escape, control, recovery, or integration.

### Coffee / caffeine

Role: work, energy, early-game productivity.

Should affect:

- manual machine speed;
- mild mining speed or movement speed;
- small camera sway/jitter at higher intensity;
- no huge combat power.

Coffee should be useful often, but not overpowered. It teaches that substances can help without immediately becoming mystical.

### Tobacco / nicotine

Role: focus, precision, ritual steadiness.

Should affect:

- reduced tremor when tremor exists;
- larger Psy Mixer golden zones while active;
- strong custom mining speed through precision, not vanilla Haste;
- an aloe vera mixture that reaches roughly a Haste II-like mining feel through the custom mining-speed system.

Presence of tobacco matters more than dose for ritual golden zones. Dose may affect mining speed slightly.

### Cannabis / cannabinoids

Role: calm, stability, symptom relief, safer recovery support.

Should affect:

- stress reduction;
- reduced coffee tremor;
- increased Psy Mixer ritual stability;
- less movement of golden zones and less golden-zone size variation while active;
- optionally lower aggressive mob detection distance if technically feasible and performant.

Cannabis should make rituals calmer and recovery easier, not just visually different.

### Cocaine / stimulants

Role: overclock, speed, adrenaline, false mastery.

Should affect:

- manual machine speed through the same abstraction as coffee;
- dash ability;
- adrenaline surge when taking damage;
- temporary increases to mining speed, movement speed, and damage during adrenaline;
- red/vein screen feedback during adrenaline.

Cocaine already has high addiction risk. Add more punishment only if playtesting shows it is too strong despite addiction, crash, heartbeat, tremor, and dose risk.

The diary should eventually frame stimulant overclocking as a shortcut that may help the player function while still avoiding recovery.

### Crack

Role: explosive, short-lived, risky stimulant meta option.

Possible identity:

- shorter but much stronger adrenaline window;
- instant burst effect after use;
- strong dash/attack/mining spike;
- harsh crash and dose instability;
- useful for emergency escape or burst combat, worse for sustained work.

Crack should not just be “cocaine but worse.” It should be tempting and clearly costly.

### Meth

Role: endgame overclock and false-solution branch.

Possible identity:

- long duration;
- strong manual machine speed;
- strong mining/movement/combat buffs;
- better Psy Mixer variants because of complex composition;
- high dose/addiction danger;
- endgame recipes should be unusually rewarding.

Meth can exist after LSD only if its story role is clear: it is not the spiritual endgame. It is the strongest version of “I can force myself to keep going.”

The diary should later guide the player back toward LSD/mushrooms, recovery, and integration.

### Psychedelics: LSD and mushrooms

Role: insight, altered perception, recovery support, and inner-world access.

Should affect:

- ore aura through blocks, optimized client-side;
- Psy Mixer clarity or golden-zone assistance;
- Psy Blueprint replacement through psychedelic vision;
- dream-state access;
- Psychotrope Resonator unlock;
- Inner Dimension entry;
- recovery/integration systems after the trip.

Psychedelics should not be treated like ordinary addictive power drugs. They should be risky mainly through:

- bad set and setting;
- high stress;
- poor preparation;
- lack of recovery room;
- lack of integration afterward;
- overwhelming perception.

They should not be framed as magic cures. Their best effects should require integration.

### Ketamine-like endgame treatment

Role: controlled dissociative-assisted integration, not casual recreation.

Design rules:

- endgame only;
- tied to recovery room, diary, or therapy-like structure;
- long cooldown;
- no ordinary “fun buff” identity;
- helps interrupt severe addiction loops or open an integration window;
- should not bypass all recovery work.

Use fictionalized language where needed. Avoid medical instructions, dosing, or real-world procedural details.

### Alcohol

Role: courage, resistance, chaos.

Should affect:

- mild resistance or custom damage reduction;
- courage/stress reduction;
- slightly increased melee confidence/damage if balanced;
- stumble, blur, input fail, vomit, and coma at high dose.

Alcohol should be useful but messy.

### Opioids

Deferred for now. Do not expand opioid systems until the existing core content is more stable and recovery systems are stronger.

## Addiction and recovery rules

- Addiction should be avoidable with careful play.
- Addiction should be recoverable with effort.
- Strong substances may tempt the player through power, not through forced narrative.
- Withdrawal should be readable and mechanically clear.
- Recovery should be multi-path, not only “talk to NPC.”

Recovery pillars:

- diary work;
- therapy-like sessions;
- Recovery Sanctuary;
- sleep;
- diet;
- exercise;
- music;
- time sober;
- cannabis or calming routes where appropriate;
- psychedelics with preparation and integration;
- ketamine-like endgame integration where appropriate.

## Bad trips

Bad trips should be difficult but meaningful.

They should not be random jumpscare punishment. A bad trip should say:

> You entered too intensely, too unprepared, or too unsupported.

Design rules:

- caused by readable conditions;
- reduced by safe setting and recovery preparation;
- accessible visual/sound intensity toggles;
- can still produce insight or useful fragments if survived/integrated;
- should not delete rare items or destroy the base.

## Ritual design rules

- The Psy Mixer is a sacred primitive altar, not a normal machine.
- Rituals should use mystical but not purely hostile messaging.
- Instability must be readable.
- Mastery should make rituals faster or safer.
- Tobacco improves precision.
- Cannabis improves stability.
- Psychedelics improve clarity/perception, especially when the player is prepared.
- Recovery/integration should eventually make rituals calmer.

## Psychotrope Resonator design rules

The old “Psychotrope Generator” direction should become **Psychotrope Resonator**.

It should not be a needy entity and should not create constant chores.

Role:

- unlocked after LSD;
- converts psychedelic experiences, Dream Residue, or Insight into Resonance/Integration Energy;
- opens or stabilizes the Inner Dimension during dream state;
- crafts healing and endgame freedom tools;
- supports recovery rather than demanding endless fuel.

Deterministic states are preferred:

```text
Dormant
Stable
Lucid
Dream
Integration
Overstrained
```

No hunger, cravings, favorite drugs, random sabotage, or passive punishment in the first implementation.

## Somatic adaptation design rules

The old DNA system should be reframed.

Avoid presenting body progression as clean sci-fi gene optimization. The better arc is:

```text
body control → body strain → somatic regulation → embodied resilience
```

Suggested terminology:

| Old direction | Preferred direction |
|---|---|
| DNA mutation | Somatic Adaptation |
| gene stat | body imprint / adaptation trait |
| CRISPR upgrade | Body Loom / Somatic Sequencer |
| mutation build | resilience pattern |
| genetic instability | adaptation strain |

Body upgrades should help the player regulate intensity, survive withdrawal, enter the dimension safely, or use endgame mobility. They should not simply be “bigger stats.”

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

## Endgame freedom

The endgame should reduce mandatory maintenance.

Possible rewards:

- flight armor made from psychedelics + recovery/integration;
- safer trips;
- reduced addiction burden;
- better recovery tools;
- stable Resonator workflows;
- ore aura / fortune-like perception;
- access to the Inner Dimension;
- optional high-risk challenges.

The player should not finish the mod only to be stuck babysitting machines, addiction meters, or hostile systems.
