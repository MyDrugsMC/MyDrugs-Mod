package org.mydrugs.mydrugs.dimension.inner.v7;

import net.minecraft.core.BlockPos;
import org.mydrugs.mydrugs.core.drug.DrugId;

public record InnerV7Location(DrugId drugId, BlockPos pos, String kind) {
}
