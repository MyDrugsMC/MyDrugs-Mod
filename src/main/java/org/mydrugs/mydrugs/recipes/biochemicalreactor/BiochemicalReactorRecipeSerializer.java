package org.mydrugs.mydrugs.recipes.biochemicalreactor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class BiochemicalReactorRecipeSerializer implements RecipeSerializer<BiochemicalReactorRecipe> {
    public static final MapCodec<BiochemicalReactorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CountedIngredient.CODEC.fieldOf("ergot").forGetter(BiochemicalReactorRecipe::ergot),
            CountedIngredient.CODEC.fieldOf("tryptophan").forGetter(BiochemicalReactorRecipe::tryptophan),
            FluidResult.CODEC.fieldOf("fluid_output").forGetter(BiochemicalReactorRecipe::fluidOutput),
            Codec.INT.fieldOf("processing_time").forGetter(BiochemicalReactorRecipe::processingTime),
            Codec.INT.optionalFieldOf("minimum_heat", 0).forGetter(BiochemicalReactorRecipe::minimumHeat),
            Codec.INT.optionalFieldOf("ergot_speed_cap", 8).forGetter(BiochemicalReactorRecipe::ergotSpeedCap),
            Codec.FLOAT.optionalFieldOf("ergot_speed_per_item", 0.08f).forGetter(BiochemicalReactorRecipe::ergotSpeedPerItem),
            Codec.FLOAT.optionalFieldOf("manual_boost_factor", 0.80f).forGetter(BiochemicalReactorRecipe::manualBoostFactor),
            Codec.FLOAT.optionalFieldOf("heat_bonus_factor", 1.20f).forGetter(BiochemicalReactorRecipe::heatBonusFactor)
    ).apply(instance, BiochemicalReactorRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BiochemicalReactorRecipe> STREAM_CODEC = StreamCodec.composite(
            CountedIngredient.STREAM_CODEC, BiochemicalReactorRecipe::ergot,
            CountedIngredient.STREAM_CODEC, BiochemicalReactorRecipe::tryptophan,
            FluidResult.STREAM_CODEC, BiochemicalReactorRecipe::fluidOutput,
            ByteBufCodecs.VAR_INT, BiochemicalReactorRecipe::processingTime,
            ByteBufCodecs.VAR_INT, BiochemicalReactorRecipe::minimumHeat,
            ByteBufCodecs.VAR_INT, BiochemicalReactorRecipe::ergotSpeedCap,
            ByteBufCodecs.FLOAT, BiochemicalReactorRecipe::ergotSpeedPerItem,
            ByteBufCodecs.FLOAT, BiochemicalReactorRecipe::manualBoostFactor,
            ByteBufCodecs.FLOAT, BiochemicalReactorRecipe::heatBonusFactor,
            BiochemicalReactorRecipe::new
    );

    @Override
    public MapCodec<BiochemicalReactorRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BiochemicalReactorRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}