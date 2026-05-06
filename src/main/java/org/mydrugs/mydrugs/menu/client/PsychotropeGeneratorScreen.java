package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.client.PsychotropeAreaPreviewClientState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.menu.PsychotropeGeneratorMenu;

public final class PsychotropeGeneratorScreen extends AbstractMachineScreen<PsychotropeGeneratorMenu> {
    private static final int GUI_W = 176;
    private static final int GUI_H = 176;
    private static final int PROGRESS_X = 44;
    private static final int PROGRESS_Y = 42;
    private static final int PROGRESS_W = 88;
    private static final int PROGRESS_H = 10;
    private static final int ENERGY_X = 8;
    private static final int ENERGY_Y = 24;
    private static final int ENERGY_W = 12;
    private static final int ENERGY_H = 54;
    private static final int RADIUS_X = 48;
    private static final int RADIUS_Y = 74;
    private static final int RADIUS_W = 80;
    private static final int RADIUS_H = 8;
    private static final int SHOW_AREA_X = 134;
    private static final int SHOW_AREA_Y = 70;
    private static final int SHOW_AREA_SIZE = 10;
    private static final int STATUS_X = 150;
    private static final int STATUS_Y = 20;
    private static final int STATUS_SIZE = 14;

    public PsychotropeGeneratorScreen(PsychotropeGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, GUI_W, GUI_H);
        this.inventoryLabelY = 82;
    }

    @Override
    protected boolean shouldShowTransferConfigButton() {
        return false;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.menu.isFormed() && isAreaPreviewEnabled()) {
            PsychotropeAreaPreviewClientState.refreshPersistent(currentDimension(), this.menu.getBlockPos(), this.menu.getPowerRadius());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawPanel(graphics, 0, 0, this.imageWidth, this.imageHeight, 0xFF1B1421);
        drawPanel(graphics, 30, 18, 116, 66, 0xFF251D30);

        int energyFill = this.menu.getScaledEnergy(ENERGY_H - 2);
        graphics.fill(guiX(ENERGY_X), guiY(ENERGY_Y), guiX(ENERGY_X + ENERGY_W), guiY(ENERGY_Y + ENERGY_H), 0xFF08070A);
        graphics.fill(guiX(ENERGY_X + 1), guiY(ENERGY_Y + ENERGY_H - 1 - energyFill), guiX(ENERGY_X + ENERGY_W - 1), guiY(ENERGY_Y + ENERGY_H - 1), 0xFF9A4DFF);

        drawHorizontalBar(graphics, PROGRESS_X, PROGRESS_Y, PROGRESS_W, PROGRESS_H, this.menu.getScaledProgress(PROGRESS_W - 2), 0xFF8D5CFF, 0xFFE0BEFF);
        drawRadiusSlider(graphics);
        drawShowAreaCheckbox(graphics);
        drawStructureStatus(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, this.title, this.imageWidth / 2, 7, 0xFFFFFFFF);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0xFFE6DFF0, false);
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(ENERGY_X, ENERGY_Y, ENERGY_W, ENERGY_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.psychotrope_energy"),
                    Component.literal(this.menu.getEnergyStored() + " / " + this.menu.getEnergyCapacity() + " PE")
            );
        } else if (isHoveringBox(PROGRESS_X, PROGRESS_Y, PROGRESS_W, PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.progress_value", this.menu.getProgress(), this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(STATUS_X, STATUS_Y, STATUS_SIZE, STATUS_SIZE, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable(this.menu.getStatus().translationKey())
            );
        } else if (isHoveringBox(RADIUS_X, RADIUS_Y - 6, RADIUS_W, RADIUS_H + 12, mouseX, mouseY)) {
            int radius = this.menu.getPowerRadius();
            int efficiency = (9 - radius) * 100 / 8;
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.powering_radius", radius),
                    Component.translatable("screen.mydrugs.ui.efficiency_percent", efficiency)
            );
        } else if (isHoveringBox(SHOW_AREA_X, SHOW_AREA_Y, SHOW_AREA_SIZE, SHOW_AREA_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.show_area"));
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
        if (event.button() == 0 && isHoveringBox(RADIUS_X, RADIUS_Y - 6, RADIUS_W, RADIUS_H + 12, event.x(), event.y())) {
            int localX = (int) event.x() - guiX(RADIUS_X);
            int radius = Math.clamp(localX * 8 / Math.max(1, RADIUS_W - 1) + 1, 1, 8);
            pressMenuButton(PsychotropeGeneratorMenu.RADIUS_BUTTON_BASE + radius - 1);
            PsychotropeAreaPreviewClientState.flash(currentDimension(), this.menu.getBlockPos(), radius, 100);
            if (isAreaPreviewEnabled()) {
                PsychotropeAreaPreviewClientState.setPersistent(currentDimension(), this.menu.getBlockPos(), radius, true);
            }
            return true;
        }
        if (event.button() == 0 && isHoveringBox(SHOW_AREA_X, SHOW_AREA_Y, SHOW_AREA_SIZE, SHOW_AREA_SIZE, event.x(), event.y())) {
            boolean enabled = !isAreaPreviewEnabled();
            PsychotropeAreaPreviewClientState.setPersistent(currentDimension(), this.menu.getBlockPos(), this.menu.getPowerRadius(), enabled);
            return true;
        }
        return super.mouseClicked(event, doubleClicked);
    }

    private void drawRadiusSlider(GuiGraphics graphics) {
        graphics.drawCenteredString(this.font, Component.translatable("screen.mydrugs.ui.radius_value", this.menu.getPowerRadius()), guiX(this.imageWidth / 2), guiY(RADIUS_Y - 12), 0xFFE6DFF0);
        graphics.fill(guiX(RADIUS_X), guiY(RADIUS_Y), guiX(RADIUS_X + RADIUS_W), guiY(RADIUS_Y + RADIUS_H), 0xFF0E0A12);
        int knobX = RADIUS_X + (this.menu.getPowerRadius() - 1) * (RADIUS_W - 6) / 7;
        graphics.fill(guiX(knobX), guiY(RADIUS_Y - 2), guiX(knobX + 6), guiY(RADIUS_Y + RADIUS_H + 2), 0xFFD9B8FF);

        DrugId activeDrug = DrugId.byNetworkId(this.menu.getActiveDrugNetworkId());
        if (activeDrug != null) {
            graphics.drawCenteredString(this.font, Component.literal(activeDrug.serializedName()), guiX(this.imageWidth / 2), guiY(26), 0xFFD9B8FF);
        }
    }

    private void drawShowAreaCheckbox(GuiGraphics graphics) {
        int x = guiX(SHOW_AREA_X);
        int y = guiY(SHOW_AREA_Y);
        boolean showArea = isAreaPreviewEnabled();
        graphics.fill(x, y, x + SHOW_AREA_SIZE, y + SHOW_AREA_SIZE, 0xFF08070A);
        graphics.fill(x + 1, y + 1, x + SHOW_AREA_SIZE - 1, y + SHOW_AREA_SIZE - 1, showArea ? 0xFF7E49D8 : 0xFF251D30);
        if (showArea) {
            graphics.fill(x + 3, y + 5, x + 5, y + 7, 0xFFFFFFFF);
            graphics.fill(x + 5, y + 7, x + 7, y + 9, 0xFFFFFFFF);
            graphics.fill(x + 7, y + 3, x + 9, y + 7, 0xFFFFFFFF);
        }
    }

    private void drawStructureStatus(GuiGraphics graphics) {
        int x = guiX(STATUS_X);
        int y = guiY(STATUS_Y);
        int fill = this.menu.isFormed() ? 0xFF1F9F4B : 0xFFB83232;
        graphics.fill(x, y, x + STATUS_SIZE, y + STATUS_SIZE, 0xFF08080A);
        graphics.fill(x + 1, y + 1, x + STATUS_SIZE - 1, y + STATUS_SIZE - 1, fill);

        if (this.menu.isFormed()) {
            graphics.fill(x + 4, y + 8, x + 6, y + 10, 0xFFFFFFFF);
            graphics.fill(x + 6, y + 10, x + 8, y + 12, 0xFFFFFFFF);
            graphics.fill(x + 8, y + 7, x + 10, y + 10, 0xFFFFFFFF);
            graphics.fill(x + 10, y + 5, x + 12, y + 8, 0xFFFFFFFF);
        } else {
            graphics.fill(x + 4, y + 4, x + 6, y + 6, 0xFFFFFFFF);
            graphics.fill(x + 6, y + 6, x + 8, y + 8, 0xFFFFFFFF);
            graphics.fill(x + 8, y + 8, x + 10, y + 10, 0xFFFFFFFF);
            graphics.fill(x + 10, y + 10, x + 12, y + 12, 0xFFFFFFFF);
            graphics.fill(x + 10, y + 4, x + 12, y + 6, 0xFFFFFFFF);
            graphics.fill(x + 8, y + 6, x + 10, y + 8, 0xFFFFFFFF);
            graphics.fill(x + 6, y + 8, x + 8, y + 10, 0xFFFFFFFF);
            graphics.fill(x + 4, y + 10, x + 6, y + 12, 0xFFFFFFFF);
        }
    }

    private boolean isAreaPreviewEnabled() {
        return PsychotropeAreaPreviewClientState.isPersistentEnabled(currentDimension(), this.menu.getBlockPos());
    }

    private static @Nullable ResourceLocation currentDimension() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? null : minecraft.level.dimension().location();
    }
}
