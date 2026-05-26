package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.FormedPsyMixerCoreBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.FormedPsyMixerPartBlockEntity;

import java.util.List;

public final class PsyMixerStructureValidator {
    private PsyMixerStructureValidator() {
    }

    public static StructureCheck check(
            @Nullable Level level,
            BlockPos corePos,
            BlockEntity expectedCore,
            List<FormedPsyMixerCoreBlockEntity.SavedSlot> savedSlots
    ) {
        if (level == null) {
            return StructureCheck.invalid(corePos, "missing_level");
        }
        if (savedSlots.isEmpty()) {
            return StructureCheck.invalid(corePos, "missing_saved_slots");
        }
        if (!level.getBlockState(corePos).is(ModBlocks.FORMED_PSY_MIXER_CORE.get())) {
            return StructureCheck.invalid(corePos, "missing_core_block");
        }
        if (level.getBlockEntity(corePos) != expectedCore) {
            return StructureCheck.invalid(corePos, "wrong_core_entity");
        }

        for (FormedPsyMixerCoreBlockEntity.SavedSlot slot : savedSlots) {
            BlockPos worldPos = slot.worldPos();
            if (worldPos.equals(corePos)) {
                continue;
            }

            BlockState currentState = level.getBlockState(worldPos);
            if (slot.blockState().isAir()) {
                if (!currentState.isAir()) {
                    return StructureCheck.invalid(worldPos, "expected_air");
                }
                continue;
            }

            if (!currentState.is(ModBlocks.FORMED_PSY_MIXER_PART.get())) {
                return StructureCheck.invalid(worldPos, "missing_part_block");
            }
            if (!(level.getBlockEntity(worldPos) instanceof FormedPsyMixerPartBlockEntity part)
                    || !corePos.equals(part.getCorePos())) {
                return StructureCheck.invalid(worldPos, "wrong_part_entity");
            }
        }
        return StructureCheck.valid();
    }

    public static boolean isIntact(
            @Nullable Level level,
            BlockPos corePos,
            BlockEntity expectedCore,
            List<FormedPsyMixerCoreBlockEntity.SavedSlot> savedSlots
    ) {
        return check(level, corePos, expectedCore, savedSlots).intact();
    }

    public record StructureCheck(boolean intact, @Nullable BlockPos problemPos, String reason) {
        private static StructureCheck valid() {
            return new StructureCheck(true, null, "");
        }

        private static StructureCheck invalid(BlockPos problemPos, String reason) {
            return new StructureCheck(false, problemPos, reason);
        }
    }
}
