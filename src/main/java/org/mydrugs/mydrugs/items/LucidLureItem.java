package org.mydrugs.mydrugs.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import org.mydrugs.mydrugs.events.LucidLureEchoEvents;

import java.util.Optional;

public final class LucidLureItem extends PsyTooltipItem {
    public LucidLureItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (!isDarkEnough(serverLevel, player.blockPosition())) {
            player.displayClientMessage(Component.translatable("message.mydrugs.lucid_lure.needs_darkness"), true);
            return InteractionResult.CONSUME;
        }

        Optional<BlockPos> spawnPos = findSpawnPos(serverLevel, player);
        if (spawnPos.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mydrugs.lucid_lure.blocked"), true);
            return InteractionResult.CONSUME;
        }

        EnderMan echo = EntityType.ENDERMAN.create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (echo == null) {
            return InteractionResult.CONSUME;
        }

        BlockPos pos = spawnPos.get();
        echo.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        echo.setYRot(player.getYRot() + 180.0F);
        echo.setXRot(0.0F);
        echo.addTag(LucidLureEchoEvents.ECHO_TAG);
        var maxHealth = echo.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(20.0D);
        }
        echo.setHealth(20.0F);
        echo.setTarget(player);
        serverLevel.addFreshEntity(echo);

        serverLevel.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_STARE, SoundSource.PLAYERS, 0.65F, 0.75F);
        serverLevel.sendParticles(ParticleTypes.PORTAL,
                pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                36, 0.4D, 0.7D, 0.4D, 0.02D);
        player.displayClientMessage(Component.translatable("message.mydrugs.lucid_lure.summoned"), true);

        if (!(player instanceof ServerPlayer serverPlayer) || !serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isDarkEnough(ServerLevel level, BlockPos pos) {
        long time = level.getDayTime() % 24000L;
        boolean night = time >= 13000L && time <= 23000L;
        return night || level.getBrightness(LightLayer.BLOCK, pos) <= 7;
    }

    private static Optional<BlockPos> findSpawnPos(ServerLevel level, Player player) {
        RandomSource random = level.getRandom();
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int distance = 6 + random.nextInt(5);
            int dx = Mth.floor(Math.cos(angle) * distance);
            int dz = Mth.floor(Math.sin(angle) * distance);
            BlockPos target = origin.offset(dx, 0, dz);
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target);
            int distanceManhattan = ground.distManhattan(origin);
            if (distanceManhattan < 4 || distanceManhattan > 16) {
                continue;
            }
            if (level.getBlockState(ground).isAir() && level.getBlockState(ground.above()).isAir()) {
                return Optional.of(ground);
            }
        }
        return Optional.empty();
    }
}
