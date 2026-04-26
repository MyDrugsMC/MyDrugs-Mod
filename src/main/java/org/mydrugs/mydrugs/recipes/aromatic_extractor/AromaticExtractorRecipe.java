package org.mydrugs.mydrugs.recipes.aromatic_extractor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.blocks.entity.AromaticExtractorBlockEntity;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

public class AromaticExtractorRecipe implements Recipe<AromaticExtractorRecipeInput> {
    private final AromaticExtractorFluidStack input;
    private final AromaticExtractorFluidStack catalyst;
    private final AromaticExtractorFluidStack output1;
    private final AromaticExtractorFluidStack output2;
    private final int baseTicks;

    public AromaticExtractorRecipe(
            AromaticExtractorFluidStack input,
            AromaticExtractorFluidStack catalyst,
            AromaticExtractorFluidStack output1,
            AromaticExtractorFluidStack output2,
            int baseTicks
    ) {
        if (catalyst.amount() < AromaticExtractorBlockEntity.MIN_CATALYST_AMOUNT) {
            throw new IllegalArgumentException("Aromatic extractor catalyst must require at least "
                    + AromaticExtractorBlockEntity.MIN_CATALYST_AMOUNT + " mB");
        }
        if (baseTicks <= 0) {
            throw new IllegalArgumentException("baseTicks must be > 0");
        }

        this.input = input;
        this.catalyst = catalyst;
        this.output1 = output1;
        this.output2 = output2;
        this.baseTicks = baseTicks;
    }

    public AromaticExtractorFluidStack input() {
        return input;
    }

    public AromaticExtractorFluidStack catalyst() {
        return catalyst;
    }

    public AromaticExtractorFluidStack output1() {
        return output1;
    }

    public AromaticExtractorFluidStack output2() {
        return output2;
    }

    public int baseTicks() {
        return baseTicks;
    }

    @Override
    public boolean matches(AromaticExtractorRecipeInput recipeInput, Level level) {
        if (recipeInput.inputFluid() == null || recipeInput.catalystFluid() == null) {
            return false;
        }

        return recipeInput.inputFluid().equals(this.input.fluid())
                && recipeInput.inputAmount() >= this.input.amount()
                && recipeInput.catalystFluid().equals(this.catalyst.fluid())
                && recipeInput.catalystAmount() >= this.catalyst.amount();
    }

    @Override
    public ItemStack assemble(AromaticExtractorRecipeInput recipeInput, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<? extends Recipe<AromaticExtractorRecipeInput>> getSerializer() {
        return ModRecipeSerializers.AROMATIC_EXTRACTOR.get();
    }

    @Override
    public RecipeType<? extends Recipe<AromaticExtractorRecipeInput>> getType() {
        return ModRecipeTypes.AROMATIC_EXTRACTOR.get();
    }

    public static class Serializer implements RecipeSerializer<AromaticExtractorRecipe> {
        public static final MapCodec<AromaticExtractorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                AromaticExtractorFluidStack.CODEC.fieldOf("input").forGetter(AromaticExtractorRecipe::input),
                AromaticExtractorFluidStack.CODEC.fieldOf("catalyst").forGetter(AromaticExtractorRecipe::catalyst),
                AromaticExtractorFluidStack.CODEC.fieldOf("output_1").forGetter(AromaticExtractorRecipe::output1),
                AromaticExtractorFluidStack.CODEC.fieldOf("output_2").forGetter(AromaticExtractorRecipe::output2),
                Codec.INT.optionalFieldOf("base_ticks", 200).forGetter(AromaticExtractorRecipe::baseTicks)
        ).apply(instance, AromaticExtractorRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AromaticExtractorRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        AromaticExtractorFluidStack.STREAM_CODEC, AromaticExtractorRecipe::input,
                        AromaticExtractorFluidStack.STREAM_CODEC, AromaticExtractorRecipe::catalyst,
                        AromaticExtractorFluidStack.STREAM_CODEC, AromaticExtractorRecipe::output1,
                        AromaticExtractorFluidStack.STREAM_CODEC, AromaticExtractorRecipe::output2,
                        ByteBufCodecs.VAR_INT, AromaticExtractorRecipe::baseTicks,
                        AromaticExtractorRecipe::new
                );

        @Override
        public MapCodec<AromaticExtractorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AromaticExtractorRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
