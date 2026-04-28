package org.mydrugs.mydrugs.pipe.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import org.mydrugs.mydrugs.pipe.PipeConnectionMode;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.PipeTier;

import java.util.EnumMap;
import java.util.EnumSet;

public class PipeRenderState extends BlockEntityRenderState {
    public final EnumMap<Direction, PipeConnectionMode> sideModes = new EnumMap<>(Direction.class);
    public final EnumSet<Direction> filteredSides = EnumSet.noneOf(Direction.class);
    public PipeResourceKind kind = PipeResourceKind.ITEM;
    public PipeTier tier = PipeTier.BASIC;
}
