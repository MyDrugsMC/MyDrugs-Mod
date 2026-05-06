package org.mydrugs.mydrugs.effects.addiction.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.client.AddictionClientState;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomFlags;
import org.mydrugs.mydrugs.effects.addiction.dose.DoseState;
import org.mydrugs.mydrugs.items.ModItems;

public final class AddictionHudRenderer {
    private static final int WITHDRAWAL_BAR_WIDTH = 81;
    private static final int WITHDRAWAL_BAR_HEIGHT = 5;
    private static final int SYMPTOM_ICON_SIZE = 13;
    private static final int SYMPTOM_ICON_GAP = 3;
    private static final float MIN_VISIBLE = 0.015F;

    private static float displayedWithdrawal;

    private static final SymptomIcon[] SYMPTOM_ICONS = {
            new SymptomIcon("insomnia", AddictionHudRenderer::insomniaIntensity),
            new SymptomIcon("hallucination", () -> flagIntensity(SymptomFlags.HALLUCINATION)),
            new SymptomIcon("vision", () -> flagIntensity(SymptomFlags.VISION)),
            new SymptomIcon("confusion", () -> Math.max(flagIntensity(SymptomFlags.CONFUSION), effectIntensity(EffectType.CONFUSION))),
            new SymptomIcon("stress", () -> Math.max(flagIntensity(SymptomFlags.STRESS), AddictionClientState.stressLevel)),
            new SymptomIcon("dissociation", () -> flagIntensity(SymptomFlags.DISSOCIATION)),
            new SymptomIcon("fatigue", () -> flagIntensity(SymptomFlags.FATIGUE)),
            new SymptomIcon("intrusive_thoughts", () -> flagIntensity(SymptomFlags.INTRUSIVE_THOUGHTS)),
            new SymptomIcon("fragility", () -> flagIntensity(SymptomFlags.FRAGILITY)),
            new SymptomIcon("blur", () -> effectIntensity(EffectType.BLUR)),
            new SymptomIcon("vomit", () -> Math.max(effectIntensity(EffectType.VOMIT), effectIntensity(EffectType.CUSTOM_NAUSEA))),
            new SymptomIcon("tremor", () -> effectIntensity(EffectType.TREMOR)),
            new SymptomIcon("stumble", () -> effectIntensity(EffectType.STUMBLE)),
            new SymptomIcon("input_fail", () -> effectIntensity(EffectType.INPUT_FAIL)),
            new SymptomIcon("camera_sway", () -> effectIntensity(EffectType.CAMERA_SWAY)),
            new SymptomIcon("heartbeat", () -> Math.max(effectIntensity(EffectType.HEARTBEAT), heartbeatIntensity())),
            new SymptomIcon("overdose", AddictionHudRenderer::overdoseIntensity),
            new SymptomIcon("dose", AddictionHudRenderer::doseIntensity)
    };

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
        drawDominantDrugIcon(guiGraphics, mc, width, height);
        drawSymptomColumn(guiGraphics, width, height);
    }

    private static void drawWithdrawalBar(GuiGraphics graphics, int width, int height) {
        float target = Mth.clamp(AddictionClientState.globalSeverity, 0.0F, 1.0F);
        displayedWithdrawal += (target - displayedWithdrawal) * 0.18F;
        if (displayedWithdrawal < MIN_VISIBLE && target < MIN_VISIBLE) {
            displayedWithdrawal = 0.0F;
            return;
        }

        int x = width / 2 - 91;
        int y = height - 59;
        int fill = Math.round(displayedWithdrawal * WITHDRAWAL_BAR_WIDTH);
        graphics.fill(x - 1, y - 1, x + WITHDRAWAL_BAR_WIDTH + 1, y + WITHDRAWAL_BAR_HEIGHT + 1, 0x66000000);
        graphics.fill(x, y, x + WITHDRAWAL_BAR_WIDTH, y + WITHDRAWAL_BAR_HEIGHT, 0xAA1C101A);
        if (fill > 0) {
            int color = displayedWithdrawal > 0.66F ? 0xFFE45A61 : displayedWithdrawal > 0.33F ? 0xFFC06C9A : 0xFF8D73D9;
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
        int y = height - 42 + offsetY;

        graphics.fill(x - 2, y - 2, x + 18, y + 18, 0x66000000);
        graphics.renderItem(stack, x, y);
    }

    private static void drawSymptomColumn(GuiGraphics graphics, int width, int height) {
        int activeCount = 0;
        for (SymptomIcon icon : SYMPTOM_ICONS) {
            if (icon.intensity() > MIN_VISIBLE) {
                activeCount++;
            }
        }
        if (activeCount == 0) {
            return;
        }

        int columnHeight = activeCount * SYMPTOM_ICON_SIZE + (activeCount - 1) * SYMPTOM_ICON_GAP;
        int x = 7;
        int y = Mth.clamp(height / 2 - columnHeight / 2, 8, Math.max(8, height - columnHeight - 8));
        for (SymptomIcon icon : SYMPTOM_ICONS) {
            float intensity = Mth.clamp(icon.intensity(), 0.0F, 1.0F);
            if (intensity <= MIN_VISIBLE) {
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
            case ALCOHOL -> new ItemStack(ModItems.HERBAL_TEA.get());
            default -> ItemStack.EMPTY;
        };
    }

    private static float flagIntensity(int flag) {
        return AddictionClientState.has(flag) ? Mth.clamp(AddictionClientState.globalSeverity, 0.25F, 1.0F) : 0.0F;
    }

    private static float effectIntensity(EffectType type) {
        return Mth.clamp(AddictionClientState.getEffectIntensity(type), 0.0F, 1.0F);
    }

    private static float insomniaIntensity() {
        if (AddictionClientState.hasInsomnia()) {
            return Mth.clamp(AddictionClientState.globalSeverity, 0.25F, 1.0F);
        }
        return 0.0F;
    }

    private static float heartbeatIntensity() {
        return AddictionClientState.has(SymptomFlags.STRESS) ? Mth.clamp(AddictionClientState.stressLevel, 0.3F, 1.0F) : 0.0F;
    }

    private static float overdoseIntensity() {
        return AddictionClientState.hasOverdoseTimer() ? 1.0F : 0.0F;
    }

    private static float doseIntensity() {
        DoseState state = AddictionClientState.getDominantDoseState();
        return switch (state) {
            case NORMAL -> 0.0F;
            case HIGH, DRUNK -> 0.45F;
            case VERY_HIGH, VERY_DRUNK -> 0.75F;
            case OVERDOSE, ETHYLIC_COMA -> 1.0F;
        };
    }

    private record SymptomIcon(String name, IntensityProvider provider) {
        private ResourceLocation texture() {
            return ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/gui/symptoms/" + name + ".png");
        }

        private float intensity() {
            return provider.get();
        }
    }

    private interface IntensityProvider {
        float get();
    }
}
