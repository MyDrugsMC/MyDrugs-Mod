package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import org.mydrugs.mydrugs.blocks.entity.SteamCrackerBlockEntity;
import org.mydrugs.mydrugs.menu.SteamCrackerMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.SteamCrackerLayout;

import java.util.List;

public class SteamCrackerScreen extends AbstractMachineScreen<SteamCrackerMenu> {
    private InvisibleButton dumpInputButton;
    private InvisibleButton dumpOutput1Button;
    private InvisibleButton dumpOutput2Button;
    private InvisibleButton dumpOutput3Button;
    private InvisibleButton dumpOutput4Button;

    public SteamCrackerScreen(SteamCrackerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, SteamCrackerLayout.GUI_WIDTH, SteamCrackerLayout.GUI_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        this.dumpInputButton = dumpButton(SteamCrackerLayout.DUMP_INPUT_X, SteamCrackerMenu.DUMP_INPUT_BUTTON_ID);
        this.dumpOutput1Button = dumpButton(SteamCrackerLayout.DUMP_OUTPUT_1_X, SteamCrackerMenu.DUMP_OUTPUT_1_BUTTON_ID);
        this.dumpOutput2Button = dumpButton(SteamCrackerLayout.DUMP_OUTPUT_2_X, SteamCrackerMenu.DUMP_OUTPUT_2_BUTTON_ID);
        this.dumpOutput3Button = dumpButton(SteamCrackerLayout.DUMP_OUTPUT_3_X, SteamCrackerMenu.DUMP_OUTPUT_3_BUTTON_ID);
        this.dumpOutput4Button = dumpButton(SteamCrackerLayout.DUMP_OUTPUT_4_X, SteamCrackerMenu.DUMP_OUTPUT_4_BUTTON_ID);
    }

