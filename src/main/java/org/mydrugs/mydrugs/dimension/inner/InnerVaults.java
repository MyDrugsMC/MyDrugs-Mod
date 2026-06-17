package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Memory vaults (B1): small lootable ruins scattered deterministically across the island —
 * surface "echo shrines" and sunken "memory cellars". Geometry and positions live here (pure
 * functions of the slot seed) and are shared by {@link InnerVaultBuilder} (structure blocks,
 * both passes) and the server-side chest placement in the overlay queue (loot + marker gating).
 */
public final class InnerVaults {
    private static final long VAULT_SALT = 0x5641_554CL;
    private static final int CELL_SIZE = 224;
    /** Roughly half the cells host a vault. */
    private static final long CELL_SELECT_CHANCE = 512L;
    private static final double INNER_LIMIT = InnerDimensionConstants.CORE_RADIUS + 160.0D;
    private static final double OUTER_LIMIT = InnerDimensionConstants.ISLAND_RADIUS - 120.0D;
    /** Vault structure footprint radius (blocks). */
    public static final int RADIUS = 4;

    public static final ResourceKey<LootTable> VAULT_CALM = lootKey("chests/inner_vault_calm");
    public static final ResourceKey<LootTable> VAULT_DEEP = lootKey("chests/inner_vault_deep");
    public static final ResourceKey<LootTable> VAULT_DANGER = lootKey("chests/inner_vault_danger");

    private InnerVaults() {
    }

