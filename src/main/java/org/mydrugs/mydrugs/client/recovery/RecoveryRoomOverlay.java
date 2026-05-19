package org.mydrugs.mydrugs.client.recovery;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.mydrugs.mydrugs.recovery.RecoveryRoomTier;

public final class RecoveryRoomOverlay {
    private static Component message = Component.empty();
    private static int ticksRemaining;

    private RecoveryRoomOverlay() {
    }

    public static void show(RecoveryRoomTier tier, int score, boolean highlighted) {
        message = Component.translatable(
                highlighted ? "recovery.mydrugs.room.overlay_highlight" : "recovery.mydrugs.room.overlay",
                Component.translatable(tier.translationKey()),
                score
        );
        ticksRemaining = highlighted ? 100 : 70;
    }

    public static void clear() {
        ticksRemaining = 0;
        message = Component.empty();
    }

    public static void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
        }
    }

    public static void render(GuiGraphics graphics) {
        if (ticksRemaining <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.font == null) {
            return;
        }
        int alpha = Math.min(255, ticksRemaining * 8);
        int color = (alpha << 24) | 0xB7E4C7;
        graphics.drawCenteredString(mc.font, message, graphics.guiWidth() / 2, Math.max(8, graphics.guiHeight() / 5), color);
    }
}
