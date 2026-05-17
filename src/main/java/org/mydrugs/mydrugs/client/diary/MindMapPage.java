package org.mydrugs.mydrugs.client.diary;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.mydrugs.mydrugs.psyche.PsycheMapNodeCatalog;
import org.mydrugs.mydrugs.psyche.PsycheMapNodeDto;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Custom diary "page": a draggable / zoomable psyche map. Lives next to the
 * normal line-based pages in {@link PersonalDiaryScreen}.
 *
 * Input contract:
 *  - {@link #mouseClicked} returns true if it consumed the click (opens playback).
 *  - {@link #mouseDragged} returns true while panning.
 *  - {@link #mouseScrolled} returns true when adjusting zoom.
 *  - {@link #keyPressed} handles R (recenter) only.
 */
public final class MindMapPage {

    private static final int LOCKED_OUTER = 0xFF2D1A0C;
    private static final int LOCKED_INNER = 0xFF150A04;
    private static final int UNLOCKED_OUTER = 0xFFE6C260;
    private static final int UNLOCKED_INNER = 0xFFFFE9A0;
    private static final int GLOW_HALO = 0x55FFE9A0;
    private static final int THREAD_FULL = 0xFFB02828;
    private static final int THREAD_FAINT = 0x66402815;
    private static final int PAPER_TINT = 0xFFE7D4A6;
    private static final int PAPER_DARK = 0xFFB89764;

    private static final float MIN_ZOOM = 0.6F;
    private static final float MAX_ZOOM = 1.8F;

    private final List<Node> nodes = new ArrayList<>();
    private final Set<String> unlocked = new HashSet<>();
    private final int totalCount;
    private final int unlockedCount;

    private int contentLeft, contentTop, contentRight, contentBottom;
    private int centerX, centerY;
    private float panX, panY;
    private float zoom = 1.0F;
    private boolean dragging;

    public MindMapPage(List<PsycheMapNodeDto> snapshotNodes,
                       int contentLeft, int contentTop, int contentRight, int contentBottom) {
        setBounds(contentLeft, contentTop, contentRight, contentBottom);
        for (PsycheMapNodeDto n : snapshotNodes) {
            unlocked.add(n.nodeId());
        }
        for (PsycheMapNodeCatalog.Entry e : PsycheMapNodeCatalog.all()) {
            boolean isUnlocked = unlocked.contains(e.nodeId);
            boolean hasMem = isUnlocked && Files.isRegularFile(
                    MemoryStoragePaths.memoryDir(e.nodeId).resolve("frame_00.png")
            );
            if (e.hiddenUntilUnlocked && !isUnlocked) {
                nodes.add(new Node(e, false, true, false));
            } else {
                nodes.add(new Node(e, isUnlocked, false, hasMem));
            }
        }
        this.totalCount = nodes.size();
        int count = 0;
        for (Node n : nodes) {
            if (n.unlocked) count++;
        }
        this.unlockedCount = count;
        recenter();
    }

    public void setBounds(int left, int top, int right, int bottom) {
        this.contentLeft = left;
        this.contentTop = top;
        this.contentRight = right;
        this.contentBottom = bottom;
        this.centerX = (left + right) / 2;
        this.centerY = (top + bottom) / 2;
    }

    public void recenter() {
        if (nodes.isEmpty()) {
            this.panX = 0;
            this.panY = 0;
            return;
        }
        // Center pan on the bounding box of unlocked nodes (or all if none unlocked yet)
        boolean anyUnlocked = unlockedCount > 0;
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (Node n : nodes) {
            if (anyUnlocked && !n.unlocked) continue;
            minX = Math.min(minX, n.entry.x);
            maxX = Math.max(maxX, n.entry.x);
            minY = Math.min(minY, n.entry.y);
            maxY = Math.max(maxY, n.entry.y);
        }
        if (minX == Integer.MAX_VALUE) {
            minX = -50; maxX = 50; minY = -50; maxY = 50;
        }
        this.panX = -(minX + maxX) / 2.0F;
        this.panY = -(minY + maxY) / 2.0F;
        this.zoom = 1.0F;
    }

    public boolean keyPressed(int key) {
        if (key == InputConstants.KEY_R) {
            recenter();
            return true;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (!inside(mouseX, mouseY)) return false;
        Node hit = pick(mouseX, mouseY);
        if (hit != null && hit.unlocked) {
            String title = Component.translatable(hit.entry.titleKey).getString();
            String caption = Component.translatable(hit.entry.captionKey).getString();
            Minecraft.getInstance().setScreen(
                    new MemoryPlaybackScreen(hit.entry.nodeId, Component.literal(title), caption)
            );
            return true;
        }
        // Start a pan drag if no node hit
        dragging = true;
        return true;
    }

    public boolean mouseReleased(int button) {
        if (button == 0 && dragging) {
            dragging = false;
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0 || !dragging) return false;
        panX += (float) (dragX / zoom);
        panY += (float) (dragY / zoom);
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double dy) {
        if (!inside(mouseX, mouseY)) return false;
        float old = zoom;
        zoom = clampZoom(zoom * (dy > 0 ? 1.12F : 1.0F / 1.12F));
        // Zoom toward cursor: keep world point under cursor stable
        if (old != zoom) {
            float wx = ((float) mouseX - centerX) / old - panX;
            float wy = ((float) mouseY - centerY) / old - panY;
            panX = ((float) mouseX - centerX) / zoom - wx;
            panY = ((float) mouseY - centerY) / zoom - wy;
        }
        return true;
    }

    private float clampZoom(float z) {
        if (z < MIN_ZOOM) return MIN_ZOOM;
        if (z > MAX_ZOOM) return MAX_ZOOM;
        return z;
    }

    private boolean inside(double mx, double my) {
        return mx >= contentLeft && mx <= contentRight && my >= contentTop && my <= contentBottom;
    }

    private Node pick(double mouseX, double mouseY) {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node n = nodes.get(i);
            int sx = worldToScreenX(n.entry.x);
            int sy = worldToScreenY(n.entry.y);
            int r = nodeRadiusScreen();
            double dx = mouseX - sx;
            double dy = mouseY - sy;
            if (dx * dx + dy * dy <= r * r) {
                return n;
            }
        }
        return null;
    }

    private int worldToScreenX(int wx) {
        return centerX + Math.round((wx + panX) * zoom);
    }

    private int worldToScreenY(int wy) {
        return centerY + Math.round((wy + panY) * zoom);
    }

    private int nodeRadiusScreen() {
        return Math.max(5, Math.round(9 * zoom));
    }

    public void render(GuiGraphics g, Font font, int mouseX, int mouseY) {
        renderBackground(g);
        g.enableScissor(contentLeft, contentTop, contentRight, contentBottom);

        renderThreads(g);
        renderNodes(g, font, mouseX, mouseY);

        g.disableScissor();

        renderCounter(g, font);
        renderHover(g, font, mouseX, mouseY);
    }

    private void renderBackground(GuiGraphics g) {
        g.fill(contentLeft, contentTop, contentRight, contentBottom, PAPER_TINT);
        // Cheap noise: two slightly darker bands
        int midY = (contentTop + contentBottom) / 2;
        g.fill(contentLeft, midY - 1, contentRight, midY, 0x10000000);
        g.fill(contentLeft, contentTop, contentLeft + 1, contentBottom, PAPER_DARK);
        g.fill(contentRight - 1, contentTop, contentRight, contentBottom, PAPER_DARK);
    }

    private void renderThreads(GuiGraphics g) {
        // Build a quick id->node map
        java.util.Map<String, Node> byId = new java.util.HashMap<>();
        for (Node n : nodes) byId.put(n.entry.nodeId, n);

        for (Node child : nodes) {
            for (String parentId : child.entry.parents) {
                Node parent = byId.get(parentId);
                if (parent == null) continue;
                boolean both = parent.unlocked && child.unlocked;
                int color = both ? THREAD_FULL : THREAD_FAINT;
                drawBezier(g,
                        worldToScreenX(parent.entry.x), worldToScreenY(parent.entry.y),
                        worldToScreenX(child.entry.x), worldToScreenY(child.entry.y),
                        color);
            }
        }
    }

    private void drawBezier(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        // Quadratic bezier with a sagging control point between them
        int cx = (x1 + x2) / 2;
        int cy = (y1 + y2) / 2 + 12; // slight sag => "red thread"
        int steps = Math.max(16, Math.min(96, Math.abs(x2 - x1) / 4 + Math.abs(y2 - y1) / 4));
        int lastX = x1, lastY = y1;
        for (int i = 1; i <= steps; i++) {
            float t = (float) i / steps;
            float omt = 1.0F - t;
            int x = Math.round(omt * omt * x1 + 2 * omt * t * cx + t * t * x2);
            int y = Math.round(omt * omt * y1 + 2 * omt * t * cy + t * t * y2);
            drawSegment(g, lastX, lastY, x, y, color);
            lastX = x;
            lastY = y;
        }
    }

    private void drawSegment(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        if (steps == 0) {
            g.fill(x1, y1, x1 + 1, y1 + 1, color);
            return;
        }
        for (int i = 0; i <= steps; i++) {
            int px = x1 + (x2 - x1) * i / steps;
            int py = y1 + (y2 - y1) * i / steps;
            g.fill(px, py, px + 1, py + 1, color);
        }
    }

    private void renderNodes(GuiGraphics g, Font font, int mouseX, int mouseY) {
        for (Node n : nodes) {
            int sx = worldToScreenX(n.entry.x);
            int sy = worldToScreenY(n.entry.y);
            int r = nodeRadiusScreen();
            // skip nodes way off-screen
            if (sx + r < contentLeft - 32 || sx - r > contentRight + 32
                    || sy + r < contentTop - 32 || sy - r > contentBottom + 32) {
                continue;
            }

            int outer = n.unlocked ? UNLOCKED_OUTER : LOCKED_OUTER;
            int inner = n.unlocked ? UNLOCKED_INNER : LOCKED_INNER;
            if (n.unlocked) {
                // soft halo
                g.fill(sx - r - 2, sy - r - 2, sx + r + 2, sy + r + 2, GLOW_HALO);
            }
            g.fill(sx - r - 1, sy - r - 1, sx + r + 1, sy + r + 1, outer);
            g.fill(sx - r, sy - r, sx + r, sy + r, inner);

            if (n.unlocked) {
                // Icon
                ItemStack icon = lookupIcon(n.entry.iconItemId);
                g.renderFakeItem(icon, sx - 8, sy - 8);

                if (n.hasMemory) {
                    g.fill(sx + r - 2, sy - r, sx + r + 1, sy - r + 3, 0xFFFFFFFF);
                } else {
                    g.fill(sx + r - 2, sy - r, sx + r + 1, sy - r + 3, 0xFF802020);
                }

                String label = Component.translatable(n.entry.titleKey).getString();
                int lw = font.width(label);
                g.drawString(font, label, sx - lw / 2, sy + r + 2, 0xFF2A1B0E, false);
            }
        }
    }

    private void renderCounter(GuiGraphics g, Font font) {
        Component txt = Component.translatable("screen.mydrugs.diary.mind_map.counter", unlockedCount, totalCount);
        int w = font.width(txt);
        int x = contentRight - w - 6;
        int y = contentTop + 4;
        g.fill(x - 4, y - 2, x + w + 4, y + 10, 0xAA1A0F06);
        g.drawString(font, txt, x, y, 0xFFFFE9A0, false);
    }

    private void renderHover(GuiGraphics g, Font font, int mouseX, int mouseY) {
        if (!inside(mouseX, mouseY)) return;
        Node hovered = pick(mouseX, mouseY);
        if (hovered == null) return;

        // Outline on hovered node
        int sx = worldToScreenX(hovered.entry.x);
        int sy = worldToScreenY(hovered.entry.y);
        int r = nodeRadiusScreen() + 2;
        g.fill(sx - r - 1, sy - r - 1, sx + r + 1, sy - r,     0xFFFFFFFF);
        g.fill(sx - r - 1, sy + r,     sx + r + 1, sy + r + 1, 0xFFFFFFFF);
        g.fill(sx - r - 1, sy - r,     sx - r,     sy + r,     0xFFFFFFFF);
        g.fill(sx + r,     sy - r,     sx + r + 1, sy + r,     0xFFFFFFFF);

        String title;
        String caption;
        if (hovered.unlocked) {
            title = Component.translatable(hovered.entry.titleKey).getString();
            caption = Component.translatable(hovered.entry.captionKey).getString();
        } else {
            title = "? ? ?";
            caption = Component.translatable("screen.mydrugs.diary.mind_map.locked_hint").getString();
        }

        // Compute tooltip box
        int previewW = 0;
        int previewH = 0;
        MemoryPreviewCache.Entry preview = null;
        if (hovered.unlocked) {
            preview = MemoryPreviewCache.getOrLoad(hovered.entry.nodeId);
            if (preview.isPresent()) {
                previewW = 96;
                previewH = Math.max(1, Math.round(previewW * (float) preview.height / preview.width));
            }
        }

        int titleW = font.width(title);
        List<String> lines = wrappedLines(font, caption, 140);
        int captionLines = lines.size();
        int textWidth = Math.max(titleW, Math.min(140, font.width(caption)));
        int boxW = Math.max(previewW, textWidth) + 12;
        int boxH = (previewH > 0 ? previewH + 4 : 0) + 12 + 10 * (captionLines + 1);

        int bx = mouseX + 12 > contentRight - boxW ? (int) mouseX - boxW - 6 : (int) mouseX + 12;
        int by = mouseY + 12 > contentBottom - boxH ? (int) mouseY - boxH - 6 : (int) mouseY + 12;

        g.fill(bx, by, bx + boxW, by + boxH, 0xEE0F0907);
        g.fill(bx, by, bx + boxW, by + 1, 0xFFCFA970);
        g.fill(bx, by + boxH - 1, bx + boxW, by + boxH, 0xFFCFA970);
        g.fill(bx, by, bx + 1, by + boxH, 0xFFCFA970);
        g.fill(bx + boxW - 1, by, bx + boxW, by + boxH, 0xFFCFA970);

        int tx = bx + 6;
        int ty = by + 6;
        if (preview != null && preview.isPresent()) {
            // torn-photo backing
            g.fill(tx - 2, ty - 2, tx + previewW + 2, ty + previewH + 2, 0xFFEFE3C7);
            g.blit(
                    RenderPipelines.GUI_TEXTURED,
                    preview.location,
                    tx, ty,
                    0.0F, 0.0F,
                    previewW, previewH,
                    previewW, previewH,
                    previewW, previewH
            );
            ty += previewH + 4;
        }
        g.drawString(font, title, tx, ty, 0xFFFFE9A0, false);
        ty += 10;
        drawWrapped(g, font, lines, tx, ty, 0xFFBFB295);
    }

    private List<String> wrappedLines(Font font, String text, int maxW) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return List.of();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String trial = line.length() == 0 ? word : line + " " + word;
            if (font.width(trial) > maxW && line.length() > 0) {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            } else {
                if (line.length() > 0) line.append(' ');
                line.append(word);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    private void drawWrapped(GuiGraphics g, Font font, List<String> lines, int x, int y, int color) {
        if (lines == null || lines.isEmpty()) return;
        int cy = y;
        for (String line : lines) {
            g.drawString(font, line, x, cy, color, false);
            cy += 10;
        }
    }

    private static ItemStack lookupIcon(String itemId) {
        if (itemId == null || itemId.isEmpty()) return new ItemStack(Items.PAPER);
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl == null) return new ItemStack(Items.PAPER);
        Item item = BuiltInRegistries.ITEM.getValue(rl);
        if (item == Items.AIR || item == null) return new ItemStack(Items.PAPER);
        return new ItemStack(item);
    }

    private static final class Node {
        final PsycheMapNodeCatalog.Entry entry;
        final boolean unlocked;
        final boolean hidden;
        final boolean hasMemory;
        Node(PsycheMapNodeCatalog.Entry entry, boolean unlocked, boolean hidden, boolean hasMemory) {
            this.entry = entry;
            this.unlocked = unlocked;
            this.hidden = hidden;
            this.hasMemory = hasMemory;
        }
    }
}
