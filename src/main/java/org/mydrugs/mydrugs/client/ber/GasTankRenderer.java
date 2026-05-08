package org.mydrugs.mydrugs.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.entity.GasTankBlockEntity;

public class GasTankRenderer implements BlockEntityRenderer<GasTankBlockEntity, GasTankRenderState> {
    private static final Material GAS_MATERIAL = new Material(
            TextureAtlas.LOCATION_BLOCKS,
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "block/gas_tank_gas")
    );

    private final MaterialSet materials;

    public GasTankRenderer(BlockEntityRendererProvider.Context context) {
        this.materials = context.materials();
    }

    @Override
    public GasTankRenderState createRenderState() {
        return new GasTankRenderState();
    }

    @Override
    public void extractRenderState(
            GasTankBlockEntity blockEntity,
            GasTankRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        var gas = blockEntity.getVisualGasType();
        renderState.gasRatio = blockEntity.getVisualGasRatio();
        renderState.hasGas = gas != null && blockEntity.getVisualGasAmount() > 0 && renderState.gasRatio > 0.0F;
        renderState.gasTint = gas == null ? 0xFFFFFFFF : gas.tint();
    }

    @Override
    public void submit(GasTankRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (!renderState.hasGas) {
            return;
        }

        TextureAtlasSprite sprite = this.materials.get(GAS_MATERIAL);
        int rgb = renderState.gasTint & 0x00FFFFFF;
        int alpha = 70 + Math.round(Mth.clamp(renderState.gasRatio, 0.0F, 1.0F) * 92.0F);
        int color = (alpha << 24) | rgb;

        float min = 0.215F;
        float max = 0.785F;
        float minY = 0.175F;
        float maxY = 0.825F;

        collector.order(-2).submitCustomGeometry(
                poseStack,
                RenderType.translucentMovingBlock(),
                (pose, consumer) -> {
                    addGasBox(consumer, pose, sprite, renderState.lightCoords, color, min, minY, min, max, maxY, max);
                    addGasInnerFaces(consumer, pose, sprite, renderState.lightCoords, color, min, minY, min, max, maxY, max);
                }
        );
    }

    private static void addGasBox(
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

        addQuadDoubleSided(consumer, pose, light, color,
                minX, minY, minZ, u0, v1,
                maxX, minY, minZ, u1, v1,
                maxX, maxY, minZ, u1, v0,
                minX, maxY, minZ, u0, v0,
                0.0F, 0.0F, -1.0F);
        addQuadDoubleSided(consumer, pose, light, color,
                maxX, minY, maxZ, u0, v1,
                minX, minY, maxZ, u1, v1,
                minX, maxY, maxZ, u1, v0,
                maxX, maxY, maxZ, u0, v0,
                0.0F, 0.0F, 1.0F);
        addQuadDoubleSided(consumer, pose, light, color,
                minX, minY, maxZ, u0, v1,
                minX, minY, minZ, u1, v1,
                minX, maxY, minZ, u1, v0,
                minX, maxY, maxZ, u0, v0,
                -1.0F, 0.0F, 0.0F);
        addQuadDoubleSided(consumer, pose, light, color,
                maxX, minY, minZ, u0, v1,
                maxX, minY, maxZ, u1, v1,
                maxX, maxY, maxZ, u1, v0,
                maxX, maxY, minZ, u0, v0,
                1.0F, 0.0F, 0.0F);
        addQuadDoubleSided(consumer, pose, light, color,
                minX, maxY, minZ, u0, v0,
                maxX, maxY, minZ, u1, v0,
                maxX, maxY, maxZ, u1, v1,
                minX, maxY, maxZ, u0, v1,
                0.0F, 1.0F, 0.0F);
        addQuadDoubleSided(consumer, pose, light, color,
                minX, minY, maxZ, u0, v1,
                maxX, minY, maxZ, u1, v1,
                maxX, minY, minZ, u1, v0,
                minX, minY, minZ, u0, v0,
                0.0F, -1.0F, 0.0F);
    }

    private static void addGasInnerFaces(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            int light,
            int color,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ
    ) {
        int highlight = ((Math.min(205, ((color >>> 24) & 0xFF) + 32)) << 24) | (color & 0x00FFFFFF);
        float inset = 0.018F;
        addQuadDoubleSided(consumer, pose, light, highlight,
                minX + inset, maxY - inset, minZ + inset, sprite.getU0(), sprite.getV0(),
                maxX - inset, maxY - inset, minZ + inset, sprite.getU1(), sprite.getV0(),
                maxX - inset, maxY - inset, maxZ - inset, sprite.getU1(), sprite.getV1(),
                minX + inset, maxY - inset, maxZ - inset, sprite.getU0(), sprite.getV1(),
                0.0F, 1.0F, 0.0F);
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
}
