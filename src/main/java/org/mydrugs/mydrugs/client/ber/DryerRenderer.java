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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.DryerBlockEntity;

public final class DryerRenderer implements BlockEntityRenderer<DryerBlockEntity, DryerRenderState> {
    private static final float[][] SLOT_POSITIONS = new float[][]{
            {0.28F, 0.28F},
            {0.72F, 0.28F},
            {0.28F, 0.72F},
            {0.72F, 0.72F}
    };

    private final ItemModelResolver itemModelResolver;

    public DryerRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = Minecraft.getInstance().getItemModelResolver();
    }

    @Override
    public DryerRenderState createRenderState() {
        return new DryerRenderState();
    }

    @Override
    public void extractRenderState(
            DryerBlockEntity blockEntity,
            DryerRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        for (int i = 0; i < 4; i++) {
            renderState.stacks[i] = blockEntity.getStack(i).copy();
        }
    }

    @Override
    public void submit(
            DryerRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {
        for (int i = 0; i < 4; i++) {
            ItemStack stack = renderState.stacks[i];
            if (stack.isEmpty()) continue;

            poseStack.pushPose();
            poseStack.translate(SLOT_POSITIONS[i][0], 0.76F, SLOT_POSITIONS[i][1]-0.1);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            poseStack.scale(0.70F, 0.70F, 0.70F);

            ItemStackRenderState itemRenderState = new ItemStackRenderState();

            // In 1.21.x, the exact nullable args on this overload may vary slightly by patch/mappings.
            // Let your IDE fill the last arguments if needed.
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
    }
}