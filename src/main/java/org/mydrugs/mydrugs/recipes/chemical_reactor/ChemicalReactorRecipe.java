package org.mydrugs.mydrugs.recipes.chemical_reactor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.Optional;

public class ChemicalReactorRecipe implements Recipe<ChemicalReactorRecipeInput> {
    private final GasRequirement primaryGas;
    private final Optional<GasRequirement> secondaryGas;
    private final Optional<FluidRequirement> secondaryFluid;
    private final ReactorOutputKind outputKind;
    private final ResourceLocation outputId;
    private final int outputAmount;
    private final int processTime;
    private final int minHeat;
    private final int heatDrain;

    public ChemicalReactorRecipe(
            GasRequirement primaryGas,
            Optional<GasRequirement> secondaryGas,
            Optional<FluidRequirement> secondaryFluid,
            ReactorOutputKind outputKind,
            ResourceLocation outputId,
            int outputAmount,
            int processTime,
            int minHeat,
            int heatDrain
    ) {
        if (outputAmount <= 0) {
            throw new IllegalArgumentException("outputAmount must be > 0");
        }
        if (processTime <= 0) {
            throw new IllegalArgumentException("processTime must be > 0");
        }
        if (secondaryGas.isPresent() && secondaryFluid.isPresent()) {
            throw new IllegalArgumentException("Recipe cannot have both secondaryGas and secondaryFluid at once in this implementation");
        }

        this.primaryGas = primaryGas;
        this.secondaryGas = secondaryGas;
        this.secondaryFluid = secondaryFluid;
        this.outputKind = outputKind;
        this.outputId = outputId;
        this.outputAmount = outputAmount;
        this.processTime = processTime;
        this.minHeat = minHeat;
        this.heatDrain = heatDrain;
    }

    @Override
    public boolean matches(ChemicalReactorRecipeInput input, Level level) {
        if (!this.primaryGas.test(input.primaryGas())) {
            return false;
        }

        if (this.secondaryGas.isPresent() && !this.secondaryGas.get().test(input.secondaryGas())) {
            return false;
        }

        return this.secondaryFluid.isEmpty() || this.secondaryFluid.get().test(input.secondaryFluid());
    }

    @Override
    public ItemStack assemble(ChemicalReactorRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<ChemicalReactorRecipeInput>> getSerializer() {
        return ModRecipeSerializers.CHEMICAL_REACTOR.get();
    }

    @Override
    public RecipeType<? extends Recipe<ChemicalReactorRecipeInput>> getType() {
        return ModRecipeTypes.CHEMICAL_REACTOR.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public GasRequirement primaryGas() {
        return primaryGas;
    }

    public Optional<GasRequirement> secondaryGas() {
        return secondaryGas;
    }

    public Optional<FluidRequirement> secondaryFluid() {
        return secondaryFluid;
    }

    public ReactorOutputKind outputKind() {
        return outputKind;
    }

    public ResourceLocation outputId() {
        return outputId;
    }

    public int outputAmount() {
        return outputAmount;
    }

    public int processTime() {
        return processTime;
    }

    public int minHeat() {
        return minHeat;
    }

    public int heatDrain() {
        return heatDrain;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<ChemicalReactorRecipe> {
        public static final Codec<GasRequirement> GAS_REQ_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("gas").forGetter(GasRequirement::gasId),
                Codec.LONG.fieldOf("amount").forGetter(GasRequirement::amount)
        ).apply(instance, GasRequirement::new));

        public static final Codec<FluidRequirement> FLUID_REQ_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("fluid").forGetter(FluidRequirement::fluidId),
                Codec.INT.fieldOf("amount").forGetter(FluidRequirement::amount)
        ).apply(instance, FluidRequirement::new));

        public static final Codec<ReactorOutputKind> OUTPUT_KIND_CODEC =
                StringRepresentable.fromEnum(ReactorOutputKind::values);

        public static final MapCodec<ChemicalReactorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                GAS_REQ_CODEC.fieldOf("primary_gas").forGetter(ChemicalReactorRecipe::primaryGas),
                GAS_REQ_CODEC.optionalFieldOf("secondary_gas").forGetter(ChemicalReactorRecipe::secondaryGas),
                FLUID_REQ_CODEC.optionalFieldOf("secondary_fluid").forGetter(ChemicalReactorRecipe::secondaryFluid),
                OUTPUT_KIND_CODEC.fieldOf("output_kind").forGetter(ChemicalReactorRecipe::outputKind),
                ResourceLocation.CODEC.fieldOf("output_id").forGetter(ChemicalReactorRecipe::outputId),
                Codec.INT.fieldOf("output_amount").forGetter(ChemicalReactorRecipe::outputAmount),
                Codec.INT.optionalFieldOf("process_time", 200).forGetter(ChemicalReactorRecipe::processTime),
                Codec.INT.optionalFieldOf("min_heat", 0).forGetter(ChemicalReactorRecipe::minHeat),
                Codec.INT.optionalFieldOf("heat_drain", 10).forGetter(ChemicalReactorRecipe::heatDrain)
        ).apply(instance, ChemicalReactorRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, GasRequirement> GAS_REQ_STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC,
                GasRequirement::gasId,
                ByteBufCodecs.VAR_LONG,
                GasRequirement::amount,
                GasRequirement::new
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidRequirement> FLUID_REQ_STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC,
                FluidRequirement::fluidId,
                ByteBufCodecs.VAR_INT,
                FluidRequirement::amount,
                FluidRequirement::new
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalReactorRecipe> STREAM_CODEC = StreamCodec.composite(
                GAS_REQ_STREAM_CODEC,
                ChemicalReactorRecipe::primaryGas,
                ByteBufCodecs.optional(GAS_REQ_STREAM_CODEC),
                ChemicalReactorRecipe::secondaryGas,
                ByteBufCodecs.optional(FLUID_REQ_STREAM_CODEC),
                ChemicalReactorRecipe::secondaryFluid,
                ByteBufCodecs.STRING_UTF8.map(ReactorOutputKind::bySerializedName, ReactorOutputKind::getSerializedName),
                ChemicalReactorRecipe::outputKind,
                ResourceLocation.STREAM_CODEC,
                ChemicalReactorRecipe::outputId,
                ByteBufCodecs.VAR_INT,
                ChemicalReactorRecipe::outputAmount,
                ByteBufCodecs.VAR_INT,
                ChemicalReactorRecipe::processTime,
                ByteBufCodecs.VAR_INT,
                ChemicalReactorRecipe::minHeat,
                ByteBufCodecs.VAR_INT,
                ChemicalReactorRecipe::heatDrain,
                ChemicalReactorRecipe::new
        );

        @Override
        public MapCodec<ChemicalReactorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ChemicalReactorRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
