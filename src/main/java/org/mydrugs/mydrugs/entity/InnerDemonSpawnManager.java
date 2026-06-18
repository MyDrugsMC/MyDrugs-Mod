package org.mydrugs.mydrugs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.manager.state.BadTripState;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;
import org.mydrugs.mydrugs.dimension.inner.InnerIslandContext;
import org.mydrugs.mydrugs.dimension.inner.InnerScarHealer;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;
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
    private static final double MIN_SPAWN_DISTANCE_SQR = 7.0D * 7.0D;
    private static final double LOOK_LINE_REJECT_DISTANCE_SQR = 12.0D * 12.0D;
    private static final double LOOK_LINE_DOT_THRESHOLD = 0.965D;
    private static final int PENDING_RETURN_GRACE_TICKS = 20 * 12;

    // B3: sparse, atmosphere-danger-weighted symbolic encounters while wandering the inner
    // dimension (distinct from the bad-trip path above). Per-player cooldown in ticks; entries are
    // dropped on overworld return and cleared on server stop (see clearInnerAmbientState).
    private static final Map<UUID, Integer> INNER_AMBIENT_COOLDOWN = new ConcurrentHashMap<>();
    private static final Map<UUID, DemonCountCache> DEMON_COUNT_CACHE = new ConcurrentHashMap<>();
    private static final int INNER_AMBIENT_GRACE_TICKS = 20 * 30;
    private static final double INNER_AMBIENT_DANGER_GATE = 0.45D;
    private static final int DEMON_COUNT_CACHE_TICKS = 10;
    private static final double DEMON_COUNT_MOVE_REFRESH_DISTANCE_SQR = 64.0D;

    // Ambient danger weighting: completing a trial or restoring a scar cell lowers the local spawn
    // pressure without touching the pure terrain/atmosphere sampler used by worldgen and the client.
    private static final double COMPLETED_TRIAL_DANGER_MULTIPLIER = 0.4D;
    private static final double RESTORED_SCAR_DANGER_MULTIPLIER = 0.25D;
    // Ambient spawn chance above the gate: scales with danger, capped so it never feels relentless.
    private static final double AMBIENT_CHANCE_PER_DANGER = 0.6D;
    private static final double AMBIENT_CHANCE_MAX = 0.5D;
    // Ambient encounter pacing (ticks): a base delay plus a random spread between encounters.
    private static final int AMBIENT_DELAY_MIN_TICKS = 20 * 18;
    private static final int AMBIENT_DELAY_RANDOM_TICKS = 20 * 24;
    // Recovery-room hallucination suppression pushes the next bad-trip spawn out by these minimums.
    private static final int RECOVERY_FIRST_SPAWN_MIN_TICKS = 20 * 5;
    private static final int RECOVERY_NEXT_ATTEMPT_MIN_TICKS = 20 * 8;
    // Bad-trip demon spawn chances and attempt pacing (violent severity is more frequent).
    private static final float BAD_TRIP_VIOLENT_SPAWN_CHANCE = 0.25F;
    private static final float BAD_TRIP_STRONG_SPAWN_CHANCE = 0.10F;
    private static final int BAD_TRIP_VIOLENT_ATTEMPT_MIN_TICKS = 20 * 15;
    private static final int BAD_TRIP_STRONG_ATTEMPT_MIN_TICKS = 20 * 20;
    private static final int BAD_TRIP_ATTEMPT_RANDOM_TICKS = 20 * 10;
    // How long owned demons linger before despawning once their bad trip ends.
    private static final int OWNED_DEMON_DESPAWN_DELAY_TICKS = 20 * 25;
    // Radius (blocks) used when collecting a player's owned demons for counting/despawn.
    private static final double OWNED_DEMON_SEARCH_RADIUS = 96.0D;

    private InnerDemonSpawnManager() {
    }

    /** Give the player a grace period before any ambient encounter when they enter the dimension. */
    public static void primeInnerAmbient(ServerPlayer player) {
        if (player != null) {
            INNER_AMBIENT_COOLDOWN.put(player.getUUID(), INNER_AMBIENT_GRACE_TICKS);
        }
    }

    /** Extend the current ambient encounter grace without shortening an existing longer delay. */
    public static void extendInnerAmbientGrace(ServerPlayer player, int graceTicks) {
        if (player != null && graceTicks > 0) {
            INNER_AMBIENT_COOLDOWN.merge(player.getUUID(), graceTicks, Math::max);
        }
    }

    /** Briefly suppress ambient encounters after a trial asks the player to return to the anchor. */
    public static void primePendingTrialReturnGrace(ServerPlayer player) {
        extendInnerAmbientGrace(player, PENDING_RETURN_GRACE_TICKS);
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
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        InnerIslandContext context = InnerIslandContext.resolve(player, data);
        if (context == null) {
            return;
        }
        int centerX = context.centerX();
        int centerZ = context.centerZ();
        InnerAtmosphere.Sample atmosphere = InnerAtmosphere.sample(centerX, centerZ, pos);
        InnerDimensionSavedData.IslandState island = context.island();
        boolean completedTrial = island != null && island.hasCompletedInnerTrial(atmosphere.dominantDrug());
        boolean restoredScar = island != null && InnerScarHealer.isRestoredAt(island, pos);
        double danger = effectiveAmbientDanger(atmosphere.danger(), completedTrial, restoredScar);
        if (danger < INNER_AMBIENT_DANGER_GATE) {
            return;
        }
        float chance = ambientSpawnChance(danger);
        if (player.getRandom().nextFloat() >= chance) {
            return;
        }
        DemonCounts counts = demonCounts(level, player);
        if (counts.boundToPlayer() >= MAX_BOUND_TO_PLAYER || counts.nearby() >= MAX_NEARBY) {
            return;
        }
        BlockPos spawnPos = findSpawnPos(level, player);
        if (spawnPos != null) {
            telegraphAmbientSpawn(level, spawnPos);
            spawn(level, player, spawnPos, false, false);
            DEMON_COUNT_CACHE.remove(player.getUUID());
        }
    }

    private static int nextInnerAmbientDelay(ServerPlayer player) {
        return AMBIENT_DELAY_MIN_TICKS + player.getRandom().nextInt(AMBIENT_DELAY_RANDOM_TICKS + 1);
    }

    /**
     * Local ambient danger after gameplay relief is applied: a completed trial and a restored scar
     * each scale the raw atmosphere danger down. Pure function so the weighting is unit-testable.
     */
    static double effectiveAmbientDanger(double rawDanger, boolean completedTrial, boolean restoredScar) {
        double danger = rawDanger;
        if (completedTrial) {
            danger *= COMPLETED_TRIAL_DANGER_MULTIPLIER;
        }
        if (restoredScar) {
            danger *= RESTORED_SCAR_DANGER_MULTIPLIER;
        }
        return danger;
    }

    /** Ambient encounter chance for a given (already weighted) danger above the gate. Pure function. */
    static float ambientSpawnChance(double danger) {
        return (float) Mth.clamp((danger - INNER_AMBIENT_DANGER_GATE) * AMBIENT_CHANCE_PER_DANGER,
                0.0D, AMBIENT_CHANCE_MAX);
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
            state.firstDemonSpawnDelay = Math.max(state.firstDemonSpawnDelay, RECOVERY_FIRST_SPAWN_MIN_TICKS);
            state.nextDemonSpawnAttempt = Math.max(state.nextDemonSpawnAttempt, RECOVERY_NEXT_ATTEMPT_MIN_TICKS);
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
        float chance = violent ? BAD_TRIP_VIOLENT_SPAWN_CHANCE : BAD_TRIP_STRONG_SPAWN_CHANCE;
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
        AABB area = new AABB(player.blockPosition()).inflate(OWNED_DEMON_SEARCH_RADIUS);
        for (InnerDemonEntity demon : level.getEntitiesOfClass(InnerDemonEntity.class, area, demon -> demon.isOwnedBy(player.getUUID()))) {
            demon.beginNaturalDespawn(OWNED_DEMON_DESPAWN_DELAY_TICKS);
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
            double distance = 7.0D + player.getRandom().nextDouble() * 9.0D;
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            int y = Mth.clamp(surfaceY, level.getMinY() + 1, level.getMaxY() - 2);
            for (int dy = 0; dy <= 2; dy++) {
                BlockPos pos = new BlockPos(x, y + dy, z);
                if (isAcceptableSpawnPos(level, pos, player)) {
                    return pos;
                }
            }
        }
        return null;
    }

    private static boolean isAcceptableSpawnPos(ServerLevel level, BlockPos pos, ServerPlayer player) {
        BlockPos headPos = pos.above();
        BlockPos belowPos = pos.below();
        BlockState feetState = level.getBlockState(pos);
        BlockState headState = level.getBlockState(headPos);
        BlockState belowState = level.getBlockState(belowPos);
        boolean feetOpen = isOpenSpawnSpace(level, pos, feetState);
        boolean headOpen = isOpenSpawnSpace(level, headPos, headState);
        boolean supportSturdy = belowState.isFaceSturdy(level, belowPos, Direction.UP);
        if (!isSpawnGeometryAcceptable(
                pos.distSqr(player.blockPosition()),
                pos.getY(),
                level.getMinY(),
                level.getMaxY(),
                feetOpen,
                headOpen,
                supportSturdy,
                isDangerousBlock(belowState),
                isSafeSpawnFluid(feetState.getFluidState()),
                isSafeSpawnFluid(headState.getFluidState())
        )) {
            return false;
        }
        if (isDangerousBlock(feetState) || isDangerousBlock(headState)) {
            return false;
        }
        AABB body = new AABB(
                pos.getX() + 0.20D,
                pos.getY(),
                pos.getZ() + 0.20D,
                pos.getX() + 0.80D,
                pos.getY() + 1.95D,
                pos.getZ() + 0.80D
        );
        return level.noCollision(body) && !isTooCloseToVisibleLookLine(level, pos, player);
    }

    private static boolean isSpawnGeometryAcceptable(
            double distanceToPlayerSqr,
            int feetY,
            int minY,
            int maxY,
            boolean feetOpen,
            boolean headOpen,
            boolean supportSturdy,
            boolean supportDangerous,
            boolean feetFluidSafe,
            boolean headFluidSafe
    ) {
        return distanceToPlayerSqr >= MIN_SPAWN_DISTANCE_SQR
                && feetY > minY
                && feetY + 1 < maxY
                && feetOpen
                && headOpen
                && supportSturdy
                && !supportDangerous
                && feetFluidSafe
                && headFluidSafe;
    }

    private static boolean isOpenSpawnSpace(ServerLevel level, BlockPos pos, BlockState state) {
        return state.isAir()
                || (!state.blocksMotion() && state.getCollisionShape(level, pos).isEmpty());
    }

    private static boolean isSafeSpawnFluid(FluidState fluid) {
        return fluid == null || (fluid.isEmpty() && !fluid.is(FluidTags.LAVA));
    }

    private static boolean isDangerousBlock(BlockState state) {
        if (state == null) {
            return false;
        }
        if (state.is(ModInnerDimensionBlocks.REDLINE_THORN.get())
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.LAVA)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.WITHER_ROSE)
                || state.is(Blocks.POWDER_SNOW)) {
            return true;
        }
        return state.getBlock() instanceof CampfireBlock;
    }

    private static boolean isTooCloseToVisibleLookLine(ServerLevel level, BlockPos pos, ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 center = Vec3.atCenterOf(pos).add(0.0D, 0.35D, 0.0D);
        Vec3 toSpawn = center.subtract(eye);
        double distanceSqr = toSpawn.lengthSqr();
        if (distanceSqr > LOOK_LINE_REJECT_DISTANCE_SQR || distanceSqr <= 0.0001D) {
            return false;
        }
        Vec3 direction = toSpawn.normalize();
        if (look.dot(direction) < LOOK_LINE_DOT_THRESHOLD) {
            return false;
        }
        HitResult result = level.clip(new ClipContext(
                eye,
                center,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));
        return result.getType() == HitResult.Type.MISS
                || result.getLocation().distanceToSqr(center) < 0.35D;
    }

    private static void telegraphAmbientSpawn(ServerLevel level, BlockPos pos) {
        level.sendParticles(
                ParticleTypes.SMOKE,
                pos.getX() + 0.5D,
                pos.getY() + 0.25D,
                pos.getZ() + 0.5D,
                6,
                0.24D,
                0.08D,
                0.24D,
                0.01D
        );
        level.playSound(
                null,
                pos,
                SoundEvents.SOUL_ESCAPE.value(),
                SoundSource.HOSTILE,
                0.18F,
                0.55F
        );
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
        AABB area = new AABB(player.blockPosition()).inflate(OWNED_DEMON_SEARCH_RADIUS);
        return level.getEntitiesOfClass(InnerDemonEntity.class, area, demon -> demon.isOwnedBy(player.getUUID())).size();
    }

    private static int countNearby(ServerLevel level, ServerPlayer player) {
        AABB area = new AABB(player.blockPosition()).inflate(NEARBY_RADIUS);
        List<InnerDemonEntity> demons = level.getEntitiesOfClass(InnerDemonEntity.class, area);
        return demons.size();
    }

    private static int nextAttemptDelay(ServerPlayer player, boolean violent) {
        int min = violent ? BAD_TRIP_VIOLENT_ATTEMPT_MIN_TICKS : BAD_TRIP_STRONG_ATTEMPT_MIN_TICKS;
        return min + player.getRandom().nextInt(BAD_TRIP_ATTEMPT_RANDOM_TICKS + 1);
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

    static boolean isSpawnGeometryAcceptableForTest(
            double distanceToPlayerSqr,
            int feetY,
            int minY,
            int maxY,
            boolean feetOpen,
            boolean headOpen,
            boolean supportSturdy,
            boolean supportDangerous,
            boolean feetFluidSafe,
            boolean headFluidSafe
    ) {
        return isSpawnGeometryAcceptable(
                distanceToPlayerSqr,
                feetY,
                minY,
                maxY,
                feetOpen,
                headOpen,
                supportSturdy,
                supportDangerous,
                feetFluidSafe,
                headFluidSafe
        );
    }
}
