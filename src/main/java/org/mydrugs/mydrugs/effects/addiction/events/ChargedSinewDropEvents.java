package org.mydrugs.mydrugs.effects.addiction.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks the moment a hostile/agile mob first targets a player, and grants a 5% CHARGED_SINEW drop
 * if that same player kills the mob within 100 ticks (5 seconds).
 */
@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ChargedSinewDropEvents {
    private static final long AGGRO_WINDOW_TICKS = 100L;
    private static final float DROP_CHANCE = 0.05F;
    private static final double MIN_SPEED_FOR_THEME = 0.23D;

    /** mobUUID -> aggro entry */
    private static final Map<UUID, AggroEntry> AGGRO = new HashMap<>();

    private ChargedSinewDropEvents() {
    }

    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!(event.getNewAboutToBeSetTarget() instanceof ServerPlayer player)) return;
        if (!(mob.level() instanceof ServerLevel serverLevel)) return;
        if (!isEligible(mob)) return;

        long now = serverLevel.getGameTime();
        cleanupStale(now);
        AGGRO.put(mob.getUUID(), new AggroEntry(player.getUUID(), now));
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;
        AggroEntry entry = AGGRO.remove(entity.getUUID());
        if (entry == null) return;

        long now = serverLevel.getGameTime();
        if (now - entry.aggroAt > AGGRO_WINDOW_TICKS) return;

        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        if (!killer.getUUID().equals(entry.playerId)) return;

        if (serverLevel.random.nextFloat() >= DROP_CHANCE) return;

        ItemStack drop = new ItemStack(ModItems.CHARGED_SINEW.get());
        entity.spawnAtLocation(serverLevel, drop);
    }

    private static boolean isEligible(Mob mob) {
        if (mob instanceof net.minecraft.world.entity.animal.Animal) return false;
        var speedAttr = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null) return false;
        return speedAttr.getValue() >= MIN_SPEED_FOR_THEME;
    }

    private static void cleanupStale(long now) {
        Iterator<Map.Entry<UUID, AggroEntry>> it = AGGRO.entrySet().iterator();
        while (it.hasNext()) {
            AggroEntry e = it.next().getValue();
            if (now - e.aggroAt > AGGRO_WINDOW_TICKS * 4) {
                it.remove();
            }
        }
    }

    private record AggroEntry(UUID playerId, long aggroAt) {
    }
}
