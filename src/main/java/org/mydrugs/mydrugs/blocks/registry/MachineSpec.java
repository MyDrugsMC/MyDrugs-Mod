package org.mydrugs.mydrugs.blocks.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public record MachineSpec<T extends Block>(
        String id,
        Function<BlockBehaviour.Properties, T> factory,
        UnaryOperator<BlockBehaviour.Properties> properties
) {
    public DeferredBlock<T> register(DeferredRegister.Blocks blocks) {
        return blocks.registerBlock(id, factory, properties);
    }
}
