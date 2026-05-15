package org.mydrugs.mydrugs.recovery;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.WeakHashMap;

public final class SocialReliefManager {
    private static final Map<ServerPlayer, CompanionCountCache> COMPANION_COUNT_CACHE = new WeakHashMap<>();

    private SocialReliefManager() {
    }

    public static int countCompanions(ServerPlayer player, double radius) {
        long gameTime = player.level().getGameTime();
        CompanionCountCache cached = COMPANION_COUNT_CACHE.get(player);
        if (cached != null && cached.gameTime == gameTime && Double.compare(cached.radius, radius) == 0) {
            return cached.count;
        }

        AABB box = player.getBoundingBox().inflate(radius);
        int players = player.level().getEntitiesOfClass(Player.class, box, p -> p != player && p.isAlive()).size();
        int animals = player.level().getEntitiesOfClass(Animal.class, box, LivingEntity::isAlive).size();
        int count = players + animals;
        COMPANION_COUNT_CACHE.put(player, new CompanionCountCache(gameTime, radius, count));
        return count;
    }

    private record CompanionCountCache(long gameTime, double radius, int count) {
    }
}
