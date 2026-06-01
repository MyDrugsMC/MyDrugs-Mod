package org.mydrugs.mydrugs.core.drug;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.Core;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DrugRegistry {
    private static final Map<DrugId, DrugModel> drugs = new HashMap<>();
    private static final EnumMap<DrugId, Integer> psychotropeValues = new EnumMap<>(DrugId.class);
    private static final EnumMap<DrugCategory, DrugId> representativeDrugs = new EnumMap<>(DrugCategory.class);
    public static final int COFFEE_MAIN_DURATION = DrugDurationScale.fromRealHours(2.0F, 12.0F);
    public static final int TOBACCO_MAIN_DURATION = DrugDurationScale.fromRealHours(1.5F, 12.0F);
    public static final int CANNABIS_MAIN_DURATION = DrugDurationScale.fromRealHours(3.0F, 12.0F);
    public static final int ALCOHOL_MAIN_DURATION = DrugDurationScale.fromRealHours(2.5F, 12.0F);
    public static final int COCAINE_HIGH_DURATION = DrugDurationScale.seconds(90);
    public static final int COCAINE_AFTEREFFECT_DURATION = DrugDurationScale.seconds(160);
    public static final int CRACK_HIGH_DURATION = DrugDurationScale.seconds(55);
    public static final int METH_MAIN_DURATION = DrugDurationScale.fromRealHours(4.0F, 12.0F);
    public static final int PSYCHEDELIC_MAIN_DURATION = DrugDurationScale.fromRealHours(5.0F, 12.0F);

    private DrugRegistry() {
    }

    public static void registerDrugs() {
        if (!drugs.isEmpty()) {
            Core.getLOGGER().warning("Could not register drugs because map was not initialized !");
            return;
        }

        addDrug(new DrugModel.Builder()
                .setId(DrugId.WEED)
                .setCategory(DrugCategory.CANNABINOID)
                .addEffect(new DrugEffect(EffectType.MOVEMENT_SLOWDOWN, CANNABIS_MAIN_DURATION, 0.06F))
                .addEffect(new DrugEffect(EffectType.STRESS_RELIEF, CANNABIS_MAIN_DURATION, 0.60F))
                .addEffect(new DrugEffect(EffectType.CHROMATIC_DREAM, CANNABIS_MAIN_DURATION, 0.85F))
                .addEffect(new DrugEffect(EffectType.TREMOR_REDUCTION, CANNABIS_MAIN_DURATION, 0.20F))
                .addEffect(new DrugEffect(EffectType.RITUAL_STABILITY, CANNABIS_MAIN_DURATION, 0.25F))
                .addEffect(new DrugEffect(EffectType.MOB_DETECTION_REDUCTION, CANNABIS_MAIN_DURATION, 0.15F))
                .setAddictionRate(2)
                .setTuningProfile(DrugModel.DrugTuningProfile.of("calm_stability", "slowdown, threat perception", 1.0F, 2.0F, 0.4F))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.HASH)
                .setCategory(DrugCategory.CANNABINOID)
                .addEffect(new DrugEffect(EffectType.MOVEMENT_SLOWDOWN, CANNABIS_MAIN_DURATION, 0.08F))
                .addEffect(new DrugEffect(EffectType.CHROMATIC_DREAM, CANNABIS_MAIN_DURATION, 1.10F))
                .addEffect(new DrugEffect(EffectType.TREMOR_REDUCTION, CANNABIS_MAIN_DURATION, 0.28F))
                .addEffect(new DrugEffect(EffectType.PRECISION, CANNABIS_MAIN_DURATION, 0.40F))
                .addEffect(new DrugEffect(EffectType.RITUAL_STABILITY, CANNABIS_MAIN_DURATION, 0.35F))
                .addEffect(new DrugEffect(EffectType.MOB_DETECTION_REDUCTION, CANNABIS_MAIN_DURATION, 0.22F))
                .setAddictionRate(2.5F)
                .setTuningProfile(DrugModel.DrugTuningProfile.of("precision_stability", "strong visual drift", 1.1F, 2.5F, 0.6F))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.METH)
                .setCategory(DrugCategory.STIMULANT)
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, METH_MAIN_DURATION, 2.6F))
                .addEffect(new DrugEffect(EffectType.MANUAL_WORK_SPEED, METH_MAIN_DURATION, 1.20F))
                .addEffect(new DrugEffect(EffectType.MINING_SPEED, METH_MAIN_DURATION, 0.45F))
                .addEffect(new DrugEffect(EffectType.ADRENALINE_SURGE, METH_MAIN_DURATION, 0.40F))
                .addEffect(new DrugEffect(EffectType.MOVEMENT_SPEED, METH_MAIN_DURATION, 0.16F))
                .addEffect(new DrugEffect(EffectType.TREMOR, METH_MAIN_DURATION, 0.35F))
                .addEffect(new DrugEffect(EffectType.HEARTBEAT, METH_MAIN_DURATION, 1.25F))
                .setAddictionRate(6)
                .setTuningProfile(DrugModel.DrugTuningProfile.of("late_overclock", "heartbeat, tremor, crash", 1.5F, 6.0F, 2.0F))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.COCAINE)
                .setCategory(DrugCategory.STIMULANT)
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, COCAINE_HIGH_DURATION, 1.8F))
                .addEffect(new DrugEffect(EffectType.MANUAL_WORK_SPEED, COCAINE_HIGH_DURATION, 0.75F))
                .addEffect(new DrugEffect(EffectType.MINING_SPEED, COCAINE_HIGH_DURATION, 0.12F))
                .addEffect(new DrugEffect(EffectType.MOVEMENT_SPEED, COCAINE_HIGH_DURATION, 0.12F))
                .addEffect(new DrugEffect(EffectType.ATTACK_SPEED, COCAINE_HIGH_DURATION, 0.30F))
                .addEffect(new DrugEffect(EffectType.HEARTBEAT, COCAINE_AFTEREFFECT_DURATION, 0.95F))
                .addEffect(new DrugEffect(EffectType.TREMOR, COCAINE_AFTEREFFECT_DURATION, 0.20F))
                .setAddictionRate(6)
                .setTuningProfile(DrugModel.DrugTuningProfile.of("short_overclock", "heartbeat, tremor", 1.3F, 6.0F, 1.5F))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.CRACK)
                .setCategory(DrugCategory.STIMULANT)
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, CRACK_HIGH_DURATION, 2.8F))
                .addEffect(new DrugEffect(EffectType.MANUAL_WORK_SPEED, CRACK_HIGH_DURATION, 1.10F))
                .addEffect(new DrugEffect(EffectType.MOVEMENT_SPEED, CRACK_HIGH_DURATION, 0.18F))
                .addEffect(new DrugEffect(EffectType.HEARTBEAT, COCAINE_AFTEREFFECT_DURATION, 1.35F))
                .addEffect(new DrugEffect(EffectType.TREMOR, COCAINE_AFTEREFFECT_DURATION, 0.35F))
                .addEffect(new DrugEffect(EffectType.INPUT_FAIL, COCAINE_AFTEREFFECT_DURATION, 0.08F))
                .setAddictionRate(6)
                .setTuningProfile(DrugModel.DrugTuningProfile.of("violent_burst", "heartbeat, tremor, input fail", 1.45F, 6.5F, 2.0F))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.LSD)
                .setCategory(DrugCategory.PSYCHEDELIC)
                .addEffect(new DrugEffect(EffectType.ACID_WARP, PSYCHEDELIC_MAIN_DURATION, 2.6F))
                .addEffect(new DrugEffect(EffectType.RITUAL_FOCUS, PSYCHEDELIC_MAIN_DURATION, 2.5F))
                .addEffect(new DrugEffect(EffectType.RITUAL_STABILITY, PSYCHEDELIC_MAIN_DURATION, 1.6F))
                .addEffect(new DrugEffect(EffectType.ORE_FORTUNE, PSYCHEDELIC_MAIN_DURATION, 2.0F))
                .addEffect(new DrugEffect(EffectType.ORE_AURA, PSYCHEDELIC_MAIN_DURATION, 3.0F))
                .setAddictionRate(0)
                .setTuningProfile(DrugModel.DrugTuningProfile.of("ritual_certainty", "strong visual distortion", 0.9F, 0.0F, 0.9F))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.MUSHROOMS)
                .setCategory(DrugCategory.PSYCHEDELIC)
                .addEffect(new DrugEffect(EffectType.EVENT_HORIZON, PSYCHEDELIC_MAIN_DURATION, 1.2F))
                .addEffect(new DrugEffect(EffectType.RITUAL_FOCUS, PSYCHEDELIC_MAIN_DURATION, 2.0F))
                .addEffect(new DrugEffect(EffectType.RITUAL_STABILITY, PSYCHEDELIC_MAIN_DURATION, 1.8F))
                .addEffect(new DrugEffect(EffectType.MULTIBLOCK_VISION, PSYCHEDELIC_MAIN_DURATION, 1.0F))
                .addEffect(new DrugEffect(EffectType.LOW_LIGHT_VISION, PSYCHEDELIC_MAIN_DURATION, 1.0F))
                .setAddictionRate(0)
                .setTuningProfile(DrugModel.DrugTuningProfile.of("structural_vision", "altered perception", 0.9F, 0.0F, 0.8F))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.ALCOHOL)
                .setCategory(DrugCategory.DEPRESSANT)
                .addEffect(new DrugEffect(EffectType.DAMAGE_RESISTANCE, ALCOHOL_MAIN_DURATION, 0.10F))
                .addEffect(new DrugEffect(EffectType.ATTACK_DAMAGE, ALCOHOL_MAIN_DURATION, 0.10F))
                .addEffect(new DrugEffect(EffectType.STUMBLE, ALCOHOL_MAIN_DURATION, 0.22F))
                .addEffect(new DrugEffect(EffectType.INPUT_FAIL, ALCOHOL_MAIN_DURATION, 0.08F))
                .addEffect(new DrugEffect(EffectType.VOMIT, ALCOHOL_MAIN_DURATION, 0.15F))
                .addEffect(new DrugEffect(EffectType.DRUNK_VISION, ALCOHOL_MAIN_DURATION, 1.0F))
                .setAddictionRate(1.0F)
                .setTuningProfile(DrugModel.DrugTuningProfile.of("courage_resistance", "stumble, input fail, nausea", 1.1F, 1.0F, 1.2F))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.TOBACCO)
                .setCategory(DrugCategory.NICOTINIC)
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, DrugDurationScale.seconds(30), 0.55F))
                .addEffect(new DrugEffect(EffectType.MINING_SPEED, TOBACCO_MAIN_DURATION, 0.20F))
                .addEffect(new DrugEffect(EffectType.PRECISION, TOBACCO_MAIN_DURATION, 0.30F))
                .addEffect(new DrugEffect(EffectType.TREMOR_REDUCTION, TOBACCO_MAIN_DURATION, 0.35F))
                .addEffect(new DrugEffect(EffectType.RITUAL_FOCUS, TOBACCO_MAIN_DURATION, 0.30F))
                .setAddictionRate(0.8F)
                .setTuningProfile(DrugModel.DrugTuningProfile.of("steady_focus", "brief visual pulse", 0.8F, 0.8F, 0.3F))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.COFFEE)
                .setCategory(DrugCategory.CAFFEINE)
                .addEffect(new DrugEffect(EffectType.MINING_SPEED, COFFEE_MAIN_DURATION, 0.07F))
                .addEffect(new DrugEffect(EffectType.MOVEMENT_SPEED, COFFEE_MAIN_DURATION, 0.05F))
                .addEffect(new DrugEffect(EffectType.FOCUS, COFFEE_MAIN_DURATION, 0.18F))
                .addEffect(new DrugEffect(EffectType.MANUAL_WORK_SPEED, COFFEE_MAIN_DURATION, 0.22F))
                .addEffect(new DrugEffect(EffectType.CAMERA_SWAY, COFFEE_MAIN_DURATION, 0.045F))
                .addEffect(new DrugEffect(EffectType.TREMOR, COFFEE_MAIN_DURATION, 0.04F))
                .addEffect(new DrugEffect(EffectType.HEARTBEAT, COFFEE_MAIN_DURATION, 0.18F))
                .setAddictionRate(0.5F)
                .setTuningProfile(DrugModel.DrugTuningProfile.of("early_productivity", "sway, tremor, heartbeat", 0.7F, 0.5F, 0.2F))
                .build()
        );

        initializeRepresentativeDrugs();
        initializePsychotropeValues();
        auditRegistry();
    }

    private static DrugModel addDrug(DrugModel model) {
        if (drugs.containsKey(model.getId())) {
            System.err.println("Drug " + model.getId().name() + " was tried to be registered twice!");
            return drugs.get(model.getId());
        }

        drugs.put(model.getId(), model);
        return model;
    }

    private static void initializeRepresentativeDrugs() {
        representativeDrugs.clear();

        representativeDrugs.put(DrugCategory.CANNABINOID, DrugId.WEED);
        representativeDrugs.put(DrugCategory.STIMULANT, DrugId.METH);
        representativeDrugs.put(DrugCategory.PSYCHEDELIC, DrugId.LSD);
        representativeDrugs.put(DrugCategory.DEPRESSANT, DrugId.ALCOHOL);
        representativeDrugs.put(DrugCategory.NICOTINIC, DrugId.TOBACCO);
        representativeDrugs.put(DrugCategory.CAFFEINE, DrugId.COFFEE);
    }

    private static void initializePsychotropeValues() {
        psychotropeValues.clear();
        psychotropeValues.put(DrugId.WEED, 1);
        psychotropeValues.put(DrugId.ALCOHOL, 5);
        psychotropeValues.put(DrugId.COCAINE, 15);
        psychotropeValues.put(DrugId.CRACK, 20);
        psychotropeValues.put(DrugId.LSD, 50);
        psychotropeValues.put(DrugId.METH, 100);
    }

    private static void auditRegistry() {
        for (DrugModel model : drugs.values()) {
            if (model.getDrugEffects().isEmpty()) {
                Core.getLOGGER().warning("Drug registry audit: " + model.getId() + " has no runtime effects.");
            }
            if (!model.hasExplicitAddictionRate()) {
                Core.getLOGGER().warning("Drug registry audit: " + model.getId() + " relies on the default addiction rate.");
            }
            if (!model.tuningProfile().isSpecified()) {
                Core.getLOGGER().warning("Drug registry audit: " + model.getId() + " has no explicit tuning profile.");
            }
            for (DrugEffect effect : model.getDrugEffects()) {
                if (effect.getBaseDuration() <= 0 || effect.getBaseDuration() > DrugDurationScale.fromRealHours(8.0F, 12.0F)) {
                    Core.getLOGGER().warning("Drug registry audit: " + model.getId() + " effect "
                            + effect.getEffectType() + " has unusual duration " + effect.getBaseDuration() + ".");
                }
                if (effect.getBaseIntensity() <= 0.0F || effect.getBaseIntensity() > 4.0F) {
                    Core.getLOGGER().warning("Drug registry audit: " + model.getId() + " effect "
                            + effect.getEffectType() + " has unusual intensity " + effect.getBaseIntensity() + ".");
                }
            }
        }

        for (Map.Entry<DrugCategory, DrugId> entry : representativeDrugs.entrySet()) {
            if (!drugs.containsKey(entry.getValue())) {
                Core.getLOGGER().warning("Drug registry audit: representative " + entry.getValue()
                        + " for category " + entry.getKey() + " is not registered.");
            }
        }

        for (DrugCategory expected : EnumSet.of(
                DrugCategory.CANNABINOID,
                DrugCategory.STIMULANT,
                DrugCategory.PSYCHEDELIC,
                DrugCategory.DEPRESSANT,
                DrugCategory.NICOTINIC,
                DrugCategory.CAFFEINE)) {
            DrugId representative = representativeDrugs.get(expected);
            if (representative == null || !drugs.containsKey(representative)) {
                Core.getLOGGER().warning("Drug registry audit: expected category " + expected + " has no registered representative.");
            }
        }
    }

    public static @Nullable DrugModel getDrug(DrugId id) {
        return drugs.get(id);
    }

    public static DrugCategory getCategory(DrugId id) {
        DrugModel model = getDrug(id);
        return model != null ? model.getDrugCategory() : DrugCategory.OTHER;
    }

    public static Collection<DrugModel> getAllDrugs() {
        return Collections.unmodifiableCollection(drugs.values());
    }

    public static List<DrugModel> getDrugsByCategory(DrugCategory category) {
        List<DrugModel> result = new ArrayList<>();
        for (DrugModel model : drugs.values()) {
            if (model.getDrugCategory() == category) {
                result.add(model);
            }
        }
        return result;
    }

    public static @Nullable DrugId getRepresentativeDrugId(DrugCategory category) {
        DrugId direct = representativeDrugs.get(category);
        if (direct != null) {
            return direct;
        }

        for (DrugModel model : drugs.values()) {
            if (model.getDrugCategory() == category) {
                return model.getId();
            }
        }

        return null;
    }

    public static int getPsychotropeValue(DrugId id) {
        return psychotropeValues.getOrDefault(id, 0);
    }

    public static void setPsychotropeValue(DrugId id, int value) {
        psychotropeValues.put(id, Math.max(0, value));
    }
}
