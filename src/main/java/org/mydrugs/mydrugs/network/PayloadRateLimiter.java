package org.mydrugs.mydrugs.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player minimum-interval gate for spammy server-bound payloads.
 *
 * The drag / shake / dash / rhythm payloads all have a small valid client rate
 * (about 20 Hz for drag/shake, far less for dash). A malicious client can
 * flood any of them to inflate gameplay state cheaply. This class enforces a
 * minimum interval per {@link Kind}; failing payloads are silently dropped
 * (no error reply — keeps the audit surface zero).
 *
 * Intervals are deliberately permissive to avoid breaking legit play; tune
 * downward only if regression tests confirm normal use stays under the cap.
 */
public final class PayloadRateLimiter {
    private PayloadRateLimiter() {
    }

    /** Per-player last-accept timestamp (millis) keyed by {@link Kind}. */
    private static final Map<UUID, EnumMap<Kind, Long>> LAST_ACCEPT = new ConcurrentHashMap<>();

    public enum Kind {
        // Drag/shake naturally pulse at ~20 Hz from the client; allow that with a
        // small margin and reject the rest.
        ROLLER_DRAG(45L),
        COFFEE_PULPER_DRAG(45L),
        SIEVE_SHAKE(45L),
        // Stimulant dash is keybind-driven; legitimate use is at most a few per
        // second even when mashing.
        STIMULANT_DASH(200L),
        // Psy mixer rhythm input — sacred altar, not a drum kit.
        PSY_MIXER_RITUAL_INPUT(150L);

        final long minIntervalMs;

        Kind(long minIntervalMs) {
            this.minIntervalMs = minIntervalMs;
        }
    }

    /**
     * @return true if this payload should be processed, false if the player is
     *         sending {@code kind} faster than its allowed cadence.
     */
    public static boolean accept(ServerPlayer player, Kind kind) {
        UUID id = player.getUUID();
        long now = System.currentTimeMillis();
        EnumMap<Kind, Long> perPlayer = LAST_ACCEPT.computeIfAbsent(id, u -> new EnumMap<>(Kind.class));
        Long last = perPlayer.get(kind);
        if (last != null && now - last < kind.minIntervalMs) {
            return false;
        }
        perPlayer.put(kind, now);
        return true;
    }

    /** Drop per-player state on logout so a long-running server does not accumulate UUIDs. */
    @EventBusSubscriber(modid = MyDrugs.MODID)
    public static final class Lifecycle {
        private Lifecycle() {
        }

        @SubscribeEvent
        public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
            LAST_ACCEPT.remove(event.getEntity().getUUID());
        }
    }
}
