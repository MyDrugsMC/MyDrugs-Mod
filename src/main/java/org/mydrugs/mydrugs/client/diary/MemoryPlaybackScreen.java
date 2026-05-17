package org.mydrugs.mydrugs.client.diary;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * v2: Polaroid-style playback.
 *
 *  - Dark "table" background.
 *  - Current frame rendered tilted as a Polaroid with a paper border.
 *  - Previous/next frames render behind at lower alpha and different rotations.
 *  - Torn paper edges simulated with small darker rectangles.
 *  - Subtle sepia/dark overlay on top (saved frames are already desaturated).
 *  - Caption block at the bottom.
 *  - Frame counter 01/10 top right.
 *  - Space pause, R replay, Left/Right step, Esc closes.
 */
public final class MemoryPlaybackScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FRAME_TICKS = 10; // 2 FPS at 20 ticks/sec
    private static int instanceCounter;

    private final String nodeId;
    private final Component playbackTitle;
    private final String caption;
    private final List<Frame> frames = new ArrayList<>();
    private final int instanceId;

    private int frameIndex;
    private int tickCounter;
    private boolean paused;

    public MemoryPlaybackScreen(String nodeId, Component title) {
        this(nodeId, title, "");
    }

    public MemoryPlaybackScreen(String nodeId, Component title, String caption) {
        super(title);
        this.nodeId = nodeId;
        this.playbackTitle = title;
        this.caption = caption == null ? "" : caption;
        this.instanceId = ++instanceCounter;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        loadFrames();
        this.frameIndex = 0;
        this.tickCounter = FRAME_TICKS;
        this.paused = false;
    }

    private void loadFrames() {
        releaseFrames();
        Path dir = MemoryStoragePaths.memoryDir(nodeId);
        if (!Files.isDirectory(dir)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        for (int i = 0; i < MemoryCaptureClient.FRAME_COUNT; i++) {
            Path file = dir.resolve(String.format("frame_%02d.png", i));
            if (!Files.isRegularFile(file)) {
                continue;
            }
            try (InputStream in = Files.newInputStream(file)) {
                NativeImage img = NativeImage.read(in);
                final int slot = frames.size();
                DynamicTexture tex = new DynamicTexture(() -> "mydrugs_memory_" + instanceId + "_" + slot, img);
                ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(
                        MyDrugs.MODID,
                        "memory/playback_" + instanceId + "_" + slot
                );
                mc.getTextureManager().register(rl, tex);
                frames.add(new Frame(rl, tex, img.getWidth(), img.getHeight()));
            } catch (IOException e) {
                LOGGER.warn("Failed to load memory frame {}: {}", file, e.toString());
            }
        }
    }

    private void releaseFrames() {
        Minecraft mc = Minecraft.getInstance();
        for (Frame f : frames) {
            try {
                mc.getTextureManager().release(f.location);
                f.texture.close();
            } catch (Throwable ignored) {
            }
        }
        frames.clear();
    }

    @Override
    public void onClose() {
        releaseFrames();
        super.onClose();
    }

    @Override
    public void tick() {
        super.tick();
        if (paused || frames.isEmpty()) return;
        tickCounter--;
        if (tickCounter <= 0) {
            frameIndex = (frameIndex + 1) % frames.size();
            tickCounter = FRAME_TICKS;
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (key == InputConstants.KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if (frames.isEmpty()) {
            return super.keyPressed(event);
        }
        if (key == InputConstants.KEY_SPACE) {
            paused = !paused;
            return true;
        }
        if (key == InputConstants.KEY_R) {
            frameIndex = 0;
            tickCounter = FRAME_TICKS;
            paused = false;
            return true;
        }
        if (key == InputConstants.KEY_LEFT) {
            frameIndex = (frameIndex - 1 + frames.size()) % frames.size();
            paused = true;
            return true;
        }
        if (key == InputConstants.KEY_RIGHT) {
            frameIndex = (frameIndex + 1) % frames.size();
            paused = true;
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(g);
        int sw = this.width;
        int sh = this.height;

        // Dark table background with faint vignette
        g.fill(0, 0, sw, sh, 0xFF1A120B);
        g.fill(0, 0, sw, 12, 0xFF0B0805);
        g.fill(0, sh - 12, sw, sh, 0xFF0B0805);

        if (frames.isEmpty()) {
            renderBlank(g);
            renderTitle(g);
            super.render(g, mouseX, mouseY, partialTick);
            return;
        }

        // Compute Polaroid size for the current frame
        Frame current = frames.get(Math.min(frameIndex, frames.size() - 1));
        int photoW = Math.min((int) (sw * 0.55F), 360);
        int photoH = (int) (photoW * (float) current.height / (float) current.width);
        if (photoH > sh * 0.55F) {
            photoH = (int) (sh * 0.55F);
            photoW = (int) (photoH * (float) current.width / (float) current.height);
        }

        int cx = sw / 2;
        int cy = sh / 2 - 6;

        // Background ghost frames (prev, next) — small tilt and alpha
        if (frames.size() > 1) {
            Frame prev = frames.get((frameIndex - 1 + frames.size()) % frames.size());
            renderPolaroid(g, prev, cx - 28, cy + 10, photoW, photoH, -7.5F, 0x55FFFFFF, false);
            Frame next = frames.get((frameIndex + 1) % frames.size());
            renderPolaroid(g, next, cx + 28, cy + 10, photoW, photoH, 6.0F, 0x55FFFFFF, false);
        }

        // Main frame on top
        renderPolaroid(g, current, cx, cy, photoW, photoH, -2.5F, 0xFFFFFFFF, true);

        renderTitle(g);
        renderCounter(g);
        renderCaption(g, cy + photoH / 2 + 20, photoW);
        renderControls(g);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderBlank(GuiGraphics g) {
        int sw = this.width;
        int sh = this.height;
        int photoW = 220;
        int photoH = 140;
        int cx = sw / 2;
        int cy = sh / 2;
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate((float) cx, (float) cy);
        pose.rotate((float) Math.toRadians(-3.5));
        int x = -photoW / 2;
        int y = -photoH / 2;
        // Polaroid paper
        g.fill(x - 8, y - 8, x + photoW + 8, y + photoH + 36, 0xFFEFE3C7);
        // Cracked photo: dark center with white slashes
        g.fill(x, y, x + photoW, y + photoH, 0xFF15110A);
        for (int i = 0; i < 4; i++) {
            int sx = x + 20 + i * 40;
            int sy1 = y + 10 + (i * 17) % (photoH - 20);
            int sy2 = sy1 + 18;
            g.fill(sx, sy1, sx + 1, sy2, 0xFF402815);
        }
        // Torn corners
        drawTornEdges(g, x, y, photoW, photoH);
        pose.popMatrix();
        Component blank = Component.translatable("screen.mydrugs.memory.blank");
        int w = this.font.width(blank);
        g.drawString(this.font, blank, (sw - w) / 2, cy + photoH / 2 + 20, 0xFFCCCCCC, true);
    }

    private void renderPolaroid(GuiGraphics g, Frame frame, int cx, int cy,
                                int photoW, int photoH, float rotationDeg, int tint, boolean addOverlay) {
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate((float) cx, (float) cy);
        pose.rotate((float) Math.toRadians(rotationDeg));
        int x = -photoW / 2;
        int y = -photoH / 2;
        int paperPadX = 8;
        int paperPadTop = 8;
        int paperPadBottom = 28;

        // Drop shadow
        g.fill(x - paperPadX + 4, y - paperPadTop + 4, x + photoW + paperPadX + 4, y + photoH + paperPadBottom + 4, 0x66000000);
        // Polaroid paper
        int paper = (tint == 0xFFFFFFFF) ? 0xFFF6EAD0 : applyAlpha(0xFFF6EAD0, tint);
        g.fill(x - paperPadX, y - paperPadTop, x + photoW + paperPadX, y + photoH + paperPadBottom, paper);

        // Photo
        if (frame != null && frame.location != null) {
            g.blit(
                    RenderPipelines.GUI_TEXTURED,
                    frame.location,
                    x, y,
                    0.0F, 0.0F,
                    photoW, photoH,
                    photoW, photoH,
                    photoW, photoH
            );
        }

        if (addOverlay) {
            // Subtle sepia darkening over the photo
            g.fill(x, y, x + photoW, y + photoH, 0x22301B0A);
            // Vignette: darker corners via overlapping rectangles
            g.fill(x, y, x + photoW, y + 6, 0x44000000);
            g.fill(x, y + photoH - 6, x + photoW, y + photoH, 0x44000000);
            g.fill(x, y, x + 6, y + photoH, 0x44000000);
            g.fill(x + photoW - 6, y, x + photoW, y + photoH, 0x44000000);
        }

        // Ripped paper edges (jagged small darker rects on the outside)
        drawTornEdges(g, x, y, photoW, photoH);

        pose.popMatrix();
    }

    private void drawTornEdges(GuiGraphics g, int x, int y, int photoW, int photoH) {
        // Top tear notches
        for (int i = 0; i < photoW; i += 12) {
            int notch = (i / 12) % 3 == 0 ? 3 : 2;
            g.fill(x + i, y - 1, x + i + 5, y + notch, 0xFF15110A);
        }
        // Bottom tear notches
        for (int i = 6; i < photoW; i += 14) {
            g.fill(x + i, y + photoH - 1, x + i + 4, y + photoH + 2, 0xFF15110A);
        }
    }

    private int applyAlpha(int color, int tint) {
        int a = ((tint >>> 24) & 0xFF);
        int r = ((color >>> 16) & 0xFF);
        int gC = ((color >>> 8) & 0xFF);
        int b = (color & 0xFF);
        return (a << 24) | (r << 16) | (gC << 8) | b;
    }

    private void renderTitle(GuiGraphics g) {
        int tw = this.font.width(playbackTitle);
        g.drawString(this.font, playbackTitle, (this.width - tw) / 2, 12, 0xFFFFE0A0, true);
    }

    private void renderCounter(GuiGraphics g) {
        String status = String.format("%02d / %02d", frameIndex + 1, frames.size());
        if (paused) status = "|| " + status;
        int w = this.font.width(status);
        g.drawString(this.font, status, this.width - w - 14, 14, 0xFFE6C260, true);
    }

    private void renderCaption(GuiGraphics g, int yBaseline, int photoW) {
        if (caption == null || caption.isEmpty()) return;
        int maxW = Math.min(this.width - 40, Math.max(photoW + 60, 280));
        int cx = this.width / 2;
        // Lay it out wrapping
        java.util.List<String> lines = wrap(caption, maxW);
        int lineH = 10;
        int totalH = lines.size() * lineH + 8;
        int boxX = cx - maxW / 2 - 6;
        int boxY = yBaseline;
        g.fill(boxX, boxY, boxX + maxW + 12, boxY + totalH, 0xCC120A06);
        g.fill(boxX, boxY, boxX + maxW + 12, boxY + 1, 0xFFCFA970);
        int y = boxY + 4;
        for (String line : lines) {
            int w = this.font.width(line);
            g.drawString(this.font, line, cx - w / 2, y, 0xFFEDE1C2, false);
            y += lineH;
        }
    }

    private void renderControls(GuiGraphics g) {
        Component hint = Component.translatable("screen.mydrugs.memory.controls");
        int hw = this.font.width(hint);
        g.drawString(this.font, hint, (this.width - hw) / 2, this.height - 14, 0xFF888888, false);
    }

    private List<String> wrap(String text, int maxW) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isEmpty()) return out;
        for (String paragraph : text.split("\\R")) {
            StringBuilder current = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String trial = current.length() == 0 ? word : current + " " + word;
                if (this.font.width(trial) > maxW && current.length() > 0) {
                    out.add(current.toString());
                    current.setLength(0);
                    current.append(word);
                } else {
                    if (current.length() > 0) current.append(' ');
                    current.append(word);
                }
            }
            if (current.length() > 0) out.add(current.toString());
        }
        return out;
    }

    private record Frame(ResourceLocation location, DynamicTexture texture, int width, int height) {
    }
}
