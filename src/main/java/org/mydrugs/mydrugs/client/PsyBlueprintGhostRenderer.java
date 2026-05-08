package org.mydrugs.mydrugs.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.mydrugs.mydrugs.network.PsyBlueprintPreviewPayload;

public final class PsyBlueprintGhostRenderer {
    private static final ByteBufferBuilder BUFFER = new ByteBufferBuilder(512 * 1024);
    private static final float GHOST_SCALE = 0.94F;

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
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();

        for (PsyBlueprintPreviewPayload.Entry entry : PsyBlueprintPreviewClientState.entries()) {
            drawGhostBlock(blockRenderer, poseStack, bufferSource, cameraPos, entry);
        }

        bufferSource.endBatch();
    }

    private static void drawGhostBlock(
            BlockRenderDispatcher blockRenderer,
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            Vec3 cameraPos,
            PsyBlueprintPreviewPayload.Entry entry
    ) {
        Block block = BuiltInRegistries.BLOCK.getValue(entry.blockId());
        if (block == null || block == Blocks.AIR) {
            return;
        }

        BlockPos pos = entry.pos();
        BlockState expectedState = block.defaultBlockState();
        MultiBufferSource ghostBuffers = renderType -> new GhostVertexConsumer(
                bufferSource.getBuffer(RenderType.translucentMovingBlock()),
                entry.wrongBlock()
        );

        poseStack.pushPose();
        poseStack.translate(pos.getX() - cameraPos.x + 0.5D, pos.getY() - cameraPos.y + 0.5D, pos.getZ() - cameraPos.z + 0.5D);
        poseStack.scale(GHOST_SCALE, GHOST_SCALE, GHOST_SCALE);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        blockRenderer.renderSingleBlock(expectedState, poseStack, ghostBuffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static final class GhostVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final boolean wrongBlock;

        private GhostVertexConsumer(VertexConsumer delegate, boolean wrongBlock) {
            this.delegate = delegate;
            this.wrongBlock = wrongBlock;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            int outRed = this.wrongBlock ? Math.max(red, 235) : red;
            int outGreen = this.wrongBlock ? Math.round(green * 0.35F) : red == 0 && green == 0 && blue == 0 ? 185 : green;
            int outBlue = this.wrongBlock ? Math.round(blue * 0.35F) : blue;
            int outAlpha = this.wrongBlock ? Math.min(178, Math.max(132, alpha)) : Math.min(132, Math.max(86, alpha));
            this.delegate.setColor(outRed, outGreen, outBlue, outAlpha);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            this.delegate.setNormal(normalX, normalY, normalZ);
            return this;
        }
    }
}
