package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.use.DrugUseResult;
import org.mydrugs.mydrugs.core.drug.use.DrugUseSource;
import org.mydrugs.mydrugs.items.ModItems;

public class CoffeeCupItem extends DrugItem {
    public CoffeeCupItem(Properties properties, DrugId drugId, ConsumptionStrategy strategy) {
        super(properties.stacksTo(16), drugId, strategy);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof ServerPlayer player)) {
            return stack;
        }

        DrugUseResult result = MyDrugs.DRUG_USE_SERVICE.consume(player, getDrugModel(), getConsumptionStrategy(), DrugUseSource.ITEM, stack);
        if (result.status() == DrugUseResult.Status.BLOCKED_MISSING_KNOWLEDGE) {
            return stack;
        }

        if (player.gameMode() != GameType.CREATIVE) {
            stack.shrink(1);
            ItemStack emptyCup = new ItemStack(ModItems.CUP.get());
            if (stack.isEmpty()) {
                return emptyCup;
            }
            if (!player.getInventory().add(emptyCup)) {
                player.drop(emptyCup, false);
            }
        }
        return stack;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }
}
