package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Consumer;

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
            boolean installed = MachineTransferAttachments.install(blockEntity);

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
            if (installed && !wasInstalled && player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag
    ) {
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.machine_transfer_upgrade"));
    }
}
