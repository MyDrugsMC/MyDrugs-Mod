package org.mydrugs.mydrugs.recipes.mixing_vat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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

    private final Optional<MixingVatFluidStack> fluidInput1;
    private final Optional<MixingVatFluidStack> fluidInput2;

    private final ItemStack resultItem;
    private final Optional<MixingVatFluidStack> resultFluid;
    private final int requiredStirs;
    private final boolean requiresHeat;

    public MixingVatRecipe(
            Optional<Ingredient> item1,
            Optional<Ingredient> item2,
            Optional<Ingredient> item3,
            Optional<Ingredient> item4,
            Optional<MixingVatFluidStack> fluidInput1,
            Optional<MixingVatFluidStack> fluidInput2,
            ItemStack resultItem,
            Optional<MixingVatFluidStack> resultFluid,
            int requiredStirs,
            boolean requiresHeat
    ) {
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        this.item4 = item4;
        this.fluidInput1 = fluidInput1;
        this.fluidInput2 = fluidInput2;
        this.resultItem = resultItem;
        this.resultFluid = resultFluid;
        this.requiredStirs = requiredStirs;
        this.requiresHeat = requiresHeat;
    }

    public Optional<Ingredient> item1() {
        return item1;
    }

    public Optional<Ingredient> item2() {
        return item2;
    }

    public Optional<Ingredient> item3() {
        return item3;
    }

    public Optional<Ingredient> item4() {
        return item4;
    }

    public Optional<MixingVatFluidStack> fluidInput1() {
        return fluidInput1;
    }

    public Optional<MixingVatFluidStack> fluidInput2() {
        return fluidInput2;
    }

    public ItemStack resultItem() {
        return resultItem;
    }

    public Optional<MixingVatFluidStack> resultFluid() {
        return resultFluid;
    }

    public int requiredStirs() {
        return requiredStirs;
    }

    public boolean requiresHeat() {
        return requiresHeat;
    }

    public List<Ingredient> requiredItems() {
        List<Ingredient> list = new ArrayList<>();
        item1.ifPresent(list::add);
        item2.ifPresent(list::add);
        item3.ifPresent(list::add);
        item4.ifPresent(list::add);
        return list;
    }

    public List<MixingVatFluidStack> requiredFluids() {
        List<MixingVatFluidStack> list = new ArrayList<>();
        fluidInput1.ifPresent(list::add);
        fluidInput2.ifPresent(list::add);
        return list;
    }

    @Override
    public boolean matches(MixingVatRecipeInput input, Level level) {
        List<MixingVatFluidStack> requiredFluids = requiredFluids();
        List<MixingVatFluidStack> presentFluids = input.fluids().stream()
                .filter(fluid -> fluid.amount() > 0)
                .toList();

        if (presentFluids.size() < requiredFluids.size()) {
            return false;
        }

        boolean[] usedFluids = new boolean[presentFluids.size()];

        for (MixingVatFluidStack required : requiredFluids) {
            boolean matched = false;

            for (int i = 0; i < presentFluids.size(); i++) {
                MixingVatFluidStack present = presentFluids.get(i);

                if (!usedFluids[i]
                        && required.fluid().equals(present.fluid())
                        && present.amount() >= required.amount()) {
                    usedFluids[i] = true;
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                return false;
            }
        }

        List<Ingredient> requiredItems = requiredItems();

        int totalItems = input.items().stream()
                .filter(stack -> !stack.isEmpty())
                .mapToInt(ItemStack::getCount)
                .sum();

        if (totalItems < requiredItems.size()) {
            return false;
        }

        int[] usedItems = new int[input.items().size()];

        for (Ingredient ingredient : requiredItems) {
            boolean matched = false;

            for (int i = 0; i < input.items().size(); i++) {
                ItemStack stack = input.items().get(i);
                if (stack.isEmpty()) {
                    continue;
                }

                if (usedItems[i] < stack.getCount() && ingredient.test(stack)) {
                    usedItems[i]++;
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
                MixingVatFluidStack.CODEC.optionalFieldOf("fluid_input_1").forGetter(MixingVatRecipe::fluidInput1),
                MixingVatFluidStack.CODEC.optionalFieldOf("fluid_input_2").forGetter(MixingVatRecipe::fluidInput2),
                ItemStack.CODEC.optionalFieldOf("result_item", ItemStack.EMPTY).forGetter(MixingVatRecipe::resultItem),
                MixingVatFluidStack.CODEC.optionalFieldOf("result_fluid").forGetter(MixingVatRecipe::resultFluid),
                Codec.INT.optionalFieldOf("required_stirs", 4).forGetter(MixingVatRecipe::requiredStirs),
                Codec.BOOL.optionalFieldOf("requires_heat", false).forGetter(MixingVatRecipe::requiresHeat)
        ).apply(instance, MixingVatRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MixingVatRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.item1());
                            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.item2());
                            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.item3());
                            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.item4());
                            MixingVatFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.fluidInput1());
                            MixingVatFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.fluidInput2());
                            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.resultItem());
                            MixingVatFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.resultFluid());
                            ByteBufCodecs.VAR_INT.encode(buf, recipe.requiredStirs());
                            ByteBufCodecs.BOOL.encode(buf, recipe.requiresHeat());
                        },
                        buf -> new MixingVatRecipe(
                                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                                MixingVatFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                                MixingVatFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                                ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                                MixingVatFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                                ByteBufCodecs.VAR_INT.decode(buf),
                                ByteBufCodecs.BOOL.decode(buf)
                        )
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