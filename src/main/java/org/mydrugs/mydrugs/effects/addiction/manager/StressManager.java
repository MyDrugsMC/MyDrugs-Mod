package org.mydrugs.mydrugs.effects.addiction.manager;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;

public final class StressManager {
    private StressManager() {
    }

    public static void tick(ServerPlayer player, PlayerAddictionStats stats, float globalSeverity, boolean inCombat, int companions, boolean inSafeZone) {
        float target = 0.15F + globalSeverity * 0.75F;

        long time = player.level().getDayTime() % 24000L;
        if (time >= 13000L && time < 23000L) target += 0.05F;
        if (inCombat) target += 0.15F;
        if (companions == 0) target += 0.08F;
        if (inSafeZone) target -= 0.08F;
        if (stats.temporaryEffects.hasDiaryCalm(player.level().getGameTime())) target -= 0.10F;
        if (stats.temporaryEffects.hasHeadphones(player.level().getGameTime())) target -= 0.10F;

        int food = player.getFoodData().getFoodLevel();

        // Hungry -> more stress
        if (food <= 6) {
            target += 0.10F;
        } else if (food <= 12) {
            target += 0.04F;
        }

        // Full bar -> 10% less target stress
        if (food >= 20) {
            target *= 0.90F;
        }

        // Low health -> more stress
        float healthRatio = player.getHealth() / player.getMaxHealth();
        if (healthRatio <= 0.25F) {
            target += 0.15F;
        } else if (healthRatio <= 0.50F) {
            target += 0.08F;
        }

        target = AddictionMath.clamp(target, 0.0F, 1.0F);
        stats.stressLevel += (target - stats.stressLevel) * 0.08F;
        stats.stressLevel = AddictionMath.clamp(stats.stressLevel, 0.0F, 1.0F);
    }

    public static void onDamage(ServerPlayer player, float finalDamage) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        stats.stressLevel = AddictionMath.clamp(stats.stressLevel + 0.08F + finalDamage * 0.01F, 0.0F, 1.0F);
    }

    public static void reduce(PlayerAddictionStats stats, float amount) {
        stats.stressLevel = AddictionMath.clamp(stats.stressLevel - amount, 0.0F, 1.0F);
    }
}