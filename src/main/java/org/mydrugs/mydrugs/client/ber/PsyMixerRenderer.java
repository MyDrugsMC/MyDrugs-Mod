package org.mydrugs.mydrugs.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
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
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualFocus;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualQuality;

public final class PsyMixerRenderer implements BlockEntityRenderer<FormedPsyMixerCoreBlockEntity, PsyMixerRenderState> {
    private static final float ITEM_SCALE = 0.30F;
    private static final float CENTER_X = 0.5F;
    private static final float CENTER_Z = 0.5F;
    private static final float CENTER_Y = 1.10F;
    private static final float OFFSET = 0.32F;
    private static final int RING_SEGMENTS = 72;
    private static final float TAU = (float) (Math.PI * 2.0D);

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
        state.focusSlot = PsyMixerRitualFocus.byId(be.getFocusIndex()).slot();
        state.resonance = be.getResonance();
        state.quality = be.getCurrentQualityPreview();
        state.mistakes = be.getMistakes();
        state.maxMistakes = be.getMaxMistakes();
    }

    @Override
    public void submit(
            PsyMixerRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera
    ) {
        if (state.running) {
            submitRitualRings(state, poseStack, collector);
        }
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

        boolean focused = state.running && slot == state.focusSlot;
        float spinSpeed = state.running ? focused ? 7.0F : 4.0F : 1.5F;
        float bobAmplitude = state.running ? focused ? 0.10F : 0.06F : 0.025F;
        float bobSpeed = state.running ? 0.16F : 0.08F;

        float pull = state.running ? Math.min(0.45F, state.progressFraction * 0.45F) : 0.0F;
        float ex = dx * (1.0F - pull);
        float ez = dz * (1.0F - pull);

        float bob = Mth.sin(state.ageInTicks * bobSpeed + slot * 0.7F) * bobAmplitude;
        float spin = state.ageInTicks * spinSpeed + slot * 60.0F;

        poseStack.pushPose();
        poseStack.translate(CENTER_X + ex, CENTER_Y + dy + bob, CENTER_Z + ez);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        float focusScale = focused ? 1.15F + state.resonance * 0.25F : 1.0F;
        poseStack.scale(ITEM_SCALE * focusScale, ITEM_SCALE * focusScale, ITEM_SCALE * focusScale);

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

    private static void submitRitualRings(
            PsyMixerRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector
    ) {
        collector.order(-3).submitCustomGeometry(
                poseStack,
                RenderType.lightning(),
                (pose, consumer) -> {
                    for (int ring = 0; ring < 3; ring++) {
                        addRitualRing(consumer, pose, state, ring);
                    }
                }
        );
    }

    private static void addRitualRing(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            PsyMixerRenderState state,
            int ring
    ) {
        boolean bad = isBadQuality(state);
        float pulse = bad
                ? 0.65F + 0.35F * Mth.sin(state.ageInTicks * 0.55F)
                : 0.82F + 0.18F * Mth.sin(state.ageInTicks * 0.12F + ring);
        float radius = 0.70F + ring * 0.18F + pulse * (bad ? 0.045F : 0.018F);
        float thickness = 0.012F + ring * 0.004F;
        float y = 0.64F + ring * 0.18F + Mth.sin(state.ageInTicks * 0.055F + ring * 1.9F) * 0.025F;
        float spin = state.ageInTicks * (0.018F + ring * 0.009F) * (ring % 2 == 0 ? 1.0F : -1.0F);
        int color = ringColor(state, ring, pulse);
        int offset = (int) (state.ageInTicks * (0.35F + ring * 0.16F));

        for (int i = 0; i < RING_SEGMENTS; i++) {
            if ((i + offset + ring * 3) % 11 == 0) {
                continue;
            }
            float a0 = spin + i * TAU / RING_SEGMENTS;
            float a1 = spin + (i + 1) * TAU / RING_SEGMENTS;
            float inner = radius - thickness;
            float outer = radius + thickness;
            float x0i = CENTER_X + Mth.cos(a0) * inner;
            float z0i = CENTER_Z + Mth.sin(a0) * inner;
            float x1i = CENTER_X + Mth.cos(a1) * inner;
            float z1i = CENTER_Z + Mth.sin(a1) * inner;
            float x1o = CENTER_X + Mth.cos(a1) * outer;
            float z1o = CENTER_Z + Mth.sin(a1) * outer;
            float x0o = CENTER_X + Mth.cos(a0) * outer;
            float z0o = CENTER_Z + Mth.sin(a0) * outer;

            vertex(consumer, pose, x0i, y, z0i, color);
            vertex(consumer, pose, x1i, y, z1i, color);
            vertex(consumer, pose, x1o, y, z1o, color);
            vertex(consumer, pose, x0o, y, z0o, color);
            vertex(consumer, pose, x0o, y, z0o, color);
            vertex(consumer, pose, x1o, y, z1o, color);
            vertex(consumer, pose, x1i, y, z1i, color);
            vertex(consumer, pose, x0i, y, z0i, color);
        }
    }

    private static int ringColor(PsyMixerRenderState state, int ring, float pulse) {
        boolean bad = isBadQuality(state);
        int red;
        int green;
        int blue;
        if (bad) {
            red = 210 + Math.round(35.0F * pulse);
            green = 35 + ring * 14;
            blue = 105 + ring * 20;
        } else if (state.quality == PsyMixerRitualQuality.MASTERWORK) {
            red = 245;
            green = 205 + ring * 12;
            blue = 110 + ring * 18;
        } else if (state.quality == PsyMixerRitualQuality.PERFECT) {
            red = 120 + ring * 22;
            green = 235;
            blue = 240;
        } else {
            red = 100 + ring * 18;
            green = 190 + ring * 16;
            blue = 230;
        }
        int alpha = Mth.clamp(Math.round((bad ? 110.0F : 82.0F) + pulse * 72.0F), 48, 205);
        return (alpha << 24)
                | (Mth.clamp(red, 0, 255) << 16)
                | (Mth.clamp(green, 0, 255) << 8)
                | Mth.clamp(blue, 0, 255);
    }

    private static boolean isBadQuality(PsyMixerRenderState state) {
        return state.quality == PsyMixerRitualQuality.CRUDE
                || (state.maxMistakes > 0 && state.mistakes * 2 >= state.maxMistakes);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int color) {
        int alpha = (color >>> 24) & 0xFF;
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;
        consumer.addVertex(pose, x, y, z).setColor(red, green, blue, alpha);
    }
}
