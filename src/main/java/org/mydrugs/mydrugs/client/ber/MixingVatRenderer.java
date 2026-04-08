package org.mydrugs.mydrugs.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.MixingVatBlockEntity;

import java.util.List;

public class MixingVatRenderer implements BlockEntityRenderer<MixingVatBlockEntity, MixingVatRenderState> {
    private static final float INNER_MIN = 3.05f / 16.0f;
    private static final float INNER_MAX = 12.95f / 16.0f;
    private static final float FLUID_FLOOR_Y = 3.02f / 16.0f;
    private static final float FLUID_MAX_Y = 9.70f / 16.0f;

    // Put a texture at assets/mydrugs/textures/block/mixing_arm.png
    private static final Material MIXING_ARM_MATERIAL = new Material(
            TextureAtlas.LOCATION_BLOCKS,
            ResourceLocation.fromNamespaceAndPath("mydrugs", "block/mixing_arm")
    );

    private final ItemModelResolver itemModelResolver;
    private final MaterialSet materials;

    public MixingVatRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.materials = context.materials();
    }

    private static int countNonEmpty(MixingVatRenderState state) {
        int count = 0;
        for (var item : state.items) {
            if (!item.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static void addQuadDoubleSided(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            int light,
            int color,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            float x4, float y4, float z4, float u4, float v4,
            float nx, float ny, float nz
    ) {
        vertex(consumer, pose, x1, y1, z1, u1, v1, light, color, nx, ny, nz);
        vertex(consumer, pose, x2, y2, z2, u2, v2, light, color, nx, ny, nz);
        vertex(consumer, pose, x3, y3, z3, u3, v3, light, color, nx, ny, nz);
        vertex(consumer, pose, x4, y4, z4, u4, v4, light, color, nx, ny, nz);

        vertex(consumer, pose, x4, y4, z4, u4, v4, light, color, -nx, -ny, -nz);
        vertex(consumer, pose, x3, y3, z3, u3, v3, light, color, -nx, -ny, -nz);
        vertex(consumer, pose, x2, y2, z2, u2, v2, light, color, -nx, -ny, -nz);
        vertex(consumer, pose, x1, y1, z1, u1, v1, light, color, -nx, -ny, -nz);
    }

    private static void vertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x, float y, float z,
            float u, float v,
            int light,
            int color,
            float nx, float ny, float nz
    ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private static void addTexturedBox(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            int light,
            int color,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ
    ) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // bottom
        addQuadDoubleSided(
                consumer, pose, light, color,
                minX, minY, maxZ, u0, v0,
                maxX, minY, maxZ, u1, v0,
                maxX, minY, minZ, u1, v1,
                minX, minY, minZ, u0, v1,
                0.0f, -1.0f, 0.0f
        );

        // top
        addQuadDoubleSided(
                consumer, pose, light, color,
                minX, maxY, minZ, u0, v0,
                maxX, maxY, minZ, u1, v0,
                maxX, maxY, maxZ, u1, v1,
                minX, maxY, maxZ, u0, v1,
                0.0f, 1.0f, 0.0f
        );

        // north
        addQuadDoubleSided(
                consumer, pose, light, color,
                minX, minY, minZ, u0, v1,
                maxX, minY, minZ, u1, v1,
                maxX, maxY, minZ, u1, v0,
                minX, maxY, minZ, u0, v0,
                0.0f, 0.0f, -1.0f
        );

        // south
        addQuadDoubleSided(
                consumer, pose, light, color,
                maxX, minY, maxZ, u0, v1,
                minX, minY, maxZ, u1, v1,
                minX, maxY, maxZ, u1, v0,
                maxX, maxY, maxZ, u0, v0,
                0.0f, 0.0f, 1.0f
        );

        // west
        addQuadDoubleSided(
                consumer, pose, light, color,
                minX, minY, maxZ, u0, v1,
                minX, minY, minZ, u1, v1,
                minX, maxY, minZ, u1, v0,
                minX, maxY, maxZ, u0, v0,
                -1.0f, 0.0f, 0.0f
        );

        // east
        addQuadDoubleSided(
                consumer, pose, light, color,
                maxX, minY, minZ, u0, v1,
                maxX, minY, maxZ, u1, v1,
                maxX, maxY, maxZ, u1, v0,
                maxX, maxY, minZ, u0, v0,
                1.0f, 0.0f, 0.0f
        );
    }

    @Override
    public MixingVatRenderState createRenderState() {
        return new MixingVatRenderState();
    }

    @Override
    public void extractRenderState(
            MixingVatBlockEntity blockEntity,
            MixingVatRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        renderState.fluid = Fluids.EMPTY;
        renderState.fluidStillTexture = null;
        renderState.fluidTint = 0xFFFFFFFF;

        renderState.stirPhase = blockEntity.getStirAnimationProgress(partialTick);
        renderState.showSpatula = blockEntity.hasContentsToMix() || renderState.stirPhase > 0.0f;

        ResourceLocation fluidId = blockEntity.getVisualFluidId();
        if (fluidId != null) {
            Fluid fluid = BuiltInRegistries.FLUID.getValue(fluidId);
            if (fluid != null && fluid != Fluids.EMPTY && blockEntity.getLevel() != null) {
                renderState.fluid = fluid;

                var fluidState = fluid.defaultFluidState();
                var ext = IClientFluidTypeExtensions.of(fluidState);

                renderState.fluidStillTexture = ext.getStillTexture(
                        fluidState,
                        blockEntity.getLevel(),
                        blockEntity.getBlockPos()
                );
                renderState.fluidTint = ext.getTintColor(
                        fluidState,
                        blockEntity.getLevel(),
                        blockEntity.getBlockPos()
                );
            }
        }

        renderState.fluidRatio = blockEntity.getVisualFluidRatio();
        renderState.hasFluid =
                renderState.fluid != Fluids.EMPTY &&
                        renderState.fluidRatio > 0.0f &&
                        renderState.fluidStillTexture != null;

        List<ItemStack> visualItems = blockEntity.getVisualItems();
        for (int i = 0; i < renderState.items.length; i++) {
            renderState.items[i].clear();

            ItemStack stack = i < visualItems.size() ? visualItems.get(i) : ItemStack.EMPTY;
            if (!stack.isEmpty()) {
                this.itemModelResolver.updateForTopItem(
                        renderState.items[i],
                        stack,
                        ItemDisplayContext.FIXED,
                        blockEntity.getLevel(),
                        null,
                        0
                );
            }
        }
    }

    @Override
    public void submit(
            MixingVatRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {
        if (renderState.hasFluid) {
            submitFluid(renderState, poseStack, collector);
        }

        float[][] positions = switch (countNonEmpty(renderState)) {
            case 1 -> new float[][]{{0.5f, 0.5f}};
            case 2 -> new float[][]{{0.35f, 0.5f}, {0.65f, 0.5f}};
            case 3 -> new float[][]{{0.35f, 0.35f}, {0.65f, 0.35f}, {0.5f, 0.65f}};
            default -> new float[][]{{0.35f, 0.35f}, {0.65f, 0.35f}, {0.35f, 0.65f}, {0.65f, 0.65f}};
        };

        float y = renderState.hasFluid ? 10.0f / 16.0f : 5.5f / 16.0f;
        int posIndex = 0;

        for (int i = 0; i < renderState.items.length; i++) {
            if (renderState.items[i].isEmpty()) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(positions[posIndex][0], y, positions[posIndex][1]);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(10.0f * posIndex));
            poseStack.scale(0.35f, 0.35f, 0.35f);

            renderState.items[i].submit(
                    poseStack,
                    collector,
                    renderState.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0
            );

            poseStack.popPose();
            posIndex++;
        }

        submitMixerArm(renderState, poseStack, collector);
    }

    private void submitMixerArm(MixingVatRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector) {
        if (!renderState.showSpatula) {
            return;
        }

        TextureAtlasSprite sprite = this.materials.get(MIXING_ARM_MATERIAL);
        int light = renderState.lightCoords;
        int color = 0xFFFFFFFF;

        float angle = renderState.stirPhase * 360.0f;

        // geometry size
        float length = 0.95f;         // total rod length
        float halfWidth = 0.06f;      // X half-size
        float halfThickness = 0.02f;  // Z half-size

        // fixed pivot point at the vat bottom center
        float pivotX = 0.5f;
        float pivotY = FLUID_FLOOR_Y + 0.01f;
        float pivotZ = 0.5f;

        poseStack.pushPose();
        poseStack.translate(pivotX, pivotY, pivotZ);

        // spin around the vat
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        // lean outward so the free end goes outside the vat
        poseStack.mulPose(Axis.ZP.rotationDegrees(-15.0f));

        // optional twist around its own axis
        // poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));

        collector.order(1).submitCustomGeometry(
                poseStack,
                RenderType.solid(), // use cutout() / cutoutMipped() if your texture needs alpha
                (pose, consumer) -> addTexturedBox(
                        consumer,
                        pose,
                        sprite,
                        light,
                        color,
                        -halfWidth, 0.0f, -halfThickness,
                        halfWidth, length, halfThickness
                )
        );

        poseStack.popPose();
    }

    private void submitFluid(MixingVatRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector) {
        if (renderState.fluidStillTexture == null) {
            return;
        }

        TextureAtlasSprite sprite = this.materials.get(
                new Material(TextureAtlas.LOCATION_BLOCKS, renderState.fluidStillTexture)
        );

        int color = renderState.fluidTint;
        int light = renderState.lightCoords;

        float minX = INNER_MIN;
        float maxX = INNER_MAX;
        float minZ = INNER_MIN;
        float maxZ = INNER_MAX;
        float minY = FLUID_FLOOR_Y;
        float maxY = Mth.lerp(renderState.fluidRatio, FLUID_FLOOR_Y, FLUID_MAX_Y);

        if (maxY <= minY + 0.001f) {
            return;
        }

        collector.order(-1).submitCustomGeometry(
                poseStack,
                RenderType.translucentMovingBlock(),
                (pose, consumer) -> {
                    float u0 = sprite.getU0();
                    float u1 = sprite.getU1();
                    float v0 = sprite.getV0();
                    float v1 = sprite.getV1();

                    addQuadDoubleSided(
                            consumer, pose, light, color,
                            minX, maxY, minZ, u0, v0,
                            minX, maxY, maxZ, u0, v1,
                            maxX, maxY, maxZ, u1, v1,
                            maxX, maxY, minZ, u1, v0,
                            0.0f, 1.0f, 0.0f
                    );

                    addQuadDoubleSided(
                            consumer, pose, light, color,
                            minX, minY, minZ, u0, v1,
                            maxX, minY, minZ, u1, v1,
                            maxX, maxY, minZ, u1, v0,
                            minX, maxY, minZ, u0, v0,
                            0.0f, 0.0f, -1.0f
                    );

                    addQuadDoubleSided(
                            consumer, pose, light, color,
                            maxX, minY, maxZ, u0, v1,
                            minX, minY, maxZ, u1, v1,
                            minX, maxY, maxZ, u1, v0,
                            maxX, maxY, maxZ, u0, v0,
                            0.0f, 0.0f, 1.0f
                    );

                    addQuadDoubleSided(
                            consumer, pose, light, color,
                            minX, minY, maxZ, u0, v1,
                            minX, minY, minZ, u1, v1,
                            minX, maxY, minZ, u1, v0,
                            minX, maxY, maxZ, u0, v0,
                            -1.0f, 0.0f, 0.0f
                    );

                    addQuadDoubleSided(
                            consumer, pose, light, color,
                            maxX, minY, minZ, u0, v1,
                            maxX, minY, maxZ, u1, v1,
                            maxX, maxY, maxZ, u1, v0,
                            maxX, maxY, minZ, u0, v0,
                            1.0f, 0.0f, 0.0f
                    );
                }
        );
    }
}