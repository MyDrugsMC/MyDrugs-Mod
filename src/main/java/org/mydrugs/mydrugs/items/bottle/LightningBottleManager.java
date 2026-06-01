package org.mydrugs.mydrugs.items.bottle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * While a player holds a THUNDER_BOTTLE during a thunderstorm with sky visibility,
 * roll 5% per tick to spawn a real lightning bolt on the player and convert
 * one THUNDER_BOTTLE into one LIGHTNING_BOTTLE. Per-player cooldown prevents chain conversion.
 */
public final class LightningBottleManager {
    private static final float STRIKE_CHANCE_PER_TICK = 0.05F;
    private static final long COOLDOWN_TICKS = 20L;
    private static final long CHECK_INTERVAL_TICKS = 5L;
    private static final float STRIKE_CHANCE_PER_CHECK =
            1.0F - (float) Math.pow(1.0F - STRIKE_CHANCE_PER_TICK, CHECK_INTERVAL_TICKS);

    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    private LightningBottleManager() {
    }

    public static void tick(ServerPlayer player) {
        if (player.isSpectator()) return;
        InteractionHand hand = findThunderBottleHand(player);
        if (hand == null) return;
        ServerLevel level = player.level();
        long now = level.getGameTime();
        if ((now + player.getId()) % CHECK_INTERVAL_TICKS != 0L) return;
        if (!level.isThundering()) return;
        Long last = COOLDOWNS.get(player.getUUID());
        if (last != null && now - last < COOLDOWN_TICKS) return;
        BlockPos head = player.blockPosition().above();
        if (!level.canSeeSky(head)) return;

        if (level.random.nextFloat() >= STRIKE_CHANCE_PER_CHECK) return;

        // Strike lightning at the player
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.snapTo(player.getX(), player.getY(), player.getZ());
            level.addFreshEntity(bolt);
        }

        ItemStack held = player.getItemInHand(hand);
        held.shrink(1);
        ItemStack converted = new ItemStack(ModItems.LIGHTNING_BOTTLE.get());
        if (!player.getInventory().add(converted)) {
            player.drop(converted, false);
        }

        COOLDOWNS.put(player.getUUID(), now);
    }

    private static InteractionHand findThunderBottleHand(ServerPlayer player) {
        if (player.getMainHandItem().is(ModItems.THUNDER_BOTTLE.get())) return InteractionHand.MAIN_HAND;
        if (player.getOffhandItem().is(ModItems.THUNDER_BOTTLE.get())) return InteractionHand.OFF_HAND;
        return null;
    }
}
