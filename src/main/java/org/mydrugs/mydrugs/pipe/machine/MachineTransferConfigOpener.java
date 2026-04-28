package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MachineTransferConfigOpener {
    private MachineTransferConfigOpener() {
    }

    public static void open(ServerPlayer player, BlockEntity target) {
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, playerInventory, ignored) ->
                        new MachineTransferConfigMenu(containerId, playerInventory, target.getBlockPos()),
                Component.translatable("menu.mydrugs.machine_transfer_config")
        );
        player.openMenu(provider, buf -> buf.writeBlockPos(target.getBlockPos()));
    }
}
