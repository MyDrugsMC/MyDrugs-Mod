package org.mydrugs.mydrugs.recovery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.mydrugs.mydrugs.recovery.TherapistHandler;

import java.util.List;

public final class TherapistDeskBlock extends Block {
    private static final double THERAPIST_RADIUS = 6.0D;

    public TherapistDeskBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        return handleUse(level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        return handleUse(level, pos, player);
    }

    private InteractionResult handleUse(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        AABB box = new AABB(pos).inflate(THERAPIST_RADIUS);
        List<Villager> therapists = level.getEntitiesOfClass(Villager.class, box, TherapistHandler::isTherapist);
        if (therapists.isEmpty()) {
            serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.therapy.desk.no_therapist"), true);
            return InteractionResult.SUCCESS_SERVER;
        }

        String key = TherapistHandler.hasUsedTherapyToday(serverPlayer)
                ? "message.mydrugs.therapy.already_today"
                : "message.mydrugs.therapy.desk.available";
        serverPlayer.displayClientMessage(Component.translatable(key), true);
        return InteractionResult.SUCCESS_SERVER;
    }
}
