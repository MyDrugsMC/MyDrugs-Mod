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
import org.mydrugs.mydrugs.recipes.psy_mixer.PsyMixerRecipe;

final class PsyMixerRecipeCategory extends AbstractNiceRecipeCategory<PsyMixerRecipe> {
    static final RecipeType<PsyMixerRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "psy_mixer"), PsyMixerRecipe.class);

    private static final int PANEL = 0xFF2A1A22;
    private static final int PANEL_DARK = 0xFF1A0E12;
    private static final int PANEL_LINE = 0xFF5C3344;
    private static final int MUTED = 0xFFB58A96;
    private static final int TEXT = 0xFFE8D6C2;
    private static final int WARN = 0xFFFFD060;

    private static final int CENTER_X = 70;
    private static final int CENTER_Y = 42;
    private static final int RADIUS = 30;
    private static final int OUTPUT_X = CENTER_X + 64;

    PsyMixerRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("menu.mydrugs.psy_mixer"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "PAINTED_CLAY_BOWL_ITEM", "PAINTED_CLAY_BOWL"),
                176,
                112
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PsyMixerRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, CENTER_X, CENTER_Y, recipe.base());
        addItemIngredient(builder, RecipeIngredientRole.INPUT, CENTER_X - RADIUS, CENTER_Y, recipe.material());
        recipe.catalyst().ifPresent(ingredient -> addItemIngredient(builder, RecipeIngredientRole.INPUT, CENTER_X, CENTER_Y - RADIUS, ingredient));
        recipe.stabilizer().ifPresent(ingredient -> addItemIngredient(builder, RecipeIngredientRole.INPUT, CENTER_X + RADIUS, CENTER_Y, ingredient));
        recipe.vessel().ifPresent(ingredient -> addItemIngredient(builder, RecipeIngredientRole.INPUT, CENTER_X, CENTER_Y + RADIUS, ingredient));
        addItemStack(builder, RecipeIngredientRole.OUTPUT, OUTPUT_X, CENTER_Y, recipe.result());
    }

    @Override
    public void draw(PsyMixerRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        g.fill(0, 0, width, height, 0xFF12070A);
        g.fill(6, 16, 166, 94, PANEL_DARK);
        g.fill(8, 18, 164, 92, PANEL);
        g.fill(6, 16, 166, 17, PANEL_LINE);
        g.fill(6, 93, 166, 94, PANEL_LINE);

        drawRitualRing(g, CENTER_X + 8, CENTER_Y + 8);
        drawSlotFrame(g, CENTER_X, CENTER_Y, true);
        drawSlotFrame(g, CENTER_X - RADIUS, CENTER_Y, true);
        drawSlotFrame(g, CENTER_X, CENTER_Y - RADIUS, recipe.catalyst().isPresent());
        drawSlotFrame(g, CENTER_X + RADIUS, CENTER_Y, recipe.stabilizer().isPresent());
        drawSlotFrame(g, CENTER_X, CENTER_Y + RADIUS, recipe.vessel().isPresent());
        drawSlotFrame(g, OUTPUT_X, CENTER_Y, true);

        var font = Minecraft.getInstance().font;
        g.drawString(font, Component.translatable("screen.mydrugs.psy_mixer.ingredients"), 10, 5, MUTED, false);
        g.drawString(font, Component.translatable("screen.mydrugs.psy_mixer.result"), 132, 25, MUTED, false);

        int infoY = 96;
        if (recipe.requiredLifetimeDose() > 0.0F) {
            g.drawString(font, Component.translatable("screen.mydrugs.jei.required_lifetime_dose", recipe.requiredLifetimeDose()), 8, infoY, TEXT, false);
            infoY += 10;
        } else {
            if (recipe.requiredKnowledge().isPresent()) {
                g.drawString(font,
                        Component.translatable("screen.mydrugs.jei.required_knowledge", knowledgeName(recipe.requiredKnowledge().get())),
                        8,
                        infoY,
                        TEXT,
                        false
                );
                infoY += 10;
            }
        }
        if (recipe.requiredDrugCategory().isPresent() && infoY < height - 8) {
            g.drawString(font, fit(Component.translatable("screen.mydrugs.jei.required_drug_category", recipe.requiredDrugCategory().get()), 158), 8, infoY, TEXT, false);
            infoY += 10;
        }
        if (recipe.requiredActiveEffect().isPresent() && infoY < height - 8) {
            g.drawString(font, fit(Component.translatable("screen.mydrugs.jei.required_active_effect", recipe.requiredActiveEffect().get()), 158), 8, infoY, TEXT, false);
            infoY += 10;
        }
        if (recipe.requiredBadTripState() && infoY < height - 8) {
            g.drawString(font, Component.translatable("screen.mydrugs.jei.required_bad_trip"), 8, infoY, WARN, false);
            infoY += 10;
        }
        if (recipe.machineSpeedModifier() > 0.001F && infoY < height - 8) {
            g.drawString(font, Component.translatable("screen.mydrugs.jei.machine_speed_modifier", Math.round(recipe.machineSpeedModifier() * 100.0F)), 8, infoY, MUTED, false);
        } else if (recipe.ritualStabilityModifier() != 0.0F && infoY < height - 8) {
            g.drawString(font, Component.translatable("screen.mydrugs.jei.ritual_stability_modifier", Math.round(recipe.ritualStabilityModifier() * 100.0F)), 8, infoY, MUTED, false);
        }
    }

    private void drawRitualRing(GuiGraphics g, int cx, int cy) {
        for (int i = 0; i < 64; i++) {
            double angle = i * Math.PI * 2.0 / 64.0;
            int dx = (int) Math.round(Math.cos(angle) * 42.0);
            int dy = (int) Math.round(Math.sin(angle) * 42.0);
            g.fill(cx + dx - 1, cy + dy - 1, cx + dx + 1, cy + dy + 1, 0xFF6A3A4A);
        }

        for (int i = 0; i < 14; i++) {
            float p = i / 128.0F;
            double angle = -Math.PI / 2.0 + p * Math.PI * 2.0;
            int dx = (int) Math.round(Math.cos(angle) * 49.0);
            int dy = (int) Math.round(Math.sin(angle) * 49.0);
            g.fill(cx + dx - 2, cy + dy - 2, cx + dx + 2, cy + dy + 2, WARN);
        }
    }

    private void drawSlotFrame(GuiGraphics g, int x, int y, boolean active) {
        int border = active ? 0xFF100407 : 0xFF4A2A34;
        g.fill(x - 3, y - 3, x + 21, y + 21, active ? 0xFF100407 : 0xFF2A1A22);
        g.fill(x - 2, y - 2, x + 20, y + 20, border);
        g.fill(x, y, x + 18, y + 18, 0xFF24111A);
    }

    private static String knowledgeName(ResourceLocation id) {
        return Component.translatable("knowledge." + id.getNamespace() + "." + id.getPath()).getString();
    }

    private static Component fit(Component component, int width) {
        var font = Minecraft.getInstance().font;
        if (font.width(component) <= width) {
            return component;
        }
        return Component.literal(font.plainSubstrByWidth(component.getString(), width - font.width("...")) + "...");
    }
}
