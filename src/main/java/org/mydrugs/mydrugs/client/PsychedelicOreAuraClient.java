package org.mydrugs.mydrugs.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.Tags;
import org.joml.Vector3f;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.client.AddictionClientState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class PsychedelicOreAuraClient {
    private static final ByteBufferBuilder BUFFER = new ByteBufferBuilder(2 * 1024 * 1024);

    private static final int SCAN_INTERVAL_TICKS = 32;
    private static final int SCAN_RADIUS = 20;

    /*
     * Scanned auras can be a bit higher than rendered auras.
     * Rendering translucent blobs is the expensive part.
     */
    private static final int MAX_AURAS = 48;
    private static final int MAX_RENDERED_AURAS = 24;

    /*
     * Radius 3 blobs overlap heavily if ores are too close.
     * 16 means one aura roughly every 4 blocks in dense ore clusters.
     */
    private static final int MIN_AURA_SPACING_SQR = 16;

    private static final double RENDER_RADIUS_SQR = 18.0D * 18.0D;
    private static final double CLOSE_LOD_SQR = 7.0D * 7.0D;
    private static final double MID_LOD_SQR = 12.0D * 12.0D;

    private static final float MIN_INSIGHT_TO_SHOW = 0.45F;
    private static final float MAX_INSIGHT_FOR_VISUALS = 2.5F;

    private static final float PI = (float) Math.PI;
    private static final float HALF_PI = PI * 0.5F;

    /*
     * Keep false for performance.
     * Turn true only if your blobs visibly lose faces from some camera angles.
     */
    private static final boolean DOUBLE_SIDED_BLOBS = false;

    private static final List<Aura> AURAS = new ArrayList<>(MAX_AURAS);

    private static int scanCooldown;

    private PsychedelicOreAuraClient() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            AURAS.clear();
            scanCooldown = 0;
            return;
        }

        if (insightIntensity() <= MIN_INSIGHT_TO_SHOW) {
            AURAS.clear();
            scanCooldown = SCAN_INTERVAL_TICKS;
            return;
        }

        if (--scanCooldown <= 0) {
            scanCooldown = SCAN_INTERVAL_TICKS;
            rescan(minecraft.level, minecraft.player.blockPosition());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLevel(RenderLevelStageEvent.AfterParticles event) {
        if (AURAS.isEmpty() || insightIntensity() <= MIN_INSIGHT_TO_SHOW) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();

        if (poseStack == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        MultiBufferSource.BufferSource source = MultiBufferSource.immediate(BUFFER);

        /*
         * Important:
         * debugFilledBox is not for arbitrary organic meshes.
         * lightning is a cheap untextured translucent POSITION_COLOR render type.
         */
        VertexConsumer consumer = source.getBuffer(RenderType.lightning());

        float rawInsight = insightIntensity();
        float normalizedInsight = Mth.clamp(
                (rawInsight - MIN_INSIGHT_TO_SHOW) / (MAX_INSIGHT_FOR_VISUALS - MIN_INSIGHT_TO_SHOW),
                0.0F,
                1.0F
        );

        float visualStrength = 0.35F + 0.65F * normalizedInsight;
        float time = animationTime();

        int rendered = 0;

        for (Aura aura : AURAS) {
            double distSqr = distSqrToCamera(aura.pos(), cameraPos);

            if (distSqr > RENDER_RADIUS_SQR) {
                continue;
            }

            if (!isRoughlyInFrontOfCamera(camera, cameraPos, aura.pos(), 4.0D)) {
                continue;
            }

            drawAura(consumer, poseStack, cameraPos, aura, visualStrength, time, distSqr);

            if (++rendered >= MAX_RENDERED_AURAS) {
                break;
            }
        }

        source.endBatch();
    }

    private static void drawAura(
            VertexConsumer consumer,
            PoseStack poseStack,
            Vec3 cameraPos,
            Aura aura,
            float visualStrength,
            float time,
            double distSqr
    ) {
        BlockPos pos = aura.pos();

        double x = pos.getX() + 0.5D - cameraPos.x;
        double y = pos.getY() + 0.5D - cameraPos.y;
        double z = pos.getZ() + 0.5D - cameraPos.z;

        float pulse = 0.5F + 0.5F * Mth.sin(time * 2.1F + aura.phase());
        float slowPulse = 0.5F + 0.5F * Mth.sin(time * 0.65F + aura.phase() * 1.9F);

        float outerRadius = 3.0F + 0.22F * (pulse - 0.5F);
        float innerRadius = 1.25F + 0.16F * slowPulse;

        int outerColor = aura.color(visualStrength * (0.34F + 0.18F * pulse), 70);
        int innerColor = aura.color(visualStrength * (0.58F + 0.22F * slowPulse), 102);

        int outerRings;
        int outerSegments;
        int innerRings;
        int innerSegments;
        boolean drawInner;

        if (distSqr <= CLOSE_LOD_SQR) {
            outerRings = 8;
            outerSegments = 16;
            innerRings = 5;
            innerSegments = 10;
            drawInner = true;
        } else if (distSqr <= MID_LOD_SQR) {
            outerRings = 6;
            outerSegments = 12;
            innerRings = 0;
            innerSegments = 0;
            drawInner = false;
        } else {
            outerRings = 4;
            outerSegments = 8;
            innerRings = 0;
            innerSegments = 0;
            drawInner = false;
        }

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        PoseStack.Pose pose = poseStack.last();

        drawClosedOrganicBlob(
                consumer,
                pose,
                outerRadius,
                0.14F,
                outerColor,
                time,
                aura.phase(),
                outerRings,
                outerSegments
        );

        if (drawInner) {
            drawClosedOrganicBlob(
                    consumer,
                    pose,
                    innerRadius,
                    0.10F,
                    innerColor,
                    time * 1.25F,
                    aura.phase() + 3.1F,
                    innerRings,
                    innerSegments
            );
        }

        poseStack.popPose();
    }

    private static void rescan(Level level, BlockPos center) {
        AURAS.clear();

        List<BlockPos> candidates = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        int radiusSqr = SCAN_RADIUS * SCAN_RADIUS;

        for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
            for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    if (x * x + y * y + z * z > radiusSqr) {
                        continue;
                    }

                    cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);

                    if (level.isOutsideBuildHeight(cursor)) {
                        continue;
                    }

                    if (!level.hasChunkAt(cursor)) {
                        continue;
                    }

                    if (!level.getBlockState(cursor).is(Tags.Blocks.ORES)) {
                        continue;
                    }

                    candidates.add(cursor.immutable());
                }
            }
        }

        candidates.sort(Comparator.comparingInt(pos -> blockDistSqr(pos, center)));

        for (BlockPos pos : candidates) {
            if (AURAS.size() >= MAX_AURAS) {
                break;
            }

            if (hasNearbyAura(pos)) {
                continue;
            }

            AURAS.add(new Aura(pos, colorFor(pos), phaseFor(pos)));
        }
    }

    private static boolean hasNearbyAura(BlockPos pos) {
        for (Aura aura : AURAS) {
            if (blockDistSqr(aura.pos(), pos) <= MIN_AURA_SPACING_SQR) {
                return true;
            }
        }

        return false;
    }

    private static int blockDistSqr(BlockPos a, BlockPos b) {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        int dz = a.getZ() - b.getZ();

        return dx * dx + dy * dy + dz * dz;
    }

    private static double distSqrToCamera(BlockPos pos, Vec3 cameraPos) {
        double x = pos.getX() + 0.5D - cameraPos.x;
        double y = pos.getY() + 0.5D - cameraPos.y;
        double z = pos.getZ() + 0.5D - cameraPos.z;

        return x * x + y * y + z * z;
    }

    private static boolean isRoughlyInFrontOfCamera(Camera camera, Vec3 cameraPos, BlockPos pos, double margin) {
        Vector3f look = camera.getLookVector();

        double x = pos.getX() + 0.5D - cameraPos.x;
        double y = pos.getY() + 0.5D - cameraPos.y;
        double z = pos.getZ() + 0.5D - cameraPos.z;

        double dot = x * look.x() + y * look.y() + z * look.z();

        return dot > -margin;
    }

    private static int colorFor(BlockPos pos) {
        int selector = Math.floorMod(pos.getX() * 31 + pos.getY() * 17 + pos.getZ() * 13, 6);

        return switch (selector) {
            case 0 -> 0x69F0FF;
            case 1 -> 0xFF65D8;
            case 2 -> 0xC7FF5E;
            case 3 -> 0xFFF06B;
            case 4 -> 0x9D7CFF;
            default -> 0xFF9F6B;
        };
    }

    private static float phaseFor(BlockPos pos) {
        int hash = pos.getX() * 73_428_767
                ^ pos.getY() * 912_783
                ^ pos.getZ() * 42_317_861;

        return Math.floorMod(hash, 6283) / 1000.0F;
    }

    private static float animationTime() {
        return (System.nanoTime() % 60_000_000_000L) / 1_000_000_000.0F;
    }

    private static float insightIntensity() {
        return Math.max(
                AddictionClientState.getEffectIntensity(EffectType.ORE_AURA),
                Math.max(
                        AddictionClientState.getEffectIntensity(EffectType.RITUAL_FOCUS),
                        AddictionClientState.getEffectIntensity(EffectType.ACID_WARP)
                )
        );
    }

    private static void drawClosedOrganicBlob(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float baseRadius,
            float wobble,
            int color,
            float time,
            float phase,
            int rings,
            int segments
    ) {
        for (int ring = 0; ring < rings; ring++) {
            for (int segment = 0; segment < segments; segment++) {
                writeBlobQuad(
                        consumer,
                        pose,
                        color,
                        baseRadius,
                        wobble,
                        time,
                        phase,
                        ring,
                        segment,
                        ring,
                        segment + 1,
                        ring + 1,
                        segment + 1,
                        ring + 1,
                        segment,
                        rings,
                        segments
                );
            }
        }
    }

    private static void writeBlobQuad(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            int color,
            float baseRadius,
            float wobble,
            float time,
            float phase,
            int ring1,
            int segment1,
            int ring2,
            int segment2,
            int ring3,
            int segment3,
            int ring4,
            int segment4,
            int rings,
            int segments
    ) {
        if (DOUBLE_SIDED_BLOBS) {
        writeBlobFace(
                consumer,
                pose,
                color,
                baseRadius,
                wobble,
                time,
                phase,
                ring1,
                segment1,
                ring2,
                segment2,
                ring3,
                segment3,
                ring4,
                segment4,
                rings,
                segments
        );
        }

            writeBlobFace(
                    consumer,
                    pose,
                    color,
                    baseRadius,
                    wobble,
                    time,
                    phase,
                    ring4,
                    segment4,
                    ring3,
                    segment3,
                    ring2,
                    segment2,
                    ring1,
                    segment1,
                    rings,
                    segments
            );

    }

    private static void writeBlobFace(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            int color,
            float baseRadius,
            float wobble,
            float time,
            float phase,
            int ring1,
            int segment1,
            int ring2,
            int segment2,
            int ring3,
            int segment3,
            int ring4,
            int segment4,
            int rings,
            int segments
    ) {
        blobVertex(consumer, pose, color, baseRadius, wobble, time, phase, ring1, segment1, rings, segments);
        blobVertex(consumer, pose, color, baseRadius, wobble, time, phase, ring2, segment2, rings, segments);
        blobVertex(consumer, pose, color, baseRadius, wobble, time, phase, ring3, segment3, rings, segments);
        blobVertex(consumer, pose, color, baseRadius, wobble, time, phase, ring4, segment4, rings, segments);
    }

    private static void blobVertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            int color,
            float baseRadius,
            float wobble,
            float time,
            float phase,
            int ring,
            int segment,
            int rings,
            int segments
    ) {
        int wrappedSegment = Math.floorMod(segment, segments);

        float theta = -HALF_PI + PI * ring / rings;
        float phi = 2.0F * PI * wrappedSegment / segments;

        float dirX;
        float dirY;
        float dirZ;

        if (ring == 0) {
            dirX = 0.0F;
            dirY = -1.0F;
            dirZ = 0.0F;
        } else if (ring == rings) {
            dirX = 0.0F;
            dirY = 1.0F;
            dirZ = 0.0F;
        } else {
            float cosTheta = Mth.cos(theta);
            dirX = cosTheta * Mth.cos(phi);
            dirY = Mth.sin(theta);
            dirZ = cosTheta * Mth.sin(phi);
        }

        /*
         * Periodic waves only.
         * This keeps the seam between segment 0 and segment N closed.
         */
        float waveA = Mth.sin(phi * 2.0F + theta * 3.0F + phase + time * 1.10F);
        float waveB = Mth.cos(phi * 3.0F - theta * 2.0F + phase * 0.70F - time * 0.75F);
        float waveC = Mth.sin(phi * 5.0F + theta + phase * 1.30F + time * 0.45F);

        float organicNoise = waveA * 0.50F + waveB * 0.32F + waveC * 0.18F;

        if (ring == 0 || ring == rings) {
            organicNoise = Mth.sin(theta * 3.0F + phase + time * 0.80F) * 0.30F;
        }

        float radius = baseRadius * (1.0F + wobble * organicNoise);

        float stretchX = 1.00F + 0.05F * Mth.sin(time * 0.80F + phase);
        float stretchY = 0.88F + 0.06F * Mth.sin(time * 0.55F + phase * 1.70F);
        float stretchZ = 1.00F + 0.05F * Mth.sin(time * 0.70F + phase * 2.30F);

        float x = dirX * radius * stretchX;
        float y = dirY * radius * stretchY;
        float z = dirZ * radius * stretchZ;

        float shimmer = 0.78F + 0.22F * (
                0.5F + 0.5F * Mth.sin(phi * 3.0F + theta * 2.0F + time * 2.4F + phase)
        );

        vertex(consumer, pose, color, x, y, z, shimmer);
    }

    private static void vertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            int color,
            float x,
            float y,
            float z,
            float alphaMultiplier
    ) {
        int alpha = (color >>> 24) & 0xFF;
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;

        alpha = Mth.clamp(Math.round(alpha * alphaMultiplier), 4, 255);

        consumer.addVertex(pose, x, y, z).setColor(red, green, blue, alpha);
    }

    private record Aura(BlockPos pos, int rgb, float phase) {
        int color(float strength, int maxAlpha) {
            int alpha = Mth.clamp(Math.round(maxAlpha * strength), 8, maxAlpha);
            return (alpha << 24) | rgb;
        }
    }
}