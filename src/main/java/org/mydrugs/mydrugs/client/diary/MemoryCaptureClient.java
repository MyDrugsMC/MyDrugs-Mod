package org.mydrugs.mydrugs.client.diary;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.network.StartMemoryCapturePayload;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * v2: mirrors F2 screenshot behaviour.
 *
 * - GPU readback is kicked off on the render thread via {@link Screenshot#takeScreenshot},
 *   the callback dispatches the heavy work (downscale + desaturate + PNG encode + write)
 *   onto {@link Util#ioPool()} so the render thread never blocks on filesystem or pixel ops.
 * - 10 frames captured at 2 FPS (one every {@link #TICKS_BETWEEN_FRAMES} client ticks).
 * - Frames are saved low-res (max {@link #MAX_FRAME_W}x{@link #MAX_FRAME_H}) and lightly desaturated.
 * - {@link NativeImage} is closed on the IO thread after writing.
 * - Failures are logged only.
 */
@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class MemoryCaptureClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int FRAME_COUNT = 10;
    public static final int TICKS_BETWEEN_FRAMES = 10; // 2 FPS at 20 ticks/sec
    public static final int MAX_FRAME_W = 480;
    public static final int MAX_FRAME_H = 270;
    /** 0.0 = grayscale, 1.0 = original; saved frames keep 35% of original saturation. */
    public static final float SATURATION = 0.35F;

    private static StartMemoryCapturePayload pending;
    private static Path captureDir;
    private static int frameIndex;
    private static int tickCounter;

    private MemoryCaptureClient() {
    }

    public static synchronized void start(StartMemoryCapturePayload payload) {
        // Setup directory + manifest on IO pool so we don't block the network/render thread.
        try {
            final Path dir = MemoryStoragePaths.memoryDir(payload.nodeId());
            pending = payload;
            captureDir = dir;
            frameIndex = 0;
            tickCounter = 0;
            Util.ioPool().execute(() -> {
                try {
                    Files.createDirectories(dir);
                    writeManifest(dir, payload);
                } catch (Throwable t) {
                    LOGGER.warn("Failed to initialize memory capture dir for {}: {}", payload.nodeId(), t.toString());
                }
            });
        } catch (Throwable t) {
            LOGGER.warn("Failed to start memory capture for {}: {}", payload.nodeId(), t.toString());
            pending = null;
            captureDir = null;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (pending == null || captureDir == null) {
            return;
        }
        if (tickCounter <= 0) {
            captureFrame();
            tickCounter = TICKS_BETWEEN_FRAMES;
            if (frameIndex >= FRAME_COUNT) {
                pending = null;
                captureDir = null;
                return;
            }
        }
        tickCounter--;
    }

    private static void captureFrame() {
        final int index = frameIndex++;
        final Path dir = captureDir;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getMainRenderTarget() == null) {
                return;
            }
            // takeScreenshot triggers GPU readback; the consumer fires when pixels are ready.
            // Inside the consumer we hand the image to the IO pool — never touch FS on render thread.
            Screenshot.takeScreenshot(mc.getMainRenderTarget(), (NativeImage rawImage) -> {
                Util.ioPool().execute(() -> processAndWrite(rawImage, dir, index));
            });
        } catch (Throwable t) {
            LOGGER.warn("Memory frame {} capture failed: {}", index, t.toString());
        }
    }

    private static void processAndWrite(NativeImage rawImage, Path dir, int index) {
        NativeImage scaled = null;
        try {
            scaled = downscaleAndDesaturate(rawImage, MAX_FRAME_W, MAX_FRAME_H, SATURATION);
            Path file = dir.resolve(String.format("frame_%02d.png", index));
            scaled.writeToFile(file.toFile());
        } catch (IOException e) {
            LOGGER.warn("Failed to write memory frame {}: {}", index, e.toString());
        } catch (Throwable t) {
            LOGGER.warn("Memory frame {} processing failed: {}", index, t.toString());
        } finally {
            try {
                if (scaled != null) scaled.close();
            } catch (Throwable ignored) {
            }
            try {
                rawImage.close();
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Nearest-neighbour downscale that also reduces saturation in-pass.
     * Pure pixel math on the calling thread (must NOT be the render thread).
     */
    private static NativeImage downscaleAndDesaturate(NativeImage src, int maxW, int maxH, float saturation) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();
        float scale = Math.min(1.0F, Math.min((float) maxW / srcW, (float) maxH / srcH));
        int dstW = Math.max(1, Math.round(srcW * scale));
        int dstH = Math.max(1, Math.round(srcH * scale));

        NativeImage out = new NativeImage(dstW, dstH, false);
        float sat = Math.max(0.0F, Math.min(1.0F, saturation));
        for (int dy = 0; dy < dstH; dy++) {
            int sy = Math.min(srcH - 1, (int) ((dy + 0.5F) / scale));
            for (int dx = 0; dx < dstW; dx++) {
                int sx = Math.min(srcW - 1, (int) ((dx + 0.5F) / scale));
                int abgr = src.getPixel(sx, sy);
                int a = (abgr >>> 24) & 0xFF;
                int b = (abgr >>> 16) & 0xFF;
                int g = (abgr >>> 8) & 0xFF;
                int r = abgr & 0xFF;
                // Rec.601 luma
                int luma = (int) (0.299F * r + 0.587F * g + 0.114F * b);
                int nr = clamp((int) (luma + (r - luma) * sat));
                int ng = clamp((int) (luma + (g - luma) * sat));
                int nb = clamp((int) (luma + (b - luma) * sat));
                int packed = (a << 24) | (nb << 16) | (ng << 8) | nr;
                out.setPixel(dx, dy, packed);
            }
        }
        return out;
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    private static void writeManifest(Path dir, StartMemoryCapturePayload payload) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"nodeId\": ").append(json(payload.nodeId())).append(",\n");
            sb.append("  \"titleKey\": ").append(json(payload.titleKey())).append(",\n");
            sb.append("  \"mood\": ").append(json(payload.mood())).append(",\n");
            sb.append("  \"gameTime\": ").append(payload.gameTime()).append(",\n");
            sb.append("  \"dominantDrugId\": ").append(json(payload.dominantDrugId())).append(",\n");
            sb.append("  \"frames\": ").append(FRAME_COUNT).append(",\n");
            sb.append("  \"fps\": 2\n");
            sb.append("}\n");
            Files.write(dir.resolve("manifest.json"), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.warn("Failed to write memory manifest: {}", e.toString());
        }
    }

    private static String json(String s) {
        if (s == null) s = "";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    /** Backwards-compat shim; new code should use {@link MemoryStoragePaths#memoryDir(String)}. */
    public static Path memoryDir(String nodeId) {
        return MemoryStoragePaths.memoryDir(nodeId);
    }
}
