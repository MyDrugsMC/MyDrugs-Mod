package org.mydrugs.mydrugs.client.item;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;

public record BottleFillProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<BottleFillProperty> MAP_CODEC = MapCodec.unit(new BottleFillProperty());

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int i) {
        return GlassBottleItem.getStoredAmount(itemStack) / (float) GlassBottleItem.CAPACITY_MB;
    }

    @Override
    public MapCodec<BottleFillProperty> type() {
        return MAP_CODEC;
    }
}