package org.mydrugs.mydrugs.client.accessibility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.lwjgl.glfw.GLFW;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.client.ui.MyDrugsUiTheme;

public final class AccessibilityScreen extends Screen {
    private static final int PANEL_WIDTH = 460;
    private static final int HEADER_H = 34;
    private static final int FOOTER_H = 34;
    private static final int PAD = 10;
    private static final int ROW_H = 23;
    private static final int PREVIEW_DURATION_TICKS = 140;
    private static final int SCROLL_STEP = ROW_H * 2;

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

    private enum ControlType {
        TOGGLE,
        DOUBLE,
        INTEGER,
        HUD_ANCHOR
    }

    private record ControlDef(
            String nameKey,
            ModConfigSpec.ConfigValue<?> value,
            ControlType type,
            double min,
            double max,
            BooleanSupplier enabled,
            String disabledReason
    ) {
        static ControlDef toggle(String key, ModConfigSpec.BooleanValue value) {
            return toggle(key, value, () -> true, null);
        }

        static ControlDef toggle(String key, ModConfigSpec.BooleanValue value, BooleanSupplier enabled, String reason) {
            return new ControlDef(key, value, ControlType.TOGGLE, 0.0D, 1.0D, enabled, reason);
        }

        static ControlDef slider(String key, ModConfigSpec.DoubleValue value, double max) {
            return slider(key, value, 0.0D, max, () -> true, null);
        }

        static ControlDef slider(
                String key,
                ModConfigSpec.DoubleValue value,
                double min,
                double max,
                BooleanSupplier enabled,
                String reason
        ) {
            return new ControlDef(key, value, ControlType.DOUBLE, min, max, enabled, reason);
        }

        static ControlDef integer(
                String key,
                ModConfigSpec.IntValue value,
                int min,
                int max,
                BooleanSupplier enabled,
                String reason
        ) {
            return new ControlDef(key, value, ControlType.INTEGER, min, max, enabled, reason);
        }

        static ControlDef hudAnchor(
                String key,
                ModConfigSpec.ConfigValue<String> value,
                BooleanSupplier enabled,
                String reason
        ) {
            return new ControlDef(key, value, ControlType.HUD_ANCHOR, 0.0D, 0.0D, enabled, reason);
        }
    }

    private final Screen parent;
    private final List<Button> presetButtons = new ArrayList<>();
    private final List<DisabledControl> disabledControls = new ArrayList<>();
    private Tab currentTab = Tab.VISUAL;
    private String selectedPreset;
    private Button stopPreviewButton;
    private int previewTicks;
    private int controlsTop;
    private int controlsBottom;
    private int controlsLeft;
    private int controlsRight;
    private int contentHeight;
    private int scroll;

    public AccessibilityScreen(Screen parent) {
        super(Component.translatable("screen.mydrugs.accessibility.title"));
        this.parent = parent;
        this.selectedPreset = Config.Client.normalizePreset(Config.CLIENT.accessibilityPreset.get());
    }

