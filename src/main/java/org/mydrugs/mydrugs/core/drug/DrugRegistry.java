package org.mydrugs.mydrugs.core.drug;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.Core;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DrugRegistry {
    private static final Map<DrugId, DrugModel> drugs = new HashMap<>();
    private static final EnumMap<DrugId, Integer> psychotropeValues = new EnumMap<>(DrugId.class);
    private static final EnumMap<DrugCategory, DrugId> representativeDrugs = new EnumMap<>(DrugCategory.class);

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
                .addEffect(new DrugEffect(EffectType.SLOWNESS, 20 * 15, 1))
                .addEffect(new DrugEffect(EffectType.FOG, 20 * 20, 1))
                .setAddictionRate(2)
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.HASH)
                .setCategory(DrugCategory.CANNABINOID)
                .addEffect(new DrugEffect(EffectType.FOG, 20 * 25, 2))
                .addEffect(new DrugEffect(EffectType.SLOWNESS, 20 * 12, 1))
                .setAddictionRate(2.5F)
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.METH)
                .setCategory(DrugCategory.STIMULANT)
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, 20 * 18, 3))
                .addEffect(new DrugEffect(EffectType.NAUSEA, 20 * 6, 1))
                .addEffect(new DrugEffect(EffectType.HEARTBEAT, 20 * 6, 1))
                .setAddictionRate(6)
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.COCAINE)
                .setCategory(DrugCategory.STIMULANT)
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, 20 * 10, 2))
                .addEffect(new DrugEffect(EffectType.HEARTBEAT, 20 * 6, 1))
                .setAddictionRate(6)
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.CRACK)
                .setCategory(DrugCategory.STIMULANT)
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, 20 * 8, 3))
                .addEffect(new DrugEffect(EffectType.NAUSEA, 20 * 5, 1))
                .addEffect(new DrugEffect(EffectType.HEARTBEAT, 20 * 4, 1))
                .setAddictionRate(6)
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.MDMA)
                .setCategory(DrugCategory.EMPATHOGEN)
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, 20 * 12, 2))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.LSD)
                .setCategory(DrugCategory.PSYCHEDELIC)
                .addEffect(new DrugEffect(EffectType.ACID_WARP, 20 * 30, 3))
                .setAddictionRate(0)
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.MUSHROOMS)
                .setCategory(DrugCategory.PSYCHEDELIC)
                .addEffect(new DrugEffect(EffectType.EVENT_HORIZON, 20 * 18, 1))
                .setAddictionRate(0)
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.SALVIA)
                .setCategory(DrugCategory.PSYCHEDELIC)
                .addEffect(new DrugEffect(EffectType.ACID_WARP, 20 * 10, 3))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.DMT)
                .setCategory(DrugCategory.PSYCHEDELIC)
                .addEffect(new DrugEffect(EffectType.ACID_WARP, 20 * 8, 4))
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, 20 * 6, 2))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.HEROIN)
                .setCategory(DrugCategory.OPIOID)
                .addEffect(new DrugEffect(EffectType.SLOWNESS, 20 * 18, 2))
                .addEffect(new DrugEffect(EffectType.FOG, 20 * 16, 1))
                .setAddictionRate(9)
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.MORPHINE)
                .setCategory(DrugCategory.OPIOID)
                .addEffect(new DrugEffect(EffectType.SLOWNESS, 20 * 20, 2))
                .addEffect(new DrugEffect(EffectType.FOG, 20 * 12, 1))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.FENTANYL)
                .setCategory(DrugCategory.OPIOID)
                .addEffect(new DrugEffect(EffectType.SLOWNESS, 20 * 14, 4))
                .addEffect(new DrugEffect(EffectType.FOG, 20 * 10, 2))
                .addEffect(new DrugEffect(EffectType.NAUSEA, 20 * 8, 2))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.OPIUM)
                .setCategory(DrugCategory.OPIOID)
                .addEffect(new DrugEffect(EffectType.SLOWNESS, 20 * 22, 1))
                .addEffect(new DrugEffect(EffectType.FOG, 20 * 18, 1))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.KETAMINE)
                .setCategory(DrugCategory.DISSOCIATIVE)
                .addEffect(new DrugEffect(EffectType.FOG, 20 * 16, 2))
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, 20 * 12, 2))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.PCP)
                .setCategory(DrugCategory.DISSOCIATIVE)
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, 20 * 18, 3))
                .addEffect(new DrugEffect(EffectType.NAUSEA, 20 * 8, 1))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.DXM)
                .setCategory(DrugCategory.DISSOCIATIVE)
                .addEffect(new DrugEffect(EffectType.FOG, 20 * 16, 2))
                .addEffect(new DrugEffect(EffectType.NAUSEA, 20 * 6, 1))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.ALCOHOL)
                .setCategory(DrugCategory.DEPRESSANT)
                .addEffect(new DrugEffect(EffectType.NAUSEA, 20 * 10, 1))
                .addEffect(new DrugEffect(EffectType.DRUNK_VISION, 20 * 20, 1))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.BENZODIAZEPINE)
                .setCategory(DrugCategory.DEPRESSANT)
                .addEffect(new DrugEffect(EffectType.SLOWNESS, 20 * 20, 2))
                .addEffect(new DrugEffect(EffectType.FOG, 20 * 16, 1))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.BARBITURATE)
                .setCategory(DrugCategory.DEPRESSANT)
                .addEffect(new DrugEffect(EffectType.SLOWNESS, 20 * 22, 3))
                .addEffect(new DrugEffect(EffectType.NAUSEA, 20 * 10, 1))
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.TOBACCO)
                .setCategory(DrugCategory.NICOTINIC)
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, 20 * 6, 1))
                .setAddictionRate(0.8F)
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.COFFEE)
                .setCategory(DrugCategory.CAFFEINE)
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, 20 * 8, 1))
                .setAddictionRate(0.2F)
                .build()
        );

        addDrug(new DrugModel.Builder()
                .setId(DrugId.NITROUS_OXIDE)
                .setCategory(DrugCategory.INHALANT)
                .addEffect(new DrugEffect(EffectType.VOID_PULSE, 20 * 5, 2))
                .addEffect(new DrugEffect(EffectType.FOG, 20 * 4, 1))
                .build()
        );

        initializeRepresentativeDrugs();
        initializePsychotropeValues();
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
        representativeDrugs.put(DrugCategory.EMPATHOGEN, DrugId.MDMA);
        representativeDrugs.put(DrugCategory.PSYCHEDELIC, DrugId.LSD);
        representativeDrugs.put(DrugCategory.OPIOID, DrugId.HEROIN);
        representativeDrugs.put(DrugCategory.DISSOCIATIVE, DrugId.KETAMINE);
        representativeDrugs.put(DrugCategory.DEPRESSANT, DrugId.ALCOHOL);
        representativeDrugs.put(DrugCategory.NICOTINIC, DrugId.TOBACCO);
        representativeDrugs.put(DrugCategory.CAFFEINE, DrugId.COFFEE);
        representativeDrugs.put(DrugCategory.INHALANT, DrugId.NITROUS_OXIDE);
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
