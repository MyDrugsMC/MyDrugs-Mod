package org.mydrugs.mydrugs.core;

import java.util.HashMap;
import java.util.Map;

public class DrugRegistry {

    private static Map<String, DrugModel> drugs = null;
    public static DrugModel WEED = addDrug(DrugModel.of("weed", new DrugEffect(Effect.SLOWNESS), new DrugEffect(Effect.FOG)));
    public static DrugModel METH = addDrug(DrugModel.of("meth", new DrugEffect(Effect.VOID_PULSE)));
    public static DrugModel LSD = addDrug(DrugModel.of("lsd", new DrugEffect(Effect.ACID_WARP)));
    public static DrugModel MUSHROOMS = addDrug(DrugModel.of("mushroom"));
    public static DrugModel HEROINE = addDrug(DrugModel.of("heroine"));
    public static DrugModel ALCOHOL = addDrug(DrugModel.of("alcohol", new DrugEffect(Effect.NAUSEA)));
    public static DrugModel TABACCO = addDrug(DrugModel.of("tabacco"));
    public static DrugModel COFFEE = addDrug(DrugModel.of("coffee"));

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
