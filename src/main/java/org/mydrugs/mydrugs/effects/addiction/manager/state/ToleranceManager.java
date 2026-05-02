package org.mydrugs.mydrugs.effects.addiction.manager.state;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.core.drug.AddictionCategoryConfig;
import org.mydrugs.mydrugs.core.drug.AddictionConfigs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.effects.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.manager.recovery.SafeZoneManager;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;

public final class ToleranceManager {
    private ToleranceManager() {
    }

    public static void onUse(PlayerAddictionStats playerStats, DrugModel model, float dose) {
        DrugAddictionStats stats = playerStats.getOrCreateDrugStats(model.getId());
        AddictionCategoryConfig cfg = AddictionConfigs.get(model.getDrugCategory());

        float gain = AddictionMath.computeToleranceGain(dose, cfg, stats.addictionNorm(), model.getAddictionRate())
                * Config.SERVER.toleranceGainMultiplier.get().floatValue();
        stats.tolerance = AddictionMath.clamp(stats.tolerance + gain * (1.0F - stats.tolerance), 0.0F, 1.0F);
    }

    public static void decay(ServerPlayer player, PlayerAddictionStats playerStats, DrugId drugId, long abstinence) {
        if (abstinence <= 200) {
            return;
        }

        DrugAddictionStats stats = playerStats.getDrugStats(drugId);
        if (stats == null) {
            return;
        }

        AddictionCategoryConfig cfg = AddictionConfigs.get(DrugRegistry.getCategory(drugId));

        boolean sleeping = player.isSleeping();
        boolean inSafeZone = SafeZoneManager.isInSafeZone(player);

        // This method is called every server tick; config/category decay values are per second.
        float decay = AddictionMath.computeToleranceDecayPerSecond(cfg, playerStats.resilience, sleeping, inSafeZone)
                * Config.SERVER.toleranceDecayMultiplier.get().floatValue()
                / 20.0F;
        stats.tolerance = AddictionMath.clamp(stats.tolerance - decay, 0.0F, 1.0F);
    }
}
