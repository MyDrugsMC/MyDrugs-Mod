package org.mydrugs.mydrugs.core.drug.ritual;

import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.use.DrugStackResolver;

import java.util.List;
import java.util.Optional;

public final class RitualBaseDrugResolver {
    private RitualBaseDrugResolver() {
    }

    public static Optional<DrugId> resolve(ItemStack stack) {
        List<DrugStackResolver.ResolvedStackDrug> resolved = DrugStackResolver.resolve(stack, null);
        if (resolved.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(resolved.getFirst().model().getId());
    }
}
