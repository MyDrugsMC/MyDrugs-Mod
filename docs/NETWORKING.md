# Networking Contracts

Networking must stay centralized, auditable, and server-authoritative.

## Central files

- `network/ModNetwork.java` — single source of truth for payload registration.
- `network/ServerPayloadHandlers.java` — shared server handler helpers where applicable.
- `client/network/ClientPayloadHandlers.java` — client handler registration and presentation updates.
- `network/PayloadValidation.java` — shared validation helpers.
- `network/PayloadRateLimiter.java` — rate limiting for spammy requests.

## Registration pattern

Keep payload registrations grouped by domain in `ModNetwork`:

- manual-machine payloads;
- machine-transfer payloads;
- ritual payloads;
- biome finder payloads;
- stimulant payloads;
- drug formula payloads;
- visual payloads;
- recovery payloads;
- addiction payloads;
- diary payloads;
- mutation payloads;
- integration payloads.

When adding a payload, place it next to related payloads so reviewers can audit server-bound traffic quickly.

## Server-bound packets are requests

A server-bound packet is never a command. The server must validate and decide.

Validate where relevant:

- sender is a `ServerPlayer`;
- player has the expected open menu and `menuId`;
- `menu.stillValid(player)`;
- menu-owned block position or item container;
- held item, hand, stack, capability, or block entity state;
- numeric bounds;
- reject `NaN`, infinities, negative work, and oversized values;
- rate limit spammy actions;
- admin/debug mutations require `player.hasPermissions(2)` plus server config gate.

Server owns ritual timing, phase, judgement, and reward. Never trust client ritual timing.

## Client-bound packets are presentation

Client-bound packets should carry visuals, sounds, HUD snapshots, UI state, or presentation hints. They should not be required to maintain server truth.

## Codec rules

- Prefer explicit, bounded codecs.
- Avoid enum ordinals for persistent/networked data unless stable IDs are documented.
- Prefer named IDs or explicit `byId`/`id()` mappings.
- Match persistent data component bounds and network codec bounds.
- For strings, define length limits and validation rules when user-controlled.

## Review checklist for a new payload

- Is it registered in `ModNetwork` under the right domain?
- Is the handler on the correct side?
- Is the payload ID stable and namespaced?
- Are all fields bounded and validated?
- Does the server recompute or verify all gameplay-important state?
- Is rate limiting needed?
- Are failure cases silent, logged, or communicated appropriately?
- Does `compileJava` pass?
- Does the dedicated-server safety grep pass if client code was touched?
