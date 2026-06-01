package org.mydrugs.mydrugs.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;
import org.mydrugs.mydrugs.dimension.inner.InnerSceneType;
import org.mydrugs.mydrugs.dimension.inner.InnerTerrain;

/**
 * Phase 7 set piece — the mirror-lake reflection. When the player stands at a {@code MIRROR_LAKE},
 * a translucent silhouette is drawn on the water that represents the player's <em>recovery state</em>
 * rather than their model: a demon-tinted, restless red shape during a bad trip or heavy withdrawal
 * (read from {@link AddictionClientState#badTripActive} / {@link AddictionClientState#globalSeverity},
 * already synced to the client), easing to a serene blue-white light when clean.
 *
 * <p>An approximation rather than a true planar reflection: a camera-facing billboard sitting just
 * below the lake surface, which reads as a reflection from player eye height without the cost and
 * fragility of a second render pass. Uses the same depth-test-no-write translucent render type as the
 * sky so it never culls terrain. No-ops outside the dimension / away from a mirror lake.
 */
@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class InnerMirrorReflectionRenderer {
    private static final ByteBufferBuilder BUFFER = new ByteBufferBuilder(8 * 1024);

    private InnerMirrorReflectionRenderer() {
    }

    @SubscribeEvent
    public static void onAfterParticles(RenderLevelStageEvent.AfterParticles event) {
        Minecraft mc = Minecraft.getInstance();
        if (!InnerAtmosphereClient.inInnerDimension(mc)) {
            return;
        }
        InnerAtmosphere.Sample atmosphere = InnerAtmosphereClient.current(mc);
        PoseStack poseStack = event.getPoseStack();
        if (atmosphere == null || poseStack == null || atmosphere.sceneType() != InnerSceneType.MIRROR_LAKE) {
            return;
        }
        Player player = mc.player;
        int bx = (int) Math.floor(player.getX());
        int bz = (int) Math.floor(player.getZ());
        int cx = InnerTerrain.slotCenter(bx);
        int cz = InnerTerrain.slotCenter(bz);
        InnerTerrain.Sample terrain = InnerTerrain.sample(cx, cz, bx, bz);
        if (!terrain.lake() && terrain.lakeStrength() <= 0.35D) {
            return;
        }

        double surfaceY = (terrain.lake() ? terrain.lakeSurfaceY() : terrain.topY());
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cam = camera.getPosition();

        // Recovery state -> colour. Demon red while unwell, serene blue-white when clean.
        float severity = Mth.clamp(AddictionClientState.globalSeverity, 0.0F, 1.0F);
        boolean unwell = AddictionClientState.badTripActive || severity > 0.45F;
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float t = (mc.level.getGameTime() + partial);
        float restless = unwell ? (0.85F + 0.15F * Mth.sin(t * 0.25F)) : 1.0F;

        int r;
        int g;
        int b;
        if (unwell) {
            r = 200;
            g = (int) (40 + (1.0F - severity) * 40);
            b = (int) (40 + (1.0F - severity) * 30);
        } else {
            r = 200;
            g = 230;
            b = 255;
        }
        int alpha = (int) ((unwell ? 120 + severity * 90 : 90) * restless);

        // World position of the reflection: under the player at the lake surface.
        float wx = (float) (player.getX() - cam.x);
        float wy = (float) (surfaceY - 0.05D - cam.y);
        float wz = (float) (player.getZ() - cam.z);

        Vector3f up = new Vector3f(camera.getUpVector());
        Vector3f left = new Vector3f(camera.getLeftVector());

        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(BUFFER);
        VertexConsumer consumer = buffers.getBuffer(RenderType.debugStructureQuads());

        // Tall, narrow silhouette hanging below the surface (a reflection of a standing figure).
        float halfW = 0.45F;
        float height = 1.9F;
        float cy = wy - height * 0.5F;
        billboard(consumer, matrix, up, left, wx, cy, wz, halfW, height * 0.5F, r, g, b, alpha);

        buffers.endBatch();
    }

    private static void billboard(
            VertexConsumer consumer,
            Matrix4f matrix,
            Vector3f up,
            Vector3f left,
            float cx, float cy, float cz,
            float halfW, float halfH,
            int r, int g, int b, int a
    ) {
        if (a <= 0) {
            return;
        }
        float ux = up.x() * halfH;
        float uy = up.y() * halfH;
        float uz = up.z() * halfH;
        float lx = left.x() * halfW;
        float ly = left.y() * halfW;
        float lz = left.z() * halfW;
        consumer.addVertex(matrix, cx - lx - ux, cy - ly - uy, cz - lz - uz).setColor(r, g, b, a);
        consumer.addVertex(matrix, cx - lx + ux, cy - ly + uy, cz - lz + uz).setColor(r, g, b, a);
        consumer.addVertex(matrix, cx + lx + ux, cy + ly + uy, cz + lz + uz).setColor(r, g, b, a);
        consumer.addVertex(matrix, cx + lx - ux, cy + ly - uy, cz + lz - uz).setColor(r, g, b, a);
    }
}
