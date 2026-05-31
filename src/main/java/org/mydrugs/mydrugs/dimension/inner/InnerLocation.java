package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.mydrugs.mydrugs.core.drug.DrugId;

public record InnerLocation(DrugId drugId, BlockPos pos, String kind) {
}
