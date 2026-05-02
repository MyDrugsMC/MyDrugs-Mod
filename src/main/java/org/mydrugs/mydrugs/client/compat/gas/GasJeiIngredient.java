package org.mydrugs.mydrugs.client.compat.gas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record GasJeiIngredient(ResourceLocation id, long amount) {
    public static final long NORMALIZED_AMOUNT = 1000L;

    public static final Codec<GasJeiIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(GasJeiIngredient::id),
            Codec.LONG.optionalFieldOf("amount", NORMALIZED_AMOUNT).forGetter(GasJeiIngredient::amount)
    ).apply(instance, GasJeiIngredient::new));

    public static GasJeiIngredient of(ResourceLocation id, long amount) {
        return new GasJeiIngredient(id, Math.max(1L, amount));
    }

    public GasJeiIngredient withAmount(long amount) {
        return new GasJeiIngredient(this.id, Math.max(1L, amount));
    }

    public GasJeiIngredient normalized() {
        return new GasJeiIngredient(this.id, NORMALIZED_AMOUNT);
    }
}