package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.mydrugs.mydrugs.pipe.PipeSideConfig;

import java.util.Map;

public record PipeNode(BlockPos pos, Map<Direction, PipeSideConfig> sideConfigs) {
}
