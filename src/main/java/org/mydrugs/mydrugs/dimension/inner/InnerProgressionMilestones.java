package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.diary.IntegrationDiary;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;

public final class InnerProgressionMilestones {
    public static final String ENTERED = "entered";
    public static final String FIRST_SIGIL = "first_sigil";
    public static final String NINE_SIGILS = "nine_sigils";
    public static final String SCAR_HEALED = "scar_healed";
    public static final String SKY_SHRINE = "sky_shrine";
    public static final String VAULT_OPENED = "vault_opened";
    public static final String SPIRAL_OPENED = "spiral_opened";
    public static final String SPIRAL_COMPLETED = "spiral_completed";

    private InnerProgressionMilestones() {
    }

    public static void entered(ServerPlayer player) {
        boolean first = IntegrationDiary.firstDimensionEntry(player);
        AdvancementEventHooks.innerDimensionMilestone(player, ENTERED);
        if (first) {
            player.sendSystemMessage(Component.translatable(
                    "message.mydrugs.inner_dimension.first_entry_hint"
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    public static void regionDiscovered(ServerPlayer player, DrugId drug) {
        IntegrationDiary.firstRegionDiscovered(player, drug);
    }

    public static void trialStarted(ServerPlayer player, DrugId drug) {
        IntegrationDiary.firstTrialStarted(player, drug);
    }

    public static void trialCompleted(
            ServerLevel level,
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            ServerPlayer player,
            DrugId drug
    ) {
        data.markTrialReturnPending(island.owner(), drug);
        IntegrationDiary.trialCompleted(player, drug);
        if (island.completedInnerTrialCount() == 1) {
            IntegrationDiary.firstSigilAwakened(player, drug);
            AdvancementEventHooks.innerDimensionMilestone(player, FIRST_SIGIL);
        }
        if (island.allInnerTrialsCompleted()) {
            IntegrationDiary.allSigilsCompleted(player);
            AdvancementEventHooks.innerDimensionMilestone(player, NINE_SIGILS);
        }
    }

    public static void returnedToAnchor(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            ServerPlayer player
    ) {
        if (!data.clearPendingTrialReturn(island.owner())) {
            return;
        }
        player.sendSystemMessage(Component.translatable(
                "message.mydrugs.inner_dimension.sigil_seen_at_anchor",
                island.completedInnerTrialCount()
        ).withStyle(ChatFormatting.AQUA));
    }

    public static void lockedVaultFound(ServerPlayer player) {
        IntegrationDiary.firstLockedVaultFound(player);
    }

    public static void vaultOpened(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            ServerPlayer player
    ) {
        if (!island.owner().equals(player.getUUID())) {
            return;
        }
        if (data.markProgressMarker(island.owner(), InnerDimensionConstants.MARKER_FIRST_VAULT_OPENED)) {
            IntegrationDiary.firstVaultOpened(player);
            player.sendSystemMessage(Component.translatable(
                    "message.mydrugs.inner_vault.first_opened"
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        AdvancementEventHooks.innerDimensionMilestone(player, VAULT_OPENED);
    }

    public static void skyShrineReached(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            ServerPlayer player
    ) {
        if (!island.owner().equals(player.getUUID())
                || !data.markProgressMarker(island.owner(), InnerDimensionConstants.MARKER_FIRST_SKY_SHRINE)) {
            return;
        }
        IntegrationDiary.firstSkyShardReached(player);
        AdvancementEventHooks.innerDimensionMilestone(player, SKY_SHRINE);
        player.sendSystemMessage(Component.translatable(
                "message.mydrugs.inner_sky_shrine.reached"
        ).withStyle(ChatFormatting.AQUA));
    }

    public static void scarHealed(ServerPlayer player) {
        IntegrationDiary.firstScarHealed(player);
        AdvancementEventHooks.innerDimensionMilestone(player, SCAR_HEALED);
    }

    public static void spiralCourtOpened(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island
    ) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(island.owner());
        if (player == null) {
            return;
        }
        boolean first = IntegrationDiary.spiralCourtOpened(player);
        AdvancementEventHooks.innerDimensionMilestone(player, SPIRAL_OPENED);
        if (first) {
            player.sendSystemMessage(Component.translatable(
                    "message.mydrugs.inner_spiral.opened"
            ).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }
    }

    public static void spiralCourtCompleted(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            ServerPlayer player
    ) {
        if (!island.owner().equals(player.getUUID())
                || !data.markProgressMarker(island.owner(), InnerDimensionConstants.MARKER_SPIRAL_COMPLETED)) {
            return;
        }
        IntegrationDiary.spiralCourtCompleted(player);
        AdvancementEventHooks.innerDimensionMilestone(player, SPIRAL_COMPLETED);
        player.sendSystemMessage(Component.translatable(
                "message.mydrugs.inner_spiral.completed"
        ).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }
}
