package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InnerCommunicationManager {
    private static final int REGION_MESSAGE_COOLDOWN = 20 * 10;
    private static final double ANCHOR_RETURN_RADIUS_SQR = 24.0D * 24.0D;
    private static final Map<UUID, DrugId> LAST_REGION = new HashMap<>();

    private InnerCommunicationManager() {
    }

    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % 20 != 0 || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(player.getUUID());
        BlockPos pos = player.blockPosition();

        double centerDistance = horizontalDistanceSqr(pos, island.centerX(), island.centerZ());
        if (centerDistance <= ANCHOR_RETURN_RADIUS_SQR) {
            LAST_REGION.remove(player.getUUID());
            InnerProgressionMilestones.returnedToAnchor(data, island, player);
            if (data.isSpiralCourtPlaced(island.owner())) {
                InnerProgressionMilestones.spiralCourtOpened(level, island);
            }
            return;
        }

        InnerTerrain.Sample sample = InnerTerrain.sample(
                island.centerX(),
                island.centerZ(),
                pos.getX(),
                pos.getZ()
        );
        if (!sample.land() || !island.hasIntegrated(sample.drugId())) {
            LAST_REGION.remove(player.getUUID());
            return;
        }
        DrugId previous = LAST_REGION.put(player.getUUID(), sample.drugId());
        if (previous == sample.drugId()) {
            return;
        }

        String key = island.hasCompletedInnerTrial(sample.drugId())
                ? "message.mydrugs.inner_region.enter.completed"
                : "message.mydrugs.inner_region.enter";
        InnerTrialDefinition definition = InnerTrialDefinition.forDrug(sample.drugId());
        Component trialTitle = definition == null
                ? Component.translatable("drug.mydrugs." + sample.drugId().serializedName())
                : Component.translatable(definition.titleKey());
        InnerMessageCooldowns.actionBar(
                player,
                "region:" + sample.drugId().serializedName(),
                REGION_MESSAGE_COOLDOWN,
                Component.translatable(
                        key,
                        Component.translatable("drug.mydrugs." + sample.drugId().serializedName()),
                        trialTitle
                ).withStyle(ChatFormatting.LIGHT_PURPLE)
        );
        InnerProgressionMilestones.regionDiscovered(player, sample.drugId());
    }

    public static void clearAll() {
        LAST_REGION.clear();
    }

    private static double horizontalDistanceSqr(BlockPos pos, int x, int z) {
        double dx = pos.getX() - x;
        double dz = pos.getZ() - z;
        return dx * dx + dz * dz;
    }
}
