package org.mydrugs.mydrugs.energy;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.mydrugs.mydrugs.MyDrugs;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class MachineUpgradeDropEvents {
    private MachineUpgradeDropEvents() {
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        BlockEntity blockEntity = event.getBlockEntity();
        if (blockEntity == null) {
            return;
        }

        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            if (stack.is(event.getState().getBlock().asItem())
                    && MachineUpgradePersistence.copyUpgradesToStack(blockEntity, stack)) {
                return;
            }
        }
    }
}
