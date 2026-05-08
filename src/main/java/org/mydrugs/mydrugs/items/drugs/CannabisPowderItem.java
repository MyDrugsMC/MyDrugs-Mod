package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.ritual.RitualDrugRegistry;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.items.rolling.RollingIngredient;

import java.util.List;

public class CannabisPowderItem extends DrugItem implements RollingIngredient {
    public static final List<DrugEffect> BRIGHTENED_EFFECTS = List.of(
            new DrugEffect(EffectType.GAMMA_BOOST, 30 * 20, 0.45F)
    );

    private final boolean brightened;

    public CannabisPowderItem(Properties properties, DrugId drugId, ConsumptionStrategy strategy) {
        this(properties, drugId, strategy, false);
    }

    public CannabisPowderItem(Properties properties, DrugId drugId, ConsumptionStrategy strategy, boolean brightened) {
        super(properties, drugId, strategy);
        this.brightened = brightened;
    }

    @Override
    public DrugId getRollingDrug(ItemStack stack) {
        return DrugId.WEED;
    }

    @Override
    public boolean isBrightenedRollingIngredient(ItemStack stack) {
        return this.brightened;
    }

    @Override
    protected List<DrugEffect> getAdditionalDrugEffects(ItemStack stack) {
        return this.brightened
                ? List.copyOf(RitualDrugRegistry.brightenedCannabisEffects())
                : List.of();
    }
}
