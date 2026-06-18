package org.mydrugs.mydrugs.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;

/**
 * Phase 1 custom sky for the Inner Dimension. Renders, in {@link RenderLevelStageEvent.AfterSky}
 * (before terrain, so opaque terrain paints over the sky naturally):
 *
 * <ul>
 *   <li>a slow vertical gradient from the {@link InnerAtmosphere} mood colour at the horizon to a
 *       darker/cooler zenith, with horizon haze pulled from {@code fogDensity} so sky and fog read
 *       as one volume;</li>
 *   <li>a constellation map where each integrated {@link DrugId} owns one fixed star pattern, lit
 *       only for drugs in the player's integrated set (unintegrated regions are dark gaps);</li>
 *   <li>a soft pulsing core beacon anchored toward the sanctuary (the player's slot centre), its
 *       brightness scaling with integrated count, acting as a constant navigational anchor.</li>
 * </ul>
 *
 * <p>Calm regions drift slowly; danger regions churn faster and cooler. Under reduced motion the
 * churn freezes but the colour is preserved. Everything no-ops outside the Inner Dimension.
 *
 * <p>Rendering uses {@link InnerRenderTypes#innerOverlayQuads()} — POSITION_COLOR, translucent
 * blend, no face cull, depth-tested but <em>not</em> depth-writing — so the far sky quads never cull
 * distant terrain. Geometry is camera-relative (a skybox), except the beacon which is placed at the
 * world azimuth of the sanctuary so it points home as the player moves.
 */
