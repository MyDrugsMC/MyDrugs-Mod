package org.mydrugs.mydrugs.recipes.psy_mixer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
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
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PsyMixerRecipe implements Recipe<PsyMixerRecipeInput> {
    private final Ingredient base;
    private final Ingredient material;
    private final Optional<Ingredient> catalyst;
    private final Optional<Ingredient> stabilizer;
    private final Optional<Ingredient> vessel;
    private final ItemStack result;
    private final int ritualTime;
    private final float baseInstability;
    private final Optional<ResourceLocation> requiredKnowledge;
    private final Optional<String> requiredDrug;
    private final float requiredLifetimeDose;
    private final Optional<String> requiredDrugCategory;
    private final Optional<String> requiredActiveEffect;
    private final boolean requiredBadTripState;
    private final Optional<String> requiredIngredientSource;
    private final float failureSeverity;
    private final float machineSpeedModifier;
    private final float ritualStabilityModifier;
    private final Optional<String> resultingCustomDrugVariant;
    private final boolean hiddenBeforeDiscovery;
    private final boolean showIfLocked;
    private final ItemStack failureResult;
    private final boolean preserveVesselOnSuccess;
    private final boolean preserveVesselOnFailure;

    private PlacementInfo placementInfo;

    public PsyMixerRecipe(
            Ingredient base,
            Ingredient material,
            Optional<Ingredient> catalyst,
            Optional<Ingredient> stabilizer,
            Optional<Ingredient> vessel,
            ItemStack result,
            int ritualTime,
            float baseInstability,
            Optional<ResourceLocation> requiredKnowledge,
            Optional<String> requiredDrug,
            float requiredLifetimeDose,
            Optional<String> requiredDrugCategory,
            Optional<String> requiredActiveEffect,
            boolean requiredBadTripState,
            Optional<String> requiredIngredientSource,
            float failureSeverity,
            float machineSpeedModifier,
            float ritualStabilityModifier,
            Optional<String> resultingCustomDrugVariant,
            boolean hiddenBeforeDiscovery,
            boolean showIfLocked,
            ItemStack failureResult,
            boolean preserveVesselOnSuccess,
            boolean preserveVesselOnFailure
    ) {
        this.base = base;
        this.material = material;
        this.catalyst = catalyst;
        this.stabilizer = stabilizer;
        this.vessel = vessel;
        this.result = result.copy();
        this.ritualTime = Math.max(20, ritualTime);
        this.baseInstability = baseInstability;
        this.requiredKnowledge = requiredKnowledge;
        this.requiredDrug = requiredDrug;
        this.requiredLifetimeDose = Math.max(0.0F, requiredLifetimeDose);
        this.requiredDrugCategory = requiredDrugCategory;
        this.requiredActiveEffect = requiredActiveEffect;
        this.requiredBadTripState = requiredBadTripState;
        this.requiredIngredientSource = requiredIngredientSource;
        this.failureSeverity = Math.max(0.0F, failureSeverity);
        this.machineSpeedModifier = machineSpeedModifier;
        this.ritualStabilityModifier = ritualStabilityModifier;
        this.resultingCustomDrugVariant = resultingCustomDrugVariant;
        this.hiddenBeforeDiscovery = hiddenBeforeDiscovery;
        this.showIfLocked = showIfLocked;
        this.failureResult = failureResult.copy();
        this.preserveVesselOnSuccess = preserveVesselOnSuccess;
        this.preserveVesselOnFailure = preserveVesselOnFailure;
    }

    public Ingredient base() { return base; }
    public Ingredient material() { return material; }
    public Optional<Ingredient> catalyst() { return catalyst; }
    public Optional<Ingredient> stabilizer() { return stabilizer; }
    public Optional<Ingredient> vessel() { return vessel; }
    public ItemStack result() { return result.copy(); }
    public int ritualTime() { return ritualTime; }
    public float baseInstability() { return baseInstability; }
    public Optional<ResourceLocation> requiredKnowledge() { return requiredKnowledge; }
    public Optional<String> requiredDrug() { return requiredDrug; }
    public float requiredLifetimeDose() { return requiredLifetimeDose; }
    public Optional<String> requiredDrugCategory() { return requiredDrugCategory; }
    public Optional<String> requiredActiveEffect() { return requiredActiveEffect; }
    public boolean requiredBadTripState() { return requiredBadTripState; }
    public Optional<String> requiredIngredientSource() { return requiredIngredientSource; }
    public float failureSeverity() { return failureSeverity; }
    public float machineSpeedModifier() { return machineSpeedModifier; }
    public float ritualStabilityModifier() { return ritualStabilityModifier; }
    public Optional<String> resultingCustomDrugVariant() { return resultingCustomDrugVariant; }
    public boolean hiddenBeforeDiscovery() { return hiddenBeforeDiscovery; }
    public boolean showIfLocked() { return showIfLocked; }
    public ItemStack failureResult() { return failureResult.copy(); }
    public boolean preserveVesselOnSuccess() { return preserveVesselOnSuccess; }
    public boolean preserveVesselOnFailure() { return preserveVesselOnFailure; }

    @Override
    public boolean matches(PsyMixerRecipeInput input, Level level) {
        if (input.base().isEmpty() || !base.test(input.base())) return false;
        if (input.material().isEmpty() || !material.test(input.material())) return false;

        if (catalyst.isPresent()) {
            if (input.catalyst().isEmpty() || !catalyst.get().test(input.catalyst())) return false;
        }
        if (stabilizer.isPresent()) {
            if (input.stabilizer().isEmpty() || !stabilizer.get().test(input.stabilizer())) return false;
        }
        if (vessel.isPresent()) {
            if (input.vessel().isEmpty() || !vessel.get().test(input.vessel())) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(PsyMixerRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            List<Ingredient> list = new ArrayList<>();
            list.add(base);
            list.add(material);
            catalyst.ifPresent(list::add);
            stabilizer.ifPresent(list::add);
            vessel.ifPresent(list::add);
            this.placementInfo = PlacementInfo.create(list);
        }
        return this.placementInfo;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<? extends Recipe<PsyMixerRecipeInput>> getSerializer() {
        return ModRecipeSerializers.PSY_MIXER.get();
    }

    @Override
    public RecipeType<? extends Recipe<PsyMixerRecipeInput>> getType() {
        return ModRecipeTypes.PSY_MIXER.get();
    }

    private CoreCodecData coreCodecData() {
        return new CoreCodecData(
                base,
                material,
                catalyst,
                stabilizer,
                vessel,
                result(),
                ritualTime,
                baseInstability,
                requiredKnowledge,
                requiredDrug,
                requiredLifetimeDose,
                showIfLocked,
                failureResult(),
                preserveVesselOnSuccess,
                preserveVesselOnFailure
        );
    }

    private ExpansionCodecData expansionCodecData() {
        return new ExpansionCodecData(
                requiredDrugCategory,
                requiredActiveEffect,
                requiredBadTripState,
                requiredIngredientSource,
                failureSeverity,
                machineSpeedModifier,
                ritualStabilityModifier,
                resultingCustomDrugVariant,
                hiddenBeforeDiscovery
        );
    }

    private static PsyMixerRecipe fromCodecData(CoreCodecData core, ExpansionCodecData expansion) {
        return new PsyMixerRecipe(
                core.base,
                core.material,
                core.catalyst,
                core.stabilizer,
                core.vessel,
                core.result,
                core.ritualTime,
                core.baseInstability,
                core.requiredKnowledge,
                core.requiredDrug,
                core.requiredLifetimeDose,
                expansion.requiredDrugCategory,
                expansion.requiredActiveEffect,
                expansion.requiredBadTripState,
                expansion.requiredIngredientSource,
                expansion.failureSeverity,
                expansion.machineSpeedModifier,
                expansion.ritualStabilityModifier,
                expansion.resultingCustomDrugVariant,
                expansion.hiddenBeforeDiscovery,
                core.showIfLocked,
                core.failureResult,
                core.preserveVesselOnSuccess,
                core.preserveVesselOnFailure
        );
    }

    private record CoreCodecData(
            Ingredient base,
            Ingredient material,
            Optional<Ingredient> catalyst,
            Optional<Ingredient> stabilizer,
            Optional<Ingredient> vessel,
            ItemStack result,
            int ritualTime,
            float baseInstability,
            Optional<ResourceLocation> requiredKnowledge,
            Optional<String> requiredDrug,
            float requiredLifetimeDose,
            boolean showIfLocked,
            ItemStack failureResult,
            boolean preserveVesselOnSuccess,
            boolean preserveVesselOnFailure
    ) {
        private static final MapCodec<CoreCodecData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("base").forGetter(CoreCodecData::base),
                Ingredient.CODEC.fieldOf("material").forGetter(CoreCodecData::material),
                Ingredient.CODEC.optionalFieldOf("catalyst").forGetter(CoreCodecData::catalyst),
                Ingredient.CODEC.optionalFieldOf("stabilizer").forGetter(CoreCodecData::stabilizer),
                Ingredient.CODEC.optionalFieldOf("vessel").forGetter(CoreCodecData::vessel),
                ItemStack.CODEC.fieldOf("result").forGetter(CoreCodecData::result),
                Codec.INT.optionalFieldOf("ritual_time", 400).forGetter(CoreCodecData::ritualTime),
                Codec.FLOAT.optionalFieldOf("base_instability", 0.25F).forGetter(CoreCodecData::baseInstability),
                ResourceLocation.CODEC.optionalFieldOf("required_knowledge").forGetter(CoreCodecData::requiredKnowledge),
                Codec.STRING.optionalFieldOf("required_drug").forGetter(CoreCodecData::requiredDrug),
                Codec.FLOAT.optionalFieldOf("required_lifetime_dose", 0.0F).forGetter(CoreCodecData::requiredLifetimeDose),
                Codec.BOOL.optionalFieldOf("show_if_locked", true).forGetter(CoreCodecData::showIfLocked),
                ItemStack.CODEC.optionalFieldOf("failure_result", ItemStack.EMPTY).forGetter(CoreCodecData::failureResult),
                Codec.BOOL.optionalFieldOf("preserve_vessel_on_success", false).forGetter(CoreCodecData::preserveVesselOnSuccess),
                Codec.BOOL.optionalFieldOf("preserve_vessel_on_failure", true).forGetter(CoreCodecData::preserveVesselOnFailure)
        ).apply(instance, CoreCodecData::new));
    }

    private record ExpansionCodecData(
            Optional<String> requiredDrugCategory,
            Optional<String> requiredActiveEffect,
            boolean requiredBadTripState,
            Optional<String> requiredIngredientSource,
            float failureSeverity,
            float machineSpeedModifier,
            float ritualStabilityModifier,
            Optional<String> resultingCustomDrugVariant,
            boolean hiddenBeforeDiscovery
    ) {
        private static final MapCodec<ExpansionCodecData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("required_drug_category").forGetter(ExpansionCodecData::requiredDrugCategory),
                Codec.STRING.optionalFieldOf("required_active_effect").forGetter(ExpansionCodecData::requiredActiveEffect),
                Codec.BOOL.optionalFieldOf("required_bad_trip_state", false).forGetter(ExpansionCodecData::requiredBadTripState),
                Codec.STRING.optionalFieldOf("required_ingredient_source").forGetter(ExpansionCodecData::requiredIngredientSource),
                Codec.FLOAT.optionalFieldOf("failure_severity", 0.0F).forGetter(ExpansionCodecData::failureSeverity),
                Codec.FLOAT.optionalFieldOf("machine_speed_modifier", 0.0F).forGetter(ExpansionCodecData::machineSpeedModifier),
                Codec.FLOAT.optionalFieldOf("ritual_stability_modifier", 0.0F).forGetter(ExpansionCodecData::ritualStabilityModifier),
                Codec.STRING.optionalFieldOf("resulting_custom_drug_variant").forGetter(ExpansionCodecData::resultingCustomDrugVariant),
                Codec.BOOL.optionalFieldOf("hidden_before_discovery", false).forGetter(ExpansionCodecData::hiddenBeforeDiscovery)
        ).apply(instance, ExpansionCodecData::new));
    }

    public static final class Serializer implements RecipeSerializer<PsyMixerRecipe> {
        public static final MapCodec<PsyMixerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CoreCodecData.CODEC.forGetter(PsyMixerRecipe::coreCodecData),
                ExpansionCodecData.CODEC.forGetter(PsyMixerRecipe::expansionCodecData)
        ).apply(instance, PsyMixerRecipe::fromCodecData));

        public static final StreamCodec<RegistryFriendlyByteBuf, PsyMixerRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::encode,
                Serializer::decode
        );

        private static void encode(RegistryFriendlyByteBuf buf, PsyMixerRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.base);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.material);
            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC).encode(buf, recipe.catalyst);
            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC).encode(buf, recipe.stabilizer);
            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC).encode(buf, recipe.vessel);
            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
            buf.writeVarInt(recipe.ritualTime);
            buf.writeFloat(recipe.baseInstability);
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buf, recipe.requiredKnowledge);
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buf, recipe.requiredDrug);
            buf.writeFloat(recipe.requiredLifetimeDose);
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buf, recipe.requiredDrugCategory);
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buf, recipe.requiredActiveEffect);
            buf.writeBoolean(recipe.requiredBadTripState);
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buf, recipe.requiredIngredientSource);
            buf.writeFloat(recipe.failureSeverity);
            buf.writeFloat(recipe.machineSpeedModifier);
            buf.writeFloat(recipe.ritualStabilityModifier);
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buf, recipe.resultingCustomDrugVariant);
            buf.writeBoolean(recipe.hiddenBeforeDiscovery);
            buf.writeBoolean(recipe.showIfLocked);
            ItemStack.STREAM_CODEC.encode(buf, recipe.failureResult);
            buf.writeBoolean(recipe.preserveVesselOnSuccess);
            buf.writeBoolean(recipe.preserveVesselOnFailure);
        }

        private static PsyMixerRecipe decode(RegistryFriendlyByteBuf buf) {
            Ingredient base = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Ingredient material = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Optional<Ingredient> catalyst = ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC).decode(buf);
            Optional<Ingredient> stabilizer = ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC).decode(buf);
            Optional<Ingredient> vessel = ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC).decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            int ritualTime = buf.readVarInt();
            float baseInstability = buf.readFloat();
            Optional<ResourceLocation> requiredKnowledge = ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buf);
            Optional<String> requiredDrug = ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buf);
            float requiredLifetimeDose = buf.readFloat();
            Optional<String> requiredDrugCategory = ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buf);
            Optional<String> requiredActiveEffect = ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buf);
            boolean requiredBadTripState = buf.readBoolean();
            Optional<String> requiredIngredientSource = ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buf);
            float failureSeverity = buf.readFloat();
            float machineSpeedModifier = buf.readFloat();
            float ritualStabilityModifier = buf.readFloat();
            Optional<String> resultingCustomDrugVariant = ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buf);
            boolean hiddenBeforeDiscovery = buf.readBoolean();
            boolean showIfLocked = buf.readBoolean();
            ItemStack failureResult = ItemStack.STREAM_CODEC.decode(buf);
            boolean preserveVesselOnSuccess = buf.readBoolean();
            boolean preserveVesselOnFailure = buf.readBoolean();
            return new PsyMixerRecipe(
                    base, material, catalyst, stabilizer, vessel, result,
                    ritualTime, baseInstability, requiredKnowledge, requiredDrug, requiredLifetimeDose,
                    requiredDrugCategory, requiredActiveEffect, requiredBadTripState, requiredIngredientSource,
                    failureSeverity, machineSpeedModifier, ritualStabilityModifier, resultingCustomDrugVariant,
                    hiddenBeforeDiscovery,
                    showIfLocked, failureResult, preserveVesselOnSuccess, preserveVesselOnFailure
            );
        }

        @Override
        public MapCodec<PsyMixerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PsyMixerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
