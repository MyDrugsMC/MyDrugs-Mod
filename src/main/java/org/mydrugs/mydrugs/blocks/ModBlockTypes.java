package org.mydrugs.mydrugs.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.function.Supplier;

public class ModBlockTypes {
    public static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, MyDrugs.MODID);

    public static final Supplier<MapCodec<AdvancedFurnaceBlock>> ADVANCED_FURNACE_CODEC =
            BLOCK_TYPES.register("advanced_furnace", () -> Block.simpleCodec(AdvancedFurnaceBlock::new));

    public static final Supplier<MapCodec<DistillerBlock>> DISTILLER_CODEC =
            BLOCK_TYPES.register("distiller", () -> Block.simpleCodec(DistillerBlock::new));

    private ModBlockTypes() {}
}
