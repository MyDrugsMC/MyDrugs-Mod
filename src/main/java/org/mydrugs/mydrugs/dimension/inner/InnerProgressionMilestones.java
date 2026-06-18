package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.diary.IntegrationDiary;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.entity.InnerDemonSpawnManager;

import java.util.UUID;

public final class InnerProgressionMilestones {
    public static final String ENTERED = "entered";
    public static final String FIRST_SIGIL = "first_sigil";
    public static final String NINE_SIGILS = "nine_sigils";
    public static final String SCAR_HEALED = "scar_healed";
    public static final String SKY_SHRINE = "sky_shrine";
    public static final String VAULT_OPENED = "vault_opened";
    public static final String SPIRAL_OPENED = "spiral_opened";
    public static final String SPIRAL_COMPLETED = "spiral_completed";
    private static final double SELF_ANCHOR_RETURN_RADIUS_SQR = 24.0D * 24.0D;
    private static final int SELF_ANCHOR_RETURN_MESSAGE_COOLDOWN = 20 * 30;
    private static final int SELF_ANCHOR_RETURN_AMBIENT_GRACE_TICKS = 20 * 12;

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
        InnerDemonSpawnManager.primePendingTrialReturnGrace(player);
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
        if (player == null || island == null) {
            return;
        }
        UUID owner = player.getUUID();
        DrugId settledDrug = pendingReturnDrug(island);
        if (!clearPendingReturnAtAnchor(data, island, owner, player.blockPosition())) {
            return;
        }
        InnerDemonSpawnManager.extendInnerAmbientGrace(player, SELF_ANCHOR_RETURN_AMBIENT_GRACE_TICKS);
        InnerObjectiveHelper.Objective nextObjective = nextObjectiveAfterReturn(data, island, owner, player.blockPosition());
        InnerMessageCooldowns.systemMessage(
                player,
                returnMessageCooldownId(owner, island, settledDrug),
                SELF_ANCHOR_RETURN_MESSAGE_COOLDOWN,
                returnSummaryMessage(settledDrug, nextObjective.message()).withStyle(ChatFormatting.AQUA)
        );
    }

    static boolean clearPendingReturnAtAnchor(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            UUID owner,
            BlockPos pos
    ) {
        if (!canClearPendingReturnAtAnchor(data, island, owner, pos)) {
            return false;
        }
        return data.clearPendingTrialReturn(owner);
    }

    static boolean canClearPendingReturnAtAnchor(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            UUID owner,
            BlockPos pos
    ) {
        return data != null
                && island != null
                && owner != null
                && owner.equals(island.owner())
                && isNearSelfAnchor(island, pos)
                && InnerGameplayLoop.inside(data, island, owner, pos).phase()
                == InnerGameplayLoop.Phase.RETURN_TO_ANCHOR;
    }

    static boolean isNearSelfAnchor(InnerDimensionSavedData.IslandState island, BlockPos pos) {
        if (island == null || pos == null) {
            return false;
        }
        double dx = pos.getX() - island.centerX();
        double dz = pos.getZ() - island.centerZ();
        return dx * dx + dz * dz <= SELF_ANCHOR_RETURN_RADIUS_SQR;
    }

    static InnerObjectiveHelper.Objective nextObjectiveAfterReturn(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            UUID owner,
            BlockPos pos
    ) {
        return InnerObjectiveHelper.currentObjectiveFromState(data, island, owner, pos);
    }

    static MutableComponent returnSummaryMessage(@Nullable DrugId settledDrug, Component nextObjectiveMessage) {
        Component next = nextObjectiveMessage == null
                ? Component.translatable("message.mydrugs.inner_objective.complete")
                : nextObjectiveMessage;
        if (settledDrug == null) {
            return Component.translatable(
                    "message.mydrugs.inner_dimension.trial_settled_at_anchor.generic",
                    next
            );
        }
        return Component.translatable(
                "message.mydrugs.inner_dimension.trial_settled_at_anchor",
                Component.translatable("drug.mydrugs." + settledDrug.serializedName()),
                next
        );
    }

    private static String returnMessageCooldownId(
            UUID owner,
            InnerDimensionSavedData.IslandState island,
            @Nullable DrugId settledDrug
    ) {
        String settled = settledDrug == null
                ? "count_" + island.completedInnerTrialCount()
                : settledDrug.serializedName();
        return "anchor_return:" + owner + ":" + settled;
    }

    private static @Nullable DrugId pendingReturnDrug(InnerDimensionSavedData.IslandState island) {
        if (island == null) {
            return null;
        }
        for (String marker : island.placedMarkers()) {
            if (marker.startsWith(InnerDimensionConstants.MARKER_RETURN_PENDING)) {
                return DrugId.bySerializedNameOrNull(marker.substring(
                        InnerDimensionConstants.MARKER_RETURN_PENDING.length()
                ));
            }
        }
        return null;
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
