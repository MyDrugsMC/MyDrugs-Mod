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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.PsyAnvilBlockEntity;

public final class PsyAnvilRenderer implements BlockEntityRenderer<PsyAnvilBlockEntity, PsyAnvilRenderState> {
    private static final float[][] SLOT_POSITIONS = new float[][]{
            {0.30F, 0.30F}, {0.50F, 0.30F}, {0.70F, 0.30F},
            {0.30F, 0.50F}, {0.50F, 0.50F}, {0.70F, 0.50F},
            {0.30F, 0.70F}, {0.50F, 0.70F}, {0.70F, 0.70F}
    };

    private final ItemModelResolver itemModelResolver;

    public PsyAnvilRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = Minecraft.getInstance().getItemModelResolver();
    }

    @Override
    public PsyAnvilRenderState createRenderState() {
        return new PsyAnvilRenderState();
    }

    @Override
    public void extractRenderState(
            PsyAnvilBlockEntity blockEntity,
            PsyAnvilRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);
        for (int i = 0; i < PsyAnvilBlockEntity.SLOT_COUNT; i++) {
            renderState.stacks[i] = blockEntity.getRenderStack(i).copy();
        }
    }

    @Override
    public void submit(PsyAnvilRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        for (int i = 0; i < renderState.stacks.length; i++) {
            ItemStack stack = renderState.stacks[i];
            if (stack.isEmpty()) continue;

            poseStack.pushPose();
            poseStack.translate(SLOT_POSITIONS[i][0], 1.03F, SLOT_POSITIONS[i][1]);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((i % 3) * 9.0F - 9.0F));
            poseStack.scale(0.28F, 0.28F, 0.28F);

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
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                    0
            );
            poseStack.popPose();
        }
    }
}
