package org.mydrugs.mydrugs.client.accessibility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.mydrugs.mydrugs.Config;

/**
 * Client-only accessibility configuration screen. Presets sit at the top, the four category tabs
 * group the individual controls, and the footer offers a self-contained visual preview, a reset,
 * and a close button. Every control here is client presentation only; server gameplay mechanics
 * remain authoritative and are never written from this screen.
 */
public final class AccessibilityScreen extends Screen {
    private static final int PANEL_WIDTH = 460;
    private static final int HEADER_H = 34;
    private static final int FOOTER_H = 34;
    private static final int PAD = 10;
    private static final int ROW_H = 23;
    private static final int PREVIEW_DURATION_TICKS = 140;

    private static final int COL_DIM = 0xE8070A09;
    private static final int COL_PANEL = 0xFF151C19;
    private static final int COL_HEADER = 0xFF1F2A26;
    private static final int COL_ACCENT = 0xFF4E8C77;
    private static final int COL_ACCENT_DIM = 0xFF2B3F37;
    private static final int COL_TEXT = 0xFFE7F1EC;
    private static final int COL_TEXT_DIM = 0xFF6F837B;
    private static final int COL_HEADING = 0xFFAEDCC6;

    private enum Tab {
        VISUAL("visual"),
        MOTION("motion"),
        AUDIO("audio"),
        PRESENTATION("presentation");

        final String key;

        Tab(String key) {
            this.key = key;
        }
    }

    private record ControlDef(String nameKey, ModConfigSpec.ConfigValue<?> value, double max, boolean slider) {
        static ControlDef toggle(String nameKey, ModConfigSpec.BooleanValue value) {
            return new ControlDef(nameKey, value, 0.0D, false);
        }

        static ControlDef slider(String nameKey, ModConfigSpec.DoubleValue value, double max) {
            return new ControlDef(nameKey, value, max, true);
        }
    }

    private final Screen parent;
    private Tab currentTab = Tab.VISUAL;
    private String selectedPreset;
    private final List<Button> presetButtons = new ArrayList<>();
    private Button stopPreviewButton;
    private int previewTicks;

    public AccessibilityScreen(Screen parent) {
        super(Component.translatable("screen.mydrugs.accessibility.title"));
        this.parent = parent;
        this.selectedPreset = Config.Client.normalizePreset(Config.CLIENT.accessibilityPreset.get());
    }

