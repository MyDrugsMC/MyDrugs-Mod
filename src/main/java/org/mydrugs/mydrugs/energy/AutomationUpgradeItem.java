package org.mydrugs.mydrugs.energy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AutomationUpgradeItem extends Item {
    public AutomationUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        Player player = context.getPlayer();
        if (blockEntity == null) {
            return InteractionResult.PASS;
        }

        if (!context.getLevel().isClientSide() && player != null) {
            MachineEnergyAttachment attachment = MachineEnergyAttachments.get(blockEntity);
            Component message;
            if (attachment.hasEnergyUpgrade()) {
                message = Component.translatable("message.mydrugs.automation_upgrade.has_energy");
            } else if (attachment.hasAutomationUpgrade()) {
                message = Component.translatable("message.mydrugs.automation_upgrade.already_installed");
            } else if (MachineEnergyAttachments.installAutomationUpgrade(blockEntity)) {
                message = Component.translatable("message.mydrugs.automation_upgrade.installed");
                if (!player.getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
            } else {
                message = Component.translatable("message.mydrugs.automation_upgrade.unsupported");
            }
            player.displayClientMessage(message, true);
        }

        return InteractionResult.SUCCESS;
    }
}
