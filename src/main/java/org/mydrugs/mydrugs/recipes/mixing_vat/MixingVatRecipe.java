package org.mydrugs.mydrugs.recipes.mixing_vat;

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
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MixingVatRecipe implements Recipe<MixingVatRecipeInput> {
    private final Optional<Ingredient> item1;
    private final Optional<Ingredient> item2;
    private final Optional<Ingredient> item3;
    private final Optional<Ingredient> item4;
    private final Optional<MixingVatFluidStack> fluidInput;

    private final ItemStack resultItem;
    private final Optional<MixingVatFluidStack> resultFluid;
    private final int requiredStirs;

    public MixingVatRecipe(
            Optional<Ingredient> item1,
            Optional<Ingredient> item2,
            Optional<Ingredient> item3,
            Optional<Ingredient> item4,
            Optional<MixingVatFluidStack> fluidInput,
            ItemStack resultItem,
            Optional<MixingVatFluidStack> resultFluid,
            int requiredStirs
    ) {
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        this.item4 = item4;
        this.fluidInput = fluidInput;
        this.resultItem = resultItem;
        this.resultFluid = resultFluid;
        this.requiredStirs = requiredStirs;
    }

    public Optional<Ingredient> item1() { return item1; }
    public Optional<Ingredient> item2() { return item2; }
    public Optional<Ingredient> item3() { return item3; }
    public Optional<Ingredient> item4() { return item4; }
    public Optional<MixingVatFluidStack> fluidInput() { return fluidInput; }
    public ItemStack resultItem() { return resultItem; }
    public Optional<MixingVatFluidStack> resultFluid() { return resultFluid; }
    public int requiredStirs() {
        return requiredStirs;
    }

    public List<Ingredient> requiredItems() {
        List<Ingredient> list = new ArrayList<>();
        item1.ifPresent(list::add);
        item2.ifPresent(list::add);
        item3.ifPresent(list::add);
        item4.ifPresent(list::add);
        return list;
    }

    @Override
    public boolean matches(MixingVatRecipeInput input, Level level) {
        if (fluidInput.isPresent()) {
            MixingVatFluidStack required = fluidInput.get();
            if (input.fluidId() == null) return false;
            if (!required.fluid().equals(input.fluidId())) return false;
            if (input.fluidAmount() < required.amount()) return false;
        } else if (input.fluidAmount() > 0) {
            return false;
        }

        List<Ingredient> required = requiredItems();

        int totalItems = input.items().stream()
                .filter(stack -> !stack.isEmpty())
                .mapToInt(ItemStack::getCount)
                .sum();

        if (totalItems != required.size()) {
            return false;
        }

        int[] used = new int[input.items().size()];

        for (Ingredient ingredient : required) {
            boolean matched = false;

            for (int i = 0; i < input.items().size(); i++) {
                ItemStack stack = input.items().get(i);
                if (stack.isEmpty()) continue;

                if (used[i] < stack.getCount() && ingredient.test(stack)) {
                    used[i]++;
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(MixingVatRecipeInput input, HolderLookup.Provider registries) {
        return resultItem.copy();
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
    public RecipeSerializer<? extends Recipe<MixingVatRecipeInput>> getSerializer() {
        return ModRecipeSerializers.MIXING_VAT.get();
    }

    @Override
    public RecipeType<? extends Recipe<MixingVatRecipeInput>> getType() {
        return ModRecipeTypes.MIXING_VAT.get();
    }

    public static class Serializer implements RecipeSerializer<MixingVatRecipe> {
        public static final MapCodec<MixingVatRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.optionalFieldOf("item_1").forGetter(MixingVatRecipe::item1),
                Ingredient.CODEC.optionalFieldOf("item_2").forGetter(MixingVatRecipe::item2),
                Ingredient.CODEC.optionalFieldOf("item_3").forGetter(MixingVatRecipe::item3),
                Ingredient.CODEC.optionalFieldOf("item_4").forGetter(MixingVatRecipe::item4),
                MixingVatFluidStack.CODEC.optionalFieldOf("fluid_input").forGetter(MixingVatRecipe::fluidInput),
                ItemStack.CODEC.optionalFieldOf("result_item", ItemStack.EMPTY).forGetter(MixingVatRecipe::resultItem),
                MixingVatFluidStack.CODEC.optionalFieldOf("result_fluid").forGetter(MixingVatRecipe::resultFluid),
                Codec.INT.optionalFieldOf("required_stirs", 4).forGetter(MixingVatRecipe::requiredStirs)
        ).apply(instance, MixingVatRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MixingVatRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional), MixingVatRecipe::item1,
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional), MixingVatRecipe::item2,
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional), MixingVatRecipe::item3,
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional), MixingVatRecipe::item4,
                        MixingVatFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional), MixingVatRecipe::fluidInput,
                        ItemStack.STREAM_CODEC, MixingVatRecipe::resultItem,
                        MixingVatFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional), MixingVatRecipe::resultFluid,
                        ByteBufCodecs.VAR_INT, MixingVatRecipe::requiredStirs,
                        MixingVatRecipe::new
                );

        @Override
        public MapCodec<MixingVatRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MixingVatRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}