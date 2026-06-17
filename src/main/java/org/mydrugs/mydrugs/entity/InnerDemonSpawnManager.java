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
import org.mydrugs.mydrugs.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.manager.state.BadTripState;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;
import org.mydrugs.mydrugs.dimension.inner.InnerScarHealer;
import org.mydrugs.mydrugs.dimension.inner.InnerTerrain;
import org.mydrugs.mydrugs.recovery.RecoveryRoomManager;
import org.mydrugs.mydrugs.recovery.RecoveryRoomReport;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InnerDemonSpawnManager {
    private static final int MAX_BOUND_TO_PLAYER = 3;
    private static final int MAX_NEARBY = 6;
    private static final double NEARBY_RADIUS = 32.0D;

    // B3: sparse, atmosphere-danger-weighted symbolic encounters while wandering the inner
    // dimension (distinct from the bad-trip path above). Per-player cooldown in ticks; entries are
    // dropped on overworld return and cleared on server stop (see clearInnerAmbientState).
    private static final Map<UUID, Integer> INNER_AMBIENT_COOLDOWN = new ConcurrentHashMap<>();
    private static final Map<UUID, DemonCountCache> DEMON_COUNT_CACHE = new ConcurrentHashMap<>();
    private static final int INNER_AMBIENT_GRACE_TICKS = 20 * 30;
    private static final double INNER_AMBIENT_DANGER_GATE = 0.45D;
    private static final int DEMON_COUNT_CACHE_TICKS = 10;
    private static final double DEMON_COUNT_MOVE_REFRESH_DISTANCE_SQR = 64.0D;

    private InnerDemonSpawnManager() {
    }

    /** Give the player a grace period before any ambient encounter when they enter the dimension. */
    public static void primeInnerAmbient(ServerPlayer player) {
        if (player != null) {
            INNER_AMBIENT_COOLDOWN.put(player.getUUID(), INNER_AMBIENT_GRACE_TICKS);
        }
    }

    /** Forget a player's ambient cooldown (e.g. when they leave the inner dimension). */
    public static void clearInnerAmbient(ServerPlayer player) {
        if (player != null) {
            INNER_AMBIENT_COOLDOWN.remove(player.getUUID());
            DEMON_COUNT_CACHE.remove(player.getUUID());
        }
    }

    /** Drop all ambient cooldown state. Call on server stop to avoid leaking across world loads. */
    public static void clearInnerAmbientState() {
        INNER_AMBIENT_COOLDOWN.clear();
        DEMON_COUNT_CACHE.clear();
    }

    /**
     * Per-tick hook for a player standing in the inner dimension. The atmosphere {@code danger} at
     * the player's position (high in scarred / hard-drug regions) acts as the spawn weight: it
     * gates and scales the chance of a sparse symbolic encounter. The relatively expensive
     * atmosphere sample only runs when the cooldown elapses, not every tick.
     */
    public static void tickInnerAmbient(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        UUID id = player.getUUID();
        int cooldown = INNER_AMBIENT_COOLDOWN.getOrDefault(id, INNER_AMBIENT_GRACE_TICKS);
        if (cooldown > 0) {
            INNER_AMBIENT_COOLDOWN.put(id, cooldown - 1);
            return;
        }
        INNER_AMBIENT_COOLDOWN.put(id, nextInnerAmbientDelay(player));

        BlockPos pos = player.blockPosition();
        int centerX = InnerTerrain.slotCenter(pos.getX());
        int centerZ = InnerTerrain.slotCenter(pos.getZ());
        InnerAtmosphere.Sample atmosphere = InnerAtmosphere.sample(centerX, centerZ, pos);
        double danger = atmosphere.danger();
        // Completed trials and manually restored scar cells lower pressure without changing the
        // pure terrain/atmosphere sampler used by worldgen and client presentation.
        InnerDimensionSavedData.IslandState island =
                InnerDimensionSavedData.get(level).findIslandBySlot(centerX, centerZ);
        if (island != null && island.hasCompletedInnerTrial(atmosphere.dominantDrug())) {
            danger *= 0.4D;
        }
        if (island != null && InnerScarHealer.isRestoredAt(island, pos)) {
            danger *= 0.25D;
        }
        if (danger < INNER_AMBIENT_DANGER_GATE) {
            return;
        }
        float chance = (float) Mth.clamp((danger - INNER_AMBIENT_DANGER_GATE) * 0.6D, 0.0D, 0.5D);
        if (player.getRandom().nextFloat() >= chance) {
            return;
        }
        DemonCounts counts = demonCounts(level, player);
        if (counts.boundToPlayer() >= MAX_BOUND_TO_PLAYER || counts.nearby() >= MAX_NEARBY) {
            return;
        }
        BlockPos spawnPos = findSpawnPos(level, player);
        if (spawnPos != null) {
            spawn(level, player, spawnPos, false, false);
            DEMON_COUNT_CACHE.remove(player.getUUID());
        }
    }

    private static int nextInnerAmbientDelay(ServerPlayer player) {
        return 20 * 18 + player.getRandom().nextInt(20 * 24 + 1);
    }

    public static void tickBadTrip(ServerPlayer player, PlayerAddictionStats stats) {
        tickBadTrip(player, stats, null);
    }

    public static void tickBadTrip(ServerPlayer player, PlayerAddictionStats stats, RecoveryRoomReport recoveryRoom) {
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

        if (RecoveryRoomManager.suppressesHostileHallucinations(recoveryRoom)) {
            state.firstDemonSpawnDelay = Math.max(state.firstDemonSpawnDelay, 20 * 5);
            state.nextDemonSpawnAttempt = Math.max(state.nextDemonSpawnAttempt, 20 * 8);
            return;
        }

        if (state.firstDemonSpawnDelay > 0) {
            state.firstDemonSpawnDelay--;
            if (state.firstDemonSpawnDelay <= 0) {
                state.firstDemonSpawnDelay = -1;
                DemonCounts counts = demonCounts(level, player);
                int boundCount = counts.boundToPlayer();
                int nearbyCount = counts.nearby();
                if (boundCount < MAX_BOUND_TO_PLAYER && nearbyCount < MAX_NEARBY) {
                    spawnForBadTrip(level, player);
                    DEMON_COUNT_CACHE.remove(player.getUUID());
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

        DemonCounts counts = demonCounts(level, player);
        int boundCount = counts.boundToPlayer();
        int nearbyCount = counts.nearby();
        if (boundCount >= MAX_BOUND_TO_PLAYER || nearbyCount >= MAX_NEARBY) {
            return;
        }

        int desired = violent ? 1 + player.getRandom().nextInt(3) : 1;
        int allowed = Math.min(desired, Math.min(MAX_BOUND_TO_PLAYER - boundCount, MAX_NEARBY - nearbyCount));
        for (int i = 0; i < allowed; i++) {
            spawnForBadTrip(level, player);
        }
        DEMON_COUNT_CACHE.remove(player.getUUID());
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
        if (player != null) {
            org.mydrugs.mydrugs.psyche.PsycheMapMilestones.innerDemon(player);
        }
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

    private static DemonCounts demonCounts(ServerLevel level, ServerPlayer player) {
        UUID id = player.getUUID();
        long now = level.getGameTime();
        BlockPos pos = player.blockPosition();
        DemonCountCache cached = DEMON_COUNT_CACHE.get(id);
        if (cached != null && cached.validFor(level, pos, now)) {
            return cached.counts;
        }
        DemonCounts counts = new DemonCounts(
                countBoundToPlayer(level, player),
                countNearby(level, player)
        );
        DEMON_COUNT_CACHE.put(id, new DemonCountCache(level.dimension(), pos.immutable(), now, counts));
        return counts;
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

    private record DemonCounts(int boundToPlayer, int nearby) {
    }

    private record DemonCountCache(
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
            BlockPos pos,
            long gameTime,
            DemonCounts counts
    ) {
        boolean validFor(ServerLevel level, BlockPos currentPos, long now) {
            return dimension.equals(level.dimension())
                    && now - gameTime < DEMON_COUNT_CACHE_TICKS
                    && pos.distSqr(currentPos) < DEMON_COUNT_MOVE_REFRESH_DISTANCE_SQR;
        }
    }
}