    private static List<ControlDef> controlsFor(Tab tab) {
        Config.Client c = Config.CLIENT;
        return switch (tab) {
            case VISUAL -> List.of(
                    ControlDef.toggle("enablePsychedelicShaders", c.enablePsychedelicShaders),
                    ControlDef.slider("shaderIntensity", c.shaderIntensity, 2.0D),
                    ControlDef.slider("overlayIntensity", c.overlayIntensity, 1.0D),
                    ControlDef.slider("colorShiftIntensity", c.colorShiftIntensity, 1.0D),
                    ControlDef.slider("blurIntensity", c.blurIntensity, 1.0D),
                    ControlDef.slider("staticNoiseIntensity", c.staticNoiseIntensity, 1.0D),
                    ControlDef.slider("maxFlashBrightness", c.maxFlashBrightness, 1.0D),
                    ControlDef.toggle("disableFullScreenFlashing", c.disableFullScreenFlashing),
                    ControlDef.toggle("colorblindSafeMode", c.colorblindSafeMode),
                    ControlDef.slider("particleDensityMultiplier", c.particleDensityMultiplier, 1.0D),
                    ControlDef.slider("dimensionFogIntensity", c.dimensionFogIntensity, 1.0D),
                    ControlDef.slider("oreAuraIntensity", c.oreAuraIntensity, 1.0D),
                    ControlDef.toggle("enableHallucinations", c.enableHallucinations),
                    ControlDef.slider("hallucinationIntensity", c.hallucinationIntensity, 1.0D));
            case MOTION -> List.of(
                    ControlDef.toggle("enableCameraShake", c.enableCameraShake),
                    ControlDef.slider("cameraShakeIntensity", c.cameraShakeIntensity, 2.0D),
                    ControlDef.slider("cameraSwayIntensity", c.cameraSwayIntensity, 1.0D),
                    ControlDef.slider("fovPulseIntensity", c.fovPulseIntensity, 1.0D),
                    ControlDef.toggle("disableForcedCameraMovement", c.disableForcedCameraMovement),
                    ControlDef.toggle("disableScreenRoll", c.disableScreenRoll),
                    ControlDef.toggle("reducedMotionMode", c.reducedMotionMode));
            case AUDIO -> List.of(
                    ControlDef.toggle("enableHeartbeatSounds", c.enableHeartbeatSounds),
                    ControlDef.slider("heartbeatVolume", c.heartbeatVolume, 1.0D),
                    ControlDef.toggle("enableDrugSounds", c.enableDrugSounds),
                    ControlDef.slider("hallucinationSoundVolume", c.hallucinationSoundVolume, 1.0D),
                    ControlDef.slider("machineBreathingVolume", c.machineBreathingVolume, 1.0D),
                    ControlDef.slider("badTripVoiceVolume", c.badTripVoiceVolume, 1.0D),
                    ControlDef.toggle("disableSuddenLoudSounds", c.disableSuddenLoudSounds),
                    ControlDef.slider("screamerVolumeCap", c.screamerVolumeCap, 1.0D),
                    ControlDef.toggle("muteScreamers", c.muteScreamers));
            case PRESENTATION -> List.of(
                    ControlDef.toggle("showAddictionHud", c.showAddictionHud),
                    ControlDef.toggle("compactAddictionHud", c.compactAddictionHud),
                    ControlDef.toggle("enableBadTripScreamers", c.enableBadTripScreamers),
                    ControlDef.slider("screamerIntensity", c.screamerIntensity, 2.0D),
                    ControlDef.toggle("replaceInputFailWithHudWarningOnly", c.replaceInputFailWithHudWarningOnly),
                    ControlDef.toggle("showActiveEffectExplanations", c.showActiveEffectExplanations),
                    ControlDef.toggle("showMachineMoodStatusText", c.showMachineMoodStatusText),
                    ControlDef.toggle("showGeneratorCravingsAsText", c.showGeneratorCravingsAsText));
        };
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(PANEL_WIDTH, width - 24);
        int panelLeft = (width - panelWidth) / 2;
        int innerLeft = panelLeft + PAD;
        int innerWidth = panelWidth - PAD * 2;

        // --- preset row ---
        presetButtons.clear();
        String[] presets = {
                Config.Client.PRESET_FULL_EXPERIENCE, Config.Client.PRESET_REDUCED_MOTION,
                Config.Client.PRESET_LOW_INTENSITY, Config.Client.PRESET_MINIMAL_EFFECTS,
                Config.Client.PRESET_CUSTOM
        };
        int presetGap = 4;
        int presetW = (innerWidth - presetGap * (presets.length - 1)) / presets.length;
        int presetY = HEADER_H + 4;
        for (int i = 0; i < presets.length; i++) {
            String preset = presets[i];
            Button button = Button.builder(
                            Component.translatable("screen.mydrugs.accessibility.preset." + preset),
                            b -> onPresetSelected(preset))
                    .bounds(innerLeft + i * (presetW + presetGap), presetY, presetW, 18)
                    .build();
            // The Custom entry is a status indicator, not an action.
            button.active = !preset.equals(Config.Client.PRESET_CUSTOM);
            presetButtons.add(button);
            addRenderableWidget(button);
        }

        // --- tab row ---
        int tabGap = 4;
        int tabW = (innerWidth - tabGap * (Tab.values().length - 1)) / Tab.values().length;
        int tabY = presetY + 22;
        for (int i = 0; i < Tab.values().length; i++) {
            Tab tab = Tab.values()[i];
            Button button = Button.builder(
                            Component.translatable("screen.mydrugs.accessibility.tab." + tab.key),
                            b -> {
                                currentTab = tab;
                                rebuildWidgets();
                            })
                    .bounds(innerLeft + i * (tabW + tabGap), tabY, tabW, 18)
                    .build();
            button.active = tab != currentTab;
            addRenderableWidget(button);
        }

        // --- control grid (two columns) ---
        int gridTop = tabY + 24;
        int colGap = 8;
        int slotW = (innerWidth - colGap) / 2;
        List<ControlDef> controls = controlsFor(currentTab);
        for (int i = 0; i < controls.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            int x = innerLeft + col * (slotW + colGap);
            int y = gridTop + row * ROW_H;
            addRenderableWidget(buildControl(controls.get(i), x, y, slotW));
        }

        // --- footer ---
        int footerY = height - FOOTER_H + 7;
        int footerW = 78;
        addRenderableWidget(Button.builder(
                        Component.translatable("button.mydrugs.accessibility.preview"),
                        b -> previewTicks = PREVIEW_DURATION_TICKS)
                .bounds(innerLeft, footerY, footerW, 20)
                .build());
        stopPreviewButton = Button.builder(
                        Component.translatable("button.mydrugs.accessibility.stop"),
                        b -> previewTicks = 0)
                .bounds(innerLeft + footerW + 4, footerY, footerW, 20)
                .build();
        addRenderableWidget(stopPreviewButton);
        addRenderableWidget(Button.builder(
                        Component.translatable("button.mydrugs.accessibility.reset"),
                        b -> {
                            AccessibilityPresets.restoreDefaults();
                            selectedPreset = Config.Client.PRESET_FULL_EXPERIENCE;
                            rebuildWidgets();
                        })
                .bounds(innerLeft + (footerW + 4) * 2, footerY, footerW, 20)
                .build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(innerLeft + innerWidth - footerW, footerY, footerW, 20)
                .build());

        refreshPresetButtons();
    }

