package org.mydrugs.mydrugs.client.compat;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.recipes.psy_anvil.PsyAnvilRecipe;

final class PsyAnvilRecipeCategory extends AbstractNiceRecipeCategory<PsyAnvilRecipe> {
    static final RecipeType<PsyAnvilRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "psy_anvil"), PsyAnvilRecipe.class);

    private static final int CENTER_X = 42;
    private static final int CENTER_Y = 45;
    private static final int RING_RADIUS = 31;
    private static final int OUTPUT_X = 138;
    private static final int OUTPUT_Y = 45;
    private static final int PANEL = 0xFF241420;
    private static final int PANEL_DARK = 0xFF13090F;
    private static final int PANEL_LINE = 0xFF67324F;
    private static final int RING = 0xFF8B4668;
    private static final int MUTED = 0xFFB98AA1;
    private static final int TEXT = 0xFFEBD7E6;

    PsyAnvilRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.psy_anvil"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "PSY_ANVIL_ITEM", "PSY_ANVIL"),
                176,
                110
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PsyAnvilRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < Math.min(9, recipe.ingredients().size()); i++) {
            PsyAnvilRecipe.PsyAnvilIngredient ingredient = recipe.ingredients().get(i);
            int x = slotX(recipe.ingredients().size(), i) + 1;
            int y = slotY(recipe.ingredients().size(), i) + 1;
            addItemIngredient(builder, RecipeIngredientRole.INPUT, x, y, ingredient.ingredient());
        }

        addItemStack(builder, RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, OUTPUT_Y + 1, recipe.result());
    }

    @Override
    public void draw(PsyAnvilRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        g.fill(0, 0, width, height, 0xFF10080D);
        g.fill(6, 16, 170, 84, PANEL_DARK);
        g.fill(8, 18, 168, 82, PANEL);
        g.fill(6, 16, 170, 17, PANEL_LINE);
        g.fill(6, 99, 170, 100, PANEL_LINE);

        //drawRitualPlate(g, CENTER_X + SLOT / 2, CENTER_Y + SLOT / 2);
        for (int i = 0; i < Math.min(9, recipe.ingredients().size()); i++) {
            drawAnvilSlotFrame(g, slotX(recipe.ingredients().size(), i), slotY(recipe.ingredients().size(), i));
        }
        drawAnvilSlotFrame(g, OUTPUT_X, OUTPUT_Y);

        var font = Minecraft.getInstance().font;
        g.drawString(font, Component.translatable("screen.mydrugs.jei.input"), 14, 6, MUTED, false);
        g.drawString(font, Component.translatable("screen.mydrugs.jei.output"), 131, 26, MUTED, false);
        drawArrow(g, 107, 49, 18, 10, 0xFFE8C96D);

        for (int i = 0; i < Math.min(9, recipe.ingredients().size()); i++) {
            int count = recipe.ingredients().get(i).count();
            if (count > 1) {
                drawSlotCount(g, slotX(recipe.ingredients().size(), i), slotY(recipe.ingredients().size(), i), count);
            }
        }

        int infoY = 102;
        recipe.requiredKnowledge().ifPresent(id -> drawCentered(
                g,
                jeiString("screen.mydrugs.jei.required_knowledge", knowledgeName(id)),
                6,
                infoY,
                width - 12,
                TEXT
        ));

        if (recipe.experienceCost() > 0) {
            drawCentered(g, jeiString("screen.mydrugs.jei.experience_cost", recipe.experienceCost()), 6, infoY + 10, width - 12, 0xFFD8C46E);
        }

        if (recipe.requiredKnowledge().isEmpty() && recipe.experienceCost() <= 0) {
            recipe.messageKey().ifPresent(key -> drawBottomInfo(g, Component.translatable(key).getString()));
        }
    }

    private static int slotX(int count, int index) {
        if (count <= 1) {
            return CENTER_X;
        }
        double angle = -Math.PI / 2.0D + Math.PI * 2.0D * index / Math.min(9, count);
        return CENTER_X + (int) Math.round(Math.cos(angle) * RING_RADIUS);
    }

    private static int slotY(int count, int index) {
        if (count <= 1) {
            return CENTER_Y;
        }
        double angle = -Math.PI / 2.0D + Math.PI * 2.0D * index / Math.min(9, count);
        return CENTER_Y + (int) Math.round(Math.sin(angle) * RING_RADIUS);
    }

    private void drawRitualPlate(GuiGraphics g, int cx, int cy) {
        for (int i = 0; i < 72; i++) {
            double angle = i * Math.PI * 2.0D / 72.0D;
            int x = cx + (int) Math.round(Math.cos(angle) * 42.0D);
            int y = cy + (int) Math.round(Math.sin(angle) * 42.0D);
            g.fill(x - 1, y - 1, x + 1, y + 1, RING);
        }
        g.fill(cx - 12, cy + 18, cx + 12, cy + 20, 0xFF4D263B);
        g.fill(cx - 8, cy + 20, cx + 8, cy + 24, 0xFF351823);
    }

    private static void drawAnvilSlotFrame(GuiGraphics g, int x, int y) {
        g.fill(x - 3, y - 3, x + 21, y + 21, 0xFF12070B);
        g.fill(x - 2, y - 2, x + 20, y + 20, 0xFF6D3855);
        g.fill(x, y, x + SLOT, y + SLOT, 0xFF201019);
    }

    private static String knowledgeName(ResourceLocation id) {
        return Component.translatable("knowledge." + id.getNamespace() + "." + id.getPath()).getString();
    }
}
