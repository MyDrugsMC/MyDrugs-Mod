package org.mydrugs.mydrugs.client.effects.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;

public final class AddictionHudRenderer {
    private static final int WITHDRAWAL_BAR_WIDTH = 81;
    private static final int WITHDRAWAL_BAR_HEIGHT = 5;
    private static final int SYMPTOM_ICON_SIZE = 16;
    private static final int SYMPTOM_ICON_GAP = 3;

    private static float displayedWithdrawal;

    private AddictionHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null
                || mc.options.hideGui
                || !Config.CLIENT.showAddictionHud.get()
                || !AddictionClientState.shouldRenderHud()) {
            return;
        }

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        drawWithdrawalBar(guiGraphics, width, height);
        // Accessibility: compact HUD drops the dominant-drug glyph and the symptom
        // icon column, keeping only the withdrawal bar so the playfield stays clear.
        if (!Config.CLIENT.compactAddictionHud.get()) {
            drawDominantDrugIcon(guiGraphics, mc, width, height);
            drawSymptomColumn(guiGraphics, width, height);
        }
    }

    private static void drawWithdrawalBar(GuiGraphics graphics, int width, int height) {
        float target = Mth.clamp(Math.max(AddictionClientState.globalSeverity, AddictionClientState.badTripSeverity), 0.0F, 1.0F);
        displayedWithdrawal += (target - displayedWithdrawal) * 0.18F;
        if (displayedWithdrawal < HudSymptomIcons.MIN_VISIBLE && target < HudSymptomIcons.MIN_VISIBLE) {
            displayedWithdrawal = 0.0F;
            return;
        }

        int x = width / 2 - 91;
        int y = height - 59;
        int fill = Math.round(displayedWithdrawal * WITHDRAWAL_BAR_WIDTH);
        graphics.fill(x - 1, y - 1, x + WITHDRAWAL_BAR_WIDTH + 1, y + WITHDRAWAL_BAR_HEIGHT + 1, 0x66000000);
        graphics.fill(x, y, x + WITHDRAWAL_BAR_WIDTH, y + WITHDRAWAL_BAR_HEIGHT, 0xAA1C101A);
        if (fill > 0) {
            int color = AddictionClientState.badTripActive ? 0xFFFF4F6D : displayedWithdrawal > 0.66F ? 0xFFE45A61 : displayedWithdrawal > 0.33F ? 0xFFC06C9A : 0xFF8D73D9;
            graphics.fill(x, y, x + fill, y + WITHDRAWAL_BAR_HEIGHT, color);
        }
        graphics.fill(x, y, x + WITHDRAWAL_BAR_WIDTH, y + 1, 0x55FFFFFF);
    }

    private static void drawDominantDrugIcon(GuiGraphics graphics, Minecraft mc, int width, int height) {
        float severity = Mth.clamp(AddictionClientState.globalSeverity, 0.0F, 1.0F);
        if (severity < 0.05F) {
            return;
        }

        ItemStack stack = dominantDrugStack(AddictionClientState.getDominantDrugIdEnum());
        if (stack.isEmpty()) {
            return;
        }

        long gameTime = mc.level == null ? 0L : mc.level.getGameTime();
        float shakeScale = Config.CLIENT.reducedMotionMode.get() ? 0.35F : 1.0F;
        int shake = Math.round(severity * 3.0F * shakeScale);
        int offsetX = shake == 0 ? 0 : Math.round(Mth.sin(gameTime * 0.73F) * shake);
        int offsetY = shake == 0 ? 0 : Math.round(Mth.cos(gameTime * 0.61F) * shake * 0.65F);
        int x = width / 2 - 8 + offsetX;
        int y = height - 52 + offsetY;

        graphics.fill(x - 2, y - 2, x + 18, y + 18, 0x66000000);
        graphics.renderItem(stack, x, y);
    }

    private static void drawSymptomColumn(GuiGraphics graphics, int width, int height) {
        int activeCount = 0;
        for (HudSymptomIcons.HudSymptomIcon icon : HudSymptomIcons.LIST) {
            if (icon.intensity() > HudSymptomIcons.MIN_VISIBLE) {
                activeCount++;
            }
        }
        if (activeCount == 0) {
            return;
        }

        int columnHeight = activeCount * SYMPTOM_ICON_SIZE + (activeCount - 1) * SYMPTOM_ICON_GAP;
        int x = 7;
        int y = Mth.clamp(height / 2 - columnHeight / 2, 8, Math.max(8, height - columnHeight - 8));
        for (HudSymptomIcons.HudSymptomIcon icon : HudSymptomIcons.LIST) {
            float intensity = Mth.clamp(icon.intensity(), 0.0F, 1.0F);
            if (intensity <= HudSymptomIcons.MIN_VISIBLE) {
                continue;
            }
            drawSymptomIcon(graphics, icon.texture(), x, y, intensity);
            y += SYMPTOM_ICON_SIZE + SYMPTOM_ICON_GAP;
        }
    }

    private static void drawSymptomIcon(GuiGraphics graphics, ResourceLocation texture, int x, int y, float intensity) {
        int alpha = 80 + Math.round(90.0F * intensity);
        graphics.fill(x - 1, y - 1, x + SYMPTOM_ICON_SIZE + 1, y + SYMPTOM_ICON_SIZE + 1, (alpha << 24) | 0x08080B);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
                0,
                0,
                SYMPTOM_ICON_SIZE,
                SYMPTOM_ICON_SIZE,
                16,
                16
        );
        int meter = Math.max(1, Math.round(SYMPTOM_ICON_SIZE * intensity));
        graphics.fill(x, y + SYMPTOM_ICON_SIZE - 1, x + meter, y + SYMPTOM_ICON_SIZE, 0xFFE6D06C);
    }

    private static ItemStack dominantDrugStack(DrugId drugId) {
        if (drugId == null) {
            return ItemStack.EMPTY;
        }

        return switch (drugId) {
            case WEED -> new ItemStack(ModItems.CANNABIS_POWDER.get());
            case HASH -> new ItemStack(ModItems.HASH_PIECE.get());
            case METH -> new ItemStack(ModItems.METH_SHARD.get());
            case COCAINE -> new ItemStack(ModItems.COCAINE_POWDER.get());
            case CRACK -> new ItemStack(ModItems.CRACK_SHARD.get());
            case LSD -> new ItemStack(ModItems.LSD_DROP.get());
            case MUSHROOMS -> new ItemStack(ModItems.MAGIC_MUSHROOM.get());
            case TOBACCO -> new ItemStack(ModItems.CIGARETTE.get());
            case COFFEE -> new ItemStack(ModItems.COFFEE_CUP.get());
            case ALCOHOL -> alcoholBottleStack();
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack alcoholBottleStack() {
        ItemStack stack = new ItemStack(ModItems.GLASS_BOTTLE.get());
        GlassBottleItem.setContent(stack, ModFluids.rl("raw_alcohol"), GlassBottleItem.CAPACITY_MB);
        return stack;
    }
}
