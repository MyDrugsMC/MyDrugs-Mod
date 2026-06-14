package org.mydrugs.mydrugs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;
import org.mydrugs.mydrugs.dimension.inner.InnerMegaForms;
import org.mydrugs.mydrugs.dimension.inner.InnerTerrain;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ambient life and weather for the Inner Dimension (B2/B3), per-player paced like the demon
 * encounters. Three strands, all server-side, capped, and config-gated:
 *
 * <ul>
 *   <li><b>The Storm</b> — visual-only lightning on the Fault's spires while a player is near the
 *       METH set piece. Never causes fire or damage; low frequency (sky-flash accessibility).</li>
 *   <li><b>Guardians</b> — a couple of Shroom Defenders patrolling the Mother Cap.</li>
 *   <li><b>Grazers</b> — stoned cows/mooshrooms drifting through the calm WEED/MUSHROOM fields.</li>
 * </ul>
 */
public final class InnerWildlifeManager {
    private static final Map<UUID, Integer> STORM_COOLDOWN = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> WILDLIFE_COOLDOWN = new ConcurrentHashMap<>();

    private static final double STORM_RANGE = 96.0D;
    private static final int STORM_MIN_DELAY = 20 * 8;
    private static final int STORM_RANDOM_DELAY = 20 * 12;
    private static final double GUARDIAN_RANGE = 64.0D;
    private static final int MAX_GUARDIANS = 2;
    private static final int MAX_GRAZERS_NEARBY = 4;
    private static final int WILDLIFE_MIN_DELAY = 20 * 25;
    private static final int WILDLIFE_RANDOM_DELAY = 20 * 35;

    private InnerWildlifeManager() {
    }

    public static void clear(ServerPlayer player) {
        if (player != null) {
            STORM_COOLDOWN.remove(player.getUUID());
            WILDLIFE_COOLDOWN.remove(player.getUUID());
        }
    }

    public static void clearAll() {
        STORM_COOLDOWN.clear();
        WILDLIFE_COOLDOWN.clear();
    }

    /** Per-tick hook for a player inside the Inner Dimension. Cheap until a cooldown elapses. */
    public static void tick(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        tickStorm(level, player);
        tickWildlife(level, player);
    }

    // -------------------------------------------------------------------------
    // B3 — the Fault storm
    // -------------------------------------------------------------------------

    private static void tickStorm(ServerLevel level, ServerPlayer player) {
        if (!Config.WORLDGEN.innerFaultStormEnabled.get()) {
            return;
        }
        UUID id = player.getUUID();
        int cooldown = STORM_COOLDOWN.getOrDefault(id, STORM_MIN_DELAY);
        if (cooldown > 0) {
            STORM_COOLDOWN.put(id, cooldown - 1);
            return;
        }
        STORM_COOLDOWN.put(id, STORM_MIN_DELAY + player.getRandom().nextInt(STORM_RANDOM_DELAY + 1));

        BlockPos pos = player.blockPosition();
        int centerX = InnerTerrain.slotCenter(pos.getX());
        int centerZ = InnerTerrain.slotCenter(pos.getZ());
        InnerMegaForms.Form fault = InnerMegaForms.formFor(centerX, centerZ, DrugId.METH);
        if (fault.distance(pos.getX(), pos.getZ()) > STORM_RANGE) {
            return;
        }
        int[][] spires = InnerMegaForms.faultSpireOffsets(fault);
        int[] spire = spires[player.getRandom().nextInt(spires.length)];
        if (!level.hasChunk(spire[0] >> 4, spire[1] >> 4)) {
            return;
        }
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, spire[0], spire[1]);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt == null) {
            return;
        }
        bolt.setPos(spire[0] + 0.5D, y, spire[1] + 0.5D);
        bolt.setVisualOnly(true); // never fire, never damage — pure spectacle
        level.addFreshEntity(bolt);
    }

    // -------------------------------------------------------------------------
    // B2 — guardians and grazers
    // -------------------------------------------------------------------------

    private static void tickWildlife(ServerLevel level, ServerPlayer player) {
        if (!Config.WORLDGEN.innerWildlifeEnabled.get()) {
            return;
        }
        UUID id = player.getUUID();
        int cooldown = WILDLIFE_COOLDOWN.getOrDefault(id, WILDLIFE_MIN_DELAY);
        if (cooldown > 0) {
            WILDLIFE_COOLDOWN.put(id, cooldown - 1);
            return;
        }
        WILDLIFE_COOLDOWN.put(id, WILDLIFE_MIN_DELAY + player.getRandom().nextInt(WILDLIFE_RANDOM_DELAY + 1));

        BlockPos pos = player.blockPosition();
        int centerX = InnerTerrain.slotCenter(pos.getX());
        int centerZ = InnerTerrain.slotCenter(pos.getZ());

        // Mother Cap guardians.
        InnerMegaForms.Form motherCap = InnerMegaForms.formFor(centerX, centerZ, DrugId.MUSHROOMS);
        if (motherCap.distance(pos.getX(), pos.getZ()) <= GUARDIAN_RANGE) {
            AABB capArea = new AABB(new BlockPos(motherCap.x(), pos.getY(), motherCap.z())).inflate(48.0D);
            int guardians = level.getEntitiesOfClass(ShroomDefenderEntity.class, capArea).size();
            if (guardians < MAX_GUARDIANS) {
                spawnMob(level, player, ModEntities.SHROOM_DEFENDER.get(),
                        motherCap.x(), motherCap.z(), 10.0D + player.getRandom().nextInt(8));
            }
            return;
        }

        // Grazing dream-fauna in the calm fields.
        InnerAtmosphere.Sample atmosphere = InnerAtmosphere.sample(centerX, centerZ, pos);
        DrugId region = atmosphere.dominantDrug();
        if ((region != DrugId.WEED && region != DrugId.MUSHROOMS) || atmosphere.danger() > 0.30D) {
            return;
        }
        AABB area = new AABB(pos).inflate(64.0D);
        int grazers = level.getEntitiesOfClass(StonedCowEntity.class, area).size()
                + level.getEntitiesOfClass(StonedMooshroomEntity.class, area).size();
        if (grazers >= MAX_GRAZERS_NEARBY) {
            return;
        }
        EntityType<? extends Mob> type = region == DrugId.MUSHROOMS
                ? ModEntities.STONED_MOOSHROOM.get()
                : ModEntities.STONED_COW.get();
        spawnMob(level, player, type, pos.getX(), pos.getZ(), 18.0D + player.getRandom().nextInt(14));
    }

    private static void spawnMob(
            ServerLevel level,
            ServerPlayer player,
            EntityType<? extends Mob> type,
            int anchorX,
            int anchorZ,
            double distance
    ) {
        double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
        int x = anchorX + (int) Math.round(Math.cos(angle) * distance);
        int z = anchorZ + (int) Math.round(Math.sin(angle) * distance);
        if (!level.hasChunk(x >> 4, z >> 4)) {
            return;
        }
        int centerX = InnerTerrain.slotCenter(x);
        int centerZ = InnerTerrain.slotCenter(z);
        InnerTerrain.Sample sample = InnerTerrain.sample(centerX, centerZ, x, z);
        if (!sample.land() || sample.lake() || sample.hole()) {
            return;
        }
        Mob mob = type.create(level, EntitySpawnReason.TRIGGERED);
        if (mob == null) {
            return;
        }
        mob.setPos(x + 0.5D, sample.topY() + 1.0D, z + 0.5D);
        mob.setYRot(level.random.nextFloat() * 360.0F);
        level.addFreshEntity(mob);
    }
}
