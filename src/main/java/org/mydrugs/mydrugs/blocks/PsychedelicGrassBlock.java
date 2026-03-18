package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PsychedelicGrassBlock extends GrassBlock {
    public PsychedelicGrassBlock(Properties props) {
        super(props);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockState rye = ModBlocks.RYE.get().defaultBlockState();
        BlockPos base = pos.above();

        for (int i = 0; i < 128; i++) {
            BlockPos target = base;
            boolean blocked = false;

            for (int j = 0; j < i / 16; j++) {
                target = target.offset(random.nextInt(3) - 1,
                        (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                        random.nextInt(3) - 1);

                if (!level.getBlockState(target.below()).is(this)
                        || level.getBlockState(target).isCollisionShapeFullBlock(level, target)) {
                    blocked = true;
                    break;
                }
            }

            if (blocked) continue;

            if (level.isEmptyBlock(target) && rye.canSurvive(level, target)) {
                level.setBlock(target, rye, 3);
            }
        }
    }
}