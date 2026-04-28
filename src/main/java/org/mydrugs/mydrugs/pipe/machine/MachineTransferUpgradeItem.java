package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MachineTransferUpgradeItem extends Item {
    public MachineTransferUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (blockEntity == null || !MachineTransferAttachments.isSupported(blockEntity)) {
            return InteractionResult.PASS;
        }

        if (!context.getLevel().isClientSide()) {
            boolean wasInstalled = MachineTransferAttachments.hasTransferUpgrade(blockEntity);
            MachineTransferAttachments.install(blockEntity);

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.translatable(wasInstalled
                                ? "message.mydrugs.transfer_upgrade.already_installed"
                                : "message.mydrugs.transfer_upgrade.installed"),
                        true
                );

                if (player.isSecondaryUseActive() || wasInstalled) {
                    MachineTransferConfigOpener.open(serverPlayer, blockEntity);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }
}
