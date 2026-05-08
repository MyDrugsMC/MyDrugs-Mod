package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugDurationScale;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;

import java.util.List;

public final class SoothingTobaccoBlendItem extends TobaccoHandfulItem {
    private static final List<DrugEffect> BLEND_EFFECTS = List.of(
            new DrugEffect(EffectType.MINING_SPEED, DrugDurationScale.fromRealHours(1.5F, 12.0F), 0.16F),
            new DrugEffect(EffectType.PRECISION, DrugDurationScale.fromRealHours(1.5F, 12.0F), 0.30F),
            new DrugEffect(EffectType.TREMOR_REDUCTION, DrugDurationScale.fromRealHours(1.5F, 12.0F), 0.30F),
            new DrugEffect(EffectType.RITUAL_FOCUS, DrugDurationScale.fromRealHours(1.5F, 12.0F), 0.18F)
    );

    public SoothingTobaccoBlendItem(Properties properties, DrugId id, ConsumptionStrategy strategy) {
        super(properties, id, strategy);
    }

    @Override
    protected List<DrugEffect> getAdditionalDrugEffects(ItemStack stack) {
        return BLEND_EFFECTS;
    }
}
