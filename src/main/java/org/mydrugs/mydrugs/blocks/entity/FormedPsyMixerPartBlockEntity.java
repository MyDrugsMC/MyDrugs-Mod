package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;

public final class FormedPsyMixerPartBlockEntity extends BlockEntity {
    private @Nullable BlockPos corePos;

    public FormedPsyMixerPartBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FORMED_PSY_MIXER_PART.get(), pos, state);
    }

    public @Nullable BlockPos getCorePos() {
        return corePos;
    }

    public void setCorePos(BlockPos corePos) {
        this.corePos = corePos == null ? null : corePos.immutable();
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (corePos != null) {
            output.putInt("core_x", corePos.getX());
            output.putInt("core_y", corePos.getY());
            output.putInt("core_z", corePos.getZ());
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (input.getIntOr("core_y", Integer.MIN_VALUE) != Integer.MIN_VALUE) {
            int x = input.getIntOr("core_x", 0);
            int y = input.getIntOr("core_y", 0);
            int z = input.getIntOr("core_z", 0);
            corePos = new BlockPos(x, y, z);
        }
    }
}