    private InvisibleButton dumpButton(int x, int id) {
        return this.addRenderableWidget(new InvisibleButton(
                guiX(x),
                guiY(SteamCrackerLayout.DUMP_BUTTON_Y),
                SteamCrackerLayout.DUMP_BUTTON_SIZE,
                SteamCrackerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(id)
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineGuiRenderer.drawSteamCracker(this, graphics, new MachineGuiRenderer.SteamCrackerState(
                tankFill(0),
                tankFill(1),
                tankFill(2),
                tankFill(3),
                tankFill(4),
                this.menu.getScaledProgress(SteamCrackerLayout.PROGRESS_W),
                this.menu.getScaledBurn(SteamCrackerLayout.BURN_W),
                this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                this.dumpOutput1Button != null && this.dumpOutput1Button.isHoveredOrFocused(),
                this.dumpOutput2Button != null && this.dumpOutput2Button.isHoveredOrFocused(),
                this.dumpOutput3Button != null && this.dumpOutput3Button.isHoveredOrFocused(),
                this.dumpOutput4Button != null && this.dumpOutput4Button.isHoveredOrFocused(),
                this.menu.getAmount(0) > 0,
                this.menu.getAmount(1) > 0,
                this.menu.getAmount(2) > 0,
                this.menu.getAmount(3) > 0,
                this.menu.getAmount(4) > 0
        ), true);
    }

    private MachineGuiRenderer.TankFill tankFill(int tank) {
        if (this.menu.isGasMode(tank)) {
            return MachineGuiRenderer.TankFill.liveTopLit(
                    this.menu.getScaledTank(tank, SteamCrackerLayout.TANK_INNER_H),
                    this.menu.getGasColor(tank)
            );
        }
        return MachineGuiRenderer.TankFill.liveColor(
                this.menu.getScaledTank(tank, SteamCrackerLayout.TANK_INNER_H),
                getFluidColor(this.menu.getFluid(tank))
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawSteamCrackerLabels(this, graphics, this.font, this.title, null, 0, null);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "input_container" -> List.of(slotHighlight(SteamCrackerLayout.INPUT_SLOT_X, SteamCrackerLayout.SLOT_Y));
            case "fuel" -> List.of(slotHighlight(SteamCrackerLayout.FUEL_SLOT_X, SteamCrackerLayout.FUEL_SLOT_Y));
            case "output_1_container" -> List.of(slotHighlight(SteamCrackerLayout.OUTPUT_1_SLOT_X, SteamCrackerLayout.SLOT_Y));
            case "output_2_container" -> List.of(slotHighlight(SteamCrackerLayout.OUTPUT_2_SLOT_X, SteamCrackerLayout.SLOT_Y));
            case "output_3_container" -> List.of(slotHighlight(SteamCrackerLayout.OUTPUT_3_SLOT_X, SteamCrackerLayout.SLOT_Y));
            case "output_4_container" -> List.of(slotHighlight(SteamCrackerLayout.OUTPUT_4_SLOT_X, SteamCrackerLayout.SLOT_Y));
            case "fluid_input", "gas_input" -> List.of(tankHighlight(SteamCrackerLayout.INPUT_TANK_X, SteamCrackerLayout.TANK_Y, SteamCrackerLayout.TANK_W, SteamCrackerLayout.TANK_H));
            case "fluid_output_1", "gas_output_1" -> List.of(tankHighlight(SteamCrackerLayout.OUTPUT_1_TANK_X, SteamCrackerLayout.TANK_Y, SteamCrackerLayout.TANK_W, SteamCrackerLayout.TANK_H));
            case "fluid_output_2", "gas_output_2" -> List.of(tankHighlight(SteamCrackerLayout.OUTPUT_2_TANK_X, SteamCrackerLayout.TANK_Y, SteamCrackerLayout.TANK_W, SteamCrackerLayout.TANK_H));
            case "fluid_output_3", "gas_output_3" -> List.of(tankHighlight(SteamCrackerLayout.OUTPUT_3_TANK_X, SteamCrackerLayout.TANK_Y, SteamCrackerLayout.TANK_W, SteamCrackerLayout.TANK_H));
            case "fluid_output_4", "gas_output_4" -> List.of(tankHighlight(SteamCrackerLayout.OUTPUT_4_TANK_X, SteamCrackerLayout.TANK_Y, SteamCrackerLayout.TANK_W, SteamCrackerLayout.TANK_H));
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        tankTooltip(graphics, mouseX, mouseY, 0, "Input", SteamCrackerLayout.INPUT_TANK_X);
        tankTooltip(graphics, mouseX, mouseY, 1, "Output 1", SteamCrackerLayout.OUTPUT_1_TANK_X);
        tankTooltip(graphics, mouseX, mouseY, 2, "Output 2", SteamCrackerLayout.OUTPUT_2_TANK_X);
        tankTooltip(graphics, mouseX, mouseY, 3, "Output 3", SteamCrackerLayout.OUTPUT_3_TANK_X);
        tankTooltip(graphics, mouseX, mouseY, 4, "Output 4", SteamCrackerLayout.OUTPUT_4_TANK_X);
        if (isHoveringBox(SteamCrackerLayout.FUEL_SLOT_X, SteamCrackerLayout.FUEL_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.fuel_slot"));
        }
        if (isHoveringBox(SteamCrackerLayout.BURN_X, SteamCrackerLayout.BURN_Y, SteamCrackerLayout.BURN_W, SteamCrackerLayout.BURN_H, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.burn_time"), Component.literal(this.menu.getBurnTimeRemaining() + " / " + this.menu.getBurnTimeTotal()));
        }
        if (isHoveringBox(SteamCrackerLayout.PROGRESS_X, SteamCrackerLayout.PROGRESS_Y, SteamCrackerLayout.PROGRESS_W, SteamCrackerLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.progress_value", this.menu.getProgress(), this.menu.getMaxProgress()));
        }
    }

    private void tankTooltip(GuiGraphics graphics, int mouseX, int mouseY, int tank, String label, int x) {
        if (!isHoveringBox(x, SteamCrackerLayout.TANK_Y, SteamCrackerLayout.TANK_W, SteamCrackerLayout.TANK_H, mouseX, mouseY)) {
            return;
        }
        if (this.menu.isGasMode(tank)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal(label + " gas tank"), Component.literal(this.menu.getGasName(tank)), Component.literal(this.menu.getAmount(tank) + " / " + SteamCrackerBlockEntity.GAS_CAPACITY));
        } else {
            Fluid fluid = this.menu.getFluid(tank);
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal(label + " fluid tank"), Component.literal(getFluidName(fluid)), Component.literal(this.menu.getAmount(tank) + " / " + SteamCrackerBlockEntity.FLUID_CAPACITY + " mB"));
        }
    }
}
