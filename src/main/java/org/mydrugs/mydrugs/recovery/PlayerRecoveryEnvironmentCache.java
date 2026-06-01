package org.mydrugs.mydrugs.recovery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Per-player server-thread cache for room, safe-zone, companion, and recovery inventory context.
 * The full recovery-room anchor cube scan is expensive, so standing players reuse it briefly while
 * movement, chunk changes, dimension changes, and explicit room invalidation still refresh sooner.
 */
public final class PlayerRecoveryEnvironmentCache {
    private static final double MOVE_REFRESH_DISTANCE_SQR = 16.0D;
    private static final Map<ServerPlayer, CachedSnapshot> CACHE = new WeakHashMap<>();

    private PlayerRecoveryEnvironmentCache() {
    }

    public static PlayerEnvironmentSnapshot snapshot(ServerPlayer player) {
        long now = player.level().getGameTime();
        BlockPos pos = player.blockPosition();
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        CachedSnapshot cached = CACHE.get(player);
        if (cached != null && cached.validFor(player, pos, chunkX, chunkZ, now, ttlTicks())) {
            return cached.snapshot;
        }

        Optional<RecoveryRoomReport> report = RecoveryRoomManager.getBestRoom(player.level(), pos);
        RecoveryRoomReport room = report.orElse(null);
        RecoveryRoomManager.notifyRoomSeen(player, room);
        boolean inSafeZone = RecoveryRoomManager.isValidRecoveryRoom(room);
        int companions = SocialReliefManager.countCompanionsNow(player, AddictionConstants.COMPANION_DETECTION_RADIUS);
        InventorySnapshot inventory = scanRecoveryInventory(player.getInventory());

        PlayerEnvironmentSnapshot snapshot = new PlayerEnvironmentSnapshot(
                now,
                player.getUUID(),
                player.level().dimension(),
                pos.immutable(),
                chunkX,
                chunkZ,
                room,
                inSafeZone,
                companions,
                inventory.hasEdibleFood(),
                inventory.hasDiary(),
                inventory.hasHeadphones()
        );
        CACHE.put(player, new CachedSnapshot(snapshot));
        return snapshot;
    }

    public static void invalidate(ServerPlayer player) {
        if (player != null) {
            CACHE.remove(player);
        }
    }

    static void invalidateLevel(net.minecraft.world.level.Level level) {
        if (level == null || CACHE.isEmpty()) {
            return;
        }
        var dimension = level.dimension();
        Iterator<Map.Entry<ServerPlayer, CachedSnapshot>> iterator = CACHE.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().snapshot.dimension().equals(dimension)) {
                iterator.remove();
            }
        }
    }

    static int ttlTicksForTest() {
        return ttlTicks();
    }

    static boolean cacheWouldRefreshForTest(
            PlayerEnvironmentSnapshot snapshot,
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
            BlockPos pos,
            int chunkX,
            int chunkZ,
            long now,
            int ttl
    ) {
        return !new CachedSnapshot(snapshot).validFor(dimension, pos, chunkX, chunkZ, now, ttl);
    }

    private static int ttlTicks() {
        return Math.min(recoveryEnvironmentTtlTicks(), recoveryInventoryTtlTicks());
    }

    private static int recoveryEnvironmentTtlTicks() {
        try {
            return Math.max(1, Config.SERVER.recoveryEnvironmentCacheTicks.get());
        } catch (Throwable ignored) {
            return 20;
        }
    }

    private static int recoveryInventoryTtlTicks() {
        try {
            return Math.max(1, Config.SERVER.headphonesInventoryCacheTicks.get());
        } catch (Throwable ignored) {
            return 20;
        }
    }

    private static InventorySnapshot scanRecoveryInventory(Inventory inventory) {
        boolean hasFood = false;
        boolean hasDiary = false;
        boolean hasHeadphones = false;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            hasFood |= stack.has(DataComponents.FOOD);
            hasDiary |= stack.is(ModItems.PERSONAL_DIARY.get());
            hasHeadphones |= stack.is(ModItems.HEADPHONES.get());
            if (hasFood && hasDiary && hasHeadphones) {
                break;
            }
        }
        return new InventorySnapshot(hasFood, hasDiary, hasHeadphones);
    }

    private record CachedSnapshot(PlayerEnvironmentSnapshot snapshot) {
        boolean validFor(ServerPlayer player, BlockPos pos, int chunkX, int chunkZ, long now, int ttl) {
            return validFor(player.level().dimension(), pos, chunkX, chunkZ, now, ttl);
        }

        boolean validFor(
                net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
                BlockPos pos,
                int chunkX,
                int chunkZ,
                long now,
                int ttl
        ) {
            if (snapshot.gameTime() == now) {
                return snapshot.dimension().equals(dimension);
            }
            if (!snapshot.dimension().equals(dimension)) {
                return false;
            }
            if (now - snapshot.gameTime() >= ttl) {
                return false;
            }
            if (snapshot.chunkX() != chunkX || snapshot.chunkZ() != chunkZ) {
                return false;
            }
            return snapshot.playerPos().distSqr(pos) < MOVE_REFRESH_DISTANCE_SQR;
        }
    }

    private record InventorySnapshot(boolean hasEdibleFood, boolean hasDiary, boolean hasHeadphones) {
    }
}
