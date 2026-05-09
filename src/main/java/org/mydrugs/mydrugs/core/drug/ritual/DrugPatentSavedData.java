package org.mydrugs.mydrugs.core.drug.ritual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class DrugPatentSavedData extends SavedData {
    private static final String DATA_ID = "mydrugs_drug_patents";

    public static final Codec<DrugPatentSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, MixedDrugData.CODEC)
                    .optionalFieldOf("by_signature", Map.of())
                    .forGetter(data -> data.bySignature)
    ).apply(instance, DrugPatentSavedData::new));

    public static final SavedDataType<DrugPatentSavedData> TYPE =
            new SavedDataType<>(DATA_ID, DrugPatentSavedData::new, CODEC);

    private final Map<String, MixedDrugData> bySignature;
    private final Map<String, String> signatureByName = new LinkedHashMap<>();

    public DrugPatentSavedData() {
        this(Map.of());
    }

    private DrugPatentSavedData(Map<String, MixedDrugData> bySignature) {
        this.bySignature = new LinkedHashMap<>(bySignature);
        rebuildNameIndex();
    }

    public static DrugPatentSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public synchronized Optional<MixedDrugData> bySignature(String signature) {
        return Optional.ofNullable(this.bySignature.get(signature));
    }

    public synchronized boolean isNameTakenByOtherFormula(String displayName, String signature) {
        String existingSignature = this.signatureByName.get(normalizeName(displayName));
        return existingSignature != null && !existingSignature.equals(signature);
    }

    public synchronized MixedDrugData patent(RitualDrugFormula formula, String displayName, java.util.UUID authorUuid, String authorName) {
        MixedDrugData existing = this.bySignature.get(formula.canonicalSignature());
        if (existing != null) {
            return existing;
        }

        MixedDrugData data = new MixedDrugData(
                formula.formulaId(),
                displayName,
                authorUuid,
                authorName,
                formula.baseDrug(),
                formula.baseEffectsSnapshot(),
                formula.addedEffects(),
                formula.canonicalSignature()
        );
        this.bySignature.put(formula.canonicalSignature(), data);
        this.signatureByName.put(normalizeName(displayName), formula.canonicalSignature());
        setDirty();
        return data;
    }

    private void rebuildNameIndex() {
        this.signatureByName.clear();
        for (MixedDrugData data : this.bySignature.values()) {
            this.signatureByName.put(normalizeName(data.displayName()), data.canonicalSignature());
        }
    }

    private static String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
