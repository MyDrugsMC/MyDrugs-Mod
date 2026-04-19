package org.mydrugs.mydrugs.effects.addiction.manager;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.core.drug.AddictionCategoryConfig;
import org.mydrugs.mydrugs.core.drug.AddictionConfigs;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.effects.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;

public final class ToleranceManager {
    private ToleranceManager() {
    }

    public static void onUse(PlayerAddictionStats playerStats, DrugCategory category, float dose) {
        DrugAddictionStats stats = playerStats.get(category);
        AddictionCategoryConfig cfg = AddictionConfigs.get(category);

        float gain = AddictionMath.computeToleranceGain(dose, cfg, stats.addictionNorm());
        stats.tolerance = AddictionMath.clamp(stats.tolerance + gain * (1.0F - stats.tolerance), 0.0F, 1.0F);
    }

    public static void decay(ServerPlayer player, PlayerAddictionStats playerStats, DrugCategory category, long abstinence) {
        if (abstinence <= 200) return;

        DrugAddictionStats stats = playerStats.get(category);
        AddictionCategoryConfig cfg = AddictionConfigs.get(category);

        boolean sleeping = player.isSleeping();
        boolean inSafeZone = SafeZoneManager.isInSafeZone(player);

        float decay = AddictionMath.computeToleranceDecayPerSecond(cfg, playerStats.resilience, sleeping, inSafeZone);
        stats.tolerance = AddictionMath.clamp(stats.tolerance - decay, 0.0F, 1.0F);
    }
}