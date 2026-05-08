package org.mydrugs.mydrugs.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.PsyBlueprintPreviewService;

public final class PsyBlueprintItem extends Item {
    public PsyBlueprintItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        BlockPos clicked = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clicked);

        if (!clickedState.is(ModBlocks.PSYCHOTROPE_CORE.get()) && !clickedState.is(ModBlocks.PAINTED_CLAY_BOWL.get())) {
            serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.psy_blueprint.unsupported"), true);
            return InteractionResult.SUCCESS;
        }

        Direction facing = clickedState.is(ModBlocks.PAINTED_CLAY_BOWL.get())
                ? player.getDirection().getOpposite()
                : Direction.NORTH;
        PsyBlueprintPreviewService.sendForBlueprint(serverPlayer, level, clicked, facing);
        return InteractionResult.SUCCESS;
    }
}
