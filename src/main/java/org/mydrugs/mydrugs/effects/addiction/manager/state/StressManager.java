package org.mydrugs.mydrugs.effects.addiction.manager.state;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;

public final class StressManager {
    private StressManager() {}

    public static void tick(ServerPlayer player, PlayerAddictionStats stats, float globalSeverity, boolean inCombat, int companions, boolean inSafeZone) {
        float target = AddictionConstants.STRESS_BASE + globalSeverity * AddictionConstants.STRESS_SEVERITY_SCALE;

        long time = player.level().getDayTime() % 24000L;
        if (time >= 13000L && time < 23000L) target += AddictionConstants.STRESS_NIGHT_BONUS;
        if (inCombat) target += AddictionConstants.STRESS_COMBAT_BONUS;
        if (companions == 0) target += AddictionConstants.STRESS_ALONE_BONUS;
        if (inSafeZone) target -= AddictionConstants.STRESS_SAFE_ZONE_REDUCTION;
        if (stats.temporaryEffects.hasDiaryCalm(player.level().getGameTime())) target -= AddictionConstants.STRESS_DIARY_REDUCTION;
        if (stats.temporaryEffects.hasHeadphones(player.level().getGameTime())) target -= AddictionConstants.STRESS_HEADPHONES_REDUCTION;

        target = AddictionMath.clamp(target, 0.0F, 1.0F);
        stats.stressLevel += (target - stats.stressLevel) * AddictionConstants.STRESS_LERP_RATE;
        stats.stressLevel = AddictionMath.clamp(stats.stressLevel, 0.0F, 1.0F);
    }

    public static void onDamage(ServerPlayer player, float finalDamage) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        stats.stressLevel = AddictionMath.clamp(stats.stressLevel + AddictionConstants.STRESS_DAMAGE_FLAT_BONUS + finalDamage * AddictionConstants.STRESS_DAMAGE_BONUS_PER_HP, 0.0F, 1.0F);
    }

    public static void reduce(PlayerAddictionStats stats, float amount) {
        stats.stressLevel = AddictionMath.clamp(stats.stressLevel - amount, 0.0F, 1.0F);
    }
}