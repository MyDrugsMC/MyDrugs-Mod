package org.mydrugs.mydrugs.core;

import java.util.HashMap;
import java.util.Map;

public class DrugRegistry {

    private static Map<String, DrugModel> drugs = null;
    public static DrugModel WEED = addDrug(DrugModel.of("weed", new DrugEffect(EffectType.SLOWNESS)));

    private DrugRegistry() {
    }

    private static DrugModel addDrug(DrugModel model) {
        if (drugs == null) drugs = new HashMap<>();
        drugs.put(model.getId(), model);
        return model;
    }

    public static DrugModel getById(String id) {
        return drugs.get(id);
    }
}
