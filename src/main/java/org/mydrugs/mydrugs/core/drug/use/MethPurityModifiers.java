package org.mydrugs.mydrugs.core.drug.use;

import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.SmokingStrategy;
import org.mydrugs.mydrugs.items.drugs.Purity;

public final class MethPurityModifiers {
    // Coefficients per spec. Lab meth (P near 1) -> strong, clean; street meth (P near 0.2) -> weak, dirty.
    public static final float DOSE_BIAS = 0.6F;
    public static final float DOSE_SLOPE = 0.9F;
    public static final float INTENSITY_BIAS = 0.6F;
    public static final float INTENSITY_SLOPE = 0.7F;
    public static final float DURATION_BIAS = 0.7F;
    public static final float DURATION_SLOPE = 0.5F;
    public static final float EXTRA_STRESS_PER_HIT_AT_P0 = 0.10F;
    public static final float CONTAMINANT_CHANCE_AT_P0 = 0.5F;
    public static final float TOLERANCE_BIAS = 1.3F;
    public static final float TOLERANCE_SLOPE = 0.5F;

    public static final MethPurityModifiers NO_OP =
            new MethPurityModifiers(1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, false);

    private final float dose;
    private final float intensity;
    private final float duration;
    private final float tolerance;
    private final float extraStress;
    private final float contaminantChance;
    private final boolean active;

    private MethPurityModifiers(float dose, float intensity, float duration, float tolerance,
                                float extraStress, float contaminantChance, boolean active) {
        this.dose = dose;
        this.intensity = intensity;
        this.duration = duration;
        this.tolerance = tolerance;
        this.extraStress = extraStress;
        this.contaminantChance = contaminantChance;
        this.active = active;
    }

    public static MethPurityModifiers from(DrugModel model, ConsumptionStrategy strategy, ItemStack sourceStack) {
        if (model == null || strategy == null) return NO_OP;
        if (!(strategy instanceof SmokingStrategy)) return NO_OP;
        if (model.getId() != DrugId.METH) return NO_OP;
        if (sourceStack == null || sourceStack.isEmpty()) return NO_OP;
        float p = Purity.of(sourceStack);
        return new MethPurityModifiers(
                DOSE_BIAS + DOSE_SLOPE * p,
                INTENSITY_BIAS + INTENSITY_SLOPE * p,
                DURATION_BIAS + DURATION_SLOPE * p,
                TOLERANCE_BIAS - TOLERANCE_SLOPE * p,
                (1.0F - p) * EXTRA_STRESS_PER_HIT_AT_P0,
                (1.0F - p) * CONTAMINANT_CHANCE_AT_P0,
                true
        );
    }

    public boolean isActive() {
        return active;
    }

    public float doseMul() {
        return dose;
    }

    public float intensityMul() {
        return intensity;
    }

    public float durationMul() {
        return duration;
    }

    public float toleranceMul() {
        return tolerance;
    }

    public float extraStressPerHit() {
        return extraStress;
    }

    public float contaminantChance() {
        return contaminantChance;
    }
}
