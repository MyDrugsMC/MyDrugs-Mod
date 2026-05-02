package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeCoreBlockEntity;

import java.util.ArrayList;
import java.util.List;

public final class PsychotropeMultiblock {
    private static final List<BlockPos> COMPONENT_OFFSETS = buildComponentOffsets();

    private PsychotropeMultiblock() {
    }

    public static List<BlockPos> componentOffsets() {
        return COMPONENT_OFFSETS;
    }

    public static boolean containsComponentOffset(BlockPos offset) {
        return COMPONENT_OFFSETS.contains(offset);
    }

    public static boolean validate(Level level, BlockPos corePos) {
        for (int y = -2; y <= 2; y++) {
            for (int z = -2; z <= 2; z++) {
                for (int x = -2; x <= 2; x++) {
                    BlockPos offset = new BlockPos(x, y, z);
                    BlockPos pos = corePos.offset(offset);
                    BlockState state = level.getBlockState(pos);

                    if (offset.equals(BlockPos.ZERO)) {
                        if (!state.is(ModBlocks.PSYCHOTROPE_CORE.get())) {
                            return false;
                        }
                    } else if (COMPONENT_OFFSETS.contains(offset)) {
                        if (!state.is(ModBlocks.PSYCHOTROPE_COMPONENT.get())) {
                            return false;
                        }
                    } else if (state.is(ModBlocks.PSYCHOTROPE_COMPONENT.get()) || state.is(ModBlocks.PSYCHOTROPE_CORE.get())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void markNearbyCoresDirty(Level level, BlockPos changedPos) {
        if (level.isClientSide()) {
            return;
        }

        for (int y = -2; y <= 2; y++) {
            for (int z = -2; z <= 2; z++) {
                for (int x = -2; x <= 2; x++) {
                    BlockPos corePos = changedPos.offset(-x, -y, -z);
                    if (level.getBlockEntity(corePos) instanceof PsychotropeCoreBlockEntity core) {
                        core.markStructureDirty();
                    }
                }
            }
        }
    }

    public static PsychotropeCoreBlockEntity findCore(Level level, BlockPos clickedPos) {
        if (level.getBlockEntity(clickedPos) instanceof PsychotropeCoreBlockEntity core) {
            return core;
        }

        for (BlockPos offset : COMPONENT_OFFSETS) {
            BlockPos corePos = clickedPos.subtract(offset);
            if (level.getBlockEntity(corePos) instanceof PsychotropeCoreBlockEntity core && core.isFormed()) {
                return core;
            }
        }

        return null;
    }

    private static List<BlockPos> buildComponentOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        addLayer(offsets, -2, new String[]{
                "XXXXX",
                "XXXXX",
                "XXCXX",
                "XXXXX",
                "XXXXX"
        });
        addLayer(offsets, -1, new String[]{
                "XXXXX",
                "XXCXX",
                "XCCCX",
                "XXCXX",
                "XXXXX"
        });
        addLayer(offsets, 0, new String[]{
                "XXCXX",
                "XCCCX",
                "CCPCC",
                "XCCCX",
                "XXCXX"
        });
        addLayer(offsets, 1, new String[]{
                "XXXXX",
                "XXCXX",
                "XCCCX",
                "XXCXX",
                "XXXXX"
        });
        addLayer(offsets, 2, new String[]{
                "XXXXX",
                "XXXXX",
                "XXCXX",
                "XXXXX",
                "XXXXX"
        });
        return List.copyOf(offsets);
    }

    private static void addLayer(List<BlockPos> offsets, int y, String[] rows) {
        for (int z = 0; z < rows.length; z++) {
            String row = rows[z];
            for (int x = 0; x < row.length(); x++) {
                if (row.charAt(x) == 'C') {
                    offsets.add(new BlockPos(x - 2, y, z - 2));
                }
            }
        }
    }
}
