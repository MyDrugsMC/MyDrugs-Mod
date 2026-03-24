package org.mydrugs.mydrugs.client.item;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;

public record LiquidColorTintSource(int defaultColor) implements ItemTintSource {
    public static final MapCodec<LiquidColorTintSource> MAP_CODEC =
            com.mojang.serialization.Codec.INT.fieldOf("default")
                    .xmap(LiquidColorTintSource::new, LiquidColorTintSource::defaultColor);

    public LiquidColorTintSource(int defaultColor) {
        this.defaultColor = ARGB.opaque(defaultColor);
    }

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        ResourceLocation fluidId = GlassBottleItem.getStoredFluidId(stack);
        if (fluidId == null) {
            return defaultColor;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(fluidId);
        if (fluid == null || fluid == Fluids.EMPTY) {
            return defaultColor;
        }

        int tint = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid).getTintColor();

        // Ensure opaque fallback if fluid tint uses 0 alpha
        if (((tint >>> 24) & 0xFF) == 0) {
            tint = ARGB.opaque(tint);
        }

        return tint;
    }

    @Override
    public MapCodec<LiquidColorTintSource> type() {
        return MAP_CODEC;
    }
}