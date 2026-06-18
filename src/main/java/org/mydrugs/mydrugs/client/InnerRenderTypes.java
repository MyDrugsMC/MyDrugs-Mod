package org.mydrugs.mydrugs.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.mydrugs.mydrugs.MyDrugs;

/**
 * Deliberate render type for the Inner Dimension's custom POSITION_COLOR overlay geometry (the
 * skybox shell, aurora ribbons, beacon, constellations and the lake mirror silhouette).
 *
 * <p>This previously borrowed {@link RenderType#debugStructureQuads()}. That is a <em>debug</em>
 * render type — Mojang reserves the right to retune or remove debug pipelines between versions, so
 * leaning on one for player-facing rendering is fragile. We instead register our own pipeline that
 * reproduces exactly the state those quads need, and own it ourselves.
 *
 * <p>The pipeline mirrors vanilla's {@code DEBUG_STRUCTURE_QUADS} on purpose, so the visuals are
 * unchanged:
 * <ul>
 *   <li>vanilla built-in {@code core/position_color} shaders (no new shader assets);</li>
 *   <li>{@link BlendFunction#TRANSLUCENT} so the translucent sky/overlay alpha blends;</li>
 *   <li>{@code cull(false)} so the inward-facing sky shell and crossed pillar quads are never
 *       back-face culled regardless of winding;</li>
 *   <li>{@code depthWrite(false)} with the default {@code LEQUAL} depth test, so the sky is depth
 *       <em>tested</em> (opaque terrain drawn afterwards paints over it) but never writes depth and
 *       therefore cannot cull distant terrain or later overlay layers.</li>
 * </ul>
 */
@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class InnerRenderTypes {
    private static final ResourceLocation PIPELINE_ID =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "pipeline/inner_overlay_quads");

    /**
     * Our owned equivalent of vanilla's {@code DEBUG_STRUCTURE_QUADS} pipeline. Built from the
     * shared projection-matrix snippet with the position_color shaders and the exact blend/cull/
     * depth state the inner-dimension overlays rely on.
     */
    private static final RenderPipeline INNER_OVERLAY_PIPELINE = RenderPipeline.builder(
                    RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(PIPELINE_ID)
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthWrite(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build();

    // sortOnUpload=true matches DEBUG_STRUCTURE_QUADS so the translucent quads are transparency
    // sorted on upload; affectsCrumbling is irrelevant to sky/overlay geometry (false).
    private static final RenderType INNER_OVERLAY_QUADS = RenderType.create(
            "inner_overlay_quads",
            1536,
            false,
            true,
            INNER_OVERLAY_PIPELINE,
            RenderType.CompositeState.builder().createCompositeState(false));

    private InnerRenderTypes() {
    }

    /** Translucent, no-cull, non-depth-writing POSITION_COLOR quads for inner-dimension overlays. */
    public static RenderType innerOverlayQuads() {
        return INNER_OVERLAY_QUADS;
    }

    @SubscribeEvent
    public static void onRegisterPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(INNER_OVERLAY_PIPELINE);
    }
}
