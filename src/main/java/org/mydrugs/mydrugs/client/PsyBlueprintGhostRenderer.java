package org.mydrugs.mydrugs.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.mydrugs.mydrugs.network.PsyBlueprintPreviewPayload;

public final class PsyBlueprintGhostRenderer {
    private static final ByteBufferBuilder BUFFER = new ByteBufferBuilder(512 * 1024);

    private PsyBlueprintGhostRenderer() {
    }

    public static void render(RenderLevelStageEvent event) {
        if (!PsyBlueprintPreviewClientState.isActive()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        if (poseStack == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().location().equals(PsyBlueprintPreviewClientState.dimension())) {
            return;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(BUFFER);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucentMovingBlock());

        for (PsyBlueprintPreviewPayload.Entry entry : PsyBlueprintPreviewClientState.entries()) {
            drawGhostCube(poseStack, consumer, cameraPos, entry.pos(), entry.blockId(), entry.wrongBlock());
        }

        bufferSource.endBatch();
    }

    private static void drawGhostCube(PoseStack poseStack, VertexConsumer consumer, Vec3 cameraPos, BlockPos pos, ResourceLocation blockId, boolean wrongBlock) {
        int color = wrongBlock ? 0xF04444 : colorFor(blockId);
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        int a = wrongBlock ? 126 : 82;

        float x0 = (float) (pos.getX() - cameraPos.x) + 0.03F;
        float y0 = (float) (pos.getY() - cameraPos.y) + 0.03F;
        float z0 = (float) (pos.getZ() - cameraPos.z) + 0.03F;
        float x1 = x0 + 0.94F;
        float y1 = y0 + 0.94F;
        float z1 = z0 + 0.94F;

        PoseStack.Pose pose = poseStack.last();
        quad(consumer, pose, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, r, g, b, a, 0, 0, -1);
        quad(consumer, pose, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, r, g, b, a, 0, 0, 1);
        quad(consumer, pose, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1, r, g, b, a, -1, 0, 0);
        quad(consumer, pose, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, r, g, b, a, 1, 0, 0);
        quad(consumer, pose, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, r, g, b, a, 0, 1, 0);
        quad(consumer, pose, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, r, g, b, a, 0, -1, 0);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             int r, int g, int b, int a,
                             float nx, float ny, float nz) {
        vertex(consumer, pose, x0, y0, z0, r, g, b, a, nx, ny, nz);
        vertex(consumer, pose, x1, y1, z1, r, g, b, a, nx, ny, nz);
        vertex(consumer, pose, x2, y2, z2, r, g, b, a, nx, ny, nz);
        vertex(consumer, pose, x3, y3, z3, r, g, b, a, nx, ny, nz);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                               float x, float y, float z,
                               int r, int g, int b, int a,
                               float nx, float ny, float nz) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }

    private static int colorFor(ResourceLocation blockId) {
        int hash = blockId.hashCode();
        int r = 110 + Math.floorMod(hash, 80);
        int g = 150 + Math.floorMod(hash >> 8, 80);
        int b = 95 + Math.floorMod(hash >> 16, 95);
        return (r << 16) | (g << 8) | b;
    }
}
