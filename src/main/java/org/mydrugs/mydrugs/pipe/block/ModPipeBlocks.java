package org.mydrugs.mydrugs.pipe.block;

import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.PipeTier;

public final class ModPipeBlocks {
    public static final DeferredBlock<PipeBlock> BASIC_ITEM_PIPE =
            register("basic_item_pipe", PipeResourceKind.ITEM, PipeTier.BASIC, 0.8F);
    public static final DeferredItem<BlockItem> BASIC_ITEM_PIPE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(BASIC_ITEM_PIPE);

    public static final DeferredBlock<PipeBlock> FAST_ITEM_PIPE =
            register("fast_item_pipe", PipeResourceKind.ITEM, PipeTier.FAST, 1.0F);
    public static final DeferredItem<BlockItem> FAST_ITEM_PIPE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(FAST_ITEM_PIPE);

    public static final DeferredBlock<PipeBlock> BASIC_FLUID_PIPE =
            register("basic_fluid_pipe", PipeResourceKind.FLUID, PipeTier.BASIC, 0.8F);
    public static final DeferredItem<BlockItem> BASIC_FLUID_PIPE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(BASIC_FLUID_PIPE);

    public static final DeferredBlock<PipeBlock> FAST_FLUID_PIPE =
            register("fast_fluid_pipe", PipeResourceKind.FLUID, PipeTier.FAST, 1.0F);
    public static final DeferredItem<BlockItem> FAST_FLUID_PIPE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(FAST_FLUID_PIPE);

    public static final DeferredBlock<PipeBlock> BASIC_GAS_PIPE =
            register("basic_gas_pipe", PipeResourceKind.GAS, PipeTier.BASIC, 0.8F);
    public static final DeferredItem<BlockItem> BASIC_GAS_PIPE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(BASIC_GAS_PIPE);

    public static final DeferredBlock<PipeBlock> FAST_GAS_PIPE =
            register("fast_gas_pipe", PipeResourceKind.GAS, PipeTier.FAST, 1.0F);
    public static final DeferredItem<BlockItem> FAST_GAS_PIPE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(FAST_GAS_PIPE);

    private ModPipeBlocks() {
    }

    private static DeferredBlock<PipeBlock> register(String id, PipeResourceKind kind, PipeTier tier, float strength) {
        return ModBlocks.BLOCKS.registerBlock(
                id,
                props -> new PipeBlock(props.noOcclusion(), kind, tier),
                props -> props.strength(strength).sound(net.minecraft.world.level.block.SoundType.METAL)
        );
    }
}
