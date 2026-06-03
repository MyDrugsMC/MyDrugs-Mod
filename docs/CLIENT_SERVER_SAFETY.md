# Client / Server Safety

Dedicated-server safety is a core invariant. Most catastrophic mod crashes come from client classes leaking into common/server code.

## Package rule

Client-only code belongs under:

```text
src/main/java/org/mydrugs/mydrugs/client/**
```

Client-only examples:

- screens and GUI rendering;
- HUD overlays;
- shaders;
- hallucination presentation;
- particles and client sounds;
- BER/model rendering;
- input distortion;
- client payload handlers;
- accessibility presentation toggles.

## Forbidden imports outside client packages

No common/server class may import:

```text
net.minecraft.client.*
net.neoforged.neoforge.client.*
org.mydrugs.mydrugs.client.*
```

Use the grep in `TESTING.md` after touching side-sensitive code.

## Event subscriber rules

- Client event subscribers must specify `value = Dist.CLIENT`.
- NeoForge 21.10 routes `@EventBusSubscriber` automatically: `IModBusEvent` goes to the mod bus, other events go to the game bus.
- Do not add explicit `bus = ...` unless there is a proven reason.

## Menu/screen split

Server menu:

- validates container state;
- exposes synced values;
- owns slots and access rules;
- may forward safe requests to server handlers.

Client screen:

- renders state;
- captures local input;
- sends small requests;
- never owns machine logic or ritual judgement.

## Client-bound payloads

Client-bound packets may update:

- visuals;
- sounds;
- HUD snapshots;
- screens;
- local presentation state;
- particles.

They must not be required for authoritative server gameplay progression.

## Accessibility

Respect `Config.CLIENT` toggles. In particular, reduced motion should reduce aggressive visual motion without disabling gameplay mechanics.

Presentation-heavy features should have at least one readable non-visual feedback path such as HUD text, tooltip, sound, particle, guide text, or JEI/GUI state.
