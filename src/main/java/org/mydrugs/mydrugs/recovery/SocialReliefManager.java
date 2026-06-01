package org.mydrugs.mydrugs.recovery;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import org.mydrugs.mydrugs.Config;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.WeakHashMap;

public final class SocialReliefManager {
    private static final double MOVE_REFRESH_DISTANCE_SQR = 16.0D;
    private static final Map<ServerPlayer, CompanionCountCache> COMPANION_COUNT_CACHE = new WeakHashMap<>();

    private SocialReliefManager() {
    }

    public static int countCompanions(ServerPlayer player, double radius) {
        long gameTime = player.level().getGameTime();
        BlockPos pos = player.blockPosition();
        CompanionCountCache cached = COMPANION_COUNT_CACHE.get(player);
        if (cached != null && cached.validFor(player, pos, radius, gameTime, ttlTicks())) {
            return cached.count;
        }

        int count = countCompanionsNow(player, radius);
        COMPANION_COUNT_CACHE.put(player, new CompanionCountCache(
                gameTime,
                player.level().dimension(),
                pos.immutable(),
                radius,
                count
        ));
        return count;
    }

    static int countCompanionsNow(ServerPlayer player, double radius) {
        AABB box = player.getBoundingBox().inflate(radius);
        int players = player.level().getEntitiesOfClass(Player.class, box, p -> p != player && p.isAlive()).size();
        int animals = player.level().getEntitiesOfClass(Animal.class, box, LivingEntity::isAlive).size();
        return players + animals;
    }

    private static int ttlTicks() {
        try {
            return Math.max(1, Config.SERVER.socialReliefCacheTicks.get());
        } catch (Throwable ignored) {
            return 20;
        }
    }

    private record CompanionCountCache(
            long gameTime,
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
            BlockPos pos,
            double radius,
            int count
    ) {
        boolean validFor(ServerPlayer player, BlockPos currentPos, double requestedRadius, long now, int ttl) {
            return Double.compare(radius, requestedRadius) == 0
                    && dimension.equals(player.level().dimension())
                    && now - gameTime < ttl
                    && pos.distSqr(currentPos) < MOVE_REFRESH_DISTANCE_SQR;
        }
    }
}
