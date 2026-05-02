package org.mydrugs.mydrugs.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.FluidPumpBlock;
import org.mydrugs.mydrugs.blocks.entity.FluidPumpBlockEntity;

/**
 * Renders the animated hand-crank on top of a Fluid Pump block.
 *
 * <p>Geometry is entirely code-generated: three axis-aligned boxes in a
 * "crank-local" coordinate frame where +Z is the outward normal of the
 * mounted face.  The crank arm + grip rotate around that +Z axis.
 */
public final class FluidPumpRenderer implements BlockEntityRenderer<FluidPumpBlockEntity, FluidPumpRenderState> {

    /** Reuses the white atlas sprite that the pipe renderer already registers. */
    private static final ResourceLocation WHITE_SPRITE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "block/pipe/pipe_debug_white");

    // ── Geometry (all coordinates in fractions of a block; origin = face centre) ─────────
    //   +Z = outward from the mounted face,  +Y = up,  +X = right
    //   The shaft starts -0.5 px inside the block to avoid Z-fighting with the face texture.

    /** Short stub that anchors the crank to the block face (2 × 2 × 4.5 px). */
    private static final Box SHAFT = new Box(
            -1f / 16f, -1f / 16f, -0.5f / 16f,
             1f / 16f,  1f / 16f,  4f  / 16f
    );

    /**
     * The rotating arm (2 × 7 × 2 px).
     * At {@code animAngle = 0°} the arm points straight up (+Y).
     * It overlaps the shaft slightly at the back so there is no gap.
     */
    private static final Box ARM = new Box(
            -1f / 15.8f, 0f,       1f / 15.8f,
             1f / 15.8f, 7f / 15.8f, 3f / 15.8f
    );

    /** Wider grip at the tip of the arm (4 × 3 × 4 px). */
    private static final Box GRIP = new Box(
            -2f / 16f, 6f / 16f, 0f,
             2f / 16f, 9f / 16f, 4f / 16f
    );

    // ── Colours ───────────────────────────────────────────────────────────────────────────
    private static final int SHAFT_COLOR = 0xFFCCCCCC;  // polished iron
    private static final int ARM_COLOR   = 0xFFAAAAAA;  // iron
    private static final int GRIP_COLOR  = 0xFF9B6B2B;  // wooden handle

    /** Ticks for the arm to complete one full 360° revolution. */
    private static final float ANIM_TICKS = 9.0f;

    private final MaterialSet materials;

    public FluidPumpRenderer(BlockEntityRendererProvider.Context context) {
        this.materials = context.materials();
    }

    // ── BlockEntityRenderer ───────────────────────────────────────────────────────────────

    @Override
    public FluidPumpRenderState createRenderState() {
        return new FluidPumpRenderState();
    }

    @Override
    public void extractRenderState(
            FluidPumpBlockEntity blockEntity,
            FluidPumpRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        renderState.hasCrank  = blockEntity.getBlockState().getValue(FluidPumpBlock.CRANK);
        renderState.crankFace = blockEntity.getBlockState().getValue(FluidPumpBlock.CRANK_FACE);

        // Compute the angle freshly each frame from world game time.
        // No client-side persistent state — both client and server share `level.getGameTime()`,
        // so once the BE syncs `lastCrankTick` the elapsed-tick math just works.
        if (!blockEntity.hasCrankAnimation() || blockEntity.getLevel() == null) {
            renderState.animAngle = 0.0f;
            return;
        }

        int currentTick = (int) blockEntity.getLevel().getGameTime();
        // Two's-complement subtraction handles the int wrap-around correctly for any
        // window smaller than 2^31 ticks (~3.4 years), which trivially covers our 9-tick window.
        int elapsedTicks = currentTick - blockEntity.getLastCrankTick();

        if (elapsedTicks < 0 || elapsedTicks >= (int) ANIM_TICKS) {
            // Outside the animation window — arm at rest (angle 0 == 360, looks identical).
            renderState.animAngle = 0.0f;
            return;
        }

        float elapsed = elapsedTicks + partialTick;
        renderState.animAngle = Mth.clamp(elapsed / ANIM_TICKS, 0.0f, 1.0f) * 360.0f;
    }

    @Override
    public void submit(
            FluidPumpRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {
        if (!renderState.hasCrank) return;

        TextureAtlasSprite sprite = materials.get(new Material(TextureAtlas.LOCATION_BLOCKS, WHITE_SPRITE));
        int light = renderState.lightCoords;

        // Translate to the center of the mounted face
        float pivotX = pivotX(renderState.crankFace);
        float pivotZ = pivotZ(renderState.crankFace);

        poseStack.pushPose();
        poseStack.translate(pivotX, 0.5f, pivotZ);

        // Rotate so that +Z_local == outward normal of the mounted face
        applyFaceOrientation(poseStack, renderState.crankFace);

        // Shaft (static, does not spin)
        collector.order(0).submitCustomGeometry(poseStack, RenderType.solid(), (pose, consumer) ->
                addBox(consumer, pose, sprite, SHAFT, SHAFT_COLOR, light)
        );

        // Arm + grip rotate around the shaft axis (+Z in local frame)
        poseStack.mulPose(Axis.ZP.rotationDegrees(renderState.animAngle));

        collector.order(0).submitCustomGeometry(poseStack, RenderType.solid(), (pose, consumer) -> {
            addBox(consumer, pose, sprite, ARM,  ARM_COLOR,  light);
            addBox(consumer, pose, sprite, GRIP, GRIP_COLOR, light);
        });

        poseStack.popPose();
    }

    // ── Face-orientation helpers ──────────────────────────────────────────────────────────

    private static float pivotX(Direction face) {
        return switch (face) {
            case EAST -> 1.0f;
            case WEST -> 0.0f;
            default   -> 0.5f;
        };
    }

    private static float pivotZ(Direction face) {
        return switch (face) {
            case NORTH -> 0.0f;
            case SOUTH -> 1.0f;
            default    -> 0.5f;
        };
    }

    /**
     * Rotates the pose stack so that +Z_local aligns with the outward normal of {@code face}.
     *
     * <p>Default orientation (no rotation) is SOUTH, where +Z is already outward.
     */
    private static void applyFaceOrientation(PoseStack poseStack, Direction face) {
        switch (face) {
            case SOUTH -> { /* already correct */ }
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            default    -> { }
        }
    }

    // ── Geometry helpers ──────────────────────────────────────────────────────────────────

    private static void addBox(
            VertexConsumer consumer, PoseStack.Pose pose,
            TextureAtlasSprite sprite, Box box, int color, int light
    ) {
        // +Y
        addQuad(consumer, pose, sprite, color, light,
                box.minX, box.maxY, box.minZ,  box.maxX, box.maxY, box.minZ,
                box.maxX, box.maxY, box.maxZ,  box.minX, box.maxY, box.maxZ,
                0, 1, 0);
        // -Y
        addQuad(consumer, pose, sprite, color, light,
                box.minX, box.minY, box.maxZ,  box.maxX, box.minY, box.maxZ,
                box.maxX, box.minY, box.minZ,  box.minX, box.minY, box.minZ,
                0, -1, 0);
        // -Z
        addQuad(consumer, pose, sprite, color, light,
                box.minX, box.minY, box.minZ,  box.maxX, box.minY, box.minZ,
                box.maxX, box.maxY, box.minZ,  box.minX, box.maxY, box.minZ,
                0, 0, -1);
        // +Z
        addQuad(consumer, pose, sprite, color, light,
                box.maxX, box.minY, box.maxZ,  box.minX, box.minY, box.maxZ,
                box.minX, box.maxY, box.maxZ,  box.maxX, box.maxY, box.maxZ,
                0, 0, 1);
        // -X
        addQuad(consumer, pose, sprite, color, light,
                box.minX, box.minY, box.maxZ,  box.minX, box.minY, box.minZ,
                box.minX, box.maxY, box.minZ,  box.minX, box.maxY, box.maxZ,
                -1, 0, 0);
        // +X
        addQuad(consumer, pose, sprite, color, light,
                box.maxX, box.minY, box.minZ,  box.maxX, box.minY, box.maxZ,
                box.maxX, box.maxY, box.maxZ,  box.maxX, box.maxY, box.minZ,
                1, 0, 0);
    }

    private static void addQuad(
            VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite,
            int color, int light,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            float nx, float ny, float nz
    ) {
        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();
        // Front winding
        vtx(consumer, pose, color, light, x1, y1, z1, u0, v0, nx, ny, nz);
        vtx(consumer, pose, color, light, x2, y2, z2, u1, v0, nx, ny, nz);
        vtx(consumer, pose, color, light, x3, y3, z3, u1, v1, nx, ny, nz);
        vtx(consumer, pose, color, light, x4, y4, z4, u0, v1, nx, ny, nz);
        // Back winding — prevents solid-render-type backface culling from hiding inner faces
        vtx(consumer, pose, color, light, x4, y4, z4, u0, v1, -nx, -ny, -nz);
        vtx(consumer, pose, color, light, x3, y3, z3, u1, v1, -nx, -ny, -nz);
        vtx(consumer, pose, color, light, x2, y2, z2, u1, v0, -nx, -ny, -nz);
        vtx(consumer, pose, color, light, x1, y1, z1, u0, v0, -nx, -ny, -nz);
    }

    private static void vtx(
            VertexConsumer consumer, PoseStack.Pose pose,
            int color, int light,
            float x, float y, float z, float u, float v,
            float nx, float ny, float nz
    ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private record Box(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {}
}
