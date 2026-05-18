package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.ritual.MixedDrugData;
import org.mydrugs.mydrugs.core.drug.ritual.RitualDrugEffectData;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.network.SubmitDrugFormulaNamePayload;

public final class DrugFormulaNamingScreen extends Screen {
    private final MixedDrugData formula;
    private EditBox nameBox;

    public DrugFormulaNamingScreen(MixedDrugData formula) {
        super(Component.translatable("screen.mydrugs.drug_formula_naming"));
        this.formula = formula;
    }

    @Override
    protected void init() {
        int panelX = this.width / 2 - 120;
        int panelY = this.height / 2 - 86;
        this.nameBox = new EditBox(this.font, panelX + 18, panelY + 118, 204, 20, Component.translatable("screen.mydrugs.formula.name"));
        this.nameBox.setMaxLength(32);
        this.addRenderableWidget(this.nameBox);
        this.addRenderableWidget(Button.builder(Component.translatable("screen.mydrugs.formula.send"), button -> submit())
                .bounds(panelX + 72, panelY + 144, 96, 20)
                .build());
        this.setInitialFocus(this.nameBox);
    }

    private void submit() {
        ClientPacketDistributor.sendToServer(new SubmitDrugFormulaNamePayload(this.nameBox.getValue()));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == 257 || keyCode == 335) {
            submit();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xAA000000);
        int panelX = this.width / 2 - 120;
        int panelY = this.height / 2 - 86;
        graphics.fill(panelX, panelY, panelX + 240, panelY + 176, 0xEE17151C);
        graphics.renderItem(iconFor(this.formula.baseDrug()), panelX + 18, panelY + 12);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelY + 10, 0xFFE8D7FF);
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.formula.base", this.formula.baseDrug().serializedName()), panelX + 18, panelY + 32, 0xFFD6D6D6, false);
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.formula.quality", Component.translatable(this.formula.quality().translationKey())), panelX + 128, panelY + 32, 0xFFD6D6D6, false);
        drawEffects(graphics, Component.translatable("screen.mydrugs.formula.base_effects"), this.formula.baseEffectsSnapshot(), panelX + 18, panelY + 48);
        drawEffects(graphics, Component.translatable("screen.mydrugs.formula.added_effects"), this.formula.addedEffects(), panelX + 128, panelY + 48);
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.formula.name"), panelX + 18, panelY + 106, 0xFFD6D6D6, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawEffects(GuiGraphics graphics, Component title, java.util.List<RitualDrugEffectData> effects, int x, int y) {
        graphics.drawString(this.font, title, x, y, 0xFFE8D7FF, false);
        int row = 0;
        for (RitualDrugEffectData effect : effects.stream().limit(4).toList()) {
            graphics.drawString(this.font, "- " + effect.type().serializedName(), x, y + 12 + row * 10, 0xFFCFCFCF, false);
            row++;
        }
        if (effects.isEmpty()) {
            graphics.drawString(this.font, "-", x, y + 12, 0xFF777777, false);
        }
    }

    private static ItemStack iconFor(DrugId baseDrug) {
        return new ItemStack(switch (baseDrug) {
            case WEED -> ModItems.CANNABIS_POWDER.get();
            case TOBACCO -> ModItems.TOBACCO_HANDFUL.get();
            case LSD -> ModItems.LSD_DROP.get();
            case MUSHROOMS -> ModItems.MAGIC_MUSHROOM.get();
            case HASH -> ModItems.HASH_PIECE.get();
            case METH -> ModItems.METH_SHARD.get();
            case COCAINE -> ModItems.COCAINE_POWDER.get();
            case CRACK -> ModItems.CRACK_SHARD.get();
            case COFFEE -> ModItems.COFFEE_CUP.get();
            default -> ModItems.MIXED_DRUG.get();
        });
    }
}
