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
import org.mydrugs.mydrugs.blocks.CoffeeDryingMatBlock;
import org.mydrugs.mydrugs.blocks.entity.CoffeeDryingMatBlockEntity;

public final class CoffeeDryingMatRenderer implements BlockEntityRenderer<CoffeeDryingMatBlockEntity, CoffeeDryingMatRenderState> {
    private static final float[] POS = new float[]{0.22F, 0.50F, 0.78F};
    private final ItemModelResolver itemModelResolver;

    public CoffeeDryingMatRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = Minecraft.getInstance().getItemModelResolver();
    }

    @Override
    public CoffeeDryingMatRenderState createRenderState() {
        return new CoffeeDryingMatRenderState();
    }

    @Override
    public void extractRenderState(CoffeeDryingMatBlockEntity blockEntity, CoffeeDryingMatRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);
        for (int i = 0; i < 9; i++) renderState.stacks[i] = blockEntity.getStack(i).copy();
        renderState.facing = blockEntity.getBlockState().getValue(CoffeeDryingMatBlock.FACING);
    }

    @Override
    public void submit(CoffeeDryingMatRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(getYRotation(renderState.facing)));
        poseStack.translate(-0.5F, 0.0F, -0.5F);

        for (int i = 0; i < 9; i++) {
            ItemStack stack = renderState.stacks[i];
            if (stack.isEmpty()) continue;
            int row = i / 3;
            int col = i % 3;
            poseStack.pushPose();
            poseStack.translate(POS[col], 0.12F, POS[row]);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.34F, 0.34F, 0.34F);
            ItemStackRenderState itemRenderState = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(itemRenderState, stack, ItemDisplayContext.GROUND, Minecraft.getInstance().level, null, 0);
            itemRenderState.submit(poseStack, collector, renderState.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static float getYRotation(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}
