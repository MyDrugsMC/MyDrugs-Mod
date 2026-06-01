package org.mydrugs.mydrugs.core.drug;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;

import java.util.ArrayList;
import java.util.List;

public class DrugModel {
    private final DrugId id;
    private final List<DrugEffect> drugEffects;
    private final DrugCategory drugCategory;
    private final float addictionRate;
    private final boolean addictionRateExplicit;
    private final DrugTuningProfile tuningProfile;

    protected DrugModel(DrugId id, DrugCategory drugCategory, List<DrugEffect> effects, float addictionRate,
                        boolean addictionRateExplicit, DrugTuningProfile tuningProfile) {
        if (id == null) {
            throw new IllegalStateException("DrugModel id must be set before build.");
        }
        this.id = id;
        this.drugCategory = drugCategory;
        this.drugEffects = List.copyOf(effects);
        this.addictionRate = addictionRate;
        this.addictionRateExplicit = addictionRateExplicit;
        this.tuningProfile = tuningProfile == null ? DrugTuningProfile.unspecified() : tuningProfile;
    }

    public DrugId getId() {
        return id;
    }

    public List<DrugEffect> getDrugEffects() {
        return drugEffects;
    }

    public DrugCategory getDrugCategory() {
        return drugCategory;
    }

    public float getAddictionRate() {
        return addictionRate;
    }

    public boolean hasExplicitAddictionRate() {
        return addictionRateExplicit;
    }

    public DrugTuningProfile tuningProfile() {
        return tuningProfile;
    }

    public DrugModel withAdditionalEffects(List<DrugEffect> extraEffects) {
        if (extraEffects.isEmpty()) {
            return this;
        }

        List<DrugEffect> combined = new ArrayList<>(this.drugEffects);
        combined.addAll(extraEffects);
        return new DrugModel(this.id, this.drugCategory, combined, this.addictionRate,
                this.addictionRateExplicit, this.tuningProfile);
    }

    public record DrugTuningProfile(String primaryBenefit, String sideRiskEffects, float doseLoadMultiplier,
                                    float dependencePressure, float comedownPressure) {
        private static final DrugTuningProfile UNSPECIFIED = new DrugTuningProfile("unspecified", "", 1.0F, 1.0F, 1.0F);

        public static DrugTuningProfile of(String primaryBenefit, String sideRiskEffects, float doseLoadMultiplier,
                                           float dependencePressure, float comedownPressure) {
            return new DrugTuningProfile(
                    primaryBenefit == null || primaryBenefit.isBlank() ? "unspecified" : primaryBenefit,
                    sideRiskEffects == null ? "" : sideRiskEffects,
                    Math.max(0.0F, doseLoadMultiplier),
                    Math.max(0.0F, dependencePressure),
                    Math.max(0.0F, comedownPressure)
            );
        }

        public static DrugTuningProfile unspecified() {
            return UNSPECIFIED;
        }

        public boolean isSpecified() {
            return this != UNSPECIFIED && !"unspecified".equals(primaryBenefit);
        }
    }

    public static class Builder {
        private final List<DrugEffect> effects = new ArrayList<>();
        private DrugId id = null;
        private DrugCategory drugCategory = DrugCategory.OTHER;
        private float addictionRate = 1;
        private boolean addictionRateExplicit;
        private DrugTuningProfile tuningProfile = DrugTuningProfile.unspecified();

        public Builder setId(DrugId id) {
            this.id = id;
            return this;
        }

        public Builder addEffect(DrugEffect effect) {
            effects.add(effect);
            return this;
        }

        public Builder setCategory(DrugCategory drugCategory) {
            this.drugCategory = drugCategory;
            return this;
        }

        public Builder setAddictionRate(float addictionRate) {
            this.addictionRate = addictionRate;
            this.addictionRateExplicit = true;
            return this;
        }

        public Builder setTuningProfile(DrugTuningProfile tuningProfile) {
            this.tuningProfile = tuningProfile;
            return this;
        }

        public DrugModel build() {
            return new DrugModel(id, drugCategory, effects, addictionRate, addictionRateExplicit, tuningProfile);
        }
    }
}
