package org.mydrugs.mydrugs.advancement;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;

public final class ModCriteriaTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, MyDrugs.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, DrugConsumedTrigger> DRUG_CONSUMED =
            TRIGGERS.register("drug_consumed", DrugConsumedTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, MachineRecipeCompletedTrigger> MACHINE_RECIPE_COMPLETED =
            TRIGGERS.register("machine_recipe_completed", MachineRecipeCompletedTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, RecoveryActionTrigger> RECOVERY_ACTION =
            TRIGGERS.register("recovery_action", RecoveryActionTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PsyCurrentTrigger> PSY_CURRENT_MACHINE_POWERED =
            TRIGGERS.register("psy_current_machine_powered", PsyCurrentTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PsyKnowledgeUnlockedTrigger> PSY_KNOWLEDGE_UNLOCKED =
            TRIGGERS.register("psy_knowledge_unlocked", PsyKnowledgeUnlockedTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, InnerDimensionMilestoneTrigger> INNER_DIMENSION_MILESTONE =
            TRIGGERS.register("inner_dimension_milestone", InnerDimensionMilestoneTrigger::new);

    private ModCriteriaTriggers() {
    }

    public static void register(IEventBus modBus) {
        TRIGGERS.register(modBus);
    }
}
