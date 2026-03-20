package org.mydrugs.mydrugs.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.GrindingBowlBlockEntity;

public class GrindingBowlRenderer implements BlockEntityRenderer<GrindingBowlBlockEntity, GrindingBowlRenderState> {
    private final ItemModelResolver itemModelResolver;

    public GrindingBowlRenderer() {
        this.itemModelResolver = Minecraft.getInstance().getItemModelResolver();
    }

    @Override
    public GrindingBowlRenderState createRenderState() {
        return new GrindingBowlRenderState();
    }

    @Override
    public void extractRenderState(
            GrindingBowlBlockEntity blockEntity,
            GrindingBowlRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(
                blockEntity,
                renderState,
                partialTick,
                cameraPos,
                crumblingOverlay
        );

        renderState.displayedStack = blockEntity.getStoredStack().copy();
        renderState.progress01 = blockEntity.getMaxProgress() <= 0
                ? 0.0F
                : (float) blockEntity.getProgress() / (float) blockEntity.getMaxProgress();

        if (!renderState.displayedStack.isEmpty()) {
            this.itemModelResolver.updateForTopItem(
                    renderState.itemRenderState,
                    renderState.displayedStack,
                    ItemDisplayContext.FIXED,
                    null,
                    null,
                    0
            );
        }
    }

    @Override
    public void submit(
            GrindingBowlRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.CameraRenderState cameraState
    ) {
        if (renderState.displayedStack.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        // center of block
        poseStack.translate(0.5F, 0.12F, 0.5F);

        // small grinding wobble
        float crushOffset = renderState.progress01 * 0.03F;
        poseStack.translate(0.0F, crushOffset, 0.0F);

        // lay the item flat in the bowl
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.35F, 0.35F, 0.35F);

        // Use the light field exposed by your BlockEntityRenderState mappings here.
        renderState.itemRenderState.submit(
                poseStack,
                collector,
                renderState.lightCoords,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                0
        );

        poseStack.popPose();
    }
}