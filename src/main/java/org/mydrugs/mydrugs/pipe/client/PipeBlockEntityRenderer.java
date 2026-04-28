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
import net.minecraft.world.inventory.InventoryMenu;
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

    private static final Map<PipeResourceKind, Map<PipeTier, ResourceLocation>> PIPE_TEXTURES;

    static {
        PIPE_TEXTURES = new EnumMap<>(PipeResourceKind.class);
        for (PipeResourceKind kind : PipeResourceKind.values()) {
            Map<PipeTier, ResourceLocation> tierMap = new EnumMap<>(PipeTier.class);
            for (PipeTier tier : PipeTier.values()) {
                String name = tier.name().toLowerCase() + "_" + kind.name().toLowerCase() + "_pipe";
                tierMap.put(tier, ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "block/pipe/" + name));
            }
            PIPE_TEXTURES.put(kind, tierMap);
        }
    }

    private static final ResourceLocation PIPE_DEBUG_WHITE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "block/pipe/pipe_debug_white");

    // INPUT/OUTPUT indicator colors (kept as flat tinted caps for clear UX feedback)
    private static final int INPUT_COLOR  = 0xFF25A7FF;
    private static final int OUTPUT_COLOR = 0xFFFF7A1A;
    private static final int FILTER_COLOR = 0xFFE6D54A;

    private static int bodyColor(PipeResourceKind kind, PipeTier tier) {
        if (kind == null || tier == null) {
            return 0xFF8A8F94;
        }

        return switch (kind) {
            case ITEM -> tier == PipeTier.FAST ? 0xFF8FB0FF : 0xFF6F8CFF;
            case FLUID -> tier == PipeTier.FAST ? 0xFF7EEBFF : 0xFF39C9FF;
            case GAS -> tier == PipeTier.FAST ? 0xFFD0A0FF : 0xFFB46DFF;
        };
    }

    private static int capColor(PipeConnectionMode mode, int bodyColor) {
        return switch (mode) {
            case INPUT -> INPUT_COLOR;
            case OUTPUT -> OUTPUT_COLOR;
            case PIPE -> bodyColor;
            case DISABLED -> 0x00000000;
        };
    }

    private static final Box CORE = new Box(5f/16, 5f/16, 5f/16, 11f/16, 11f/16, 11f/16);

    private static final float EPS = 0.001F;

    private static final Box[] ARM_BOXES = new Box[Direction.values().length];
    private static final Box[] END_CAP_BOXES = new Box[Direction.values().length];
    private static final Box[] FILTER_MARKER_BOXES = new Box[Direction.values().length];
    static {
        // Arms do not overlap end caps. This avoids internal overlapping faces.
        ARM_BOXES[Direction.NORTH.ordinal()] = new Box(5f/16, 5f/16,  2f/16, 11f/16, 11f/16,  5f/16);
        ARM_BOXES[Direction.SOUTH.ordinal()] = new Box(5f/16, 5f/16, 11f/16, 11f/16, 11f/16, 14f/16);
        ARM_BOXES[Direction.WEST.ordinal()]  = new Box( 2f/16, 5f/16,  5f/16,  5f/16, 11f/16, 11f/16);
        ARM_BOXES[Direction.EAST.ordinal()]  = new Box(11f/16, 5f/16,  5f/16, 14f/16, 11f/16, 11f/16);
        ARM_BOXES[Direction.DOWN.ordinal()]  = new Box(5f/16,  2f/16,  5f/16, 11f/16,  5f/16, 11f/16);
        ARM_BOXES[Direction.UP.ordinal()]    = new Box(5f/16, 11f/16,  5f/16, 11f/16, 14f/16, 11f/16);

        // End caps are very close to neighbor blocks, but not exactly coplanar.
        // EPS prevents z-fighting with the adjacent block face.
        END_CAP_BOXES[Direction.NORTH.ordinal()] = new Box(4f/16, 4f/16, EPS,      12f/16, 12f/16,  2f/16);
        END_CAP_BOXES[Direction.SOUTH.ordinal()] = new Box(4f/16, 4f/16, 14f/16,   12f/16, 12f/16, 1f - EPS);
        END_CAP_BOXES[Direction.WEST.ordinal()]  = new Box(EPS,    4f/16, 4f/16,    2f/16, 12f/16, 12f/16);
        END_CAP_BOXES[Direction.EAST.ordinal()]  = new Box(14f/16, 4f/16, 4f/16,   1f - EPS, 12f/16, 12f/16);
        END_CAP_BOXES[Direction.DOWN.ordinal()]  = new Box(4f/16, EPS,    4f/16,   12f/16,  2f/16, 12f/16);
        END_CAP_BOXES[Direction.UP.ordinal()]    = new Box(4f/16, 14f/16, 4f/16,   12f/16, 1f - EPS, 12f/16);

        // Filter markers sit on top of the end cap, not on the block boundary.
        FILTER_MARKER_BOXES[Direction.NORTH.ordinal()] = new Box(6f/16, 12f/16, 0.5f/16, 10f/16, 13f/16, 1.5f/16);
        FILTER_MARKER_BOXES[Direction.SOUTH.ordinal()] = new Box(6f/16, 12f/16, 14.5f/16, 10f/16, 13f/16, 15.5f/16);
        FILTER_MARKER_BOXES[Direction.WEST.ordinal()]  = new Box(0.5f/16, 12f/16, 6f/16, 1.5f/16, 13f/16, 10f/16);
        FILTER_MARKER_BOXES[Direction.EAST.ordinal()]  = new Box(14.5f/16, 12f/16, 6f/16, 15.5f/16, 13f/16, 10f/16);
        FILTER_MARKER_BOXES[Direction.DOWN.ordinal()]  = new Box(6f/16, 0.5f/16, 12f/16, 10f/16, 1.5f/16, 13f/16);
        FILTER_MARKER_BOXES[Direction.UP.ordinal()]    = new Box(6f/16, 14.5f/16, 12f/16, 10f/16, 15.5f/16, 13f/16);
    }

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
            if (blockEntity.hasFilter(dir)) renderState.filteredSides.add(dir);
        }
    }

    @Override
    public void submit(
            PipeRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {
        TextureAtlasSprite sprite = materials.get(new Material(TextureAtlas.LOCATION_BLOCKS, PIPE_DEBUG_WHITE));
        int bodyColor = bodyColor(renderState.kind, renderState.tier);

        collector.order(-1).submitCustomGeometry(poseStack, RenderType.solid(), (pose, consumer) -> {
            // Core: full texture, no tint
            addBox(consumer, pose, sprite, CORE, bodyColor, renderState.lightCoords);

            for (Direction dir : Direction.values()) {
                PipeConnectionMode mode = renderState.sideModes.getOrDefault(dir, PipeConnectionMode.DISABLED);
                if (mode == PipeConnectionMode.DISABLED) {
                    continue;
                }

                addBox(consumer, pose, sprite, ARM_BOXES[dir.ordinal()], bodyColor, renderState.lightCoords);

                addBox(
                        consumer,
                        pose,
                        sprite,
                        END_CAP_BOXES[dir.ordinal()],
                        capColor(mode, bodyColor),
                        renderState.lightCoords
                );

                if (renderState.filteredSides.contains(dir)) {
                    addBox(consumer, pose, sprite, FILTER_MARKER_BOXES[dir.ordinal()], FILTER_COLOR, renderState.lightCoords);
                }
            }
        });
    }

//    private static int capColor(PipeConnectionMode mode) {
//        return switch (mode) {
//            case INPUT    -> INPUT_COLOR;
//            case OUTPUT   -> OUTPUT_COLOR;
//            case PIPE     -> PIPE_COLOR;
//            case DISABLED -> 0x00000000;
//        };
//    }

    private static void addBox(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite, Box box, int color, int light) {
        // +Y face
        addQuad(consumer, pose, sprite, color, light, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ,  0,  1,  0);
        // -Y face
        addQuad(consumer, pose, sprite, color, light, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, box.maxX, box.minY, box.minZ, box.minX, box.minY, box.minZ,  0, -1,  0);
        // -Z face
        addQuad(consumer, pose, sprite, color, light, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, box.minX, box.maxY, box.minZ,  0,  0, -1);
        // +Z face
        addQuad(consumer, pose, sprite, color, light, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ,  0,  0,  1);
        // -X face
        addQuad(consumer, pose, sprite, color, light, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, -1,  0,  0);
        // +X face
        addQuad(consumer, pose, sprite, color, light, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ,  1,  0,  0);
    }

    private static void addQuad(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            TextureAtlasSprite sprite,
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
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Front side
        addQuadOneSided(
                consumer, pose, color, light,
                x1, y1, z1, u0, v0,
                x2, y2, z2, u1, v0,
                x3, y3, z3, u1, v1,
                x4, y4, z4, u0, v1,
                normalX, normalY, normalZ
        );

        // Back side, reversed winding.
        // This prevents RenderType.solid() culling from making one face invisible.
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

    private record Box(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {}
}
