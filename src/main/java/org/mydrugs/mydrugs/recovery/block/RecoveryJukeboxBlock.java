package org.mydrugs.mydrugs.recovery.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.recovery.RecoveryRoomManager;
import org.mydrugs.mydrugs.recovery.RecoveryRoomReport;
import org.mydrugs.mydrugs.recovery.RecoverySessionAction;
import org.mydrugs.mydrugs.recovery.RecoverySessionManager;
import org.mydrugs.mydrugs.recovery.item.PersonalMusicDiscItem;

public final class RecoveryJukeboxBlock extends BaseEntityBlock {
    public static final MapCodec<RecoveryJukeboxBlock> CODEC = simpleCodec(RecoveryJukeboxBlock::new);

    public RecoveryJukeboxBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RecoveryJukeboxBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.RECOVERY_JUKEBOX.get(), RecoveryJukeboxBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!(level.getBlockEntity(pos) instanceof RecoveryJukeboxBlockEntity jukebox) || !jukebox.isEmpty()) {
            return InteractionResult.PASS;
        }
        boolean personalDisc = PersonalMusicDiscItem.isPersonalDisc(stack);
        if (!personalDisc && !stack.has(DataComponents.JUKEBOX_PLAYABLE)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack inserted = stack.copyWithCount(1);
        if (!jukebox.insert(inserted, level.getGameTime())) {
            return InteractionResult.PASS;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (personalDisc) {
                serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.music.jukebox_started_personal"), true);
                RecoveryRoomReport room = RecoveryRoomManager.getBestRoom(serverPlayer).orElse(null);
                if (RecoveryRoomManager.isValidRecoveryRoom(room)) {
                    RecoverySessionManager.onGroundingAction(serverPlayer, RecoverySessionAction.MUSIC);
                }
            } else {
                serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.music.jukebox_started_vanilla"), true);
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof RecoveryJukeboxBlockEntity jukebox)) {
            return InteractionResult.PASS;
        }
        if (jukebox.isEmpty()) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.music.jukebox_empty"), true);
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack disc = jukebox.eject();
        if (!disc.isEmpty() && !player.addItem(disc)) {
            player.drop(disc, false);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.music.jukebox_stopped"), true);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof RecoveryJukeboxBlockEntity jukebox) {
            return jukebox.comparatorOutput();
        }
        return 0;
    }
}
