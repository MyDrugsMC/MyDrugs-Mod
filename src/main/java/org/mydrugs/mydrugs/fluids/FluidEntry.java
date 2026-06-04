package org.mydrugs.mydrugs.fluids;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Supplier;

public final class FluidEntry {
    private final FluidSpec spec;

    private Supplier<FluidType> type;
    private Supplier<FlowingFluid> source;
    private Supplier<FlowingFluid> flowing;
    private Supplier<LiquidBlock> block;
    private Supplier<Item> bucket;

    public FluidEntry(FluidSpec spec) {
        this.spec = spec;
    }

    /** The original specification this entry was built from. */
    public FluidSpec spec() {
        return spec;
    }

    public String name() {
        return spec.name();
    }

    public int tint() {
        return spec.tint();
    }

    public Supplier<FluidType> type() {
        return type;
    }

    public void setType(Supplier<FluidType> type) {
        this.type = type;
    }

    public Supplier<FlowingFluid> source() {
        return source;
    }

    public void setSource(Supplier<FlowingFluid> source) {
        this.source = source;
    }

    public Supplier<FlowingFluid> flowing() {
        return flowing;
    }

    public void setFlowing(Supplier<FlowingFluid> flowing) {
        this.flowing = flowing;
    }

    public Supplier<LiquidBlock> block() {
        return block;
    }

    public void setBlock(Supplier<LiquidBlock> block) {
        this.block = block;
    }

    public Supplier<Item> bucket() {
        return bucket;
    }

    public void setBucket(Supplier<Item> bucket) {
        this.bucket = bucket;
    }
}