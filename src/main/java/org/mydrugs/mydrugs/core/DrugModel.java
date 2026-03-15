package org.mydrugs.mydrugs.core;

import java.util.Arrays;
import java.util.List;

public class DrugModel {
    private final String id;
    private final List<DrugEffect> drugEffects;

    private DrugModel(String id, List<DrugEffect> effects) {
        this.id = id;
        this.drugEffects = effects;
    }

    public static DrugModel of(String id, DrugEffect... effects) {
        return new DrugModel(id, Arrays.asList(effects));
    }

    public String getId() {
        return id;
    }

    public List<DrugEffect> getDrugEffects() {
        return drugEffects;
    }
}
