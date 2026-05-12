package org.mydrugs.mydrugs.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class ShroomHarvesterItem extends Item {
    private static final float DROP_CHANCE = 0.05F;

    public ShroomHarvesterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (!state.is(Blocks.RED_MUSHROOM_BLOCK)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide() || player == null) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        ItemStack tool = context.getItemInHand();
        InteractionHand hand = context.getHand();
        if (serverLevel.random.nextFloat() < DROP_CHANCE) {
            Vec3 dropPos = Vec3.atCenterOf(pos).add(0.0D, 0.5D, 0.0D);
            net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(
                    serverLevel,
                    dropPos.x, dropPos.y, dropPos.z,
                    new ItemStack(ModItems.CALMING_SPORES.get())
            );
            drop.setDefaultPickUpDelay();
            serverLevel.addFreshEntity(drop);
        }

        serverLevel.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 0.7F, 1.2F);
        tool.hurtAndBreak(1, player, hand);
        player.swing(hand, true);
        return InteractionResult.SUCCESS;
    }
}
