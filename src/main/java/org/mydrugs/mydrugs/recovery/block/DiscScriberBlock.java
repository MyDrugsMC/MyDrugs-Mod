package org.mydrugs.mydrugs.recovery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.network.OpenHeadphonesMusicScreenPayload;

public final class DiscScriberBlock extends Block {
    public DiscScriberBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new OpenHeadphonesMusicScreenPayload());
            serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.music.disc_scriber_todo"), true);
        }
        return InteractionResult.SUCCESS;
    }
}
