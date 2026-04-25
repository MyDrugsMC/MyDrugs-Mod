package org.mydrugs.mydrugs.core.drug;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;

import java.util.ArrayList;
import java.util.List;

public class DrugModel {
    private final DrugId id;
    private final List<DrugEffect> drugEffects;
    private final DrugCategory drugCategory;
    private final float addictionRate;

    private DrugModel(DrugId id) {
        this(id, DrugCategory.OTHER, new ArrayList<>(), 1);
    }

    protected DrugModel(DrugId id, DrugCategory drugCategory, List<DrugEffect> effects, float addictionRate) {
        this.id = id;
        this.drugCategory = drugCategory;
        this.drugEffects = effects;
        this.addictionRate = addictionRate;
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

    public static class Builder {
        private final List<DrugEffect> effects = new ArrayList<>();
        private DrugId id = null;
        private DrugCategory drugCategory = DrugCategory.OTHER;
        private float addictionRate = 1;

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
            return this;
        }

        public DrugModel build() {
            return new DrugModel(id, drugCategory, effects, addictionRate);
        }
    }
}
