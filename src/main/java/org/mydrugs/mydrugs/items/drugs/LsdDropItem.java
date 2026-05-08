package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.items.ModItems;

public class LsdDropItem extends DrugItem {
    public LsdDropItem(Properties properties, DrugId drugId, ConsumptionStrategy strategy) {
        super(properties, drugId, strategy);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        int before = stack.getCount();
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);

        if (!level.isClientSide()
                && livingEntity instanceof ServerPlayer player
                && !player.getAbilities().instabuild
                && result.getCount() < before) {
            ItemStack cardboard = new ItemStack(ModItems.CUPBOARD_PIECE.get());
            if (!player.addItem(cardboard)) {
                player.drop(cardboard, false);
            }
        }

        return result;
    }
}
