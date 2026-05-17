package org.mydrugs.mydrugs.client.diary;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tiny synchronous cache for frame_00.png previews used in the mind-map hover popup.
 * Loads lazily on the render thread (one-shot per node), keeps a bounded LRU.
 *
 * Frames on disk are already small (480x270 max) and desaturated, so a sync load
 * here is cheap. If the file is missing we cache a {@link Entry#MISSING} marker.
 */
public final class MemoryPreviewCache {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_ENTRIES = 24;

    public static final class Entry {
        public static final Entry MISSING = new Entry(null, null, 0, 0);
        public final ResourceLocation location;
        public final DynamicTexture texture;
        public final int width;
        public final int height;
        Entry(ResourceLocation location, DynamicTexture texture, int width, int height) {
            this.location = location;
            this.texture = texture;
            this.width = width;
            this.height = height;
        }
        public boolean isPresent() {
            return location != null;
        }
    }

    private static final Map<String, Entry> CACHE = new LinkedHashMap<>(16, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
            if (size() > MAX_ENTRIES) {
                Entry e = eldest.getValue();
                if (e != null && e.isPresent()) {
                    try {
                        Minecraft.getInstance().getTextureManager().release(e.location);
                        e.texture.close();
                    } catch (Throwable ignored) {
                    }
                }
                return true;
            }
            return false;
        }
    };

    private MemoryPreviewCache() {
    }

    public static Entry getOrLoad(String nodeId) {
        Entry cached = CACHE.get(nodeId);
        if (cached != null) {
            return cached;
        }
        Entry loaded = loadFrame00(nodeId);
        CACHE.put(nodeId, loaded);
        return loaded;
    }

    private static Entry loadFrame00(String nodeId) {
        try {
            Path file = MemoryStoragePaths.memoryDir(nodeId).resolve("frame_00.png");
            if (!Files.isRegularFile(file)) {
                return Entry.MISSING;
            }
            try (InputStream in = Files.newInputStream(file)) {
                NativeImage img = NativeImage.read(in);
                ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(
                        MyDrugs.MODID,
                        "memory_preview/" + sanitizeForRl(nodeId)
                );
                DynamicTexture tex = new DynamicTexture(() -> "mydrugs_memory_preview_" + nodeId, img);
                Minecraft.getInstance().getTextureManager().register(rl, tex);
                return new Entry(rl, tex, img.getWidth(), img.getHeight());
            }
        } catch (Throwable t) {
            LOGGER.warn("Failed to load preview for {}: {}", nodeId, t.toString());
            return Entry.MISSING;
        }
    }

    private static String sanitizeForRl(String s) {
        return s.toLowerCase(java.util.Locale.ROOT)
                .replace(':', '_')
                .replaceAll("[^a-z0-9._/-]", "_");
    }
}
