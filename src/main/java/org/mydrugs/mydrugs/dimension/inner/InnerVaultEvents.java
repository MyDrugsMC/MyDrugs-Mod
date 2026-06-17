package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.InnerDimensions;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class InnerVaultEvents {
    private InnerVaultEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(InnerDimensions.INNER_LEVEL)
                || !level.getBlockState(event.getPos()).is(Blocks.CHEST)) {
            return;
        }
        int centerX = InnerTerrain.slotCenter(event.getPos().getX());
        int centerZ = InnerTerrain.slotCenter(event.getPos().getZ());
        InnerDimensionSavedData.IslandState island =
                InnerDimensionSavedData.get(level).findIslandBySlot(centerX, centerZ);
        if (island == null) {
            return;
        }
        InnerVaults.Vault vault = InnerVaults.vaultAtChest(island, event.getPos());
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        if (vault != null && InnerVaults.isUnlocked(vault, island)) {
            boolean ownsIsland = island.owner().equals(player.getUUID());
            boolean firstVault = ownsIsland && !data.hasProgressMarker(
                    island.owner(),
                    InnerDimensionConstants.MARKER_FIRST_VAULT_OPENED
            );
            InnerProgressionMilestones.vaultOpened(data, island, player);
            if (ownsIsland && !firstVault) {
                InnerMessageCooldowns.actionBar(
                        player,
                        "vault:opened:" + event.getPos().asLong(),
                        20 * 60,
                        Component.translatable(
                                "message.mydrugs.inner_vault.first_opened"
                        ).withStyle(ChatFormatting.LIGHT_PURPLE)
                );
            }
            return;
        }
        if (vault != null) {
            String tier = InnerVaults.tierFor(vault.drug()).name().toLowerCase(java.util.Locale.ROOT);
            InnerMessageCooldowns.actionBar(
                    player,
                    "vault:locked:" + tier,
                    40,
                    Component.translatable(
                            "message.mydrugs.inner_vault.locked." + tier
                    ).withStyle(ChatFormatting.DARK_PURPLE)
            );
            InnerProgressionMilestones.lockedVaultFound(player);
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }
        if (InnerSkyShardLootBuilder.isShrineChest(island, event.getPos())) {
            boolean ownsIsland = island.owner().equals(player.getUUID());
            boolean firstShrine = ownsIsland && !data.hasProgressMarker(
                    island.owner(),
                    InnerDimensionConstants.MARKER_FIRST_SKY_SHRINE
            );
            InnerProgressionMilestones.skyShrineReached(data, island, player);
            if (ownsIsland && !firstShrine) {
                InnerMessageCooldowns.actionBar(
                        player,
                        "sky_shrine:" + event.getPos().asLong(),
                        20 * 60,
                        Component.translatable(
                                "message.mydrugs.inner_sky_shrine.reached"
                        ).withStyle(ChatFormatting.AQUA)
                );
            }
            return;
        }
        if (InnerSpiralCourtBuilder.isRewardChest(level, island, event.getPos())) {
            InnerProgressionMilestones.spiralCourtCompleted(data, island, player);
        }
    }
}
