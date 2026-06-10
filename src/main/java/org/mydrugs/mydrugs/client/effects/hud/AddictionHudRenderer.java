package org.mydrugs.mydrugs.client.effects.hud;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.addiction.network.DrugEffectCueKind;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;
import org.mydrugs.mydrugs.client.ui.MyDrugsUiTheme;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.effect.EffectCategory;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;

public final class AddictionHudRenderer {
    private static final int WITHDRAWAL_BAR_WIDTH = 81;
    private static final int WITHDRAWAL_BAR_HEIGHT = 5;
    private static final int SYMPTOM_ICON_SIZE = 16;
    private static final int SYMPTOM_ICON_GAP = 3;
    private static final int LABEL_GAP = 4;
    private static final int MAX_LABEL_WIDTH = 112;
    private static final int NORMAL_VISIBLE_ICONS = 6;
    private static final int PULSE_DURATION_TICKS = 34;
    private static final int PULSE_COOLDOWN_TICKS = 24;
    private static final int MAX_PULSES = 4;

    private static final ArrayDeque<Pulse> PULSES = new ArrayDeque<>();
    private static final EnumMap<EffectType, Integer> PULSE_COOLDOWNS = new EnumMap<>(EffectType.class);
    private static float displayedWithdrawal;
    private static int scrollOffset;
    private static HudLayout lastLayout = HudLayout.EMPTY;

    private AddictionHudRenderer() {
    }

    public static void tick() {
        PULSES.removeIf(Pulse::tick);
        PULSE_COOLDOWNS.replaceAll((ignored, cooldown) -> Math.max(0, cooldown - 1));
        PULSE_COOLDOWNS.values().removeIf(cooldown -> cooldown <= 0);
    }

    public static void enqueuePulse(EffectType type, DrugEffectCueKind kind, float intensity) {
        if (type == null || PULSE_COOLDOWNS.getOrDefault(type, 0) > 0) {
            return;
        }
        while (PULSES.size() >= MAX_PULSES) {
            PULSES.removeLast();
        }
        PULSES.addFirst(new Pulse(type, kind, Mth.clamp(intensity, 0.15F, 1.5F)));
        PULSE_COOLDOWNS.put(type, PULSE_COOLDOWN_TICKS);
    }

    public static void clearPulses() {
        PULSES.clear();
        PULSE_COOLDOWNS.clear();
        scrollOffset = 0;
        lastLayout = HudLayout.EMPTY;
    }

    public static HudBounds occupiedBounds() {
        return new HudBounds(lastLayout.x(), lastLayout.y(), lastLayout.width(), lastLayout.height());
    }

