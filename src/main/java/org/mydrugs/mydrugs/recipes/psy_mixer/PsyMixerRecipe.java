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
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.ritual.DrugEffectCombiner;
import org.mydrugs.mydrugs.core.drug.ritual.MixedDrugStackFactory;
import org.mydrugs.mydrugs.core.drug.ritual.RitualBaseDrugResolver;
import org.mydrugs.mydrugs.core.drug.ritual.RitualDrugEffectData;
import org.mydrugs.mydrugs.core.drug.ritual.RitualDrugFormula;
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
    private final List<RitualDrugEffectData> effects;
    /**
     * Not the authoritative success output. Psy Mixer success creates dynamic mixed-drug stacks
     * from the recipe effects and the resolved base drug. This stack is only a legacy/fallback
     * display value for contexts that cannot build a dynamic preview.
     */
    private final ItemStack fallbackResult;
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
    private final boolean hiddenBeforeDiscovery;
    private final boolean showIfLocked;
    private final ItemStack failureResult;
    private final boolean preserveVesselOnSuccess;
    private final boolean preserveVesselOnFailure;
    private final float catalystTimeMultiplier;
    private final float missingCatalystTimeMultiplier;
    private final float stabilizerInstabilityMultiplier;
    private final float missingStabilizerInstabilityMultiplier;

    private PlacementInfo placementInfo;

    public PsyMixerRecipe(
            Ingredient base,
            Ingredient material,
            Optional<Ingredient> catalyst,
            Optional<Ingredient> stabilizer,
            Optional<Ingredient> vessel,
            List<RitualDrugEffectData> effects,
            ItemStack fallbackResult,
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
            boolean hiddenBeforeDiscovery,
            boolean showIfLocked,
            ItemStack failureResult,
            boolean preserveVesselOnSuccess,
            boolean preserveVesselOnFailure,
            float catalystTimeMultiplier,
            float missingCatalystTimeMultiplier,
            float stabilizerInstabilityMultiplier,
            float missingStabilizerInstabilityMultiplier
    ) {
        this.base = base;
        this.material = material;
        this.catalyst = catalyst;
        this.stabilizer = stabilizer;
        this.vessel = vessel;
        this.effects = List.copyOf(effects);
        this.fallbackResult = fallbackResult.copy();
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
        this.hiddenBeforeDiscovery = hiddenBeforeDiscovery;
        this.showIfLocked = showIfLocked;
        this.failureResult = failureResult.copy();
        this.preserveVesselOnSuccess = preserveVesselOnSuccess;
        this.preserveVesselOnFailure = preserveVesselOnFailure;
        this.catalystTimeMultiplier = catalystTimeMultiplier;
        this.missingCatalystTimeMultiplier = missingCatalystTimeMultiplier;
        this.stabilizerInstabilityMultiplier = stabilizerInstabilityMultiplier;
        this.missingStabilizerInstabilityMultiplier = missingStabilizerInstabilityMultiplier;
    }

    public Ingredient base() { return base; }
    public Ingredient material() { return material; }
    public Optional<Ingredient> catalyst() { return catalyst; }
    public Optional<Ingredient> stabilizer() { return stabilizer; }
    public Optional<Ingredient> vessel() { return vessel; }
    public List<RitualDrugEffectData> effects() { return effects; }
    public ItemStack fallbackResult() { return fallbackResult.copy(); }
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
    public boolean hiddenBeforeDiscovery() { return hiddenBeforeDiscovery; }
    public boolean showIfLocked() { return showIfLocked; }
    public ItemStack failureResult() { return failureResult.copy(); }
    public boolean preserveVesselOnSuccess() { return preserveVesselOnSuccess; }
    public boolean preserveVesselOnFailure() { return preserveVesselOnFailure; }
    public float catalystTimeMultiplier() { return catalystTimeMultiplier; }
    public float missingCatalystTimeMultiplier() { return missingCatalystTimeMultiplier; }
    public float stabilizerInstabilityMultiplier() { return stabilizerInstabilityMultiplier; }
    public float missingStabilizerInstabilityMultiplier() { return missingStabilizerInstabilityMultiplier; }

    public boolean supportsCatalyst() { return catalyst.isPresent(); }
    public boolean hasValidCatalyst(ItemStack stack) { return catalyst.isPresent() && !stack.isEmpty() && catalyst.get().test(stack); }
    public boolean hasMissingCatalyst(ItemStack stack) { return catalyst.isPresent() && stack.isEmpty(); }

    public boolean supportsStabilizer() { return stabilizer.isPresent(); }
    public boolean hasValidStabilizer(ItemStack stack) { return stabilizer.isPresent() && !stack.isEmpty() && stabilizer.get().test(stack); }
    public boolean hasMissingStabilizer(ItemStack stack) { return stabilizer.isPresent() && stack.isEmpty(); }

    public float getEffectiveTimeMultiplier(ItemStack catalystStack) {
        if (catalyst.isEmpty()) return 1.0F;
        return (catalystStack.isEmpty() || !catalyst.get().test(catalystStack))
                ? missingCatalystTimeMultiplier
                : catalystTimeMultiplier;
    }

    public float getEffectiveInstabilityMultiplier(ItemStack stabilizerStack) {
        if (stabilizer.isEmpty()) return 1.0F;
        return (stabilizerStack.isEmpty() || !stabilizer.get().test(stabilizerStack))
                ? missingStabilizerInstabilityMultiplier
                : stabilizerInstabilityMultiplier;
    }

    public Optional<RitualDrugFormula> buildFormula(PsyMixerRecipeInput input) {
        Optional<DrugId> baseDrug = RitualBaseDrugResolver.resolve(input.base());
        if (baseDrug.isEmpty()) {
            return Optional.empty();
        }
        if (requiredDrug.isPresent()) {
            DrugId required = DrugId.bySerializedNameOrNull(requiredDrug.get());
            if (required == null || baseDrug.get() != required) {
                return Optional.empty();
            }
        }
        return buildFormula(baseDrug.get());
    }

    public Optional<RitualDrugFormula> previewFormula() {
        Optional<DrugId> baseDrug = requiredDrug.flatMap(DrugId::bySerializedName);
        return baseDrug.flatMap(this::buildFormula);
    }

    public ItemStack dynamicPreviewResult() {
        return previewFormula()
                .map(MixedDrugStackFactory::createPendingStack)
                .orElseGet(this::fallbackResult);
    }

    private Optional<RitualDrugFormula> buildFormula(DrugId baseDrug) {
        DrugModel baseModel = DrugRegistry.getDrug(baseDrug);
        if (baseModel == null) {
            return Optional.empty();
        }

        List<RitualDrugEffectData> baseEffects = baseModel.getDrugEffects().stream()
                .map(RitualDrugEffectData::from)
                .toList();
        return Optional.of(RitualDrugFormula.of(baseDrug, baseEffects, DrugEffectCombiner.combine(effects)));
    }

    @Override
    public boolean matches(PsyMixerRecipeInput input, Level level) {
        if (input.base().isEmpty() || !base.test(input.base())) return false;
        if (input.material().isEmpty() || !material.test(input.material())) return false;
        if (effects.isEmpty()) return false;
        if (buildFormula(input).isEmpty()) return false;

        if (catalyst.isPresent()) {
            if (!input.catalyst().isEmpty() && !catalyst.get().test(input.catalyst())) return false;
        } else {
            if (!input.catalyst().isEmpty()) return false;
        }
        if (stabilizer.isPresent()) {
            if (!input.stabilizer().isEmpty() && !stabilizer.get().test(input.stabilizer())) return false;
        } else {
            if (!input.stabilizer().isEmpty()) return false;
        }
        if (vessel.isPresent()) {
            if (input.vessel().isEmpty() || !vessel.get().test(input.vessel())) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(PsyMixerRecipeInput input, HolderLookup.Provider registries) {
        return buildFormula(input)
                .map(MixedDrugStackFactory::createPendingStack)
                .orElseGet(this::fallbackResult);
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
                effects,
                fallbackResult(),
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
                hiddenBeforeDiscovery,
                catalystTimeMultiplier,
                missingCatalystTimeMultiplier,
                stabilizerInstabilityMultiplier,
                missingStabilizerInstabilityMultiplier
        );
    }

    private static PsyMixerRecipe fromCodecData(CoreCodecData core, ExpansionCodecData expansion) {
        return new PsyMixerRecipe(
                core.base,
                core.material,
                core.catalyst,
                core.stabilizer,
                core.vessel,
                core.effects,
                core.fallbackResult,
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
                expansion.hiddenBeforeDiscovery,
                core.showIfLocked,
                core.failureResult,
                core.preserveVesselOnSuccess,
                core.preserveVesselOnFailure,
                expansion.catalystTimeMultiplier,
                expansion.missingCatalystTimeMultiplier,
                expansion.stabilizerInstabilityMultiplier,
                expansion.missingStabilizerInstabilityMultiplier
        );
    }

    private record CoreCodecData(
            Ingredient base,
            Ingredient material,
            Optional<Ingredient> catalyst,
            Optional<Ingredient> stabilizer,
            Optional<Ingredient> vessel,
            List<RitualDrugEffectData> effects,
            ItemStack fallbackResult,
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
                RitualDrugEffectData.CODEC.listOf().fieldOf("effects").forGetter(CoreCodecData::effects),
                ItemStack.CODEC.optionalFieldOf("fallback_result", ItemStack.EMPTY).forGetter(CoreCodecData::fallbackResult),
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
            boolean hiddenBeforeDiscovery,
            float catalystTimeMultiplier,
            float missingCatalystTimeMultiplier,
            float stabilizerInstabilityMultiplier,
            float missingStabilizerInstabilityMultiplier
    ) {
        private static final MapCodec<ExpansionCodecData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("required_drug_category").forGetter(ExpansionCodecData::requiredDrugCategory),
                Codec.STRING.optionalFieldOf("required_active_effect").forGetter(ExpansionCodecData::requiredActiveEffect),
                Codec.BOOL.optionalFieldOf("required_bad_trip_state", false).forGetter(ExpansionCodecData::requiredBadTripState),
                Codec.STRING.optionalFieldOf("required_ingredient_source").forGetter(ExpansionCodecData::requiredIngredientSource),
                Codec.FLOAT.optionalFieldOf("failure_severity", 0.0F).forGetter(ExpansionCodecData::failureSeverity),
                Codec.FLOAT.optionalFieldOf("machine_speed_modifier", 0.0F).forGetter(ExpansionCodecData::machineSpeedModifier),
                Codec.FLOAT.optionalFieldOf("ritual_stability_modifier", 0.0F).forGetter(ExpansionCodecData::ritualStabilityModifier),
                Codec.BOOL.optionalFieldOf("hidden_before_discovery", false).forGetter(ExpansionCodecData::hiddenBeforeDiscovery),
                Codec.FLOAT.optionalFieldOf("catalyst_time_multiplier", 0.75F).forGetter(ExpansionCodecData::catalystTimeMultiplier),
                Codec.FLOAT.optionalFieldOf("missing_catalyst_time_multiplier", 1.30F).forGetter(ExpansionCodecData::missingCatalystTimeMultiplier),
                Codec.FLOAT.optionalFieldOf("stabilizer_instability_multiplier", 0.75F).forGetter(ExpansionCodecData::stabilizerInstabilityMultiplier),
                Codec.FLOAT.optionalFieldOf("missing_stabilizer_instability_multiplier", 1.35F).forGetter(ExpansionCodecData::missingStabilizerInstabilityMultiplier)
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
            RitualDrugEffectData.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.effects);
            ItemStack.STREAM_CODEC.encode(buf, recipe.fallbackResult);
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
            buf.writeBoolean(recipe.hiddenBeforeDiscovery);
            buf.writeBoolean(recipe.showIfLocked);
            ItemStack.STREAM_CODEC.encode(buf, recipe.failureResult);
            buf.writeBoolean(recipe.preserveVesselOnSuccess);
            buf.writeBoolean(recipe.preserveVesselOnFailure);
            buf.writeFloat(recipe.catalystTimeMultiplier);
            buf.writeFloat(recipe.missingCatalystTimeMultiplier);
            buf.writeFloat(recipe.stabilizerInstabilityMultiplier);
            buf.writeFloat(recipe.missingStabilizerInstabilityMultiplier);
        }

        private static PsyMixerRecipe decode(RegistryFriendlyByteBuf buf) {
            Ingredient base = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Ingredient material = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Optional<Ingredient> catalyst = ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC).decode(buf);
            Optional<Ingredient> stabilizer = ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC).decode(buf);
            Optional<Ingredient> vessel = ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC).decode(buf);
            List<RitualDrugEffectData> effects = RitualDrugEffectData.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
            ItemStack fallbackResult = ItemStack.STREAM_CODEC.decode(buf);
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
            boolean hiddenBeforeDiscovery = buf.readBoolean();
            boolean showIfLocked = buf.readBoolean();
            ItemStack failureResult = ItemStack.STREAM_CODEC.decode(buf);
            boolean preserveVesselOnSuccess = buf.readBoolean();
            boolean preserveVesselOnFailure = buf.readBoolean();
            float catalystTimeMultiplier = buf.readFloat();
            float missingCatalystTimeMultiplier = buf.readFloat();
            float stabilizerInstabilityMultiplier = buf.readFloat();
            float missingStabilizerInstabilityMultiplier = buf.readFloat();
            return new PsyMixerRecipe(
                    base, material, catalyst, stabilizer, vessel, effects, fallbackResult,
                    ritualTime, baseInstability, requiredKnowledge, requiredDrug, requiredLifetimeDose,
                    requiredDrugCategory, requiredActiveEffect, requiredBadTripState, requiredIngredientSource,
                    failureSeverity, machineSpeedModifier, ritualStabilityModifier,
                    hiddenBeforeDiscovery,
                    showIfLocked, failureResult, preserveVesselOnSuccess, preserveVesselOnFailure,
                    catalystTimeMultiplier, missingCatalystTimeMultiplier,
                    stabilizerInstabilityMultiplier, missingStabilizerInstabilityMultiplier
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
