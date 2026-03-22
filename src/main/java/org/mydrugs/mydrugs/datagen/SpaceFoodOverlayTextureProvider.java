package org.mydrugs.mydrugs.datagen;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.ModItems;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SpaceFoodOverlayTextureProvider implements DataProvider {
    private static final String SOURCE_OVERLAY_PATH =
            "/assets/" + MyDrugs.MODID + "/textures/item/space_overlay.png";

    private static final byte[] MCMETA_BYTES = """
            {
              "animation": {
                "frametime": 1,
                "interpolate": true
              }
            }
            """.getBytes(StandardCharsets.UTF_8);

    private final PackOutput.PathProvider texturePath;

    public SpaceFoodOverlayTextureProvider(PackOutput output) {
        this.texturePath = output.createPathProvider(
                PackOutput.Target.RESOURCE_PACK,
                "textures/item/space_overlays"
        );
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.runAsync(() -> {
            try {
                BufferedImage overlayStrip = readClasspathPng(SOURCE_OVERLAY_PATH);

                int frameWidth = overlayStrip.getWidth();
                int totalHeight = overlayStrip.getHeight();

                if (frameWidth <= 0 || totalHeight <= 0 || totalHeight % frameWidth != 0) {
                    throw new IllegalStateException(
                            "space_overlay.png must be a vertical strip of square frames. " +
                                    "Got " + frameWidth + "x" + totalHeight
                    );
                }

                List<CompletableFuture<?>> writes = new ArrayList<>();

                ModItems.SPACE_FOODS_BY_BASE_ID.forEach((baseId, holder) -> {
                    try {
                        BufferedImage baseTexture = readVanillaItemTexture(baseId);
                        if (baseTexture == null) {
                            return;
                        }
                        BufferedImage maskedOverlay = maskOverlayByBaseAlpha(overlayStrip, baseTexture);

                        ResourceLocation outId = ResourceLocation.fromNamespaceAndPath(
                                MyDrugs.MODID,
                                "space_" + baseId.getPath()
                        );

                        Path pngPath = texturePath.file(outId, "png");
                        Path mcmetaPath = texturePath.file(outId, "png.mcmeta");

                        writes.add(writePng(cache, pngPath, maskedOverlay));
                        writes.add(writeBytes(cache, mcmetaPath, MCMETA_BYTES));
                    } catch (IOException e) {
                        throw new RuntimeException("Failed generating overlay for " + baseId, e);
                    }
                });

                CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new)).join();
            } catch (IOException e) {
                throw new RuntimeException("Failed to generate masked space food overlays", e);
            }
        });
    }

    private static BufferedImage readVanillaItemTexture(ResourceLocation itemId) throws IOException {
        String path = "/assets/" + itemId.getNamespace() + "/textures/item/" + itemId.getPath() + ".png";
        return readClasspathPng(path);
    }

    private static BufferedImage readClasspathPng(String path) throws IOException {
        try (InputStream in = SpaceFoodOverlayTextureProvider.class.getResourceAsStream(path)) {
            if (in == null) {
                System.err.println("Could not find " + path);
                return null;
                // throw new IOException("Missing classpath resource: " + path);
            }

            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                throw new IOException("Could not decode PNG: " + path);
            }
            return image;
        }
    }

    /**
     * Keeps the RGB from the animated shimmer strip,
     * but multiplies each pixel's alpha by the base texture alpha.
     */
    private static BufferedImage maskOverlayByBaseAlpha(BufferedImage overlayStrip, BufferedImage baseTexture) {
        int frameSize = overlayStrip.getWidth();
        int frameCount = overlayStrip.getHeight() / frameSize;

        BufferedImage output = new BufferedImage(
                frameSize,
                overlayStrip.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        for (int frame = 0; frame < frameCount; frame++) {
            int yOffset = frame * frameSize;

            for (int y = 0; y < frameSize; y++) {
                for (int x = 0; x < frameSize; x++) {
                    int overlayArgb = overlayStrip.getRGB(x, yOffset + y);
                    int overlayA = (overlayArgb >>> 24) & 0xFF;

                    if (overlayA == 0) {
                        output.setRGB(x, yOffset + y, 0x00000000);
                        continue;
                    }

                    int baseA = sampleAlphaNearest(baseTexture, x, y, frameSize, frameSize);
                    int outA = (overlayA * baseA) / 255;

                    int rgb = overlayArgb & 0x00FFFFFF;
                    int outArgb = (outA << 24) | rgb;

                    output.setRGB(x, yOffset + y, outArgb);
                }
            }
        }

        return output;
    }

    private static int sampleAlphaNearest(BufferedImage image, int x, int y, int targetW, int targetH) {
        int srcX = Math.min(image.getWidth() - 1, x * image.getWidth() / targetW);
        int srcY = Math.min(image.getHeight() - 1, y * image.getHeight() / targetH);
        return (image.getRGB(srcX, srcY) >>> 24) & 0xFF;
    }

    private static CompletableFuture<?> writePng(CachedOutput cache, Path path, BufferedImage image) {
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(image, "PNG", out);
                byte[] bytes = out.toByteArray();
                HashCode hash = Hashing.sha1().hashBytes(bytes);
                cache.writeIfNeeded(path, bytes, hash);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write PNG: " + path, e);
            }
        });
    }

    private static CompletableFuture<?> writeBytes(CachedOutput cache, Path path, byte[] bytes) {
        return CompletableFuture.runAsync(() -> {
            try {
                HashCode hash = Hashing.sha1().hashBytes(bytes);
                cache.writeIfNeeded(path, bytes, hash);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write file: " + path, e);
            }
        });
    }

    @Override
    public String getName() {
        return "Masked space food overlay textures";
    }
}