    public static boolean mouseScrolled(double mouseX, double mouseY, double scrollDeltaY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null || scrollDeltaY == 0.0D || !lastLayout.contains(mouseX, mouseY)) {
            return false;
        }
        List<HudEffectEntry> entries = buildEffectEntries(true);
        int visible = visibleSlots(mc.getWindow().getGuiScaledHeight(), true, lastLayout.scale());
        int maxOffset = Math.max(0, entries.size() - visible);
        if (maxOffset <= 0) {
            scrollOffset = 0;
            return false;
        }
        scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(scrollDeltaY), 0, maxOffset);
        return true;
    }

    public static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null
                || mc.options.hideGui
                || !Config.CLIENT.showAddictionHud.get()
                || !AddictionClientState.shouldRenderHud()) {
            lastLayout = HudLayout.EMPTY;
            return;
        }

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        drawWithdrawalBar(graphics, width, height);
        boolean compact = Config.CLIENT.compactAddictionHud.get();
        boolean labels = Config.CLIENT.addictionHudTextLabels.get();
        if (!compact) {
            drawDominantDrugIcon(graphics, mc, width, height);
        }
        if (!compact || labels) {
            drawEffectColumn(graphics, mc, width, height, labels, compact);
        } else {
            lastLayout = HudLayout.EMPTY;
        }
        drawPulses(graphics, mc);
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
            int color = AddictionClientState.badTripActive ? 0xFFFF4F6D
                    : displayedWithdrawal > 0.66F ? 0xFFE45A61
                    : displayedWithdrawal > 0.33F ? 0xFFC06C9A : 0xFF8D73D9;
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

    private static void drawEffectColumn(
            GuiGraphics graphics,
            Minecraft mc,
            int screenWidth,
            int screenHeight,
            boolean labels,
            boolean compact
    ) {
        boolean expanded = mc.screen != null;
        List<HudEffectEntry> entries = buildEffectEntries(expanded);
        if (entries.isEmpty()) {
            lastLayout = HudLayout.EMPTY;
            scrollOffset = 0;
            return;
        }

        float scale = Config.CLIENT.addictionHudScale.get().floatValue();
        int visibleLimit = compact ? Math.min(4, entries.size()) : entries.size();
        int visible = Math.min(visibleLimit, visibleSlots(screenHeight, expanded, scale));
        int maxOffset = Math.max(0, entries.size() - visible);
        scrollOffset = expanded ? Mth.clamp(scrollOffset, 0, maxOffset) : 0;
        int hidden = entries.size() - visible - scrollOffset;

        int labelWidth = labels ? entries.stream()
                .skip(scrollOffset)
                .limit(visible)
                .mapToInt(entry -> mc.font.width(entry.label()))
                .max().orElse(0) : 0;
        labelWidth = Math.min(MAX_LABEL_WIDTH, labelWidth);
        int logicalWidth = SYMPTOM_ICON_SIZE + (labels ? LABEL_GAP + labelWidth + 4 : 0);
        int logicalHeight = visible * SYMPTOM_ICON_SIZE + Math.max(0, visible - 1) * SYMPTOM_ICON_GAP;
        if (!expanded && hidden > 0) {
            logicalHeight += 9;
        }
        HudLayout layout = layout(screenWidth, screenHeight, logicalWidth, logicalHeight, scale);
        lastLayout = layout;

        graphics.pose().pushMatrix();
        graphics.pose().translate(layout.x(), layout.y());
        graphics.pose().scale(scale, scale);
        int index = scrollOffset;
        int y = 0;
        for (int row = 0; row < visible && index < entries.size(); row++, index++) {
            HudEffectEntry entry = entries.get(index);
            drawSymptomIcon(graphics, entry.texture(), 0, y, entry.intensity(), entry.comedown() || entry.fading());
            if (labels) {
                graphics.fill(SYMPTOM_ICON_SIZE + 2, y, logicalWidth, y + SYMPTOM_ICON_SIZE, 0xB0151917);
                graphics.drawString(mc.font, entry.label(), SYMPTOM_ICON_SIZE + LABEL_GAP, y + 4,
                        entry.priority() >= 90 ? MyDrugsUiTheme.DANGER : MyDrugsUiTheme.NORMAL_TEXT, false);
            }
            y += SYMPTOM_ICON_SIZE + SYMPTOM_ICON_GAP;
        }

        if (!expanded && hidden > 0) {
            drawMoreIndicator(graphics, mc, 0, y - SYMPTOM_ICON_GAP, hidden);
        }
        graphics.pose().popMatrix();

        if (expanded) {
            renderHoveredTooltip(graphics, mc, entries, visible);
        }
    }

    private static HudLayout layout(int screenWidth, int screenHeight, int logicalWidth, int logicalHeight, float scale) {
        int safe = Config.CLIENT.addictionHudSafeArea.get();
        int scaledWidth = Math.max(1, Math.round(logicalWidth * scale));
        int scaledHeight = Math.max(1, Math.round(logicalHeight * scale));
        String anchor = Config.Client.normalizeAddictionHudAnchor(Config.CLIENT.addictionHudAnchor.get());
        int x = switch (anchor) {
            case Config.Client.HUD_ANCHOR_RIGHT, Config.Client.HUD_ANCHOR_TOP_RIGHT,
                    Config.Client.HUD_ANCHOR_BOTTOM_RIGHT -> screenWidth - safe - scaledWidth;
            default -> safe;
        };
        int y = switch (anchor) {
            case Config.Client.HUD_ANCHOR_TOP_LEFT, Config.Client.HUD_ANCHOR_TOP_RIGHT -> safe;
            case Config.Client.HUD_ANCHOR_BOTTOM_LEFT, Config.Client.HUD_ANCHOR_BOTTOM_RIGHT ->
                    screenHeight - safe - scaledHeight;
            default -> screenHeight / 2 - scaledHeight / 2;
        };
        x = Mth.clamp(x, 0, Math.max(0, screenWidth - scaledWidth));
        y = Mth.clamp(y, 0, Math.max(0, screenHeight - scaledHeight));
        return new HudLayout(x, y, scaledWidth, scaledHeight, scale, anchor);
    }

    private static List<HudEffectEntry> buildEffectEntries(boolean expanded) {
        List<HudEffectEntry> entries = new ArrayList<>();
        Set<String> usedIcons = new HashSet<>();

        for (AddictionClientState.ClientEffectView view : AddictionClientState.activeEffectViews()) {
            String icon = HudSymptomIcons.iconNameForEffect(view.type());
            usedIcons.add(icon);
            entries.add(new HudEffectEntry(
                    HudSymptomIcons.texture(icon),
                    Component.translatable("effect_type.mydrugs." + view.type().serializedName()),
                    view.effectiveIntensity(),
                    view.remainingTicks(),
                    view.fading(),
                    view.comedown(),
                    priority(view.type(), icon)
            ));
        }

        for (HudSymptomIcons.HudSymptomIcon icon : HudSymptomIcons.LIST) {
            float intensity = Mth.clamp(icon.intensity(), 0.0F, 1.0F);
            if (intensity <= HudSymptomIcons.MIN_VISIBLE || (!expanded && usedIcons.contains(icon.iconName()))) {
                continue;
            }
            usedIcons.add(icon.iconName());
            entries.add(new HudEffectEntry(
                    icon.texture(),
                    Component.translatable("hud.mydrugs.symptom." + icon.iconName()),
                    intensity,
                    0,
                    false,
                    false,
                    priority(null, icon.iconName())
            ));
        }

        entries.sort(Comparator.comparingInt(HudEffectEntry::priority).reversed());
        return entries;
    }

    private static int priority(EffectType type, String icon) {
        if ("overdose".equals(icon)) {
            return 100;
        }
        if (AddictionClientState.badTripActive && Set.of(
                "hallucination", "vision", "confusion", "intrusive_thoughts", "dissociation", "stress").contains(icon)) {
            return 95;
        }
        if (type == EffectType.INPUT_FAIL || type == EffectType.STUMBLE || "input_fail".equals(icon)) {
            return 90;
        }
        if (type == EffectType.CAMERA_SWAY || type == EffectType.BLUR
                || (type != null && type.getCategory() == EffectCategory.SHADER)
                || "camera_sway".equals(icon) || "vision".equals(icon) || "blur".equals(icon)) {
            return 80;
        }
        return 0;
    }

    private static int visibleSlots(int height, boolean expanded, float scale) {
        if (!expanded) {
            return NORMAL_VISIBLE_ICONS;
        }
        int rowHeight = Math.max(1, Math.round((SYMPTOM_ICON_SIZE + SYMPTOM_ICON_GAP) * scale));
        return Math.max(3, (height - 32) / rowHeight);
    }

    private static void drawSymptomIcon(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            float intensity,
            boolean transitional
    ) {
        int alpha = 80 + Math.round(90.0F * Mth.clamp(intensity, 0.0F, 1.0F));
        int border = Config.CLIENT.colorblindSafeMode.get()
                ? MyDrugsUiTheme.STRONG_BORDER
                : transitional ? 0xFFDA9A56 : 0xFF1D1A24;
        graphics.fill(x - 1, y - 1, x + SYMPTOM_ICON_SIZE + 1, y + SYMPTOM_ICON_SIZE + 1,
                (alpha << 24) | (border & 0x00FFFFFF));
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0,
                SYMPTOM_ICON_SIZE, SYMPTOM_ICON_SIZE, 16, 16);
        int meter = Math.max(1, Math.round(SYMPTOM_ICON_SIZE * Mth.clamp(intensity, 0.0F, 1.0F)));
        graphics.fill(x, y + SYMPTOM_ICON_SIZE - 1, x + meter, y + SYMPTOM_ICON_SIZE,
                Config.CLIENT.colorblindSafeMode.get() ? 0xFFFFFFFF : 0xFFE6D06C);
    }

    private static void drawMoreIndicator(GuiGraphics graphics, Minecraft mc, int x, int y, int hidden) {
        String text = "+" + hidden;
        graphics.fill(x - 1, y - 1, x + SYMPTOM_ICON_SIZE + 1, y + 9, 0xAA08080B);
        graphics.drawString(mc.font, text, x + 1, y, MyDrugsUiTheme.WARNING, false);
    }

    private static void renderHoveredTooltip(GuiGraphics graphics, Minecraft mc, List<HudEffectEntry> entries, int visible) {
        int mouseX = scaledMouseX(mc);
        int mouseY = scaledMouseY(mc);
        if (!lastLayout.contains(mouseX, mouseY)) {
            return;
        }
        int logicalY = (int) ((mouseY - lastLayout.y()) / lastLayout.scale());
        int row = logicalY / (SYMPTOM_ICON_SIZE + SYMPTOM_ICON_GAP);
        if (row < 0 || row >= visible) {
            return;
        }
        int index = scrollOffset + row;
        if (index < 0 || index >= entries.size()) {
            return;
        }
        HudEffectEntry entry = entries.get(index);
        List<Component> lines = new ArrayList<>();
        lines.add(entry.label());
        if (entry.remainingTicks() > 0) {
            lines.add(Component.literal(AddictionClientState.formatDuration(entry.remainingTicks()))
                    .withStyle(ChatFormatting.GRAY));
        }
        graphics.renderTooltip(
                mc.font,
                lines.stream().map(line -> ClientTooltipComponent.create(line.getVisualOrderText())).toList(),
                mouseX,
                mouseY,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
    }

    private static void drawPulses(GuiGraphics graphics, Minecraft mc) {
        if (PULSES.isEmpty()) {
            return;
        }
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        boolean right = lastLayout.anchor().contains("right");
        int baseX = lastLayout.height() > 0
                ? (right ? Math.max(2, lastLayout.x() - 26) : Math.min(screenWidth - 18, lastLayout.x() + lastLayout.width() + 8))
                : Config.CLIENT.addictionHudSafeArea.get();
        int baseY = lastLayout.height() > 0 ? lastLayout.y() : mc.getWindow().getGuiScaledHeight() / 2 - 16;
        boolean reducedMotion = Config.CLIENT.reducedMotionMode.get();
        int index = 0;
        for (Pulse pulse : PULSES) {
            float progress = pulse.progress();
            int offsetY = reducedMotion ? 0 : Math.round(Mth.sin(progress * (float) Math.PI) * -4.0F);
            int x = baseX;
            int y = baseY + index * (SYMPTOM_ICON_SIZE + 2) + offsetY;
            int alpha = Math.round((1.0F - progress) * 150.0F);
            graphics.fill(x - 2, y - 2, x + 18, y + 18, (alpha << 24) | 0x201018);
            drawSymptomIcon(graphics, HudSymptomIcons.textureForEffect(pulse.type()), x, y,
                    pulse.intensity(), pulse.kind().isWarning());
            index++;
        }
    }

    private static int scaledMouseX(Minecraft mc) {
        return (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
    }

    private static int scaledMouseY(Minecraft mc) {
        return (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());
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
        };
    }

    private static ItemStack alcoholBottleStack() {
        ItemStack stack = new ItemStack(ModItems.GLASS_BOTTLE.get());
        GlassBottleItem.setContent(stack, ModFluids.rl("raw_alcohol"), GlassBottleItem.CAPACITY_MB);
        return stack;
    }

    public record HudBounds(int x, int y, int width, int height) {
        public boolean isVisible() {
            return width > 0 && height > 0;
        }
    }

    private record HudLayout(int x, int y, int width, int height, float scale, String anchor) {
        private static final HudLayout EMPTY = new HudLayout(0, 0, 0, 0, 1.0F, Config.Client.HUD_ANCHOR_LEFT);

        private boolean contains(double mouseX, double mouseY) {
            return width > 0 && height > 0
                    && mouseX >= x - 2 && mouseX <= x + width + 2
                    && mouseY >= y - 2 && mouseY <= y + height + 2;
        }
    }

    private record HudEffectEntry(
            ResourceLocation texture,
            Component label,
            float intensity,
            int remainingTicks,
            boolean fading,
            boolean comedown,
            int priority
    ) {
    }

    private static final class Pulse {
        private final EffectType type;
        private final DrugEffectCueKind kind;
        private final float intensity;
        private int ticks;

        private Pulse(EffectType type, DrugEffectCueKind kind, float intensity) {
            this.type = type;
            this.kind = kind;
            this.intensity = intensity;
        }

        private boolean tick() {
            ticks++;
            return ticks >= PULSE_DURATION_TICKS;
        }

        private float progress() {
            return Math.clamp(ticks / (float) PULSE_DURATION_TICKS, 0.0F, 1.0F);
        }

        private EffectType type() {
            return type;
        }

        private DrugEffectCueKind kind() {
            return kind;
        }

        private float intensity() {
            return intensity;
        }
    }
}
