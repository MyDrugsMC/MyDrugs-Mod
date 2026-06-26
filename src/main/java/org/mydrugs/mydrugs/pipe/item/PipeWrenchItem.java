package org.mydrugs.mydrugs.pipe.item;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachment;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachments;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.pipe.PipeConnectionMode;
import org.mydrugs.mydrugs.pipe.PipeSideSelector;
import org.mydrugs.mydrugs.pipe.blockentity.PipeBlockEntity;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferAttachments;

import java.util.Locale;

public class PipeWrenchItem extends Item {
    public PipeWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof PipeBlockEntity pipe)) {
            if (context.getPlayer() != null && context.getPlayer().isCrouching()) {
                return tryRemoveMachineUpgrade(context, blockEntity);
            }
            return InteractionResult.PASS;
        }

        Direction side = PipeSideSelector.selectSide(context);

        if (!context.getLevel().isClientSide()) {
            PipeConnectionMode mode;
            if (context.getPlayer() != null && context.getPlayer().isCrouching()) {
                mode = pipe.disableSide(side);
            } else {
                mode = pipe.cycleSide(side);
            }

            Player player = context.getPlayer();
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable(
                                "message.mydrugs.pipe.side_mode",
                                Component.translatable("direction.mydrugs." + side.getSerializedName()),
                                Component.translatable("pipe_mode.mydrugs." + mode.name().toLowerCase(Locale.ROOT))
                        ),
                        true
                );
            }

            context.getLevel().playSound(
                    null,
                    context.getClickedPos(),
                    SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.BLOCKS,
                    0.35F,
                    1.4F
            );
        }

        return InteractionResult.SUCCESS;
    }

    private InteractionResult tryRemoveMachineUpgrade(UseOnContext context, BlockEntity blockEntity) {
        Player player = context.getPlayer();
        if (blockEntity == null || player == null) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack removed = ItemStack.EMPTY;
        Component message;
        if (MachineTransferAttachments.hasTransferUpgrade(blockEntity) && MachineTransferAttachments.remove(blockEntity)) {
            removed = new ItemStack(ModItems.MACHINE_TRANSFER_UPGRADE.get());
            message = Component.translatable("message.mydrugs.pipe_wrench.removed_transfer_upgrade");
        } else {
            MachineEnergyAttachment energy = MachineEnergyAttachments.get(blockEntity);
            if (energy.hasAutomationUpgrade()) {
                if (energy.storage().stored() > 0) {
                    message = Component.translatable("message.mydrugs.pipe_wrench.current_not_empty");
                } else if (MachineEnergyAttachments.removeAutomationUpgrade(blockEntity)) {
                    removed = new ItemStack(ModItems.AUTOMATION_UPGRADE.get());
                    message = Component.translatable("message.mydrugs.pipe_wrench.removed_automation_upgrade");
                } else {
                    message = Component.translatable("message.mydrugs.pipe_wrench.no_removable_upgrade");
                }
            } else if (energy.hasEnergyUpgrade()) {
                if (energy.storage().stored() > 0) {
                    message = Component.translatable("message.mydrugs.pipe_wrench.current_not_empty");
                } else if (MachineEnergyAttachments.removeEnergyUpgrade(blockEntity)) {
                    removed = new ItemStack(ModItems.ENERGY_UPGRADE.get());
                    message = Component.translatable("message.mydrugs.pipe_wrench.removed_energy_upgrade");
                } else {
                    message = Component.translatable("message.mydrugs.pipe_wrench.no_removable_upgrade");
                }
            } else {
                message = Component.translatable("message.mydrugs.pipe_wrench.no_removable_upgrade");
            }
        }

        player.displayClientMessage(message, true);
        if (!removed.isEmpty()) {
            if (!player.getInventory().add(removed)) {
                player.drop(removed, false);
            }
            context.getItemInHand().hurtAndBreak(1, player, context.getHand());
            context.getLevel().playSound(
                    null,
                    context.getClickedPos(),
                    SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.BLOCKS,
                    0.35F,
                    1.2F
            );
        }
        return InteractionResult.SUCCESS;
    }
}