    private void onPresetSelected(String preset) {
        AccessibilityPresets.apply(preset);
        selectedPreset = preset;
        rebuildWidgets();
    }

    private AbstractWidget buildControl(ControlDef def, int x, int y, int width) {
        Component label = Component.translatable("screen.mydrugs.accessibility.control." + def.nameKey());
        if (def.slider()) {
            return new IntensitySlider(x, y, width, (ModConfigSpec.DoubleValue) def.value(), def.max(), label);
        }
        ModConfigSpec.BooleanValue value = (ModConfigSpec.BooleanValue) def.value();
        return Button.builder(toggleMessage(label, value.get()), b -> {
                    value.set(!value.get());
                    b.setMessage(toggleMessage(label, value.get()));
                    markCustom();
                })
                .bounds(x, y, width, 20)
                .build();
    }

    private static Component toggleMessage(Component label, boolean on) {
        return Component.empty().append(label).append(": ")
                .append(on ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    private void markCustom() {
        Config.CLIENT.accessibilityPreset.set(Config.Client.PRESET_CUSTOM);
        selectedPreset = Config.Client.PRESET_CUSTOM;
        refreshPresetButtons();
    }

    private void refreshPresetButtons() {
        // The selected preset's button is greyed (inactive); Custom is always a status-only chip.
        String[] presets = {
                Config.Client.PRESET_FULL_EXPERIENCE, Config.Client.PRESET_REDUCED_MOTION,
                Config.Client.PRESET_LOW_INTENSITY, Config.Client.PRESET_MINIMAL_EFFECTS,
                Config.Client.PRESET_CUSTOM
        };
        for (int i = 0; i < presetButtons.size() && i < presets.length; i++) {
            String preset = presets[i];
            boolean isCustom = preset.equals(Config.Client.PRESET_CUSTOM);
            presetButtons.get(i).active = !isCustom && !preset.equals(selectedPreset);
        }
    }

    @Override
    public void tick() {
        if (previewTicks > 0) {
            previewTicks--;
        }
        if (stopPreviewButton != null) {
            stopPreviewButton.active = previewTicks > 0;
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int panelWidth = Math.min(PANEL_WIDTH, width - 24);
        int panelLeft = (width - panelWidth) / 2;
        int panelRight = panelLeft + panelWidth;

        graphics.fill(0, 0, width, height, COL_DIM);
        graphics.fill(panelLeft, 0, panelRight, height, COL_PANEL);
        graphics.fill(panelLeft, 0, panelLeft + 1, height, COL_ACCENT_DIM);
        graphics.fill(panelRight - 1, 0, panelRight, height, COL_ACCENT_DIM);

        graphics.fill(panelLeft, 0, panelRight, HEADER_H, COL_HEADER);
        graphics.fill(panelLeft, HEADER_H - 1, panelRight, HEADER_H, COL_ACCENT);
        graphics.drawCenteredString(font, title, (panelLeft + panelRight) / 2, 6, COL_TEXT);
        graphics.drawCenteredString(font,
                Component.translatable("screen.mydrugs.accessibility.subtitle"),
                (panelLeft + panelRight) / 2, 18, COL_TEXT_DIM);

        graphics.fill(panelLeft, height - FOOTER_H, panelRight, height - FOOTER_H + 1, COL_ACCENT_DIM);

        if (previewTicks > 0) {
            renderPreview(graphics, panelLeft, panelRight);
        }
    }

    /**
     * Self-contained preview of the current visual intensities. Honors disableFullScreenFlashing by
     * keeping the demo a steady, non-flashing tint, and never plays audio.
     */
    private void renderPreview(GuiGraphics graphics, int panelLeft, int panelRight) {
        float overlay = Config.CLIENT.overlayIntensity.get().floatValue();
        float colorShift = Config.CLIENT.colorShiftIntensity.get().floatValue();
        float flashCap = Config.CLIENT.maxFlashBrightness.get().floatValue();
        float fade = Mth.clamp(previewTicks / 20.0F, 0.0F, 1.0F);

        float phase = (PREVIEW_DURATION_TICKS - previewTicks) / 40.0F;
        float hue = (phase * 0.15F) % 1.0F;
        int rgb = Mth.hsvToRgb(hue, 0.55F * colorShift, 0.85F);
        float steadyAlpha = 0.32F * overlay * fade;
        if (!Config.CLIENT.disableFullScreenFlashing.get()) {
            steadyAlpha += 0.10F * flashCap * fade * (0.5F + 0.5F * Mth.sin(phase * 3.0F));
        }
        int alpha = Mth.clamp((int) (steadyAlpha * 255.0F), 0, 200);
        graphics.fill(panelLeft + 1, HEADER_H, panelRight - 1, height - FOOTER_H,
                (alpha << 24) | (rgb & 0xFFFFFF));
        graphics.drawCenteredString(font,
                Component.translatable("screen.mydrugs.accessibility.preview_active"),
                (panelLeft + panelRight) / 2, height - FOOTER_H - 14, COL_HEADING);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int panelWidth = Math.min(PANEL_WIDTH, width - 24);
        int panelLeft = (width - panelWidth) / 2;
        graphics.drawString(font,
                Component.translatable("screen.mydrugs.accessibility.note"),
                panelLeft + PAD, height - FOOTER_H - 12, COL_TEXT_DIM, false);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private final class IntensitySlider extends AbstractSliderButton {
        private final ModConfigSpec.DoubleValue cfg;
        private final double max;
        private final Component label;

        IntensitySlider(int x, int y, int width, ModConfigSpec.DoubleValue cfg, double max, Component label) {
            super(x, y, width, 20, Component.empty(),
                    Mth.clamp(cfg.get() / max, 0.0D, 1.0D));
            this.cfg = cfg;
            this.max = max;
            this.label = label;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double v = value * max;
            String shown = max > 1.0D
                    ? String.format(Locale.ROOT, "%.2fx", v)
                    : Math.round(v * 100.0D) + "%";
            setMessage(Component.empty().append(label).append(": ").append(shown));
        }

        @Override
        protected void applyValue() {
            cfg.set(value * max);
            markCustom();
        }
    }
}
