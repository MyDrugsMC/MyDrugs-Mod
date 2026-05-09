package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.core.drug.ritual.MixedDrugData;
import org.mydrugs.mydrugs.core.drug.ritual.RitualDrugEffectData;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.menu.SingleSlotMenu;
import org.mydrugs.mydrugs.menu.layout.SingleSlotMenuLayout;

public class SingleSlotMenuScreen extends AbstractMachineScreen<SingleSlotMenu> {
    public SingleSlotMenuScreen(SingleSlotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, SingleSlotMenuLayout.GUI_WIDTH, SingleSlotMenuLayout.GUI_HEIGHT);
        this.inventoryLabelX = SingleSlotMenuLayout.PLAYER_INV_X;
        this.inventoryLabelY = standardInventoryLabelY(SingleSlotMenuLayout.PLAYER_INV_Y);
    }

    @Override
    protected boolean shouldShowTransferConfigButton() {
        return false;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindowColored(graphics, 0xFF181818, 0xFF262626);

        drawPanel(
                graphics,
                SingleSlotMenuLayout.MACHINE_PANEL_X,
                SingleSlotMenuLayout.MACHINE_PANEL_Y,
                SingleSlotMenuLayout.MACHINE_PANEL_W,
                SingleSlotMenuLayout.MACHINE_PANEL_H,
                0xFF323232,
                0xFF595959,
                0xFF101010
        );

        drawSieveInventoryPanels(
                graphics,
                SingleSlotMenuLayout.PLAYER_INV_X,
                SingleSlotMenuLayout.PLAYER_INV_Y
        );

        drawSlotFrame(graphics, SingleSlotMenuLayout.STORAGE_SLOT_X, SingleSlotMenuLayout.STORAGE_SLOT_Y, 0xFF8A8A8A, 0xFF111111);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = SingleSlotMenuLayout.MACHINE_PANEL_X
                + (SingleSlotMenuLayout.MACHINE_PANEL_W - this.font.width(this.title)) / 2;

        graphics.drawString(this.font, this.title, titleX, 4, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xD0D0D0, false);
        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.mydrugs.ui.storage"),
                SingleSlotMenuLayout.MACHINE_PANEL_X + SingleSlotMenuLayout.MACHINE_PANEL_W / 2,
                SingleSlotMenuLayout.MACHINE_PANEL_Y + 6,
                0xFFD0D0D0
        );
        if (this.title.getString().equals(Component.translatable("menu.mydrugs.drug_analyzer").getString())) {
            drawAnalyzerInfo(graphics);
        }
    }

    private void drawAnalyzerInfo(GuiGraphics graphics) {
        ItemStack stack = this.menu.getStoredStack();
        MixedDrugData data = stack.get(ModDataComponents.MIXED_DRUG_DATA.get());
        int x = SingleSlotMenuLayout.MACHINE_PANEL_X + 68;
        int y = SingleSlotMenuLayout.MACHINE_PANEL_Y + 20;
        if (data == null) {
            graphics.drawString(this.font, Component.translatable("screen.mydrugs.drug_analyzer.none"), x, y, 0xFFB0B0B0, false);
            return;
        }
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.drug_analyzer.name", data.displayName()), x, y, 0xFFE8D7FF, false);
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.drug_analyzer.author", data.authorName()), x, y + 10, 0xFFD0D0D0, false);
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.drug_analyzer.base", data.baseDrug().serializedName()), x, y + 20, 0xFFD0D0D0, false);
        int effectY = y + 32;
        effectY = drawEffectList(graphics, Component.translatable("screen.mydrugs.drug_analyzer.base_effects"), data.baseEffectsSnapshot(), x, effectY);
        effectY = drawEffectList(graphics, Component.translatable("screen.mydrugs.drug_analyzer.added_effects"), data.addedEffects(), x, effectY + 4);
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.drug_analyzer.formula", data.formulaId()), x, effectY + 4, 0xFFAAAAAA, false);
    }

    private int drawEffectList(GuiGraphics graphics, Component title, java.util.List<RitualDrugEffectData> effects, int x, int y) {
        graphics.drawString(this.font, title, x, y, 0xFFE8D7FF, false);
        int row = 1;
        for (RitualDrugEffectData effect : effects.stream().limit(3).toList()) {
            graphics.drawString(this.font, "- " + effect.type().serializedName(), x + 4, y + row * 10, 0xFFD0D0D0, false);
            row++;
        }
        if (effects.isEmpty()) {
            graphics.drawString(this.font, "-", x + 4, y + 10, 0xFF777777, false);
            row++;
        }
        return y + row * 10;
    }
}
