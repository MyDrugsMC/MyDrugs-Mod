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
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.DryingRackBlock;
import org.mydrugs.mydrugs.blocks.entity.DryingRackBlockEntity;

public final class DryingRackRenderer implements BlockEntityRenderer<DryingRackBlockEntity, DryingRackRenderState> {
    private static final float[][] SLOT_POSITIONS = new float[][]{
            {0.28F, 0.28F},
            {0.72F, 0.28F},
            {0.28F, 0.72F},
            {0.72F, 0.72F}
    };

    private final ItemModelResolver itemModelResolver;

    public DryingRackRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = Minecraft.getInstance().getItemModelResolver();
    }

    @Override
    public DryingRackRenderState createRenderState() {
        return new DryingRackRenderState();
    }

    @Override
    public void extractRenderState(
            DryingRackBlockEntity blockEntity,
            DryingRackRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        for (int i = 0; i < 4; i++) {
            renderState.stacks[i] = blockEntity.getStack(i).copy();
        }

        renderState.facing = blockEntity.getBlockState().getValue(DryingRackBlock.FACING);
    }

    @Override
    public void submit(
            DryingRackRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {
        poseStack.pushPose();

        // Rotate the 4 item positions with the block
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(getYRotation(renderState.facing)));
        poseStack.translate(-0.5F, 0.0F, -0.5F);

        for (int i = 0; i < 4; i++) {
            ItemStack stack = renderState.stacks[i];
            if (stack.isEmpty()) continue;

            poseStack.pushPose();
            poseStack.translate(SLOT_POSITIONS[i][0], 0.77F, SLOT_POSITIONS[i][1]);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.45F, 0.45F, 0.45F);

            ItemStackRenderState itemRenderState = new ItemStackRenderState();

            // Depending on exact 1.21.10 patch + mappings, your IDE may suggest
            // a slightly different overload. Keep the same idea if it does.
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

        poseStack.popPose();
    }

    private static float getYRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 0.0F;
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}