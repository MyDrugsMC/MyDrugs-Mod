package org.mydrugs.mydrugs.client.effects.hud;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;
import org.mydrugs.mydrugs.client.effects.hallucination.FakeEntityRenderController;
import org.mydrugs.mydrugs.client.shaders.ShaderManager;
import org.mydrugs.mydrugs.client.shaders.WithdrawalTunnelShader;
import org.mydrugs.mydrugs.client.ui.MyDrugsUiTheme;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

public final class ActiveVisualEffectsPanel {
    private static final int MAX_LINES = 6;
    private static final int PAD = 5;
    private static final int LINE_HEIGHT = 10;

    private ActiveVisualEffectsPanel() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (!Config.CLIENT.showActiveEffectExplanations.get()
                || mc.options.hideGui
                || mc.player == null) {
            return;
        }

        List<Component> lines = buildLines();
        if (lines.isEmpty()) {
            return;
        }

        int panelWidth = lines.stream().mapToInt(mc.font::width).max().orElse(80) + PAD * 2;
        int panelHeight = lines.size() * LINE_HEIGHT + PAD * 2;
        int safe = Math.max(4, Config.CLIENT.addictionHudSafeArea.get());
        int x = safe;
        int y = safe;
        AddictionHudRenderer.HudBounds hud = AddictionHudRenderer.occupiedBounds();
        if (hud.isVisible() && intersects(x, y, panelWidth, panelHeight, hud)) {
            x = mc.getWindow().getGuiScaledWidth() - safe - panelWidth;
        }

        graphics.fill(x, y, x + panelWidth, y + panelHeight, MyDrugsUiTheme.PANEL_BACKGROUND);
        MyDrugsUiTheme.drawBorder(graphics, x, y, panelWidth, panelHeight, MyDrugsUiTheme.contrastBorder());
        int lineY = y + PAD;
        for (Component line : lines) {
            graphics.drawString(mc.font, line, x + PAD, lineY, MyDrugsUiTheme.NORMAL_TEXT, false);
            lineY += LINE_HEIGHT;
        }
    }

    private static List<Component> buildLines() {
        List<Component> lines = new ArrayList<>();
        ShaderManager.INSTANCE.strongestActiveShader().ifPresent(info -> lines.add(Component.translatable(
                "hud.mydrugs.active_visual.shader",
                Component.translatable("effect_type.mydrugs." + info.type().serializedName()),
                percent(info.strength()))));

        float withdrawal = WithdrawalTunnelShader.INSTANCE.getStrength();
        if (withdrawal > 0.01F) {
            lines.add(Component.translatable("hud.mydrugs.active_visual.withdrawal", percent(withdrawal)));
        }

        float blur = AddictionClientState.getEffectIntensity(EffectType.BLUR);
        if (blur > 0.01F) {
            lines.add(Component.translatable("hud.mydrugs.active_visual.overlay",
                    Component.translatable("effect_type.mydrugs.blur"), percent(blur)));
        }

        int hallucinations = FakeEntityRenderController.activeCount();
        if (hallucinations > 0 && !FakeEntityRenderController.isPresentationSuppressed()) {
            lines.add(Component.translatable("hud.mydrugs.active_visual.hallucinations", hallucinations));
        }

        boolean meaningfulVisual = !lines.isEmpty();
        if (!meaningfulVisual) {
            return List.of();
        }
        if (Config.CLIENT.disableFullScreenFlashing.get() && lines.size() < MAX_LINES) {
            lines.add(Component.translatable("hud.mydrugs.active_visual.flashing_disabled"));
        }
        if (Config.CLIENT.reducedMotionMode.get() && lines.size() < MAX_LINES) {
            lines.add(Component.translatable("hud.mydrugs.active_visual.reduced_motion"));
        }
        if (FakeEntityRenderController.isPresentationSuppressed()
                && AddictionClientState.badTripActive
                && lines.size() < MAX_LINES) {
            lines.add(Component.translatable("hud.mydrugs.active_visual.hallucinations_suppressed"));
        }
        return lines.size() <= MAX_LINES ? List.copyOf(lines) : List.copyOf(lines.subList(0, MAX_LINES));
    }

    private static int percent(float strength) {
        return Math.round(Mth.clamp(strength, 0.0F, 2.0F) * 100.0F);
    }

    private static boolean intersects(
            int x,
            int y,
            int width,
            int height,
            AddictionHudRenderer.HudBounds other
    ) {
        return x < other.x() + other.width()
                && x + width > other.x()
                && y < other.y() + other.height()
                && y + height > other.y();
    }
}
