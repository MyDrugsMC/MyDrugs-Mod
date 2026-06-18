package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InnerMessageCooldowns {
    private static final Map<UUID, Map<String, Long>> LAST_MESSAGES = new HashMap<>();

    private InnerMessageCooldowns() {
    }

    public static boolean actionBar(
            ServerPlayer player,
            String id,
            int cooldownTicks,
            Component message
    ) {
        return send(player, id, cooldownTicks, message, true);
    }

    public static boolean systemMessage(
            ServerPlayer player,
            String id,
            int cooldownTicks,
            Component message
    ) {
        return send(player, id, cooldownTicks, message, false);
    }

    private static boolean send(
            ServerPlayer player,
            String id,
            int cooldownTicks,
            Component message,
            boolean actionBar
    ) {
        if (player == null || id == null || id.isBlank() || message == null) {
            return false;
        }
        long now = player.level().getGameTime();
        Map<String, Long> playerMessages = LAST_MESSAGES.computeIfAbsent(
                player.getUUID(),
                ignored -> new HashMap<>()
        );
        long last = playerMessages.getOrDefault(id, Long.MIN_VALUE / 2L);
        if (now - last < Math.max(0, cooldownTicks)) {
            return false;
        }
        playerMessages.put(id, now);
        if (actionBar) {
            player.displayClientMessage(message, true);
        } else {
            player.sendSystemMessage(message);
        }
        return true;
    }

    public static void clearAll() {
        LAST_MESSAGES.clear();
    }
}
