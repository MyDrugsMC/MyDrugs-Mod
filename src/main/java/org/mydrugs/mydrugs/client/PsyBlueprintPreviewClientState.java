package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.network.PsyBlueprintPreviewPayload;

import java.util.List;

public final class PsyBlueprintPreviewClientState {
    private static ResourceLocation dimension;
    private static net.minecraft.core.BlockPos origin;
    private static List<PsyBlueprintPreviewPayload.Entry> entries = List.of();
    private static int ticksRemaining;

    private PsyBlueprintPreviewClientState() {
    }

    public static void apply(PsyBlueprintPreviewPayload payload) {
        if (ticksRemaining > 0 && payload.dimension().equals(dimension) && payload.origin().equals(origin)) {
            clear();
            return;
        }

        dimension = payload.dimension();
        origin = payload.origin().immutable();
        entries = List.copyOf(payload.entries());
        ticksRemaining = Math.max(1, payload.durationTicks());
    }

    public static void tick() {
        if (ticksRemaining <= 0) {
            return;
        }
        ticksRemaining--;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !mc.level.dimension().location().equals(dimension)) {
            return;
        }

        if (mc.player.blockPosition().distSqr(origin) > 48.0D * 48.0D) {
            clear();
        }
    }

    public static boolean isActive() {
        return ticksRemaining > 0 && !entries.isEmpty();
    }

    public static ResourceLocation dimension() {
        return dimension;
    }

    public static List<PsyBlueprintPreviewPayload.Entry> entries() {
        return entries;
    }

    private static void clear() {
        dimension = null;
        origin = null;
        entries = List.of();
        ticksRemaining = 0;
    }
}
