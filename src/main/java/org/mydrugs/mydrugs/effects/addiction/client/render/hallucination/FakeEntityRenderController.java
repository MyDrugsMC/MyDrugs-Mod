package org.mydrugs.mydrugs.effects.addiction.client.render.hallucination;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.effects.addiction.client.AddictionClientState;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomFlags;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class FakeEntityRenderController {
    private static final List<FakeHallucination> ACTIVE = new ArrayList<>();
    private static final Random RANDOM = new Random();

    private static final int MAX_ACTIVE = 3;
    private static final int MAX_SPAWN_TRIES = 24;

    private static final ResourceLocation SHADOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/misc/hallucination_shadow.png");
    private static final ResourceLocation EYES_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/misc/hallucination_eyes.png");
    private static final ByteBufferBuilder HALLUCINATION_BUFFER = new ByteBufferBuilder(256 * 1024);

    private FakeEntityRenderController() {
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        long time = mc.level.getGameTime();

        ACTIVE.removeIf(fakeHallucination -> fakeHallucination.expired(time));

        if (!AddictionClientState.has(SymptomFlags.HALLUCINATION)) {
            return;
        }

        updateStareReaction(mc, time);

        if (ACTIVE.size() >= MAX_ACTIVE) return;
        if (RANDOM.nextFloat() > 0.015F + AddictionClientState.globalSeverity * 0.035F) return;

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 spawnPos = findHiddenSpawn(mc, camera);
        if (spawnPos == null) return;

        float scale = Mth.lerp(RANDOM.nextFloat(), 0.92F, 1.18F);
        float yOffset = -0.02F + RANDOM.nextFloat() * 0.08F;
        float phase = RANDOM.nextFloat() * ((float) Math.PI * 2.0F);
        boolean hasEyes = RANDOM.nextFloat() < 0.80F;

        ACTIVE.add(new FakeHallucination(
                spawnPos,
                time,
                time + 50L + RANDOM.nextInt(50),
                scale,
                yOffset,
                phase,
                hasEyes
        ));
    }

    public static void render(RenderLevelStageEvent event) {

        if (ACTIVE.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        if (poseStack == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        Frustum frustum = event.getLevelRenderer().getCapturedFrustum();
        long gameTime = mc.level.getGameTime();

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(HALLUCINATION_BUFFER);

        for (FakeHallucination hallucination : ACTIVE) {
            if (frustum != null && !frustum.isVisible(hallucination.bounds())) {
                continue;
            }

            renderOne(
                    poseStack,
                    camera,
                    bufferSource,
                    hallucination,
                    gameTime
            );
        }

        bufferSource.endBatch();
    }

    private static void renderOne(
            PoseStack poseStack,
            Camera camera,
            MultiBufferSource.BufferSource bufferSource,
            FakeHallucination h,
            long gameTime
    ) {
        float alpha = h.alpha(gameTime);
        if (alpha <= 0.01F) {
            return;
        }

        float severity = Mth.clamp(AddictionClientState.globalSeverity, 0.0F, 1.0F);
        float time = (float) gameTime;

        float bob = (float) Math.sin(time * 0.10F + h.phase * 1.37F) * (0.03F + severity * 0.03F);
        float swayX = (float) Math.sin(time * 0.07F + h.phase) * (0.02F + severity * 0.05F);
        float scalePulse = 1.0F + (float) Math.sin(time * 0.12F + h.phase * 0.75F) * 0.025F;

        Vec3 cameraPos = camera.getPosition();

        poseStack.pushPose();
        poseStack.translate(
                h.position.x - cameraPos.x + swayX,
                h.position.y - cameraPos.y + h.yOffset + bob,
                h.position.z - cameraPos.z
        );

        poseStack.mulPose(camera.rotation());

        float scale = h.scale * scalePulse;
        poseStack.scale(scale, scale * 1.08F, scale);

        int shadowAlpha = Mth.clamp((int) (alpha * 210.0F), 0, 255);
        int eyesAlpha = Mth.clamp((int) (90.0F + alpha * 165.0F), 0, 255);

        drawQuadBothSides(
                poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucentEmissive(SHADOW_TEXTURE)),
                1.5F,
                2.10F,
                8, 8, 8, shadowAlpha,
                LightTexture.FULL_BRIGHT
        );

        if (h.hasEyes) {
            drawQuadBothSides(
                    poseStack,
                    bufferSource.getBuffer(RenderType.eyes(EYES_TEXTURE)),
                    1.5F,
                    2.10F,
                    255, 255, 255, eyesAlpha,
                    LightTexture.FULL_BRIGHT
            );
        }

        poseStack.popPose();
    }

    private static void drawQuadBothSides(
            PoseStack poseStack,
            VertexConsumer consumer,
            float width,
            float height,
            int r, int g, int b, int a,
            int light
    ) {
        PoseStack.Pose pose = poseStack.last();
        float halfW = width * 0.5F;

        consumer.addVertex(pose, -halfW, 0.0F, 0.0F)
                .setColor(r, g, b, a)
                .setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, -halfW, height, 0.0F)
                .setColor(r, g, b, a)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, halfW, height, 0.0F)
                .setColor(r, g, b, a)
                .setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, halfW, 0.0F, 0.0F)
                .setColor(r, g, b, a)
                .setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, halfW, 0.0F, 0.0F)
                .setColor(r, g, b, a)
                .setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);

        consumer.addVertex(pose, halfW, height, 0.0F)
                .setColor(r, g, b, a)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);

        consumer.addVertex(pose, -halfW, height, 0.0F)
                .setColor(r, g, b, a)
                .setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);

        consumer.addVertex(pose, -halfW, 0.0F, 0.0F)
                .setColor(r, g, b, a)
                .setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
    }

    private static void updateStareReaction(Minecraft mc, long time) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        Vector3f look3f = camera.getLookVector();
        Vec3 look = new Vec3(look3f.x(), look3f.y(), look3f.z()).normalize();

        for (FakeHallucination h : ACTIVE) {
            Vec3 headPos = h.position.add(0.0D, h.yOffset + 1.55D * h.scale, 0.0D);
            Vec3 toTarget = headPos.subtract(cameraPos);

            if (toTarget.lengthSqr() > 16.0D * 16.0D) {
                h.staredAtTicks = Math.max(0, h.staredAtTicks - 1);
                continue;
            }

            double dot = look.dot(toTarget.normalize());
            boolean directlyLookedAt = dot > 0.985D && !isOccluded(mc, cameraPos, headPos);

            if (directlyLookedAt) {
                h.staredAtTicks++;
                if (h.staredAtTicks >= 3) {
                    h.expireAt = Math.min(h.expireAt, time + 4L);
                }
            } else {
                h.staredAtTicks = Math.max(0, h.staredAtTicks - 1);
            }
        }
    }

    private static Vec3 findHiddenSpawn(Minecraft mc, Camera camera) {
        Vec3 playerPos = mc.player.position();
        Vec3 cameraPos = camera.getPosition();

        Vector3f lookVec3f = camera.getLookVector();
        Vec3 cameraLook = new Vec3(lookVec3f.x(), lookVec3f.y(), lookVec3f.z()).normalize();

        for (int i = 0; i < MAX_SPAWN_TRIES; i++) {
            double distance = Mth.lerp(RANDOM.nextDouble(), 5.0D, 11.0D);
            double angle = RANDOM.nextDouble() * Math.PI * 2.0D;

            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;

            Vec3 raw = playerPos.add(x, 0.0D, z);
            Vec3 grounded = snapToGroundNearPlayerY(mc, raw);

            if (grounded == null) continue;
            if (!isFarEnoughFromOthers(grounded, 3.5D)) continue;

            Vec3 target = grounded.add(0.0D, 1.4D, 0.0D);

            boolean behind = isBehindCamera(cameraPos, cameraLook, target);
            boolean occluded = isOccluded(mc, cameraPos, target);

            if (behind || occluded) {
                return grounded;
            }
        }

        return null;
    }

    private static boolean isBehindCamera(Vec3 cameraPos, Vec3 cameraLook, Vec3 target) {
        Vec3 toTarget = target.subtract(cameraPos).normalize();
        return cameraLook.dot(toTarget) < -0.15D;
    }

    private static boolean isOccluded(Minecraft mc, Vec3 from, Vec3 to) {
        BlockHitResult hit = mc.level.clip(new ClipContext(
                from,
                to,
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE,
                mc.player
        ));

        if (hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        double blockDistSqr = hit.getLocation().distanceToSqr(from);
        double targetDistSqr = to.distanceToSqr(from);

        return blockDistSqr + 0.01D < targetDistSqr;
    }

    private static boolean isFarEnoughFromOthers(Vec3 pos, double minDistance) {
        double minDistSqr = minDistance * minDistance;

        for (FakeHallucination h : ACTIVE) {
            if (h.position.distanceToSqr(pos) < minDistSqr) {
                return false;
            }
        }

        return true;
    }

    private static Vec3 snapToGroundNearPlayerY(Minecraft mc, Vec3 pos) {
        int baseY = Mth.floor(mc.player.getY());

        for (int dy = 3; dy >= -6; dy--) {
            BlockPos feet = BlockPos.containing(pos.x, baseY + dy, pos.z);
            BlockPos head = feet.above();
            BlockPos below = feet.below();

            boolean feetFree = mc.level.getBlockState(feet).canBeReplaced();
            boolean headFree = mc.level.getBlockState(head).canBeReplaced();
            boolean floorSolid = mc.level.getBlockState(below).isSolidRender();

            if (feetFree && headFree && floorSolid) {
                return new Vec3(pos.x, feet.getY(), pos.z);
            }
        }

        return null;
    }
}