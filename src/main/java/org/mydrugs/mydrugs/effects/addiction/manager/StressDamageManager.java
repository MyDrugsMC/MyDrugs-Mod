package org.mydrugs.mydrugs.effects.addiction.manager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import org.mydrugs.mydrugs.damage.ModDamageTypes;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;

public final class StressDamageManager {
    private static final float DAMAGE_START_THRESHOLD = 0.90F;
    private static final float DAMAGE_END_THRESHOLD = 1.00F;
    private static final float MAX_DAMAGE_PER_SECOND = 0.50F;

    private StressDamageManager() {
    }

    public static void tick(ServerPlayer player, PlayerAddictionStats stats) {
        if (player.level().isClientSide()) {
            return;
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        // Strict survival-only check.
        if (player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) {
            return;
        }

        float stress = Mth.clamp(stats.stressLevel, 0.0F, 1.0F);
        if (stress <= DAMAGE_START_THRESHOLD) {
            return;
        }

        float normalized = (stress - DAMAGE_START_THRESHOLD) / (DAMAGE_END_THRESHOLD - DAMAGE_START_THRESHOLD);
        normalized = Mth.clamp(normalized, 0.0F, 1.0F);

        float damage = normalized * MAX_DAMAGE_PER_SECOND;
        if (damage <= 0.0F) {
            return;
        }

        player.hurt(ModDamageTypes.stressOverload(player.level()), damage);
    }
}