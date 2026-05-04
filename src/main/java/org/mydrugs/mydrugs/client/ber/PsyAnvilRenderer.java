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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.PsyAnvilBlockEntity;

import java.util.ArrayList;
import java.util.List;

public final class PsyAnvilRenderer implements BlockEntityRenderer<PsyAnvilBlockEntity, PsyAnvilRenderState> {
    private static final float CENTER_X = 0.50F;
    private static final float CENTER_Z = 0.50F;

    private static final float BASE_FLOAT_Y = 1.18F;
    private static final float FLOAT_BOB_AMOUNT = 0.035F;
    private static final float FLOAT_BOB_SPEED = 0.10F;

    private static final float DEFAULT_RADIUS = 0.30F;
    private static final float SMALL_RADIUS = 0.23F;

    private static final float ITEM_SCALE = 0.28F;

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

        if (blockEntity.getLevel() != null) {
            renderState.ageInTicks = blockEntity.getLevel().getGameTime() + partialTick;
        } else {
            renderState.ageInTicks = 0.0F;
        }
    }

    @Override
    public void submit(
            PsyAnvilRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {
        List<ItemStack> visibleStacks = new ArrayList<>();

        for (ItemStack stack : renderState.stacks) {
            if (!stack.isEmpty()) {
                visibleStacks.add(stack);
            }
        }

        int visibleCount = visibleStacks.size();

        if (visibleCount <= 0) {
            return;
        }

        for (int i = 0; i < visibleCount; i++) {
            ItemStack stack = visibleStacks.get(i);

            float x = CENTER_X;
            float z = CENTER_Z;

            if (visibleCount > 1) {
                float radius = radiusFor(visibleCount);
                double angle = -Math.PI / 2.0D + 2.0D * Math.PI * i / visibleCount;

                x += (float) Math.cos(angle) * radius;
                z += (float) Math.sin(angle) * radius;
            }

            float bobOffset = Mth.sin(renderState.ageInTicks * FLOAT_BOB_SPEED + i * 0.75F) * FLOAT_BOB_AMOUNT;
            float spin = renderState.ageInTicks * 2.0F + i * (360.0F / visibleCount);

            poseStack.pushPose();

            poseStack.translate(x, BASE_FLOAT_Y + bobOffset, z);

            poseStack.mulPose(Axis.YP.rotationDegrees(spin));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);

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

    private static float radiusFor(int visibleCount) {
        if (visibleCount <= 2) {
            return SMALL_RADIUS;
        }

        return DEFAULT_RADIUS;
    }
}