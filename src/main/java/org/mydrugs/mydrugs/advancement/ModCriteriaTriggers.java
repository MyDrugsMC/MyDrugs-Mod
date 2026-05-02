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
    public static final DeferredHolder<CriterionTrigger<?>, PsychotropeEnergyTrigger> PSYCHOTROPE_ENERGY =
            TRIGGERS.register("psychotrope_energy", PsychotropeEnergyTrigger::new);

    private ModCriteriaTriggers() {
    }

    public static void register(IEventBus modBus) {
        TRIGGERS.register(modBus);
    }
}
