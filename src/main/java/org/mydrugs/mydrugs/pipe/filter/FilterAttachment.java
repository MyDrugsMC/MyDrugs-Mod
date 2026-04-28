package org.mydrugs.mydrugs.pipe.filter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record FilterAttachment(BlockPos pipePos, Direction side, PipeFilterConfig filter) {
}
