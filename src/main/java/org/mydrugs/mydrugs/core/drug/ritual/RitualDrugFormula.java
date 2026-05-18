package org.mydrugs.mydrugs.core.drug.ritual;

import org.mydrugs.mydrugs.core.drug.DrugId;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public record RitualDrugFormula(
        String formulaId,
        DrugId baseDrug,
        List<RitualDrugEffectData> baseEffectsSnapshot,
        List<RitualDrugEffectData> addedEffects,
        String canonicalSignature
) {
    public RitualDrugFormula {
        baseEffectsSnapshot = List.copyOf(baseEffectsSnapshot);
        addedEffects = List.copyOf(addedEffects);
    }

    public static RitualDrugFormula of(
            DrugId baseDrug,
            List<RitualDrugEffectData> baseEffectsSnapshot,
            List<RitualDrugEffectData> addedEffects
    ) {
        List<RitualDrugEffectData> canonicalEffects = addedEffects.stream()
                .sorted(RitualDrugEffectData.CANONICAL_ORDER)
                .toList();
        String signature = canonicalEffectsSignature(baseDrug, canonicalEffects);
        String formulaId = UUID.nameUUIDFromBytes(signature.getBytes(StandardCharsets.UTF_8)).toString();
        return new RitualDrugFormula(formulaId, baseDrug, baseEffectsSnapshot, canonicalEffects, signature);
    }

    public static RitualDrugFormula of(
            String formulaId,
            DrugId baseDrug,
            List<RitualDrugEffectData> baseEffectsSnapshot,
            List<RitualDrugEffectData> addedEffects
    ) {
        List<RitualDrugEffectData> canonicalEffects = addedEffects.stream()
                .sorted(RitualDrugEffectData.CANONICAL_ORDER)
                .toList();
        String normalizedFormulaId = normalizeFormulaId(formulaId);
        String signature = identitySignature(baseDrug, normalizedFormulaId);
        return new RitualDrugFormula(normalizedFormulaId, baseDrug, baseEffectsSnapshot, canonicalEffects, signature);
    }

    private static String canonicalEffectsSignature(DrugId baseDrug, List<RitualDrugEffectData> addedEffects) {
        StringBuilder builder = new StringBuilder("base_drug=")
                .append(baseDrug.serializedName())
                .append(";effects=[");
        for (int i = 0; i < addedEffects.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(addedEffects.get(i).canonicalPart());
        }
        return builder.append(']').toString();
    }

    private static String identitySignature(DrugId baseDrug, String formulaId) {
        return "base_drug=" + baseDrug.serializedName() + ";formula_id=" + formulaId;
    }

    private static String normalizeFormulaId(String formulaId) {
        String normalized = formulaId == null ? "" : formulaId.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return UUID.nameUUIDFromBytes("empty_formula".getBytes(StandardCharsets.UTF_8)).toString();
        }
        return normalized;
    }
}
