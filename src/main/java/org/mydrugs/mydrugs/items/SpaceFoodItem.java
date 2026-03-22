package org.mydrugs.mydrugs.items;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpaceFoodItem extends Item {
    private final Item baseFood;

    public SpaceFoodItem(Item baseFood, Properties properties) {
        super(properties);
        this.baseFood = baseFood;
    }

    public Item getBaseFood() {
        return this.baseFood;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide()) {
            // Replace with your own effect if you want
            // entity.addEffect(new MobEffectInstance(ModEffects.HIGH, 20 * 20, 0));
        }

        return result;
    }
}