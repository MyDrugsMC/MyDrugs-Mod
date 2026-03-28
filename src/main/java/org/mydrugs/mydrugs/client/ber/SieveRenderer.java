package org.mydrugs.mydrugs.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.SieveBlockEntity;

public final class SieveRenderer implements BlockEntityRenderer<SieveBlockEntity, SieveRenderState> {
    private final ItemModelResolver itemModelResolver;

    public SieveRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = Minecraft.getInstance().getItemModelResolver();
    }

    @Override
    public SieveRenderState createRenderState() {
        return new SieveRenderState();
    }

    @Override
    public void extractRenderState(
            SieveBlockEntity blockEntity,
            SieveRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        renderState.input = blockEntity.getRenderStack(SieveBlockEntity.SLOT_INPUT).copy();
        renderState.result = blockEntity.getRenderStack(SieveBlockEntity.SLOT_RESULT).copy();
        renderState.bonus = blockEntity.getRenderStack(SieveBlockEntity.SLOT_BONUS).copy();
    }

    private void submitItem(
            SieveRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            ItemStack stack,
            float x,
            float y,
            float z,
            float scale,
            float xRot,
            float yRot,
            float zRot
    ) {
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
        poseStack.scale(scale, scale, scale);

        ItemStackRenderState itemRenderState = new ItemStackRenderState();
        this.itemModelResolver.updateForTopItem(
                itemRenderState,
                stack,
                ItemDisplayContext.GROUND,
                Minecraft.getInstance().level,
                null,
                0
        );

        itemRenderState.submit(
                poseStack,
                collector,
                renderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );

        poseStack.popPose();
    }

    @Override
    public void submit(
            SieveRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {
        // input: flat on the sieve floor
        submitItem(renderState, poseStack, collector,
                renderState.input,
                0.50F, 0.705F, 0.50F,
                0.55F,
                90.0F, 18.0F, 0.0F);

        // main output: also flat, slightly offset on top
        submitItem(renderState, poseStack, collector,
                renderState.result,
                0.32F, 0.715F, 0.34F,
                0.38F,
                90.0F, -14.0F, 0.0F);

        // bonus output: flat inside the sieve
        submitItem(renderState, poseStack, collector,
                renderState.bonus,
                0.67F, 0.45F, 0.60F,
                0.34F,
                90.0F, 26.0F, 0.0F);
    }
}