@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class InnerSkyRenderer {
    private static final ByteBufferBuilder BUFFER = new ByteBufferBuilder(256 * 1024);

    /** Camera-relative radius of the sky shell, in blocks. Kept well inside the far clip plane. */
    private static final float SKY_RADIUS = 96.0F;
    private static final int CYLINDER_SEGMENTS = 24;
    private static final float HORIZON_Y = -18.0F;
    private static final float ZENITH_Y = 78.0F;

    private InnerSkyRenderer() {
    }

    @SubscribeEvent
    public static void onAfterSky(RenderLevelStageEvent.AfterSky event) {
        Minecraft mc = Minecraft.getInstance();
        if (!InnerAtmosphereClient.inInnerDimension(mc)) {
            return;
        }
        InnerAtmosphere.Sample sample = InnerAtmosphereClient.current(mc);
        PoseStack poseStack = event.getPoseStack();
        if (sample == null || poseStack == null) {
            return;
        }

        Player player = mc.player;
        Camera camera = mc.gameRenderer.getMainCamera();
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        // Drift speed: calm warms and slows, danger cools and churns. The shared sky-motion scale
        // freezes both to a static sky under reduced motion (colour/gradient are unaffected below).
        float motion = (float) InnerClientIntensity.skyMotionScale();
        float time = motion * (mc.level.getGameTime() + partial);
        float churn = motion * (0.06F + (float) sample.danger() * 0.5F - (float) sample.calm() * 0.03F);

        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(BUFFER);
        VertexConsumer consumer = buffers.getBuffer(InnerRenderTypes.innerOverlayQuads());

        renderGradient(consumer, matrix, sample);
        renderAurora(consumer, matrix, sample, time);
        renderConstellations(consumer, matrix, camera, sample, time, churn);
        renderLandmarkPillars(consumer, matrix, player, time);
        renderBeacon(consumer, matrix, camera, player, sample, time);

        buffers.endBatch();
    }

    // -------------------------------------------------------------------------
    // Gradient dome
    // -------------------------------------------------------------------------

    private static void renderGradient(VertexConsumer consumer, Matrix4f matrix, InnerAtmosphere.Sample sample) {
        // Vista points earn a subtle sky lift: a brighter horizon and thinner haze so the long
        // view reads as the reward it is. Static per scene (no motion), so reduced-motion safe.
        float lift = sample.sceneType() == org.mydrugs.mydrugs.dimension.inner.InnerSceneType.PATH_VISTA
                ? 1.12F
                : 1.0F;
        int hr = (int) Math.min(255, sample.fogRed() * lift);
        int hg = (int) Math.min(255, sample.fogGreen() * lift);
        int hb = (int) Math.min(255, sample.fogBlue() * lift);
        // Zenith: darker and pushed cooler/bluer than the horizon mood colour.
        int zr = (int) (hr * 0.30F);
        int zg = (int) (hg * 0.34F);
        int zb = (int) Math.min(255, hb * 0.46F + 18);
        // Haze: a denser-fogged region fills more of the sky with horizon colour.
        float haze = (float) Mth.clamp(sample.fogDensity() / lift, 0.0D, 1.0D);
        int horizonAlpha = (int) (40 + haze * 70);
        int zenithAlpha = (int) (150 + haze * 70);

        float angleStep = (float) (Math.PI * 2.0 / CYLINDER_SEGMENTS);
        for (int i = 0; i < CYLINDER_SEGMENTS; i++) {
            float a0 = i * angleStep;
            float a1 = (i + 1) * angleStep;
            float x0 = Mth.cos(a0) * SKY_RADIUS;
            float z0 = Mth.sin(a0) * SKY_RADIUS;
            float x1 = Mth.cos(a1) * SKY_RADIUS;
            float z1 = Mth.sin(a1) * SKY_RADIUS;
            // Wall quad: bottom (horizon) -> top (high). Winding doesn't matter (cull off).
            vertex(consumer, matrix, x0, HORIZON_Y, z0, hr, hg, hb, horizonAlpha);
            vertex(consumer, matrix, x1, HORIZON_Y, z1, hr, hg, hb, horizonAlpha);
            vertex(consumer, matrix, x1, ZENITH_Y, z1, zr, zg, zb, zenithAlpha);
            vertex(consumer, matrix, x0, ZENITH_Y, z0, zr, zg, zb, zenithAlpha);
        }

        // Zenith cap so straight-up is covered by the cool dark colour.
        float cap = SKY_RADIUS;
        vertex(consumer, matrix, -cap, ZENITH_Y, -cap, zr, zg, zb, zenithAlpha);
        vertex(consumer, matrix, cap, ZENITH_Y, -cap, zr, zg, zb, zenithAlpha);
        vertex(consumer, matrix, cap, ZENITH_Y, cap, zr, zg, zb, zenithAlpha);
        vertex(consumer, matrix, -cap, ZENITH_Y, cap, zr, zg, zb, zenithAlpha);
    }

    // -------------------------------------------------------------------------
    // Aurora — slow ribbons over calm skies (C2)
    // -------------------------------------------------------------------------

    private static void renderAurora(VertexConsumer consumer, Matrix4f matrix, InnerAtmosphere.Sample sample, float time) {
        float calm = (float) sample.calm();
        if (calm < 0.40F) {
            return;
        }
        float strength = Mth.clamp((calm - 0.40F) / 0.60F, 0.0F, 1.0F);
        // Aurora hue: the region mood pushed toward spectral green/teal.
        int ar = (int) Mth.clamp(sample.fogRed() * 0.35F + 30, 0, 255);
        int ag = (int) Mth.clamp(sample.fogGreen() * 0.45F + 130, 0, 255);
        int ab = (int) Mth.clamp(sample.fogBlue() * 0.55F + 60, 0, 255);
        int segments = 18;
        for (int ribbon = 0; ribbon < 2; ribbon++) {
            float baseAz = ribbon * 2.4F + 0.6F;
            float baseEl = 0.85F + ribbon * 0.18F;
            int alpha = (int) (26 * strength) + ribbon * 6;
            float prevX = 0.0F, prevYLo = 0.0F, prevYHi = 0.0F, prevZ = 0.0F;
            boolean hasPrev = false;
            for (int i = 0; i <= segments; i++) {
                float t = i / (float) segments;
                float az = baseAz + t * 2.6F;
                // The ribbon undulates slowly; with time frozen (reduced motion) it is a still arc.
                float wave = Mth.sin(time * 0.006F + t * 9.0F + ribbon * 3.0F) * 0.07F;
                float el = baseEl + wave;
                float cosEl = Mth.cos(el);
                float x = Mth.cos(az) * cosEl * SKY_RADIUS * 0.9F;
                float z = Mth.sin(az) * cosEl * SKY_RADIUS * 0.9F;
                float yLo = Mth.sin(el) * SKY_RADIUS * 0.9F;
                float yHi = yLo + 10.0F + Mth.sin(time * 0.004F + t * 5.0F) * 2.5F;
                if (hasPrev) {
                    vertex(consumer, matrix, prevX, prevYLo, prevZ, ar, ag, ab, alpha);
                    vertex(consumer, matrix, x, yLo, z, ar, ag, ab, alpha);
                    vertex(consumer, matrix, x, yHi, z, ar, ag, ab, 0);
                    vertex(consumer, matrix, prevX, prevYHi, prevZ, ar, ag, ab, 0);
                }
                prevX = x;
                prevYLo = yLo;
                prevYHi = yHi;
                prevZ = z;
                hasPrev = true;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Landmark light pillars — progression lights up the world (C1)
    // -------------------------------------------------------------------------

    private static void renderLandmarkPillars(VertexConsumer consumer, Matrix4f matrix, Player player, float time) {
        if (InnerSkyClientState.integratedCount() == 0) {
            return;
        }
        int slotX = InnerAtmosphereClient.slotCenterX();
        int slotZ = InnerAtmosphereClient.slotCenterZ();
        for (DrugId drug : InnerSkyClientState.integratedDrugs()) {
            if (!org.mydrugs.mydrugs.dimension.inner.InnerRegionMap.hasAngle(drug)) {
                continue;
            }
            var landmark = org.mydrugs.mydrugs.dimension.inner.InnerRegionMap.landmarkFor(slotX, slotZ, drug);
            double toX = landmark.getX() - player.getX();
            double toZ = landmark.getZ() - player.getZ();
            double distance = Math.sqrt(toX * toX + toZ * toZ);
            if (distance < 24.0D) {
                continue; // standing at the shrine: the pillar would fill the screen
            }
            float dirX = (float) (toX / distance);
            float dirZ = (float) (toZ / distance);
            // Project onto the sky shell at the landmark's azimuth; nearer shrines read wider.
            float r = SKY_RADIUS * 0.88F;
            float px = dirX * r;
            float pz = dirZ * r;
            float halfWidth = (float) Mth.clamp(140.0D / distance, 0.35D, 2.6D);
            int[] tint = constellationTint(drug);
            float shimmer = 0.85F + 0.15F * Mth.sin(time * 0.03F + drug.networkId());
            int alpha = (int) (70 * shimmer);
            // Two crossed vertical quads from below the horizon to high sky — a column of light.
            float perpX = -dirZ * halfWidth;
            float perpZ = dirX * halfWidth;
            pillarQuad(consumer, matrix, px - perpX, pz - perpZ, px + perpX, pz + perpZ, tint, alpha);
            float diagX = dirX * halfWidth * 0.5F;
            float diagZ = dirZ * halfWidth * 0.5F;
            pillarQuad(consumer, matrix, px - diagX, pz - diagZ, px + diagX, pz + diagZ, tint, (int) (alpha * 0.6F));
        }
    }

    private static void pillarQuad(
            VertexConsumer consumer,
            Matrix4f matrix,
            float x0, float z0,
            float x1, float z1,
            int[] tint,
            int alpha
    ) {
        // Bright at the base band, fading to nothing high up — reads as a beam, not a wall.
        vertex(consumer, matrix, x0, HORIZON_Y * 0.4F, z0, tint[0], tint[1], tint[2], alpha);
        vertex(consumer, matrix, x1, HORIZON_Y * 0.4F, z1, tint[0], tint[1], tint[2], alpha);
        vertex(consumer, matrix, x1, ZENITH_Y * 0.9F, z1, tint[0], tint[1], tint[2], 0);
        vertex(consumer, matrix, x0, ZENITH_Y * 0.9F, z0, tint[0], tint[1], tint[2], 0);
    }

    // -------------------------------------------------------------------------
    // Constellations
    // -------------------------------------------------------------------------

    private static void renderConstellations(
            VertexConsumer consumer,
            Matrix4f matrix,
            Camera camera,
            InnerAtmosphere.Sample sample,
            float time,
            float churn
    ) {
        Vector3f up = new Vector3f(camera.getUpVector());
        Vector3f left = new Vector3f(camera.getLeftVector());

        // Slow global rotation of the whole star field about the vertical axis.
        float spin = time * churn * 0.0015F;

        DrugId[] all = DrugId.values();
        for (DrugId drug : all) {
            if (!InnerSkyClientState.isIntegrated(drug)) {
                continue; // dark gap
            }
            drawConstellation(consumer, matrix, up, left, drug, spin, time);
        }
    }

    private static void drawConstellation(
            VertexConsumer consumer,
            Matrix4f matrix,
            Vector3f up,
            Vector3f left,
            DrugId drug,
            float spin,
            float time
    ) {
        // Deterministic, stable pattern per drug seeded from its network id.
        RandomSource random = RandomSource.create(0x5DEECE66DL ^ (drug.networkId() * 0x9E3779B97F4A7C15L));
        int starCount = 5 + random.nextInt(4);
        // Each drug owns a base azimuth slice of the sky so constellations don't overlap.
        float baseAzimuth = (drug.networkId() / 23.0F) * (float) (Math.PI * 2.0) + spin;
        float baseElevation = 0.32F + random.nextFloat() * 0.5F; // radians above horizon

        int[] tint = constellationTint(drug);

        for (int s = 0; s < starCount; s++) {
            float az = baseAzimuth + (random.nextFloat() - 0.5F) * 0.55F;
            float el = baseElevation + (random.nextFloat() - 0.5F) * 0.40F;
            float cosEl = Mth.cos(el);
            float dx = Mth.cos(az) * cosEl;
            float dy = Mth.sin(el);
            float dz = Mth.sin(az) * cosEl;
            float r = SKY_RADIUS * 0.92F;
            float cx = dx * r;
            float cy = dy * r;
            float cz = dz * r;

            // Gentle twinkle.
            float twinkle = 0.7F + 0.3F * Mth.sin(time * 0.05F + s * 1.7F + drug.networkId());
            float size = (0.7F + random.nextFloat() * 0.9F) * twinkle;
            int alpha = (int) Mth.clamp(150 + 90 * twinkle, 0, 255);
            billboardQuad(consumer, matrix, up, left, cx, cy, cz, size, tint[0], tint[1], tint[2], alpha);
        }
    }

    /** Star colour per drug — warm/cool family derived from the drug identity. */
    private static int[] constellationTint(DrugId drug) {
        return switch (drug) {
            case WEED, HASH, MUSHROOMS -> new int[]{180, 255, 200};
            case LSD -> new int[]{200, 190, 255};
            case METH, COCAINE, CRACK -> new int[]{255, 200, 190};
            case ALCOHOL -> new int[]{190, 210, 255};
            case COFFEE, TOBACCO -> new int[]{255, 226, 180};
            default -> new int[]{225, 235, 255};
        };
    }

    // -------------------------------------------------------------------------
    // Core beacon
    // -------------------------------------------------------------------------

    private static void renderBeacon(
            VertexConsumer consumer,
            Matrix4f matrix,
            Camera camera,
            Player player,
            InnerAtmosphere.Sample sample,
            float time
    ) {
        float brightness = InnerSkyClientState.beaconBrightness();
        if (brightness <= 0.01F) {
            return;
        }
        Vector3f up = new Vector3f(camera.getUpVector());
        Vector3f left = new Vector3f(camera.getLeftVector());

        // Horizontal direction from the player toward the sanctuary (slot centre): points home.
        double toX = InnerAtmosphereClient.slotCenterX() - player.getX();
        double toZ = InnerAtmosphereClient.slotCenterZ() - player.getZ();
        double horiz = Math.sqrt(toX * toX + toZ * toZ);
        float dirX;
        float dirZ;
        if (horiz < 1.0E-3) {
            dirX = 0.0F;
            dirZ = 0.0F;
        } else {
            dirX = (float) (toX / horiz);
            dirZ = (float) (toZ / horiz);
        }
        // Sit high in the sky toward home.
        float r = SKY_RADIUS * 0.78F;
        float cx = dirX * r;
        float cy = ZENITH_Y * 0.62F;
        float cz = dirZ * r;

        float pulse = 0.85F + 0.15F * Mth.sin(time * 0.04F);
        float core = brightness * pulse;
        // Warm, calm-leaning core colour, cooled slightly by danger.
        int rr = (int) Mth.clamp(255 - sample.danger() * 40, 0, 255);
        int gg = (int) Mth.clamp(236 - sample.danger() * 20, 0, 255);
        int bb = (int) Mth.clamp(200 + sample.calm() * 30, 0, 255);

        // Stacked halo quads (large faint -> small bright) approximate a soft glowing orb.
        billboardQuad(consumer, matrix, up, left, cx, cy, cz, 9.0F, rr, gg, bb, (int) (40 * core));
        billboardQuad(consumer, matrix, up, left, cx, cy, cz, 5.5F, rr, gg, bb, (int) (80 * core));
        billboardQuad(consumer, matrix, up, left, cx, cy, cz, 3.0F, rr, gg, bb, (int) (150 * core));
        billboardQuad(consumer, matrix, up, left, cx, cy, cz, 1.5F, 255, 250, 235, (int) (220 * core));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static void billboardQuad(
            VertexConsumer consumer,
            Matrix4f matrix,
            Vector3f up,
            Vector3f left,
            float cx, float cy, float cz,
            float size,
            int r, int g, int b, int a
    ) {
        if (a <= 0) {
            return;
        }
        float ux = up.x() * size;
        float uy = up.y() * size;
        float uz = up.z() * size;
        float lx = left.x() * size;
        float ly = left.y() * size;
        float lz = left.z() * size;
        vertex(consumer, matrix, cx - lx - ux, cy - ly - uy, cz - lz - uz, r, g, b, a);
        vertex(consumer, matrix, cx - lx + ux, cy - ly + uy, cz - lz + uz, r, g, b, a);
        vertex(consumer, matrix, cx + lx + ux, cy + ly + uy, cz + lz + uz, r, g, b, a);
        vertex(consumer, matrix, cx + lx - ux, cy + ly - uy, cz + lz - uz, r, g, b, a);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, int r, int g, int b, int a) {
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }
}
