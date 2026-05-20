package org.mydrugs.mydrugs.client.recovery;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.network.RecoveryRoomParticlesPayload;
import org.mydrugs.mydrugs.recovery.RecoveryRoomTier;

import java.util.List;

public final class RecoveryRoomParticleClient {
    private static ActiveRoom activeRoom;

    private RecoveryRoomParticleClient() {
    }

    public static void handle(RecoveryRoomParticlesPayload payload, IPayloadContext context) {
        spawn(payload);
    }

    public static void spawn(RecoveryRoomParticlesPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }

        RecoveryRoomTier tier = RecoveryRoomTier.byNetworkId(payload.tierId());
        activeRoom = new ActiveRoom(payload, tier, 80);
        if (payload.ambient()) {
            return;
        }

        RecoveryRoomOverlay.show(tier, payload.score(), payload.highlight());

        RandomSource random = RandomSource.create(payload.seed());
        List<BlockPos> samples = payload.samples();
        if (samples.isEmpty()) {
            samples = List.of(payload.anchorPos().above());
        }

        int count = switch (tier) {
            case NONE -> 8;
            case FRAGILE_ROOM -> 18;
            case RESTING_ROOM -> 34;
            case SAFE_ROOM -> 58;
            case SANCTUARY -> 88;
        };

        ParticleOptions primary = switch (tier) {
            case NONE, FRAGILE_ROOM -> ParticleTypes.COMPOSTER;
            case RESTING_ROOM -> ParticleTypes.HAPPY_VILLAGER;
            case SAFE_ROOM, SANCTUARY -> ParticleTypes.END_ROD;
        };

        for (int i = 0; i < count; i++) {
            BlockPos base = samples.get(random.nextInt(samples.size()));
            double x = base.getX() + 0.20D + random.nextDouble() * 0.60D;
            double y = base.getY() + 0.15D + random.nextDouble() * 1.45D;
            double z = base.getZ() + 0.20D + random.nextDouble() * 0.60D;
            double drift = tier == RecoveryRoomTier.SANCTUARY ? 0.025D : 0.012D;
            level.addParticle(
                    primary,
                    x,
                    y,
                    z,
                    (random.nextDouble() - 0.5D) * drift,
                    0.010D + random.nextDouble() * drift,
                    (random.nextDouble() - 0.5D) * drift
            );
        }

        if (tier == RecoveryRoomTier.SANCTUARY) {
            spawnAnchorSpiral(level, payload.anchorPos(), random);
        }

        if (payload.highlight()) {
            spawnBounds(level, payload, random);
        }
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || activeRoom == null) {
            activeRoom = null;
            return;
        }
        activeRoom.ticksRemaining--;
        if (activeRoom.ticksRemaining <= 0) {
            activeRoom = null;
            return;
        }
        RecoveryRoomParticlesPayload payload = activeRoom.payload;
        List<BlockPos> samples = payload.samples();
        if (samples.isEmpty()) {
            return;
        }

        int interval = switch (activeRoom.tier) {
            case NONE -> 20;
            case FRAGILE_ROOM -> 16;
            case RESTING_ROOM -> 11;
            case SAFE_ROOM -> 7;
            case SANCTUARY -> 4;
        };
        if (mc.player != null && mc.player.tickCount % interval != 0) {
            return;
        }

        RandomSource random = level.random;
        int motes = switch (activeRoom.tier) {
            case NONE -> 0;
            case FRAGILE_ROOM -> 1;
            case RESTING_ROOM -> 1;
            case SAFE_ROOM -> 2;
            case SANCTUARY -> 3;
        };
        ParticleOptions particle = switch (activeRoom.tier) {
            case NONE, FRAGILE_ROOM -> ParticleTypes.COMPOSTER;
            case RESTING_ROOM -> ParticleTypes.HAPPY_VILLAGER;
            case SAFE_ROOM, SANCTUARY -> ParticleTypes.END_ROD;
        };
        for (int i = 0; i < motes; i++) {
            BlockPos base = samples.get(random.nextInt(samples.size()));
            double x = base.getX() + 0.15D + random.nextDouble() * 0.70D;
            double y = base.getY() + 0.20D + random.nextDouble() * 1.35D;
            double z = base.getZ() + 0.15D + random.nextDouble() * 0.70D;
            double drift = activeRoom.tier == RecoveryRoomTier.SANCTUARY ? 0.018D : 0.010D;
            level.addParticle(particle, x, y, z,
                    (random.nextDouble() - 0.5D) * drift,
                    0.006D + random.nextDouble() * drift,
                    (random.nextDouble() - 0.5D) * drift);
        }

        if (activeRoom.tier == RecoveryRoomTier.SANCTUARY && mc.player != null && mc.player.tickCount % 28 == 0) {
            spawnAnchorSpiral(level, payload.anchorPos(), random);
        }
    }

    public static void clear() {
        activeRoom = null;
    }

    private static void spawnAnchorSpiral(Level level, BlockPos anchor, RandomSource random) {
        for (int i = 0; i < 36; i++) {
            double angle = i / 36.0D * Math.PI * 2.0D;
            double radius = 0.45D + random.nextDouble() * 0.20D;
            double x = anchor.getX() + 0.5D + Math.cos(angle) * radius;
            double y = anchor.getY() + 0.35D + i / 36.0D * 1.4D;
            double z = anchor.getZ() + 0.5D + Math.sin(angle) * radius;
            level.addParticle(ParticleTypes.END_ROD, x, y, z, -Math.sin(angle) * 0.012D, 0.014D, Math.cos(angle) * 0.012D);
        }
    }

    private static void spawnBounds(Level level, RecoveryRoomParticlesPayload payload, RandomSource random) {
        BlockPos min = payload.min();
        BlockPos max = payload.max();
        for (int i = 0; i < 64; i++) {
            double x = random.nextBoolean() ? min.getX() : max.getX() + 1.0D;
            double y = min.getY() + random.nextDouble() * Math.max(1, max.getY() - min.getY() + 1);
            double z = min.getZ() + random.nextDouble() * Math.max(1, max.getZ() - min.getZ() + 1);
            if (random.nextBoolean()) {
                x = min.getX() + random.nextDouble() * Math.max(1, max.getX() - min.getX() + 1);
                z = random.nextBoolean() ? min.getZ() : max.getZ() + 1.0D;
            }
            level.addParticle(ParticleTypes.WAX_ON, x, y, z, 0.0D, 0.015D, 0.0D);
        }
    }

    private static final class ActiveRoom {
        private final RecoveryRoomParticlesPayload payload;
        private final RecoveryRoomTier tier;
        private int ticksRemaining;

        private ActiveRoom(RecoveryRoomParticlesPayload payload, RecoveryRoomTier tier, int ticksRemaining) {
            this.payload = payload;
            this.tier = tier;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
