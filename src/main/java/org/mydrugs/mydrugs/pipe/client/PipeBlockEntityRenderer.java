package org.mydrugs.mydrugs.pipe.client;

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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.pipe.PipeConnectionMode;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.PipeTier;
import org.mydrugs.mydrugs.pipe.blockentity.PipeBlockEntity;

import java.util.EnumMap;
import java.util.Map;

public class PipeBlockEntityRenderer implements BlockEntityRenderer<PipeBlockEntity, PipeRenderState> {
    private static final Map<PipeResourceKind, Map<PipeTier, ResourceLocation>> FRAME_TEXTURES;
    private static final Map<PipeResourceKind, ResourceLocation> GLASS_TEXTURES;

    static {
        FRAME_TEXTURES = new EnumMap<>(PipeResourceKind.class);
        GLASS_TEXTURES = new EnumMap<>(PipeResourceKind.class);
        for (PipeResourceKind kind : PipeResourceKind.values()) {
            Map<PipeTier, ResourceLocation> tierMap = new EnumMap<>(PipeTier.class);
            for (PipeTier tier : PipeTier.values()) {
                String frameName = "pipe_frame_" + tier.name().toLowerCase() + "_" + kind.name().toLowerCase();
                tierMap.put(tier, ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "block/pipe/" + frameName));
            }
            FRAME_TEXTURES.put(kind, tierMap);

            String glassName = "pipe_glass_" + kind.name().toLowerCase();
            GLASS_TEXTURES.put(kind, ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "block/pipe/" + glassName));
        }
    }

    private static final ResourceLocation PIPE_INPUT_INDICATOR =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "block/pipe/pipe_input_indicator");
    private static final ResourceLocation PIPE_OUTPUT_INDICATOR =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "block/pipe/pipe_output_indicator");
    private static final ResourceLocation PIPE_FILTER_BAND =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "block/pipe/pipe_filter_band");

    private static final int WHITE = 0xFFFFFFFF;
    private static final int GLASS_TINT = 0xB8FFFFFF;

    private static final float EPS = 0.001F;
    private static final float OUTER_MIN = 4f / 16f;
    private static final float OUTER_MAX = 12f / 16f;
    private static final float INNER_MIN = 5f / 16f;
    private static final float INNER_MAX = 11f / 16f;
    private static final float COLLAR_MIN = 3f / 16f;
    private static final float COLLAR_MAX = 13f / 16f;
    private static final float COLLAR_INNER_MIN = 5f / 16f;
    private static final float COLLAR_INNER_MAX = 11f / 16f;
    private static final float RAIL = 1f / 16f;
    private static final float GLASS_PANE = 0.25f / 16f;

    private static final TextureSlice FULL_TEXTURE = new TextureSlice(0.0F, 1.0F);
    private static final TextureSlice FRAME_NEGATIVE_END = new TextureSlice(0.0F, 1.0F / 3.0F);
    private static final TextureSlice FRAME_CENTER = new TextureSlice(1.0F / 3.0F, 2.0F / 3.0F);
    private static final TextureSlice FRAME_POSITIVE_END = new TextureSlice(2.0F / 3.0F, 1.0F);

    private final MaterialSet materials;

    public PipeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.materials = context.materials();
    }

    @Override
    public PipeRenderState createRenderState() {
        return new PipeRenderState();
    }

    @Override
    public void extractRenderState(
            PipeBlockEntity blockEntity,
            PipeRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);
        renderState.sideModes.clear();
        renderState.filteredSides.clear();
        renderState.kind = blockEntity.kind();
        renderState.tier = blockEntity.tier();

        blockEntity.copySideConfigs().forEach((dir, cfg) -> renderState.sideModes.put(dir, cfg.mode()));
        for (Direction dir : Direction.values()) {
            if (blockEntity.hasFilter(dir)) {
                renderState.filteredSides.add(dir);
            }
        }
    }

    @Override
    public void submit(
            PipeRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {
        TextureAtlasSprite frameSprite = this.sprite(frameTexture(renderState.kind, renderState.tier));
        TextureAtlasSprite glassSprite = this.sprite(glassTexture(renderState.kind));
        TextureAtlasSprite inputSprite = this.sprite(PIPE_INPUT_INDICATOR);
        TextureAtlasSprite outputSprite = this.sprite(PIPE_OUTPUT_INDICATOR);
        TextureAtlasSprite filterSprite = this.sprite(PIPE_FILTER_BAND);

        collector.order(-1).submitCustomGeometry(poseStack, RenderType.solid(), (pose, consumer) -> {
            renderCoreFrame(consumer, pose, frameSprite, renderState.lightCoords);

            for (Direction dir : Direction.values()) {
                PipeConnectionMode mode = renderState.sideModes.getOrDefault(dir, PipeConnectionMode.DISABLED);
                if (mode == PipeConnectionMode.DISABLED) {
                    continue;
                }

                renderArmFrame(consumer, pose, frameSprite, dir, mode, renderState.lightCoords);

                if (mode == PipeConnectionMode.INPUT || mode == PipeConnectionMode.OUTPUT) {
                    renderEndpointConnector(consumer, pose, frameSprite, dir, renderState.lightCoords);
                    renderEndpointIndicator(
                            consumer,
                            pose,
                            mode == PipeConnectionMode.INPUT ? inputSprite : outputSprite,
                            dir,
                            renderState.lightCoords
                    );
                }

                if (renderState.filteredSides.contains(dir)) {
                    renderFilterMarker(consumer, pose, filterSprite, dir, renderState.lightCoords);
                }
            }
        });

        collector.order(0).submitCustomGeometry(poseStack, RenderType.cutout(), (pose, consumer) -> {
            for (Direction dir : Direction.values()) {
                if (renderState.sideModes.getOrDefault(dir, PipeConnectionMode.DISABLED) == PipeConnectionMode.DISABLED) {
                    renderCoreGlassPanel(consumer, pose, glassSprite, dir, renderState.lightCoords);
                } else {
                    PipeConnectionMode mode = renderState.sideModes.getOrDefault(dir, PipeConnectionMode.DISABLED);
                    renderArmGlass(consumer, pose, glassSprite, dir, mode, renderState.lightCoords);
                }
            }
        });
    }

    private TextureAtlasSprite sprite(ResourceLocation location) {
        return this.materials.get(new Material(TextureAtlas.LOCATION_BLOCKS, location));
    }

    private static ResourceLocation frameTexture(PipeResourceKind kind, PipeTier tier) {
        PipeResourceKind safeKind = kind == null ? PipeResourceKind.ITEM : kind;
        PipeTier safeTier = tier == null ? PipeTier.BASIC : tier;
        Map<PipeTier, ResourceLocation> tierMap = FRAME_TEXTURES.get(safeKind);
        return tierMap.getOrDefault(safeTier, tierMap.get(PipeTier.BASIC));
    }

    private static ResourceLocation glassTexture(PipeResourceKind kind) {
        return GLASS_TEXTURES.getOrDefault(kind == null ? PipeResourceKind.ITEM : kind, GLASS_TEXTURES.get(PipeResourceKind.ITEM));
    }

    private static void renderCoreFrame(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite, int light) {
        // The center junction is split into corner cubes and shortened edge beams.
        // This avoids drawing three overlapping axis rail sets in the same corner volume.
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    addBox(
                            consumer,
                            pose,
                            sprite,
                            new Box(
                                    cornerMin(x), cornerMin(y), cornerMin(z),
                                    cornerMax(x), cornerMax(y), cornerMax(z)
                            ),
                            FRAME_CENTER,
                            WHITE,
                            light
                    );
                }
            }
        }

        for (Direction.Axis axis : Direction.Axis.values()) {
            addCoreEdgeBeam(consumer, pose, sprite, axis, false, false, light);
            addCoreEdgeBeam(consumer, pose, sprite, axis, false, true, light);
            addCoreEdgeBeam(consumer, pose, sprite, axis, true, false, light);
            addCoreEdgeBeam(consumer, pose, sprite, axis, true, true, light);
        }
    }

    private static void addCoreEdgeBeam(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            Direction.Axis axis,
            boolean highA,
            boolean highB,
            int light
    ) {
        addBox(
                consumer,
                pose,
                sprite,
                boxForAxis(
                        axis,
                        INNER_MIN,
                        INNER_MAX,
                        highA ? OUTER_MAX - RAIL : OUTER_MIN,
                        highA ? OUTER_MAX : OUTER_MIN + RAIL,
                        highB ? OUTER_MAX - RAIL : OUTER_MIN,
                        highB ? OUTER_MAX : OUTER_MIN + RAIL
                ),
                FRAME_CENTER,
                WHITE,
                light
        );
    }

    private static void renderArmFrame(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            Direction direction,
            PipeConnectionMode mode,
            int light
    ) {
        Direction.Axis axis = direction.getAxis();
        AxisRange range = mode == PipeConnectionMode.PIPE ? pipeArmRange(direction) : endpointBridgeRange(direction);
        TextureSlice slice = frameSlice(direction);

        addOpenAxisBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), OUTER_MIN, OUTER_MIN + RAIL, OUTER_MIN, OUTER_MIN + RAIL), axis, slice, WHITE, light);
        addOpenAxisBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), OUTER_MAX - RAIL, OUTER_MAX, OUTER_MIN, OUTER_MIN + RAIL), axis, slice, WHITE, light);
        addOpenAxisBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), OUTER_MIN, OUTER_MIN + RAIL, OUTER_MAX - RAIL, OUTER_MAX), axis, slice, WHITE, light);
        addOpenAxisBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), OUTER_MAX - RAIL, OUTER_MAX, OUTER_MAX - RAIL, OUTER_MAX), axis, slice, WHITE, light);
    }

    private static void renderArmGlass(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            Direction direction,
            PipeConnectionMode mode,
            int light
    ) {
        Direction.Axis axis = direction.getAxis();
        AxisRange range = mode == PipeConnectionMode.PIPE ? pipeArmRange(direction) : endpointBridgeRange(direction);

        addOpenAxisBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), INNER_MIN, INNER_MAX, OUTER_MIN + RAIL, OUTER_MIN + RAIL + GLASS_PANE), axis, FULL_TEXTURE, GLASS_TINT, light);
        addOpenAxisBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), INNER_MIN, INNER_MAX, OUTER_MAX - RAIL - GLASS_PANE, OUTER_MAX - RAIL), axis, FULL_TEXTURE, GLASS_TINT, light);
        addOpenAxisBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), OUTER_MIN + RAIL, OUTER_MIN + RAIL + GLASS_PANE, INNER_MIN, INNER_MAX), axis, FULL_TEXTURE, GLASS_TINT, light);
        addOpenAxisBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), OUTER_MAX - RAIL - GLASS_PANE, OUTER_MAX - RAIL, INNER_MIN, INNER_MAX), axis, FULL_TEXTURE, GLASS_TINT, light);
    }

    private static void renderCoreGlassPanel(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            Direction direction,
            int light
    ) {
        Direction.Axis axis = direction.getAxis();
        AxisRange range = facePaneRange(direction);
        addBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), INNER_MIN, INNER_MAX, INNER_MIN, INNER_MAX), FULL_TEXTURE, GLASS_TINT, light);
    }

    private static void renderEndpointConnector(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            Direction direction,
            int light
    ) {
        Direction.Axis axis = direction.getAxis();
        AxisRange range = endpointRange(direction);
        TextureSlice slice = frameSlice(direction);

        addBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), COLLAR_MIN, COLLAR_MAX, COLLAR_MIN, COLLAR_INNER_MIN), slice, WHITE, light);
        addBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), COLLAR_MIN, COLLAR_MAX, COLLAR_INNER_MAX, COLLAR_MAX), slice, WHITE, light);
        addBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), COLLAR_MIN, COLLAR_INNER_MIN, COLLAR_INNER_MIN, COLLAR_INNER_MAX), slice, WHITE, light);
        addBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), COLLAR_INNER_MAX, COLLAR_MAX, COLLAR_INNER_MIN, COLLAR_INNER_MAX), slice, WHITE, light);
    }

    private static void renderEndpointIndicator(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            Direction direction,
            int light
    ) {
        Direction.Axis axis = direction.getAxis();
        AxisRange range = outwardInsetRange(direction);
        addBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), INNER_MIN, INNER_MAX, INNER_MIN, INNER_MAX), FULL_TEXTURE, WHITE, light);
    }

    private static void renderFilterMarker(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            Direction direction,
            int light
    ) {
        Direction.Axis axis = direction.getAxis();
        AxisRange range = filterRange(direction);
        addBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), OUTER_MIN, OUTER_MAX, OUTER_MIN, OUTER_MIN + RAIL), FULL_TEXTURE, WHITE, light);
        addBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), OUTER_MIN, OUTER_MAX, OUTER_MAX - RAIL, OUTER_MAX), FULL_TEXTURE, WHITE, light);
        addBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), OUTER_MIN, OUTER_MIN + RAIL, INNER_MIN, INNER_MAX), FULL_TEXTURE, WHITE, light);
        addBox(consumer, pose, sprite, boxForAxis(axis, range.min(), range.max(), OUTER_MAX - RAIL, OUTER_MAX, INNER_MIN, INNER_MAX), FULL_TEXTURE, WHITE, light);
    }

    private static AxisRange pipeArmRange(Direction direction) {
        return direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE
                ? new AxisRange(0.0F, OUTER_MIN)
                : new AxisRange(OUTER_MAX, 1.0F);
    }

    private static AxisRange endpointBridgeRange(Direction direction) {
        return direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE
                ? new AxisRange(2f / 16f, OUTER_MIN)
                : new AxisRange(OUTER_MAX, 14f / 16f);
    }

    private static AxisRange endpointRange(Direction direction) {
        return direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE
                ? new AxisRange(EPS, 2f / 16f)
                : new AxisRange(14f / 16f, 1.0F - EPS);
    }

    private static AxisRange outwardInsetRange(Direction direction) {
        return direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE
                ? new AxisRange(EPS, 0.5f / 16f)
                : new AxisRange(15.5f / 16f, 1.0F - EPS);
    }

    private static AxisRange filterRange(Direction direction) {
        return direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE
                ? new AxisRange(2f / 16f, 3f / 16f)
                : new AxisRange(13f / 16f, 14f / 16f);
    }

    private static AxisRange facePaneRange(Direction direction) {
        float pane = GLASS_PANE;
        return direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE
                ? new AxisRange(OUTER_MIN, OUTER_MIN + pane)
                : new AxisRange(OUTER_MAX - pane, OUTER_MAX);
    }

    private static float cornerMin(int high) {
        return high == 0 ? OUTER_MIN : OUTER_MAX - RAIL;
    }

    private static float cornerMax(int high) {
        return high == 0 ? OUTER_MIN + RAIL : OUTER_MAX;
    }

    private static TextureSlice frameSlice(Direction direction) {
        return direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? FRAME_NEGATIVE_END : FRAME_POSITIVE_END;
    }

    private static Box boxForAxis(
            Direction.Axis axis,
            float axisMin,
            float axisMax,
            float crossAMin,
            float crossAMax,
            float crossBMin,
            float crossBMax
    ) {
        return switch (axis) {
            case X -> new Box(axisMin, crossAMin, crossBMin, axisMax, crossAMax, crossBMax);
            case Y -> new Box(crossAMin, axisMin, crossBMin, crossAMax, axisMax, crossBMax);
            case Z -> new Box(crossAMin, crossBMin, axisMin, crossAMax, crossBMax, axisMax);
        };
    }

    private static void addOpenAxisBox(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            Box box,
            Direction.Axis openAxis,
            TextureSlice texture,
            int color,
            int light
    ) {
        addBox(consumer, pose, sprite, box, texture, color, light, openAxis);
    }

    private static void addBox(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            Box box,
            TextureSlice texture,
            int color,
            int light
    ) {
        addBox(consumer, pose, sprite, box, texture, color, light, null);
    }

    private static void addBox(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            Box box,
            TextureSlice texture,
            int color,
            int light,
            @Nullable Direction.Axis openAxis
    ) {
        if (openAxis != Direction.Axis.Y) {
            addQuad(consumer, pose, sprite, texture, color, light, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, 0, 1, 0);
            addQuad(consumer, pose, sprite, texture, color, light, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, box.maxX, box.minY, box.minZ, box.minX, box.minY, box.minZ, 0, -1, 0);
        }
        if (openAxis != Direction.Axis.Z) {
            addQuad(consumer, pose, sprite, texture, color, light, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, box.minX, box.maxY, box.minZ, 0, 0, -1);
            addQuad(consumer, pose, sprite, texture, color, light, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, 0, 0, 1);
        }
        if (openAxis != Direction.Axis.X) {
            addQuad(consumer, pose, sprite, texture, color, light, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, -1, 0, 0);
            addQuad(consumer, pose, sprite, texture, color, light, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ, 1, 0, 0);
        }
    }

    private static void addQuad(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
            TextureSlice texture,
            int color,
            int light,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            float normalX,
            float normalY,
            float normalZ
    ) {
        float u0 = lerp(sprite.getU0(), sprite.getU1(), texture.minU());
        float u1 = lerp(sprite.getU0(), sprite.getU1(), texture.maxU());
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        addQuadOneSided(
                consumer, pose, color, light,
                x1, y1, z1, u0, v0,
                x2, y2, z2, u1, v0,
                x3, y3, z3, u1, v1,
                x4, y4, z4, u0, v1,
                normalX, normalY, normalZ
        );

        addQuadOneSided(
                consumer, pose, color, light,
                x4, y4, z4, u0, v1,
                x3, y3, z3, u1, v1,
                x2, y2, z2, u1, v0,
                x1, y1, z1, u0, v0,
                -normalX, -normalY, -normalZ
        );
    }

    private static void addQuadOneSided(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            int color,
            int light,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            float x4, float y4, float z4, float u4, float v4,
            float normalX,
            float normalY,
            float normalZ
    ) {
        vertex(consumer, pose, x1, y1, z1, u1, v1, color, light, normalX, normalY, normalZ);
        vertex(consumer, pose, x2, y2, z2, u2, v2, color, light, normalX, normalY, normalZ);
        vertex(consumer, pose, x3, y3, z3, u3, v3, color, light, normalX, normalY, normalZ);
        vertex(consumer, pose, x4, y4, z4, u4, v4, color, light, normalX, normalY, normalZ);
    }

    private static void vertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int color,
            int light,
            float normalX,
            float normalY,
            float normalZ
    ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, normalX, normalY, normalZ);
    }

    private static float lerp(float min, float max, float amount) {
        return min + (max - min) * amount;
    }

    private record AxisRange(float min, float max) {}

    private record TextureSlice(float minU, float maxU) {}

    private record Box(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {}
}
