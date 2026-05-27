package org.mydrugs.mydrugs.core.drug.integration;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.attachment.PlayerIntegrationAttachment;
import org.mydrugs.mydrugs.core.drug.integration.network.IntegrationSyncPayload;
import org.mydrugs.mydrugs.core.drug.runtime.DrugEffectRuntimeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies unlocked {@link IntegratedTrait}s passively (Phase A.5).
 *
 * Traits flow through the existing effect runtime: their clean effect is re-added on a short,
 * always-refreshed timer so it never lapses. The runtime never feeds addiction or tolerance, so
 * integrated traits are inherently addiction-free.
 */
public final class IntegratedTraitManager {
    private IntegratedTraitManager() {
    }

    /** The trait's magnitude if the player has unlocked it, otherwise 0. */
    public static float getValue(ServerPlayer player, IntegratedTrait trait) {
        if (player == null || trait == null) {
            return 0.0F;
        }
        PlayerIntegrationAttachment integration = player.getData(ModAttachments.PLAYER_INTEGRATION.get());
        return integration.has(trait) ? trait.magnitude() : 0.0F;
    }

    public static void tickPlayer(ServerPlayer player) {
        if (player == null || player.tickCount % IntegrationConstants.TRAIT_REFRESH_INTERVAL_TICKS != 0) {
            return;
        }
        PlayerIntegrationAttachment integration = player.getData(ModAttachments.PLAYER_INTEGRATION.get());
        if (integration.isEmpty()) {
            return;
        }

        for (IntegratedTrait trait : integration.all()) {
            for (IntegratedTrait.EffectEcho echo : trait.effects()) {
                DrugEffectRuntimeManager.addEffect(
                        player,
                        echo.effect(),
                        echo.magnitude(),
                        IntegrationConstants.TRAIT_EFFECT_DURATION_TICKS
                );
            }
        }

        if (player.tickCount % IntegrationConstants.CLIENT_SYNC_INTERVAL_TICKS == 0) {
            syncToClient(player);
        }
    }

    public static void syncToClient(ServerPlayer player) {
        if (player == null) {
            return;
        }
        PlayerIntegrationAttachment integration = player.getData(ModAttachments.PLAYER_INTEGRATION.get());
        List<String> ids = new ArrayList<>();
        for (IntegratedTrait trait : integration.all()) {
            ids.add(trait.serializedName());
        }
        PacketDistributor.sendToPlayer(player, new IntegrationSyncPayload(ids));
    }
}
