package org.mydrugs.mydrugs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.effects.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.manager.state.BadTripState;

import java.util.List;

public final class InnerDemonSpawnManager {
    private static final int MAX_BOUND_TO_PLAYER = 3;
    private static final int MAX_NEARBY = 6;
    private static final double NEARBY_RADIUS = 32.0D;

    private InnerDemonSpawnManager() {
    }

    public static void tickBadTrip(ServerPlayer player, PlayerAddictionStats stats) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        BadTripState state = stats.badTrip;
        if (!state.active || state.sourceDrug == DrugId.ALCOHOL) {
            markOwnedDemonsForDespawn(player);
            return;
        }

        boolean violent = state.severity >= AddictionConstants.BAD_TRIP_VIOLENT_THRESHOLD;
        boolean strong = state.severity >= AddictionConstants.BAD_TRIP_STRONG_THRESHOLD;
        if (!strong) {
            return;
        }

        int boundCount = countBoundToPlayer(level, player);
        int nearbyCount = countNearby(level, player);

        if (state.firstDemonSpawnDelay > 0) {
            state.firstDemonSpawnDelay--;
            if (state.firstDemonSpawnDelay <= 0) {
                state.firstDemonSpawnDelay = -1;
                if (boundCount < MAX_BOUND_TO_PLAYER && nearbyCount < MAX_NEARBY) {
                    spawnForBadTrip(level, player);
                    state.nextDemonSpawnAttempt = nextAttemptDelay(player, violent);
                    return;
                }
            } else {
                return;
            }
        }

        if (state.nextDemonSpawnAttempt > 0) {
            state.nextDemonSpawnAttempt--;
            return;
        }

        state.nextDemonSpawnAttempt = nextAttemptDelay(player, violent);
        float chance = violent ? 0.25F : 0.10F;
        if (player.getRandom().nextFloat() >= chance) {
            return;
        }

        if (boundCount >= MAX_BOUND_TO_PLAYER || nearbyCount >= MAX_NEARBY) {
            return;
        }

        int desired = violent ? 1 + player.getRandom().nextInt(3) : 1;
        int allowed = Math.min(desired, Math.min(MAX_BOUND_TO_PLAYER - boundCount, MAX_NEARBY - nearbyCount));
        for (int i = 0; i < allowed; i++) {
            spawnForBadTrip(level, player);
        }
    }

    public static boolean spawnDebug(ServerPlayer player, boolean droppable) {
        if (!(player.level() instanceof ServerLevel level)) {
            return false;
        }
        BlockPos pos = findSpawnPos(level, player);
        if (pos == null) {
            pos = player.blockPosition().offset(2, 1, 0);
        }
        return spawn(level, player, pos, droppable, false) != null;
    }

    public static void markOwnedDemonsForDespawn(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        AABB area = new AABB(player.blockPosition()).inflate(96.0D);
        for (InnerDemonEntity demon : level.getEntitiesOfClass(InnerDemonEntity.class, area, demon -> demon.isOwnedBy(player.getUUID()))) {
            demon.beginNaturalDespawn(20 * 25);
        }
    }

    private static void spawnForBadTrip(ServerLevel level, ServerPlayer player) {
        BlockPos pos = findSpawnPos(level, player);
        if (pos == null) {
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            int x = Mth.floor(player.getX() + Math.cos(angle) * 7.0D);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * 7.0D);
            int y = Mth.clamp(
                    Mth.floor(player.getY()) + 1,
                    level.getMinY() + 2,
                    level.getMaxY() - 2
            );
            pos = new BlockPos(x, y, z);
        }
        spawn(level, player, pos, true, true);
    }

    private static InnerDemonEntity spawn(ServerLevel level, ServerPlayer player, BlockPos pos, boolean droppable, boolean badTripBound) {
        InnerDemonEntity demon = ModEntities.INNER_DEMON.get().create(level, EntitySpawnReason.TRIGGERED);
        if (demon == null) {
            return null;
        }
        demon.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        demon.setYRot(level.random.nextFloat() * 360.0F);
        demon.setXRot(0.0F);
        demon.configure(player, droppable, badTripBound);
        level.addFreshEntity(demon);
        return demon;
    }

    private static BlockPos findSpawnPos(ServerLevel level, ServerPlayer player) {
        for (int tries = 0; tries < 16; tries++) {
            double distance = 6.0D + player.getRandom().nextDouble() * 8.0D;
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
            int y = Mth.clamp(
                    Mth.floor(player.getY()) + player.getRandom().nextInt(6) - 2,
                    level.getMinY() + 2,
                    level.getMaxY() - 2
            );
            BlockPos pos = new BlockPos(x, y, z);
            if (isAcceptableSpawnPos(level, pos, player)) {
                return pos;
            }
        }
        return null;
    }

    private static boolean isAcceptableSpawnPos(ServerLevel level, BlockPos pos, ServerPlayer player) {
        if (pos.distSqr(player.blockPosition()) < 25.0D || pos.getY() <= level.getMinY() + 1) {
            return false;
        }
        FluidState fluid = level.getFluidState(pos);
        return !fluid.is(FluidTags.LAVA);
    }

    private static int countBoundToPlayer(ServerLevel level, ServerPlayer player) {
        AABB area = new AABB(player.blockPosition()).inflate(96.0D);
        return level.getEntitiesOfClass(InnerDemonEntity.class, area, demon -> demon.isOwnedBy(player.getUUID())).size();
    }

    private static int countNearby(ServerLevel level, ServerPlayer player) {
        AABB area = new AABB(player.blockPosition()).inflate(NEARBY_RADIUS);
        List<InnerDemonEntity> demons = level.getEntitiesOfClass(InnerDemonEntity.class, area);
        return demons.size();
    }

    private static int nextAttemptDelay(ServerPlayer player, boolean violent) {
        int min = violent ? 20 * 15 : 20 * 20;
        int random = violent ? 20 * 10 : 20 * 10;
        return min + player.getRandom().nextInt(random + 1);
    }
}
