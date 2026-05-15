package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.client.ClientStateHolder;
import org.mydrugs.mydrugs.core.client.shader.Shader;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public abstract class AnimatedShader extends Shader {
    private final String name;
    private final int uboSize;

    private MappableRingBuffer ubo;
    private GpuBuffer quadVertexBuffer;

    private RenderPipeline renderPipeline;
    private boolean warnedMissingPipeline = false;

    private RenderTarget copiedInputTarget;
    private int copiedInputWidth = -1;
    private int copiedInputHeight = -1;
    private GpuBuffer triangleVertexBuffer;

    protected AnimatedShader(String shaderName) {
        super(shaderName);
        this.name = shaderName;

        Std140SizeCalculator calc = new Std140SizeCalculator()
                .putFloat() // Time
                .putVec2(); // ScreenSize

        this.uboSize = extendUboLayout(calc).get();
    }

    public final String getName() {
        return name;
    }

    public final RenderPipeline getRenderPipeline() {
        return renderPipeline;
    }

    public final void buildPipeline() {
        RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name))
                .withVertexShader(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "core/" + name))
                .withFragmentShader(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "core/" + name))
                .withUniform(getFormattedUniformName(), UniformType.UNIFORM_BUFFER)
                .withVertexFormat(
                        DefaultVertexFormat.POSITION_TEX,
                        useFullscreenTriangle() ? VertexFormat.Mode.TRIANGLES : VertexFormat.Mode.QUADS
                )
                .withDepthWrite(false);

        if (usesInputSampler()) {
            builder.withSampler("InSampler");
        }

        for (String sampler : extraSamplerNames()) {
            builder.withSampler(sampler);
        }

        configurePipeline(builder);

        this.renderPipeline = builder.build();
        this.warnedMissingPipeline = false;
    }

    /**
     * Called each dose-sync cycle with the current dose for this shader's category.
     * Subclasses should scale their effect-intensity parameters by {@code dose}
     * (dose = 1.0 → default parameters; dose = 2.0 → double intensity, etc.).
     * Speed, color and structural parameters typically stay constant.
     */
    public void setStrength(float dose) {
        // Default: no-op. Subclasses override to scale visual intensity.
    }

    public void tick(Minecraft mc) {
        if (mc.isPaused()) return;
        time += deltaTime;
        if (time > 1000.0F) {
            time -= 1000.0F;
        }
    }

    protected boolean usesInputSampler() {
        return true;
    }

    public void render(Minecraft mc) {
        if (mc.level == null) {
            return;
        }
        if (renderPipeline == null) {
            if (!warnedMissingPipeline) {
                MyDrugs.getLOGGER().warn("Skipping shader '{}' because its render pipeline was not registered", name);
                warnedMissingPipeline = true;
            }
            return;
        }

        try {
            renderInternal(mc);
        } catch (RuntimeException e) {
            ClientStateHolder.get().setShader(null);
            MyDrugs.getLOGGER().error("Disabled shader '{}' after render failure", name, e);
        }
    }

    private void renderInternal(Minecraft mc) {
        RenderTarget mainTarget = mc.getMainRenderTarget();
        if (mainTarget == null) {
            MyDrugs.getLOGGER().warn("Skipping shader '{}' because the main render target is unavailable", name);
            return;
        }

        float width = mainTarget.width;
        float height = mainTarget.height;

        uploadUniforms(time, width, height);

        GpuBuffer vertices = useFullscreenTriangle() ? getTriangleVertexBuffer() : getQuadVertexBuffer();

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        RenderTarget inputTarget = resolveInputTarget(mc, encoder, mainTarget, (int) width, (int) height);

        try (RenderPass pass = encoder.createRenderPass(
                () -> "mydrugs_" + name + "_pass",
                mainTarget.getColorTextureView(),
                OptionalInt.empty(),
                mainTarget.getDepthTextureView(),
                OptionalDouble.empty()
        )) {
            pass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setVertexBuffer(0, vertices);

            if (usesInputSampler()) {
                pass.bindSampler("InSampler", inputTarget.getColorTextureView());
            }
            bindExtraSamplers(pass, mainTarget, inputTarget);
            pass.setUniform(getFormattedUniformName(), getUbo().currentBuffer());
            if (useFullscreenTriangle()) {
                pass.draw(0, 3);
            } else {
                RenderSystem.AutoStorageIndexBuffer indices =
                        RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
                GpuBuffer indexBuffer = indices.getBuffer(6);
                pass.setIndexBuffer(indexBuffer, indices.type());
                pass.drawIndexed(0, 0, 6, 1);
            }
        }
    }

    protected Std140SizeCalculator extendUboLayout(Std140SizeCalculator calc) {
        return calc;
    }

    protected void writeExtraUniforms(Std140Builder builder, Minecraft mc) {
    }

    protected List<String> extraSamplerNames() {
        return List.of();
    }

    protected void bindExtraSamplers(RenderPass pass, RenderTarget mainTarget, RenderTarget inputTarget) {
    }

    protected void configurePipeline(RenderPipeline.Builder builder) {
    }

    protected boolean useCopiedInputTarget() {
        return false;
    }

    private void uploadUniforms(float time, float width, float height) {
        MappableRingBuffer currentUbo = getUbo();
        currentUbo.rotate();

        try (GpuBuffer.MappedView view = RenderSystem.getDevice()
                .createCommandEncoder()
                .mapBuffer(currentUbo.currentBuffer(), false, true)) {

            Std140Builder builder = Std140Builder.intoBuffer(view.data())
                    .putFloat(time)
                    .putVec2(width, height);

            writeExtraUniforms(builder, Minecraft.getInstance());
        }
    }

    private MappableRingBuffer getUbo() {
        if (ubo == null) {
            ubo = new MappableRingBuffer(
                    () -> "mydrugs_" + name + "_uniforms",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
                    uboSize
            );
        }
        return ubo;
    }

    private GpuBuffer getQuadVertexBuffer() {
        if (quadVertexBuffer == null) {
            ByteBufferBuilder byteBuilder =
                    new ByteBufferBuilder(DefaultVertexFormat.POSITION_TEX.getVertexSize() * 4);
            BufferBuilder bufferBuilder =
                    new BufferBuilder(byteBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            bufferBuilder.addVertex(-1.0F, -1.0F, 0.0F).setUv(0.0F, 0.0F);
            bufferBuilder.addVertex(1.0F, -1.0F, 0.0F).setUv(1.0F, 0.0F);
            bufferBuilder.addVertex(1.0F, 1.0F, 0.0F).setUv(1.0F, 1.0F);
            bufferBuilder.addVertex(-1.0F, 1.0F, 0.0F).setUv(0.0F, 1.0F);

            MeshData mesh = bufferBuilder.buildOrThrow();

            try {
                quadVertexBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "mydrugs_" + name + "_quad_vbo",
                        GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                        mesh.vertexBuffer()
                );
            } finally {
                mesh.close();
                byteBuilder.close();
            }
        }

        return quadVertexBuffer;
    }

    private RenderTarget resolveInputTarget(
            Minecraft mc,
            CommandEncoder encoder,
            RenderTarget mainTarget,
            int width,
            int height
    ) {
        if (!useCopiedInputTarget()) {
            return mainTarget;
        }

        RenderTarget copy = getOrCreateCopiedInputTarget(width, height);

        // This exact copy call may need a tiny mapping tweak in your workspace.
        encoder.copyTextureToTexture(
                mainTarget.getColorTexture(), // source
                copy.getColorTexture(),       // target
                0,                            // mipLevel
                0,                            // intoX
                0,                            // intoY
                0,                            // sourceX
                0,                            // sourceY
                width,
                height
        );
        return copy;
    }

    private RenderTarget getOrCreateCopiedInputTarget(int width, int height) {
        if (copiedInputTarget == null || copiedInputWidth != width || copiedInputHeight != height) {
            destroyCopiedInputTarget();

            copiedInputTarget = new TextureTarget(
                    MyDrugs.MODID + "_" + name + "_input",
                    width,
                    height,
                    true
            );

            copiedInputWidth = width;
            copiedInputHeight = height;
        }

        return copiedInputTarget;
    }

    public void onResize() {
        destroyCopiedInputTarget();
    }

    public void close() {
        destroyCopiedInputTarget();

        if (ubo != null) {
            ubo.close();
            ubo = null;
        }

        if (quadVertexBuffer != null) {
            quadVertexBuffer.close();
            quadVertexBuffer = null;
        }
    }

    private void destroyCopiedInputTarget() {
        if (copiedInputTarget != null) {
            copiedInputTarget.destroyBuffers();
            copiedInputTarget = null;
        }
        copiedInputWidth = -1;
        copiedInputHeight = -1;
    }

    protected boolean useFullscreenTriangle() {
        return false;
    }

    private GpuBuffer getTriangleVertexBuffer() {
        if (triangleVertexBuffer == null) {
            ByteBufferBuilder byteBuilder =
                    new ByteBufferBuilder(DefaultVertexFormat.POSITION_TEX.getVertexSize() * 3);
            BufferBuilder bufferBuilder =
                    new BufferBuilder(byteBuilder, VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);

            bufferBuilder.addVertex(-1.0F, -1.0F, 0.0F).setUv(0.0F, 0.0F);
            bufferBuilder.addVertex(3.0F, -1.0F, 0.0F).setUv(2.0F, 0.0F);
            bufferBuilder.addVertex(-1.0F, 3.0F, 0.0F).setUv(0.0F, 2.0F);

            MeshData mesh = bufferBuilder.buildOrThrow();

            try {
                triangleVertexBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "mydrugs_" + name + "_triangle_vbo",
                        GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                        mesh.vertexBuffer()
                );
            } finally {
                mesh.close();
                byteBuilder.close();
            }
        }

        return triangleVertexBuffer;
    }
}
