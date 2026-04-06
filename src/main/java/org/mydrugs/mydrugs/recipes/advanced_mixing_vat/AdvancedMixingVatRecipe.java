package org.mydrugs.mydrugs.recipes.advanced_mixing_vat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdvancedMixingVatRecipe implements Recipe<AdvancedMixingVatRecipeInput> {
    private final List<CountedIngredient> itemInputs;
    private final List<FluidRequirement> fluidInputs;
    @Nullable
    private final GasRequirement gasInput;
    private final FluidResult output;
    private final int processingTime;
    @Nullable
    private PlacementInfo placementInfo;

    public AdvancedMixingVatRecipe(
            List<CountedIngredient> itemInputs,
            List<FluidRequirement> fluidInputs,
            @Nullable GasRequirement gasInput,
            FluidResult output,
            int processingTime
    ) {
        if (itemInputs.size() > 4) {
            throw new IllegalArgumentException("Advanced Mixing Vat supports at most 4 item ingredients.");
        }
        if (fluidInputs.size() > 3) {
            throw new IllegalArgumentException("Advanced Mixing Vat supports at most 2 fluid ingredients.");
        }

        this.itemInputs = List.copyOf(itemInputs);
        this.fluidInputs = List.copyOf(fluidInputs);
        this.gasInput = gasInput;
        this.output = output;
        this.processingTime = Math.max(1, processingTime);
    }

    public List<CountedIngredient> itemInputs() {
        return this.itemInputs;
    }

    public List<FluidRequirement> fluidInputs() {
        return this.fluidInputs;
    }

    @Nullable
    public GasRequirement gasInput() {
        return this.gasInput;
    }

    public FluidResult output() {
        return this.output;
    }

    public int processingTime() {
        return this.processingTime;
    }

    @Override
    public boolean matches(AdvancedMixingVatRecipeInput input, Level level) {
        return itemsMatch(this.itemInputs, input.items())
                && fluidsMatch(this.fluidInputs, input.inputA(), input.inputB())
                && gasMatches(this.gasInput, input.gas());
    }

    @Override
    public ItemStack assemble(AdvancedMixingVatRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.itemInputs.isEmpty()) {
            return PlacementInfo.NOT_PLACEABLE;
        }

        if (this.placementInfo == null) {
            List<Optional<Ingredient>> optionals = new ArrayList<>(this.itemInputs.size());
            for (CountedIngredient counted : this.itemInputs) {
                optionals.add(Optional.of(counted.ingredient()));
            }
            this.placementInfo = PlacementInfo.createFromOptionals(optionals);
        }

        return this.placementInfo;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<? extends Recipe<AdvancedMixingVatRecipeInput>> getSerializer() {
        return ModRecipeSerializers.ADVANCED_MIXING_VAT_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<AdvancedMixingVatRecipeInput>> getType() {
        return ModRecipeTypes.ADVANCED_MIXING_VAT_RECIPE_TYPE.get();
    }

    public FluidStack resultStack() {
        Fluid fluid = BuiltInRegistries.FLUID.getValue(this.output.fluid());
        if (fluid == Fluids.EMPTY || this.output.amount() <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, this.output.amount());
    }

    public static boolean itemsMatch(List<CountedIngredient> requirements, List<ItemStack> available) {
        List<ItemStack> working = new ArrayList<>(available.size());
        for (ItemStack stack : available) {
            working.add(stack.copy());
        }

        for (CountedIngredient requirement : requirements) {
            int remaining = requirement.count();

            for (ItemStack stack : working) {
                if (remaining <= 0) {
                    break;
                }
                if (!stack.isEmpty() && requirement.ingredient().test(stack)) {
                    int used = Math.min(remaining, stack.getCount());
                    stack.shrink(used);
                    remaining -= used;
                }
            }

            if (remaining > 0) {
                return false;
            }
        }

        return true;
    }

    public static void consumeItems(List<CountedIngredient> requirements, NonNullList<ItemStack> available) {
        for (CountedIngredient requirement : requirements) {
            int remaining = requirement.count();

            for (int i = 0; i < available.size(); i++) {
                if (remaining <= 0) {
                    break;
                }

                ItemStack stack = available.get(i);
                if (!stack.isEmpty() && requirement.ingredient().test(stack)) {
                    int used = Math.min(remaining, stack.getCount());
                    stack.shrink(used);
                    if (stack.isEmpty()) {
                        available.set(i, ItemStack.EMPTY);
                    }
                    remaining -= used;
                }
            }
        }
    }

    public static boolean fluidsMatch(List<FluidRequirement> requirements, FluidStack a, FluidStack b) {
        if (requirements.isEmpty()) {
            return true;
        }
        if (requirements.size() == 1) {
            FluidRequirement req = requirements.get(0);
            return req.matches(a) || req.matches(b);
        }

        FluidRequirement first = requirements.get(0);
        FluidRequirement second = requirements.get(1);

        return (first.matches(a) && second.matches(b))
                || (first.matches(b) && second.matches(a));
    }

    public static void consumeFluids(
            List<FluidRequirement> requirements,
            NonNullList<FluidStack> tankA,
            NonNullList<FluidStack> tankB,
            NonNullList<FluidStack> tankC
    ) {
        if (requirements.isEmpty()) {
            return;
        }

        if (requirements.size() > 3) {
            throw new IllegalArgumentException("AdvancedMixingVat supports at most 3 fluid requirements");
        }

        NonNullList<FluidStack>[] tanks = new NonNullList[] { tankA, tankB, tankC };
        FluidStack[] fluids = new FluidStack[] { tankA.get(0), tankB.get(0), tankC.get(0) };

        int[] assignment = new int[requirements.size()];
        for (int i = 0; i < assignment.length; i++) {
            assignment[i] = -1;
        }

        boolean[] usedTanks = new boolean[3];

        if (!assignFluidRequirements(requirements, fluids, 0, usedTanks, assignment)) {
            return;
        }

        for (int i = 0; i < requirements.size(); i++) {
            shrinkFluid(tanks[assignment[i]], requirements.get(i).amount());
        }
    }

    private static boolean assignFluidRequirements(
            List<FluidRequirement> requirements,
            FluidStack[] fluids,
            int requirementIndex,
            boolean[] usedTanks,
            int[] assignment
    ) {
        if (requirementIndex >= requirements.size()) {
            return true;
        }

        FluidRequirement requirement = requirements.get(requirementIndex);

        for (int tankIndex = 0; tankIndex < fluids.length; tankIndex++) {
            if (usedTanks[tankIndex]) {
                continue;
            }

            if (!requirement.matches(fluids[tankIndex])) {
                continue;
            }

            usedTanks[tankIndex] = true;
            assignment[requirementIndex] = tankIndex;

            if (assignFluidRequirements(requirements, fluids, requirementIndex + 1, usedTanks, assignment)) {
                return true;
            }

            usedTanks[tankIndex] = false;
            assignment[requirementIndex] = -1;
        }

        return false;
    }

    private static void shrinkFluid(NonNullList<FluidStack> tank, int amount) {
        FluidStack stored = tank.get(0);
        if (stored.isEmpty()) {
            return;
        }

        int newAmount = stored.getAmount() - amount;
        if (newAmount <= 0) {
            tank.set(0, FluidStack.EMPTY);
        } else {
            tank.set(0, stored.copyWithAmount(newAmount));
        }
    }

    public static boolean gasMatches(@Nullable GasRequirement requirement, GasStack available) {
        if (requirement == null) {
            return true;
        }
        if (available == null || available.isEmpty()) {
            return false;
        }

        ResourceLocation availableId = available.type().id();
        return requirement.gas().equals(availableId) && available.amount() >= requirement.amount();
    }

    public record CountedIngredient(Ingredient ingredient, int count) {
        public static final Codec<CountedIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(CountedIngredient::ingredient),
                Codec.INT.optionalFieldOf("count", 1).forGetter(CountedIngredient::count)
        ).apply(instance, CountedIngredient::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CountedIngredient> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, CountedIngredient::ingredient,
                        ByteBufCodecs.VAR_INT, CountedIngredient::count,
                        CountedIngredient::new
                );
    }

    public record FluidRequirement(ResourceLocation fluid, int amount) {
        public static final Codec<FluidRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("fluid").forGetter(FluidRequirement::fluid),
                Codec.INT.fieldOf("amount").forGetter(FluidRequirement::amount)
        ).apply(instance, FluidRequirement::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidRequirement> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, FluidRequirement::fluid,
                        ByteBufCodecs.VAR_INT, FluidRequirement::amount,
                        FluidRequirement::new
                );

        public boolean matches(FluidStack stack) {
            if (stack == null || stack.isEmpty()) {
                return false;
            }
            return BuiltInRegistries.FLUID.getKey(stack.getFluid()).equals(this.fluid) && stack.getAmount() >= this.amount;
        }
    }

    public record GasRequirement(ResourceLocation gas, long amount) {
        public static final Codec<GasRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("gas").forGetter(GasRequirement::gas),
                Codec.LONG.fieldOf("amount").forGetter(GasRequirement::amount)
        ).apply(instance, GasRequirement::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, GasRequirement> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, GasRequirement::gas,
                        ByteBufCodecs.VAR_LONG, GasRequirement::amount,
                        GasRequirement::new
                );
    }

    public record FluidResult(ResourceLocation fluid, int amount) {
        public static final Codec<FluidResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("fluid").forGetter(FluidResult::fluid),
                Codec.INT.fieldOf("amount").forGetter(FluidResult::amount)
        ).apply(instance, FluidResult::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidResult> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, FluidResult::fluid,
                        ByteBufCodecs.VAR_INT, FluidResult::amount,
                        FluidResult::new
                );
    }

    public static final class Serializer implements RecipeSerializer<AdvancedMixingVatRecipe> {
        public static final MapCodec<AdvancedMixingVatRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CountedIngredient.CODEC.listOf().optionalFieldOf("item_inputs", List.of()).forGetter(AdvancedMixingVatRecipe::itemInputs),
                FluidRequirement.CODEC.listOf().optionalFieldOf("fluid_inputs", List.of()).forGetter(AdvancedMixingVatRecipe::fluidInputs),
                GasRequirement.CODEC.optionalFieldOf("gas_input").forGetter(recipe -> Optional.ofNullable(recipe.gasInput())),
                FluidResult.CODEC.fieldOf("output").forGetter(AdvancedMixingVatRecipe::output),
                Codec.INT.optionalFieldOf("processing_time", 100).forGetter(AdvancedMixingVatRecipe::processingTime)
        ).apply(instance, (items, fluids, gas, output, time) ->
                new AdvancedMixingVatRecipe(items, fluids, gas.orElse(null), output, time)
        ));

        public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedMixingVatRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        CountedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), AdvancedMixingVatRecipe::itemInputs,
                        FluidRequirement.STREAM_CODEC.apply(ByteBufCodecs.list()), AdvancedMixingVatRecipe::fluidInputs,
                        GasRequirement.STREAM_CODEC.apply(ByteBufCodecs::optional), recipe -> Optional.ofNullable(recipe.gasInput()),
                        FluidResult.STREAM_CODEC, AdvancedMixingVatRecipe::output,
                        ByteBufCodecs.VAR_INT, AdvancedMixingVatRecipe::processingTime,
                        (items, fluids, gas, output, time) -> new AdvancedMixingVatRecipe(items, fluids, gas.orElse(null), output, time)
                );

        @Override
        public MapCodec<AdvancedMixingVatRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AdvancedMixingVatRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}