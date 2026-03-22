package org.mydrugs.mydrugs.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.registry.ModDataComponents;

public record FilledProperty() implements ConditionalItemModelProperty {
    public static final MapCodec<FilledProperty> MAP_CODEC = MapCodec.unit(new FilledProperty());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
        return Boolean.TRUE.equals(stack.get(ModDataComponents.FILLED.get()));
    }

    @Override
    public MapCodec<FilledProperty> type() {
        return MAP_CODEC;
    }
}