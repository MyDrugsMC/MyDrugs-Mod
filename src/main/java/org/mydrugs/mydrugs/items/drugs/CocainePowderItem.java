package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.blocks.CocainePowderPileBlock;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.items.rolling.RollingIngredient;

public class CocainePowderItem extends DrugItem implements RollingIngredient {
    public CocainePowderItem(Properties properties, DrugId id, ConsumptionStrategy strategy) {
        super(properties, id, strategy);
    }

    @Override
    public DrugId getRollingDrug(ItemStack stack) {
        return DrugId.COCAINE;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        // Only place when right-clicking the top face of a full solid surface.
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }

        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!clickedState.isFaceSturdy(level, clickedPos, Direction.UP)) {
            // Falling back to PASS lets normal in-air consumption keep working.
            return InteractionResult.PASS;
        }

        BlockPos placePos = clickedPos.above();
        BlockState aboveState = level.getBlockState(placePos);
        if (!aboveState.canBeReplaced()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Direction facing = player.getDirection();
        BlockState pile = ModBlocks.COCAINE_POWDER_PILE.get().defaultBlockState()
                .setValue(CocainePowderPileBlock.STAGE, CocainePowderPileBlock.Stage.PILE)
                .setValue(CocainePowderPileBlock.FACING, facing);
        level.setBlock(placePos, pile, Block.UPDATE_ALL);

        level.playSound(null, placePos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 0.4F, 1.5F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.WHITE_ASH,
                    placePos.getX() + 0.5, placePos.getY() + 0.1, placePos.getZ() + 0.5,
                    6, 0.25, 0.05, 0.25, 0.0
            );
        }

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
