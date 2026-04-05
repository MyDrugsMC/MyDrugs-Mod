package org.mydrugs.mydrugs.recipes.gasifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

public class GasifierRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ResourceLocation gasOutput;
    private final long gasAmount;
    private final int processTime;

    public GasifierRecipe(
            Ingredient input,
            ResourceLocation gasOutput,
            long gasAmount,
            int processTime
    ) {
        this.input = input;
        this.gasOutput = gasOutput;
        this.gasAmount = gasAmount;
        this.processTime = processTime;
    }

    public Ingredient input() {
        return input;
    }

    public ResourceLocation gasOutput() {
        return gasOutput;
    }

    public long gasAmount() {
        return gasAmount;
    }

    public int processTime() {
        return processTime;
    }

    public @Nullable GasType gas() {
        return ModGases.get(this.gasOutput);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRecipeSerializers.GASIFIER.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipeTypes.GASIFIER.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public static class Serializer implements RecipeSerializer<GasifierRecipe> {
        public static final MapCodec<GasifierRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("input").forGetter(GasifierRecipe::input),
                ResourceLocation.CODEC.fieldOf("gas_output").forGetter(GasifierRecipe::gasOutput),
                Codec.LONG.fieldOf("gas_amount").forGetter(GasifierRecipe::gasAmount),
                Codec.INT.optionalFieldOf("process_time", 200).forGetter(GasifierRecipe::processTime)
        ).apply(instance, GasifierRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, GasifierRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, GasifierRecipe::input,
                        ResourceLocation.STREAM_CODEC, GasifierRecipe::gasOutput,
                        ByteBufCodecs.VAR_LONG, GasifierRecipe::gasAmount,
                        ByteBufCodecs.VAR_INT, GasifierRecipe::processTime,
                        GasifierRecipe::new
                );

        @Override
        public MapCodec<GasifierRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GasifierRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}