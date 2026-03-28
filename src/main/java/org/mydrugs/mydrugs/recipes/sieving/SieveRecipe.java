package org.mydrugs.mydrugs.recipes.sieving;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.List;
import java.util.Optional;

public final class SieveRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ItemStack result;
    private final Optional<ItemStack> bonusResult;
    private final float bonusChance;
    private final int sieveTime;

    private PlacementInfo placementInfo;

    public SieveRecipe(
            Ingredient input,
            ItemStack result,
            Optional<ItemStack> bonusResult,
            float bonusChance,
            int sieveTime
    ) {
        this.input = input;
        this.result = result;
        this.bonusResult = bonusResult;
        this.bonusChance = bonusChance;
        this.sieveTime = sieveTime;
    }

    public Ingredient input() {
        return input;
    }

    public ItemStack result() {
        return result;
    }

    public Optional<ItemStack> bonusResult() {
        return bonusResult;
    }

    public float bonusChance() {
        return bonusChance;
    }

    public int sieveTime() {
        return sieveTime;
    }

    public boolean hasBonus() {
        return this.bonusResult.isPresent() && this.bonusChance > 0.0F;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.input);
        }
        return this.placementInfo;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.FURNACE_MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipeTypes.SIEVING_TYPE.get();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRecipeSerializers.SIEVING_SERIALIZER.get();
    }

    public static final class Serializer implements RecipeSerializer<SieveRecipe> {
        public static final MapCodec<SieveRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(SieveRecipe::input),
                ItemStack.CODEC.fieldOf("result").forGetter(SieveRecipe::result),
                ItemStack.CODEC.optionalFieldOf("bonus_result").forGetter(SieveRecipe::bonusResult),
                Codec.FLOAT.optionalFieldOf("bonus_chance", 0.0F).forGetter(SieveRecipe::bonusChance),
                Codec.INT.optionalFieldOf("sieve_time", 200).forGetter(SieveRecipe::sieveTime)
        ).apply(instance, SieveRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SieveRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, SieveRecipe::input,
                ItemStack.STREAM_CODEC, SieveRecipe::result,
                ByteBufCodecs.optional(ItemStack.STREAM_CODEC), SieveRecipe::bonusResult,
                ByteBufCodecs.FLOAT, SieveRecipe::bonusChance,
                ByteBufCodecs.VAR_INT, SieveRecipe::sieveTime,
                SieveRecipe::new
        );

        @Override
        public MapCodec<SieveRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SieveRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}