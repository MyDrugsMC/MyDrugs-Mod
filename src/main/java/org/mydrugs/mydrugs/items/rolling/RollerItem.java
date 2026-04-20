package org.mydrugs.mydrugs.items.rolling;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.menu.RollerMenu;

public class RollerItem extends Item {
    public RollerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, ignored) ->
                            new RollerMenu(containerId, playerInventory, new SimpleContainer(6), new SimpleContainerData(2)),
                    Component.translatable("menu.mydrugs.roller")
            ));
        }

        return InteractionResult.SUCCESS;
    }
}