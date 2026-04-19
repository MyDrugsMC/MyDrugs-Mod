package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.ModSounds;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.rolling.RolledDrugContent;
import org.mydrugs.mydrugs.items.data.ModDataComponents;

import java.util.List;

public class RolledSmokedItem extends DrugItem {
    public RolledSmokedItem(Properties properties, @Nullable DrugId id, ConsumptionStrategy strategy) {
        super(properties, id, strategy);
    }

    @Override
    public List<DrugModel> getDrugModels(ItemStack stack) {
        RolledDrugContent content = stack.get(ModDataComponents.ROLLED_CONTENT.get());
        if (content == null) {
            content = defaultContent();
            if (content == null) {
                return List.of();
            }
        }

        return content.asList().stream()
                .map(DrugRegistry::getDrug)
                .toList();
    }

    @Nullable
    protected RolledDrugContent defaultContent() {
        return null;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.SMOKE.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 40;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.TOOT_HORN;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        boolean creative = livingEntity instanceof Player player && player.getAbilities().instabuild;

        if (!creative) {
            stack.shrink(1);
        }

        if (!level.isClientSide() && !creative && livingEntity instanceof Player player) {
            ItemStack filterStack = new ItemStack(ModItems.FILTER.get());

            if (!player.getInventory().add(filterStack)) {
                player.drop(filterStack, false);
            }
        }

        return super.finishUsingItem(stack, level, livingEntity);
    }
}
