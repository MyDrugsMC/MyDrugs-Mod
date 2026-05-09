package org.mydrugs.mydrugs.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.menu.SingleSlotMenu;

public final class DrugAnalyzerItem extends Item implements SingleSlotContainerItem {
    public DrugAnalyzerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(net.minecraft.world.level.Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            ItemStack carrier = player.getItemInHand(hand);
            MenuProvider provider = new SimpleMenuProvider(
                    (id, inv, ignored) -> new SingleSlotMenu(id, inv, carrier, hand, serverLevel),
                    net.minecraft.network.chat.Component.translatable("menu.mydrugs.drug_analyzer")
            );
            player.openMenu(provider);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack, ServerLevel level) {
        return true;
    }
}
