package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface MachineTransferMenuAccess {
    @Nullable
    BlockEntity getMachineTransferTarget(Player player);
}