    private static List<ControlDef> controlsFor(Tab tab) {
        Config.Client c = Config.CLIENT;
        BooleanSupplier shaders = c.enablePsychedelicShaders::get;
        BooleanSupplier hallucinations = c.enableHallucinations::get;
        BooleanSupplier cameraMotion = () -> c.enableCameraShake.get()
                && !c.disableForcedCameraMovement.get()
                && !c.reducedMotionMode.get();
        BooleanSupplier heartbeat = c.enableHeartbeatSounds::get;
        BooleanSupplier drugAudio = c.enableDrugSounds::get;
        BooleanSupplier screamersAudible = () -> !c.muteScreamers.get();
        BooleanSupplier screamersVisible = c.enableBadTripScreamers::get;
        BooleanSupplier hud = c.showAddictionHud::get;

        return switch (tab) {
            case VISUAL -> List.of(
                    ControlDef.toggle("enablePsychedelicShaders", c.enablePsychedelicShaders),
                    ControlDef.slider("shaderIntensity", c.shaderIntensity, 0.0D, 2.0D, shaders, "shaders_off"),
                    ControlDef.slider("overlayIntensity", c.overlayIntensity, 1.0D),
                    ControlDef.slider("colorShiftIntensity", c.colorShiftIntensity, 0.0D, 1.0D, shaders, "shaders_off"),
                    ControlDef.slider("blurIntensity", c.blurIntensity, 0.0D, 1.0D, shaders, "shaders_off"),
                    ControlDef.slider("staticNoiseIntensity", c.staticNoiseIntensity, 0.0D, 1.0D, shaders, "shaders_off"),
                    ControlDef.slider("maxFlashBrightness", c.maxFlashBrightness, 0.0D, 1.0D, shaders, "shaders_off"),
                    ControlDef.toggle("disableFullScreenFlashing", c.disableFullScreenFlashing),
                    ControlDef.toggle("colorblindSafeMode", c.colorblindSafeMode),
                    ControlDef.slider("particleDensityMultiplier", c.particleDensityMultiplier, 1.0D),
                    ControlDef.slider("dimensionFogIntensity", c.dimensionFogIntensity, 1.0D),
                    ControlDef.slider("oreAuraIntensity", c.oreAuraIntensity, 1.0D),
                    ControlDef.toggle("enableHallucinations", c.enableHallucinations),
                    ControlDef.slider("hallucinationIntensity", c.hallucinationIntensity, 0.0D, 1.0D,
                            hallucinations, "hallucinations_off"),
                    ControlDef.toggle("hallucinationSilhouetteOnly", c.hallucinationSilhouetteOnly,
                            hallucinations, "hallucinations_off"),
                    ControlDef.toggle("smoothHallucinationTransitions", c.smoothHallucinationTransitions,
                            hallucinations, "hallucinations_off"),
                    ControlDef.slider("hallucinationSpawnDistanceScale", c.hallucinationSpawnDistanceScale,
                            0.5D, 2.0D, hallucinations, "hallucinations_off"));
            case MOTION -> List.of(
                    ControlDef.toggle("enableCameraShake", c.enableCameraShake),
                    ControlDef.slider("cameraShakeIntensity", c.cameraShakeIntensity, 0.0D, 2.0D,
                            cameraMotion, "camera_motion_off"),
                    ControlDef.slider("cameraSwayIntensity", c.cameraSwayIntensity, 0.0D, 1.0D,
                            cameraMotion, "camera_motion_off"),
                    ControlDef.slider("fovPulseIntensity", c.fovPulseIntensity, 0.0D, 1.0D,
                            cameraMotion, "camera_motion_off"),
                    ControlDef.toggle("disableForcedCameraMovement", c.disableForcedCameraMovement),
                    ControlDef.toggle("disableScreenRoll", c.disableScreenRoll, cameraMotion, "camera_motion_off"),
                    ControlDef.toggle("reducedMotionMode", c.reducedMotionMode));
            case AUDIO -> List.of(
                    ControlDef.toggle("enableHeartbeatSounds", c.enableHeartbeatSounds),
                    ControlDef.slider("heartbeatVolume", c.heartbeatVolume, 0.0D, 1.0D,
                            heartbeat, "heartbeat_off"),
                    ControlDef.toggle("enableDrugSounds", c.enableDrugSounds),
                    ControlDef.slider("hallucinationSoundVolume", c.hallucinationSoundVolume, 0.0D, 1.0D,
                            drugAudio, "drug_audio_off"),
                    ControlDef.slider("machineBreathingVolume", c.machineBreathingVolume, 1.0D),
                    ControlDef.slider("badTripVoiceVolume", c.badTripVoiceVolume, 0.0D, 1.0D,
                            drugAudio, "drug_audio_off"),
                    ControlDef.toggle("disableSuddenLoudSounds", c.disableSuddenLoudSounds),
                    ControlDef.slider("screamerVolumeCap", c.screamerVolumeCap, 0.0D, 1.0D,
                            screamersAudible, "screamers_muted"),
                    ControlDef.toggle("muteScreamers", c.muteScreamers));
            case PRESENTATION -> List.of(
                    ControlDef.toggle("showAddictionHud", c.showAddictionHud),
                    ControlDef.toggle("compactAddictionHud", c.compactAddictionHud, hud, "hud_off"),
                    ControlDef.hudAnchor("addictionHudAnchor", c.addictionHudAnchor, hud, "hud_off"),
                    ControlDef.slider("addictionHudScale", c.addictionHudScale, 0.65D, 1.75D, hud, "hud_off"),
                    ControlDef.toggle("addictionHudTextLabels", c.addictionHudTextLabels, hud, "hud_off"),
                    ControlDef.integer("addictionHudSafeArea", c.addictionHudSafeArea, 0, 48, hud, "hud_off"),
                    ControlDef.toggle("enableBadTripScreamers", c.enableBadTripScreamers),
                    ControlDef.slider("screamerIntensity", c.screamerIntensity, 0.0D, 2.0D,
                            screamersVisible, "screamers_off"),
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

        presetButtons.clear();
        disabledControls.clear();
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
            button.active = !preset.equals(Config.Client.PRESET_CUSTOM);
            presetButtons.add(button);
            addRenderableWidget(button);
        }

        int tabGap = 4;
        int tabW = (innerWidth - tabGap * (Tab.values().length - 1)) / Tab.values().length;
        int tabY = presetY + 22;
        for (int i = 0; i < Tab.values().length; i++) {
            Tab tab = Tab.values()[i];
            Button button = Button.builder(
                            Component.translatable("screen.mydrugs.accessibility.tab." + tab.key),
                            b -> {
                                currentTab = tab;
                                scroll = 0;
                                rebuildWidgets();
                            })
                    .bounds(innerLeft + i * (tabW + tabGap), tabY, tabW, 18)
                    .build();
            button.active = tab != currentTab;
            addRenderableWidget(button);
        }

        controlsTop = tabY + 24;
        controlsBottom = Math.max(controlsTop, height - FOOTER_H - 16);
        controlsLeft = innerLeft;
        controlsRight = innerLeft + innerWidth;
        addControlWidgets(innerLeft, innerWidth);

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
                            scroll = 0;
                            rebuildWidgets();
                        })
                .bounds(innerLeft + (footerW + 4) * 2, footerY, footerW, 20)
                .build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(innerLeft + innerWidth - footerW, footerY, footerW, 20)
                .build());

        refreshPresetButtons();
    }

    private void addControlWidgets(int innerLeft, int innerWidth) {
        int columns = innerWidth >= 360 ? 2 : 1;
        int colGap = columns == 2 ? 8 : 0;
        int slotW = (innerWidth - colGap) / columns;
        List<ControlDef> controls = controlsFor(currentTab);
        int rows = (controls.size() + columns - 1) / columns;
        contentHeight = rows * ROW_H;
        clampScroll();

        for (int i = 0; i < controls.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int x = innerLeft + col * (slotW + colGap);
            int y = controlsTop + row * ROW_H - scroll;
            if (y < controlsTop || y + 20 > controlsBottom) {
                continue;
            }
            ControlDef def = controls.get(i);
            boolean enabled = def.enabled().getAsBoolean();
            AbstractWidget widget = buildControl(def, x, y, slotW, enabled);
            addRenderableWidget(widget);
            if (!enabled) {
                disabledControls.add(new DisabledControl(x, y, slotW, 20));
            }
        }
    }

    private void onPresetSelected(String preset) {
        AccessibilityPresets.apply(preset);
        selectedPreset = preset;
        scroll = 0;
        rebuildWidgets();
    }

    private AbstractWidget buildControl(ControlDef def, int x, int y, int width, boolean enabled) {
        Component label = Component.translatable("screen.mydrugs.accessibility.control." + def.nameKey());
        AbstractWidget widget = switch (def.type()) {
            case DOUBLE, INTEGER -> new ConfigSlider(x, y, width, def, label);
            case HUD_ANCHOR -> Button.builder(anchorMessage(label), b -> {
                        cycleHudAnchor();
                        b.setMessage(anchorMessage(label));
                        markCustom();
                    })
                    .bounds(x, y, width, 20)
                    .build();
            case TOGGLE -> {
                ModConfigSpec.BooleanValue value = (ModConfigSpec.BooleanValue) def.value();
                yield Button.builder(toggleMessage(label, value.get()), b -> {
                            value.set(!value.get());
                            b.setMessage(toggleMessage(label, value.get()));
                            markCustom();
                            rebuildWidgets();
                        })
                        .bounds(x, y, width, 20)
                        .build();
            }
        };
        widget.active = enabled;
        if (!enabled && def.disabledReason() != null) {
            widget.setTooltip(Tooltip.create(Component.translatable(
                    "screen.mydrugs.accessibility.disabled." + def.disabledReason())));
        }
        return widget;
    }

    private static Component toggleMessage(Component label, boolean on) {
        return Component.empty().append(label).append(": ")
                .append(on ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    private static Component anchorMessage(Component label) {
        String anchor = Config.Client.normalizeAddictionHudAnchor(Config.CLIENT.addictionHudAnchor.get());
        return Component.empty().append(label).append(": ")
                .append(Component.translatable("screen.mydrugs.accessibility.hud_anchor." + anchor));
    }

    private static void cycleHudAnchor() {
        String[] anchors = {
                Config.Client.HUD_ANCHOR_LEFT, Config.Client.HUD_ANCHOR_RIGHT,
                Config.Client.HUD_ANCHOR_TOP_LEFT, Config.Client.HUD_ANCHOR_TOP_RIGHT,
                Config.Client.HUD_ANCHOR_BOTTOM_LEFT, Config.Client.HUD_ANCHOR_BOTTOM_RIGHT
        };
        String current = Config.Client.normalizeAddictionHudAnchor(Config.CLIENT.addictionHudAnchor.get());
        int index = 0;
        for (int i = 0; i < anchors.length; i++) {
            if (anchors[i].equals(current)) {
                index = i;
                break;
            }
        }
        Config.CLIENT.addictionHudAnchor.set(anchors[(index + 1) % anchors.length]);
    }

    private void markCustom() {
        Config.CLIENT.accessibilityPreset.set(Config.Client.PRESET_CUSTOM);
        selectedPreset = Config.Client.PRESET_CUSTOM;
        refreshPresetButtons();
    }

    private void refreshPresetButtons() {
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

    private int maxScroll() {
        return Math.max(0, contentHeight - Math.max(0, controlsBottom - controlsTop));
    }

    private void clampScroll() {
        scroll = Mth.clamp(scroll, 0, maxScroll());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= controlsLeft && mouseX <= controlsRight
                && mouseY >= controlsTop && mouseY <= controlsBottom
                && maxScroll() > 0) {
            scroll = Mth.clamp(scroll - (int) Math.round(scrollY * SCROLL_STEP), 0, maxScroll());
            rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int page = Math.max(ROW_H, controlsBottom - controlsTop);
        switch (event.key()) {
            case GLFW.GLFW_KEY_DOWN -> scroll += SCROLL_STEP;
            case GLFW.GLFW_KEY_UP -> scroll -= SCROLL_STEP;
            case GLFW.GLFW_KEY_PAGE_DOWN -> scroll += page;
            case GLFW.GLFW_KEY_PAGE_UP -> scroll -= page;
            case GLFW.GLFW_KEY_HOME -> scroll = 0;
            case GLFW.GLFW_KEY_END -> scroll = maxScroll();
            default -> {
                return super.keyPressed(event);
            }
        }
        clampScroll();
        rebuildWidgets();
        return true;
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

        graphics.fill(0, 0, width, height, 0xE8070A09);
        graphics.fill(panelLeft, 0, panelRight, height, MyDrugsUiTheme.PANEL_BACKGROUND);
        MyDrugsUiTheme.drawBorder(graphics, panelLeft, 0, panelWidth, height, MyDrugsUiTheme.SUBTLE_BORDER);
        graphics.fill(panelLeft, 0, panelRight, HEADER_H, MyDrugsUiTheme.HEADER);
        graphics.fill(panelLeft, HEADER_H - 1, panelRight, HEADER_H, MyDrugsUiTheme.ACCENT);
        graphics.drawCenteredString(font, title, (panelLeft + panelRight) / 2, 6, MyDrugsUiTheme.NORMAL_TEXT);
        graphics.drawCenteredString(font,
                Component.translatable("screen.mydrugs.accessibility.subtitle"),
                (panelLeft + panelRight) / 2, 18, MyDrugsUiTheme.DIM_TEXT);
        graphics.fill(panelLeft, height - FOOTER_H, panelRight, height - FOOTER_H + 1,
                MyDrugsUiTheme.SUBTLE_BORDER);

        if (previewTicks > 0) {
            renderPreview(graphics, panelLeft, panelRight);
        }
    }

    private void renderPreview(GuiGraphics graphics, int panelLeft, int panelRight) {
        float overlay = Config.CLIENT.overlayIntensity.get().floatValue();
        float colorShift = Config.CLIENT.colorShiftIntensity.get().floatValue();
        float flashCap = Config.CLIENT.maxFlashBrightness.get().floatValue();
        float fade = Mth.clamp(previewTicks / 20.0F, 0.0F, 1.0F);
        float phase = (PREVIEW_DURATION_TICKS - previewTicks) / 40.0F;
        int rgb = Mth.hsvToRgb((phase * 0.15F) % 1.0F, 0.55F * colorShift, 0.85F);
        float steadyAlpha = 0.32F * overlay * fade;
        if (!Config.CLIENT.disableFullScreenFlashing.get()) {
            steadyAlpha += 0.10F * flashCap * fade * (0.5F + 0.5F * Mth.sin(phase * 3.0F));
        }
        int alpha = Mth.clamp((int) (steadyAlpha * 255.0F), 0, 200);
        graphics.fill(panelLeft + 1, HEADER_H, panelRight - 1, height - FOOTER_H,
                (alpha << 24) | (rgb & 0xFFFFFF));
        graphics.drawCenteredString(font,
                Component.translatable("screen.mydrugs.accessibility.preview_active"),
                (panelLeft + panelRight) / 2, height - FOOTER_H - 14, MyDrugsUiTheme.NORMAL_TEXT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        for (DisabledControl control : disabledControls) {
            MyDrugsUiTheme.drawBorder(graphics, control.x(), control.y(), control.width(), control.height(),
                    MyDrugsUiTheme.contrastBorder());
        }
        renderScrollbar(graphics);

        int panelWidth = Math.min(PANEL_WIDTH, width - 24);
        int panelLeft = (width - panelWidth) / 2;
        graphics.drawString(font,
                Component.translatable("screen.mydrugs.accessibility.note"),
                panelLeft + PAD, height - FOOTER_H - 12, MyDrugsUiTheme.DIM_TEXT, false);
    }

    private void renderScrollbar(GuiGraphics graphics) {
        int max = maxScroll();
        int trackHeight = controlsBottom - controlsTop;
        if (max <= 0 || trackHeight <= 0) {
            return;
        }
        int barHeight = Math.max(16, trackHeight * trackHeight / Math.max(trackHeight, contentHeight));
        int barY = controlsTop + (trackHeight - barHeight) * scroll / max;
        graphics.fill(controlsRight - 3, controlsTop, controlsRight, controlsBottom, MyDrugsUiTheme.SUBTLE_BORDER);
        graphics.fill(controlsRight - 3, barY, controlsRight, barY + barHeight, MyDrugsUiTheme.ACCENT);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private record DisabledControl(int x, int y, int width, int height) {
    }

    private final class ConfigSlider extends AbstractSliderButton {
        private final ControlDef def;
        private final Component label;

        ConfigSlider(int x, int y, int width, ControlDef def, Component label) {
            super(x, y, width, 20, Component.empty(), normalizedValue(def));
            this.def = def;
            this.label = label;
            updateMessage();
        }

        private static double normalizedValue(ControlDef def) {
            double current = def.type() == ControlType.INTEGER
                    ? ((ModConfigSpec.IntValue) def.value()).get()
                    : ((ModConfigSpec.DoubleValue) def.value()).get();
            return Mth.clamp((current - def.min()) / (def.max() - def.min()), 0.0D, 1.0D);
        }

        @Override
        protected void updateMessage() {
            double shownValue = def.min() + value * (def.max() - def.min());
            String shown;
            if (def.type() == ControlType.INTEGER) {
                shown = Integer.toString((int) Math.round(shownValue));
            } else if (def.max() > 1.0D || def.min() > 0.0D) {
                shown = String.format(Locale.ROOT, "%.2fx", shownValue);
            } else {
                shown = Math.round(shownValue * 100.0D) + "%";
            }
            setMessage(Component.empty().append(label).append(": ").append(shown));
        }

        @Override
        protected void applyValue() {
            if (!def.enabled().getAsBoolean()) {
                return;
            }
            double raw = def.min() + value * (def.max() - def.min());
            if (def.type() == ControlType.INTEGER) {
                ((ModConfigSpec.IntValue) def.value()).set((int) Math.round(raw));
            } else {
                ((ModConfigSpec.DoubleValue) def.value()).set(raw);
            }
            markCustom();
        }
    }
}