    private static ResourceKey<LootTable> lootKey(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, path));
    }

    /** @param cellar true = sunken memory cellar, false = surface echo shrine */
    public record Vault(int x, int z, boolean cellar, long hash, DrugId drug) {
        public String marker() {
            return "vault:" + x + ":" + z;
        }
    }

    public enum VaultTier {
        CALM,
        DEEP,
        DANGER
    }

    /** All vaults whose footprint could intersect the given chunk. */
    public static List<Vault> vaultsTouchingChunk(int centerX, int centerZ, int chunkMinX, int chunkMinZ) {
        long seed = InnerTerrain.seedForSlot(centerX, centerZ);
        List<Vault> vaults = new ArrayList<>(2);
        int cellMinX = Math.floorDiv(chunkMinX - RADIUS - 1, CELL_SIZE);
        int cellMaxX = Math.floorDiv(chunkMinX + 15 + RADIUS + 1, CELL_SIZE);
        int cellMinZ = Math.floorDiv(chunkMinZ - RADIUS - 1, CELL_SIZE);
        int cellMaxZ = Math.floorDiv(chunkMinZ + 15 + RADIUS + 1, CELL_SIZE);
        for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
            for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
                Vault vault = vaultForCell(seed, centerX, centerZ, cellX, cellZ);
                if (vault != null
                        && vault.x() + RADIUS >= chunkMinX && vault.x() - RADIUS <= chunkMinX + 15
                        && vault.z() + RADIUS >= chunkMinZ && vault.z() - RADIUS <= chunkMinZ + 15) {
                    vaults.add(vault);
                }
            }
        }
        return vaults;
    }

    private static Vault vaultForCell(long seed, int centerX, int centerZ, int cellX, int cellZ) {
        long hash = InnerNoise.mix64(seed + VAULT_SALT
                + (long) cellX * 341873128712L
                + (long) cellZ * 132897987541L);
        if ((hash & 1023L) >= CELL_SELECT_CHANCE) {
            return null;
        }
        int x = cellX * CELL_SIZE + CELL_SIZE / 2
                + (int) (InnerNoise.value(seed + VAULT_SALT + 5L, cellX, cellZ) * (CELL_SIZE * 0.3D));
        int z = cellZ * CELL_SIZE + CELL_SIZE / 2
                + (int) (InnerNoise.value(seed + VAULT_SALT + 9L, cellX, cellZ) * (CELL_SIZE * 0.3D));
        double distance = Math.hypot(x - centerX, z - centerZ);
        if (distance < INNER_LIMIT || distance > OUTER_LIMIT) {
            return null;
        }
        DrugId drug = InnerRegionMap.dominantDrug(centerX, centerZ, x, z);
        if (!groundSuitable(centerX, centerZ, x, z, drug)) {
            return null;
        }
        return new Vault(x, z, ((hash >>> 12) & 1L) == 0L, hash, drug);
    }

    /** The vault centre must be calm, open ground — never a path, water body, rift or set piece. */
    private static boolean groundSuitable(int centerX, int centerZ, int x, int z, DrugId drug) {
        InnerTerrain.Sample sample = InnerTerrain.sample(centerX, centerZ, x, z);
        if (!sample.land()
                || sample.lake()
                || sample.hole()
                || sample.pathStrength() > 0.30D
                || sample.scarStrength() > 0.40D) {
            return false;
        }
        InnerMegaForms.Form form = InnerMegaForms.formFor(centerX, centerZ, drug);
        if (form.distance(x, z) < form.radius() + InnerMegaForms.APRON + 8.0D) {
            return false;
        }
        if (InnerRegionMap.hasAngle(drug)) {
            var landmark = InnerRegionMap.landmarkFor(centerX, centerZ, drug);
            if (Math.hypot(x - landmark.getX(), z - landmark.getZ()) < 80.0D) {
                return false;
            }
        }
        return true;
    }

    /** Region tier decides what a vault's chest holds. */
    public static ResourceKey<LootTable> lootTableFor(DrugId drug) {
        return switch (tierFor(drug)) {
            case DANGER -> VAULT_DANGER;
            case DEEP -> VAULT_DEEP;
            case CALM -> VAULT_CALM;
        };
    }

    public static VaultTier tierFor(DrugId drug) {
        return switch (drug) {
            case COCAINE, METH -> VaultTier.DANGER;
            case ALCOHOL, LSD, MUSHROOMS -> VaultTier.DEEP;
            default -> VaultTier.CALM;
        };
    }

    public static boolean isUnlocked(Vault vault, InnerDimensionSavedData.IslandState island) {
        Set<DrugId> completed = island.completedInnerTrials();
        return switch (tierFor(vault.drug())) {
            case CALM -> hasAny(completed, DrugId.COFFEE, DrugId.WEED, DrugId.HASH, DrugId.MUSHROOMS);
            case DEEP -> hasAny(completed, DrugId.LSD, DrugId.ALCOHOL, DrugId.MUSHROOMS, DrugId.HASH);
            case DANGER -> hasAny(completed, DrugId.COCAINE, DrugId.METH, DrugId.TOBACCO);
        };
    }

    public static Vault vaultAtChest(
            InnerDimensionSavedData.IslandState island,
            BlockPos chestPos
    ) {
        ChunkPos chunk = new ChunkPos(chestPos);
        for (Vault vault : vaultsTouchingChunk(
                island.centerX(),
                island.centerZ(),
                chunk.getMinBlockX(),
                chunk.getMinBlockZ()
        )) {
            InnerTerrain.Sample centre = InnerTerrain.sample(
                    island.centerX(),
                    island.centerZ(),
                    vault.x(),
                    vault.z()
            );
            if (chestPos.getX() == vault.x()
                    && chestPos.getY() == chestY(vault, centre.topY())
                    && chestPos.getZ() == vault.z()) {
                return vault;
            }
        }
        return null;
    }

    private static boolean hasAny(Set<DrugId> completed, DrugId... drugs) {
        for (DrugId drug : drugs) {
            if (completed.contains(drug)) {
                return true;
            }
        }
        return false;
    }

    /** Y of the loot chest relative to the vault's ground level. */
    public static int chestY(Vault vault, int groundY) {
        return vault.cellar() ? groundY - 2 : groundY + 2;
    }

    /**
     * Server-side loot chest placement, called from the overlay queue's per-chunk pass. The ruin
     * structure itself is built by {@link InnerVaultBuilder} in both passes; the chest needs a
     * live level for its loot-table block entity, so it is placed here once per vault, gated by
     * the island marker so it never re-rolls or duplicates.
     */
    static void placeVaultChests(
            ServerLevel level,
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeVaultChests(level, data, island, chunkPos, count, InnerPlacement.PlacementMode.LIVE_OVERLAY);
    }

    static void placeVaultChests(
            ServerLevel level,
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        for (Vault vault : vaultsTouchingChunk(island.centerX(), island.centerZ(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ())) {
            if (vault.x() < chunkPos.getMinBlockX() || vault.x() > chunkPos.getMaxBlockX()
                    || vault.z() < chunkPos.getMinBlockZ() || vault.z() > chunkPos.getMaxBlockZ()) {
                continue; // the chest cell belongs to another chunk's pass
            }
            if (island.hasMarker(vault.marker())) {
                continue;
            }
            InnerTerrain.Sample centre = InnerTerrain.sample(island.centerX(), island.centerZ(), vault.x(), vault.z());
            BlockPos chestPos = new BlockPos(vault.x(), chestY(vault, centre.topY()), vault.z());
            if (!InnerPlacement.safeSet(level, chestPos, Blocks.CHEST.defaultBlockState(), true, count, mode)) {
                continue;
            }
            RandomizableContainer.setBlockEntityLootTable(level, level.getRandom(), chestPos, lootTableFor(vault.drug()));
            data.markStructurePlaced(island.owner(), vault.marker());
        }
    }
}
