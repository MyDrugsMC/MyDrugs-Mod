package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.data.BiomeFinderTarget;
import org.mydrugs.mydrugs.items.data.ModDataComponents;

public final class BiomeFinderCompassOverlay {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/gui/biome_finder_compass.png");
    private static final int SIZE = 96;
    private static final int MARGIN = 9;

    private BiomeFinderCompassOverlay() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) {
            return;
        }

        BlockPos targetPos = resolveTargetPos(mc, mc.player.getMainHandItem());
        if (targetPos == null) {
            targetPos = resolveTargetPos(mc, mc.player.getOffhandItem());
        }
        if (targetPos == null) {
            return;
        }

        int x = mc.getWindow().getGuiScaledWidth() - SIZE - MARGIN;
        int y = MARGIN;
        drawCompass(graphics, mc.player, targetPos, x, y);
    }

    private static BlockPos resolveTargetPos(Minecraft mc, ItemStack stack) {
        if (!stack.is(ModItems.VANILLA_BIOME_FINDER.get())) {
            return null;
        }

        BiomeFinderTarget target = stack.getOrDefault(ModDataComponents.BIOME_FINDER_TARGET.get(), BiomeFinderTarget.EMPTY);
        if (target.selectedBiome().isEmpty() || target.cachedPos().isEmpty() || target.cachedDimension().isEmpty()) {
            return null;
        }
        if (!target.cachedDimension().get().equals(mc.level.dimension().location())) {
            return null;
        }
        return target.cachedPos().get();
    }

    private static void drawCompass(GuiGraphics graphics, LocalPlayer player, BlockPos targetPos, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, SIZE, SIZE, 192, 96);

        float angle = needleAngle(player, targetPos);
        int centerX = x + SIZE / 2;
        int centerY = y + SIZE / 2;

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(centerX, centerY);
        pose.rotate(angle);
        pose.translate(-centerX, -centerY);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 96.0F, 0.0F, SIZE, SIZE, 192, 96);
        pose.popMatrix();
    }

    private static float needleAngle(LocalPlayer player, BlockPos targetPos) {
        double dx = targetPos.getX() + 0.5D - player.getX();
        double dz = targetPos.getZ() + 0.5D - player.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 0.001D) {
            return 0.0F;
        }

        double targetX = dx / length;
        double targetZ = dz / length;
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;

        double forwardX = -Mth.sin(yaw);
        double forwardZ = Mth.cos(yaw);
        double rightX = -Mth.cos(yaw);
        double rightZ = -Mth.sin(yaw);

        double forward = targetX * forwardX + targetZ * forwardZ;
        double right = targetX * rightX + targetZ * rightZ;
        return (float) Math.atan2(right, forward);
    }
}
