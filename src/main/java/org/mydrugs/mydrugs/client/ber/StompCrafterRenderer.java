package org.mydrugs.mydrugs.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.StompCrafterBlockEntity;

import java.util.List;

public class StompCrafterRenderer implements BlockEntityRenderer<StompCrafterBlockEntity, StompCrafterRenderState> {
    private final ItemModelResolver itemResolver;

    public StompCrafterRenderer(BlockEntityRendererProvider.Context context) {
        this.itemResolver = Minecraft.getInstance().getItemModelResolver();
    }

    private static void applyLayout(PoseStack poseStack, int count, int index, float y) {
        switch (count) {
            case 1 -> poseStack.translate(0.50D, y, 0.50D);

            case 2 -> {
                if (index == 0) poseStack.translate(0.42D, y, 0.50D);
                else poseStack.translate(0.58D, y, 0.50D);
            }

            case 3 -> {
                if (index == 0) poseStack.translate(0.42D, y, 0.44D);
                else if (index == 1) poseStack.translate(0.58D, y, 0.44D);
                else poseStack.translate(0.50D, y, 0.58D);
            }

            case 4 -> {
                double[][] p = {
                        {0.42D, y, 0.42D},
                        {0.58D, y, 0.42D},
                        {0.42D, y, 0.58D},
                        {0.58D, y, 0.58D}
                };
                poseStack.translate(p[index][0], p[index][1], p[index][2]);
            }

            default -> {
                int row = index / 3;
                int col = index % 3;
                double x = 0.36D + col * 0.08D;
                double z = 0.36D + row * 0.08D;
                poseStack.translate(x, y, z);
            }
        }
    }

    @Override
    public StompCrafterRenderState createRenderState() {
        return new StompCrafterRenderState();
    }

    @Override
    public void extractRenderState(StompCrafterBlockEntity blockEntity,
                                   StompCrafterRenderState renderState,
                                   float partialTick,
                                   Vec3 cameraPos,
                                   @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        renderState.exampleStacks.clear();
        renderState.exampleStacks.addAll(blockEntity.getUniqueExampleStacks());
        renderState.progress = blockEntity.getProgressPercent();

        if (blockEntity.getLevel() != null) {
            renderState.lightCoords = LevelRenderer.getLightColor(
                    blockEntity.getLevel(),
                    blockEntity.getBlockPos().above()
            );
        } else {
            renderState.lightCoords = LightTexture.FULL_BRIGHT;
        }
    }

    @Override
    public void submit(StompCrafterRenderState renderState,
                       PoseStack poseStack,
                       SubmitNodeCollector collector,
                       CameraRenderState cameraState) {

        float t = Mth.clamp(renderState.progress / 100.0F, 0.0F, 1.0F);

        renderPlate(renderState, poseStack, collector, t);
        renderIngredients(renderState, poseStack, collector, t);
    }

    private void renderPlate(StompCrafterRenderState renderState,
                             PoseStack poseStack,
                             SubmitNodeCollector collector,
                             float t) {
        float plateY = Mth.lerp(t, 0.72F, 0.34F);

        poseStack.pushPose();

        poseStack.translate(0.0D, plateY, 0.0D);

        // shrink a little around the block center
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.scale(0.96F, 1.0F, 0.96F);
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        collector.submitBlock(
                poseStack,
                ModBlocks.STOMP_PLATE_BLOCK.get().defaultBlockState(),
                renderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );

        poseStack.popPose();
    }

    private void renderIngredients(StompCrafterRenderState renderState,
                                   PoseStack poseStack,
                                   SubmitNodeCollector collector,
                                   float t) {
        List<ItemStack> stacks = renderState.exampleStacks;
        if (stacks.isEmpty()) {
            return;
        }

        int count = Math.min(stacks.size(), 9);

        // Items sink slightly as the plate comes down
        float itemY = Mth.lerp(t, 0.26F, 0.18F);
        float squashY = Mth.lerp(t, 1.0F, 0.55F);

        for (int i = 0; i < count; i++) {
            ItemStack stack = stacks.get(i);
            ItemStackRenderState itemState = new ItemStackRenderState();

            this.itemResolver.updateForTopItem(
                    itemState,
                    stack,
                    ItemDisplayContext.FIXED,
                    null,
                    null,
                    0
            );

            poseStack.pushPose();
            applyLayout(poseStack, count, i, itemY);
            poseStack.scale(0.28F, 0.28F * squashY, 0.28F);

            itemState.submit(
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