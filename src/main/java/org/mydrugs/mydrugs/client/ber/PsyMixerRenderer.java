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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;
import org.mydrugs.mydrugs.blocks.entity.FormedPsyMixerCoreBlockEntity;

public final class PsyMixerRenderer implements BlockEntityRenderer<FormedPsyMixerCoreBlockEntity, PsyMixerRenderState> {
    private static final float ITEM_SCALE = 0.30F;
    private static final float CENTER_X = 0.5F;
    private static final float CENTER_Z = 0.5F;
    private static final float CENTER_Y = 1.10F;
    private static final float OFFSET = 0.32F;

    private final ItemModelResolver itemModelResolver;

    public PsyMixerRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = Minecraft.getInstance().getItemModelResolver();
    }

    @Override
    public PsyMixerRenderState createRenderState() {
        return new PsyMixerRenderState();
    }

    @Override
    public void extractRenderState(
            FormedPsyMixerCoreBlockEntity be,
            PsyMixerRenderState state,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay);
        for (int i = 0; i < PsyMixerMultiblock.SLOT_COUNT; i++) {
            state.stacks[i] = be.getRenderStack(i).copy();
        }
        state.ageInTicks = be.getLevel() == null ? 0.0F : be.getLevel().getGameTime() + partialTick;
        state.running = be.isRunning();
        state.progressFraction = be.getRitualMaxTime() > 0 ? (float) be.getProgress() / be.getRitualMaxTime() : 0.0F;
    }

    @Override
    public void submit(
            PsyMixerRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera
    ) {
        renderItem(state, poseStack, collector, PsyMixerMultiblock.SLOT_BASE, 0.0F, 0.0F, 0.0F);
        renderItem(state, poseStack, collector, PsyMixerMultiblock.SLOT_MATERIAL, -OFFSET, 0.0F, 0.0F);
        renderItem(state, poseStack, collector, PsyMixerMultiblock.SLOT_CATALYST, 0.0F, 0.20F, -OFFSET);
        renderItem(state, poseStack, collector, PsyMixerMultiblock.SLOT_STABILIZER, OFFSET, 0.0F, 0.0F);
        renderItem(state, poseStack, collector, PsyMixerMultiblock.SLOT_VESSEL, 0.0F, -0.05F, OFFSET);
        renderItem(state, poseStack, collector, PsyMixerMultiblock.SLOT_OUTPUT, 0.0F, 0.45F, 0.0F);
    }

    private void renderItem(
            PsyMixerRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int slot,
            float dx, float dy, float dz
    ) {
        ItemStack stack = state.stacks[slot];
        if (stack.isEmpty()) return;

        float spinSpeed = state.running ? 4.0F : 1.5F;
        float bobAmplitude = state.running ? 0.06F : 0.025F;
        float bobSpeed = state.running ? 0.16F : 0.08F;

        float pull = state.running ? Math.min(0.45F, state.progressFraction * 0.45F) : 0.0F;
        float ex = dx * (1.0F - pull);
        float ez = dz * (1.0F - pull);

        float bob = Mth.sin(state.ageInTicks * bobSpeed + slot * 0.7F) * bobAmplitude;
        float spin = state.ageInTicks * spinSpeed + slot * 60.0F;

        poseStack.pushPose();
        poseStack.translate(CENTER_X + ex, CENTER_Y + dy + bob, CENTER_Z + ez);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
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
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );
        poseStack.popPose();
    }
}
