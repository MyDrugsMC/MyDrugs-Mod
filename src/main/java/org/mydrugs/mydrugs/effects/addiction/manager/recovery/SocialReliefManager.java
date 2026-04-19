package org.mydrugs.mydrugs.effects.addiction.manager.recovery;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class SocialReliefManager {
    private SocialReliefManager() {
    }

    public static int countCompanions(ServerPlayer player, double radius) {
        AABB box = player.getBoundingBox().inflate(radius);
        int players = player.level().getEntitiesOfClass(Player.class, box, p -> p != player && p.isAlive()).size();
        int animals = player.level().getEntitiesOfClass(Animal.class, box, LivingEntity::isAlive).size();
        return players + animals;
    }
}