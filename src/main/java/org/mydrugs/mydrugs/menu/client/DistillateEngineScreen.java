package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.blocks.entity.DistillateEngineBlockEntity;
import org.mydrugs.mydrugs.energy.PsyCurrentConstants;
import org.mydrugs.mydrugs.energy.psycurrent.DistillateFuel;
import org.mydrugs.mydrugs.energy.psycurrent.DistillateFuelRegistry;
import org.mydrugs.mydrugs.energy.psycurrent.StrainRisk;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.menu.DistillateEngineMenu;
import org.mydrugs.mydrugs.menu.layout.DistillateEngineLayout;

import java.util.ArrayList;
import java.util.List;

public final class DistillateEngineScreen extends AbstractMachineScreen<DistillateEngineMenu> {
    private InvisibleButton radiusDownButton;
    private InvisibleButton radiusUpButton;
    private InvisibleButton showAreaButton;

    public DistillateEngineScreen(DistillateEngineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, DistillateEngineLayout.GUI_WIDTH, DistillateEngineLayout.GUI_HEIGHT);
    }

    @Override
    protected boolean shouldRenderSharedEnergyBar() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        this.radiusDownButton = this.addRenderableWidget(new InvisibleButton(
                guiX(DistillateEngineLayout.RADIUS_DOWN_X),
                guiY(DistillateEngineLayout.RADIUS_BUTTON_Y),
                DistillateEngineLayout.RADIUS_BUTTON_W,
                DistillateEngineLayout.RADIUS_BUTTON_H,
                button -> pressMenuButton(DistillateEngineMenu.RADIUS_DOWN_BUTTON_ID)
        ));
        this.radiusUpButton = this.addRenderableWidget(new InvisibleButton(
                guiX(DistillateEngineLayout.RADIUS_UP_X),
                guiY(DistillateEngineLayout.RADIUS_BUTTON_Y),
                DistillateEngineLayout.RADIUS_BUTTON_W,
                DistillateEngineLayout.RADIUS_BUTTON_H,
                button -> pressMenuButton(DistillateEngineMenu.RADIUS_UP_BUTTON_ID)
        ));
        this.showAreaButton = this.addRenderableWidget(new InvisibleButton(
                guiX(DistillateEngineLayout.SHOW_AREA_X),
                guiY(DistillateEngineLayout.SHOW_AREA_Y),
                DistillateEngineLayout.SHOW_AREA_W,
                DistillateEngineLayout.SHOW_AREA_H,
                button -> pressMenuButton(DistillateEngineMenu.SHOW_AREA_BUTTON_ID)
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);
        drawPanel(
                graphics,
                DistillateEngineLayout.MACHINE_PANEL_X,
                DistillateEngineLayout.MACHINE_PANEL_Y,
                DistillateEngineLayout.MACHINE_PANEL_W,
                DistillateEngineLayout.MACHINE_PANEL_H,
                0xFF1F2529
        );
        drawSieveInventoryPanels(graphics, DistillateEngineLayout.PLAYER_INV_X, DistillateEngineLayout.PLAYER_INV_Y);

        drawSlotFrame(graphics, DistillateEngineLayout.FUEL_SLOT_X, DistillateEngineLayout.FUEL_SLOT_Y);
        drawSlotFrame(graphics, DistillateEngineLayout.REGULATOR_SLOT_X, DistillateEngineLayout.REGULATOR_SLOT_Y);
        drawSlotFrame(graphics, DistillateEngineLayout.WASTE_SLOT_X, DistillateEngineLayout.WASTE_SLOT_Y);

        drawHorizontalBar(
                graphics,
                DistillateEngineLayout.CURRENT_BAR_X,
                DistillateEngineLayout.CURRENT_BAR_Y,
                DistillateEngineLayout.CURRENT_BAR_W,
                DistillateEngineLayout.CURRENT_BAR_H,
                this.menu.getScaledCurrent(DistillateEngineLayout.CURRENT_BAR_W),
                0xFF8E7CFF,
                0xFFCFC7FF
        );
        drawHorizontalBar(
                graphics,
                DistillateEngineLayout.FUEL_BAR_X,
                DistillateEngineLayout.FUEL_BAR_Y,
                DistillateEngineLayout.FUEL_BAR_W,
                DistillateEngineLayout.FUEL_BAR_H,
                this.menu.getScaledFuel(DistillateEngineLayout.FUEL_BAR_W),
                0xFF62B6CB,
                0xFFB3F0FF
        );
        drawHorizontalBar(
                graphics,
                DistillateEngineLayout.STRAIN_BAR_X,
                DistillateEngineLayout.STRAIN_BAR_Y,
                DistillateEngineLayout.STRAIN_BAR_W,
                DistillateEngineLayout.STRAIN_BAR_H,
                this.menu.getScaledStrain(DistillateEngineLayout.STRAIN_BAR_W),
                this.menu.strain() >= 80 ? 0xFFFF5D4D : 0xFFE8A84A,
                0xFFFFD899
        );

        drawRadiusButton(graphics, DistillateEngineLayout.RADIUS_DOWN_X, this.radiusDownButton != null && this.radiusDownButton.isHoveredOrFocused(), "-");
        drawRadiusButton(graphics, DistillateEngineLayout.RADIUS_UP_X, this.radiusUpButton != null && this.radiusUpButton.isHoveredOrFocused(), "+");
        drawShowAreaButton(graphics, this.showAreaButton != null && this.showAreaButton.isHoveredOrFocused());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFE8ECEF, false);
        // Labels aligned on the same row so they read as a single status line.
        int labelY = 80;
        graphics.drawString(
                this.font,
                Component.translatable("screen.mydrugs.distillate_engine.radius", this.menu.powerRadius()),
                18,
                labelY,
                0xFFE8ECEF,
                false
        );
        graphics.drawString(
                this.font,
                Component.translatable("screen.mydrugs.distillate_engine.output", this.menu.currentPerTick()),
                DistillateEngineLayout.CURRENT_BAR_X,
                labelY,
                0xFFE8ECEF,
                false
        );

        // Target summary line.
        Component targetsLine = Component.translatable(
                "screen.mydrugs.distillate_engine.targets_summary",
                this.menu.validTargetCount(),
                this.menu.fullTargetCount(),
                this.menu.incompatibleTargetCount()
        );
        graphics.drawString(
                this.font,
                targetsLine,
                DistillateEngineLayout.STATUS_BLOCK_X,
                DistillateEngineLayout.STATUS_TARGETS_Y,
                0xFFD5C9FF,
                false
        );

        // Buffer line.
        graphics.drawString(
                this.font,
                Component.translatable(
                        "screen.mydrugs.distillate_engine.buffer",
                        formatNumber(this.menu.currentStored()),
                        formatNumber(this.menu.currentCapacity())
                ),
                DistillateEngineLayout.STATUS_BLOCK_X,
                DistillateEngineLayout.STATUS_BUFFER_Y,
                0xFFE8ECEF,
                false
        );

        // Strain state line / overload cooldown.
        StrainRisk risk = StrainRisk.forStrain(this.menu.strain());
        Component strainLine = this.menu.overloadTicks() > 0
                ? Component.translatable(
                        "screen.mydrugs.distillate_engine.overload_cooldown",
                        formatSeconds(this.menu.overloadTicks()))
                : Component.translatable("screen.mydrugs.distillate_engine.strain_state", risk.label());
        int strainColor = this.menu.overloadTicks() > 0 ? 0xFFFF5D4D : risk.colorPrimary();
        graphics.drawString(
                this.font,
                strainLine,
                DistillateEngineLayout.STATUS_BLOCK_X,
                DistillateEngineLayout.STATUS_STRAIN_Y,
                strainColor,
                false
        );

        graphics.drawString(
                this.font,
                Component.translatable("screen.mydrugs.ui.inventory"),
                DistillateEngineLayout.PLAYER_INV_X,
                standardInventoryLabelY(DistillateEngineLayout.PLAYER_INV_Y),
                0xFFE8ECEF,
                false
        );
    }

    private static String formatNumber(int value) {
        return String.format("%,d", value);
    }

    private static String formatSeconds(int ticks) {
        int seconds = (ticks + 19) / 20;
        return seconds + "s";
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "distillate_fuel" -> List.of(slotHighlight(DistillateEngineLayout.FUEL_SLOT_X, DistillateEngineLayout.FUEL_SLOT_Y));
            case "current_regulator" -> List.of(slotHighlight(DistillateEngineLayout.REGULATOR_SLOT_X, DistillateEngineLayout.REGULATOR_SLOT_Y));
            case "engine_waste" -> List.of(slotHighlight(DistillateEngineLayout.WASTE_SLOT_X, DistillateEngineLayout.WASTE_SLOT_Y));
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(DistillateEngineLayout.CURRENT_BAR_X, DistillateEngineLayout.CURRENT_BAR_Y,
                DistillateEngineLayout.CURRENT_BAR_W, DistillateEngineLayout.CURRENT_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.psy_current"),
                    Component.translatable("screen.mydrugs.ui.amount_unit", this.menu.currentStored(), this.menu.currentCapacity(), "PC")
            );
        } else if (isHoveringBox(DistillateEngineLayout.FUEL_BAR_X, DistillateEngineLayout.FUEL_BAR_Y,
                DistillateEngineLayout.FUEL_BAR_W, DistillateEngineLayout.FUEL_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.fuel_burn"),
                    Component.translatable("screen.mydrugs.ui.amount", this.menu.fuelTicksRemaining(), this.menu.fuelTicksTotal())
            );
        } else if (isHoveringBox(DistillateEngineLayout.STRAIN_BAR_X, DistillateEngineLayout.STRAIN_BAR_Y,
                DistillateEngineLayout.STRAIN_BAR_W, DistillateEngineLayout.STRAIN_BAR_H, mouseX, mouseY)) {
            StrainRisk risk = StrainRisk.forStrain(this.menu.strain());
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.distillate_engine.strain"),
                    Component.translatable("screen.mydrugs.distillate_engine.strain_value", this.menu.strain()),
                    Component.translatable("screen.mydrugs.distillate_engine.strain_state", risk.label())
            );
        } else if (isHoveringBox(DistillateEngineLayout.STATUS_BLOCK_X, DistillateEngineLayout.STATUS_TARGETS_Y,
                DistillateEngineLayout.STATUS_BLOCK_W, DistillateEngineLayout.STATUS_LINE_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.distillate_engine.targets_tooltip_title"),
                    Component.translatable(
                            "screen.mydrugs.distillate_engine.targets_total",
                            this.menu.totalTargetCount()),
                    Component.translatable(
                            "screen.mydrugs.distillate_engine.targets_valid",
                            this.menu.validTargetCount()),
                    Component.translatable(
                            "screen.mydrugs.distillate_engine.targets_full",
                            this.menu.fullTargetCount()),
                    Component.translatable(
                            "screen.mydrugs.distillate_engine.targets_incompatible",
                            this.menu.incompatibleTargetCount()),
                    Component.translatable(
                            "screen.mydrugs.distillate_engine.receivable",
                            formatNumber(this.menu.totalReceivable()))
            );
        }
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> tooltip = super.getTooltipFromContainerItem(stack);
        if (this.hoveredSlot == null || stack.isEmpty()) {
            return tooltip;
        }
        if (this.hoveredSlot.index != DistillateEngineBlockEntity.SLOT_FUEL) {
            return tooltip;
        }
        DistillateFuel fuel = DistillateFuelRegistry.get(stack).orElse(null);
        if (fuel == null) {
            return tooltip;
        }

        boolean hasRegulator = this.menu.slots.get(DistillateEngineBlockEntity.SLOT_REGULATOR).getItem()
                .is(ModItems.CURRENT_REGULATOR.get());
        int strainPerSecond = fuel.strainPerSecond();
        if (strainPerSecond > 0 && hasRegulator) {
            strainPerSecond = Math.max(0,
                    strainPerSecond - PsyCurrentConstants.ENGINE_CURRENT_REGULATOR_STRAIN_REDUCTION);
        }
        int seconds = Math.max(1, fuel.durationTicks() / 20);
        int averagePerTick = Math.max(1, fuel.totalCurrent() / fuel.durationTicks());
        StrainRisk forecast = StrainRisk.forecast(this.menu.strain(), fuel, hasRegulator);

        List<Component> extended = new ArrayList<>(tooltip);
        extended.add(Component.empty());
        extended.add(Component.translatable("screen.mydrugs.distillate_engine.fuel_forecast_title")
                .withStyle(net.minecraft.ChatFormatting.GOLD));
        extended.add(Component.translatable(
                "screen.mydrugs.distillate_engine.fuel_output", formatNumber(fuel.totalCurrent())));
        extended.add(Component.translatable(
                "screen.mydrugs.distillate_engine.fuel_duration", seconds));
        extended.add(Component.translatable(
                "screen.mydrugs.distillate_engine.fuel_average", averagePerTick));
        extended.add(Component.translatable(
                "screen.mydrugs.distillate_engine.fuel_strain_start", signed(fuel.strainOnStart())));
        extended.add(Component.translatable(
                "screen.mydrugs.distillate_engine.fuel_strain_per_second", signed(strainPerSecond)));
        extended.add(Component.translatable(
                "screen.mydrugs.distillate_engine.fuel_risk", forecast.label()));
        return extended;
    }

    private static String signed(int value) {
        return value >= 0 ? "+" + value : Integer.toString(value);
    }

    private void drawRadiusButton(GuiGraphics graphics, int x, boolean hovered, String symbol) {
        int gx = guiX(x);
        int gy = guiY(DistillateEngineLayout.RADIUS_BUTTON_Y);
        graphics.fill(gx, gy, gx + DistillateEngineLayout.RADIUS_BUTTON_W, gy + DistillateEngineLayout.RADIUS_BUTTON_H, 0xFF7C818C);
        graphics.fill(gx + 1, gy + 1, gx + DistillateEngineLayout.RADIUS_BUTTON_W - 1, gy + DistillateEngineLayout.RADIUS_BUTTON_H - 1, hovered ? 0xFF303943 : 0xFF181A1F);
        graphics.drawString(this.font, symbol, gx + 7, gy + 3, 0xFFE8ECEF, false);
    }

    private void drawShowAreaButton(GuiGraphics graphics, boolean hovered) {
        int gx = guiX(DistillateEngineLayout.SHOW_AREA_X);
        int gy = guiY(DistillateEngineLayout.SHOW_AREA_Y);
        graphics.fill(gx, gy, gx + DistillateEngineLayout.SHOW_AREA_W, gy + DistillateEngineLayout.SHOW_AREA_H, 0xFF7C818C);
        graphics.fill(gx + 1, gy + 1, gx + DistillateEngineLayout.SHOW_AREA_W - 1, gy + DistillateEngineLayout.SHOW_AREA_H - 1, hovered ? 0xFF303943 : 0xFF181A1F);
        graphics.drawString(this.font, "[]", gx + 3, gy + 3, 0xFFE8ECEF, false);
    }
}
