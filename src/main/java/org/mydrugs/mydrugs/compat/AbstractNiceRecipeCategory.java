package org.mydrugs.mydrugs.compat;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.compat.gas.GasJeiIngredient;
import org.mydrugs.mydrugs.compat.gas.GasJeiTypes;
import org.mydrugs.mydrugs.compat.gas.GasJeiUtil;
import org.mydrugs.mydrugs.menu.client.util.AbstractMachineDrawMethods;
import org.mydrugs.mydrugs.menu.layout.LayoutMath;

import java.util.List;

public abstract class AbstractNiceRecipeCategory<T> implements AbstractMachineDrawMethods, IRecipeCategory<T> {
    protected static final int SLOT = 18;

    protected final CategoryMode mode;

    protected final int width;
    protected final int height;

    protected final int leftX;
    protected final int leftW;
    protected final int rightX;
    protected final int rightW;
    protected final int panelY;
    protected final int panelH;

    private static final int PANEL_BG = 0xFF15181E;
    private static final int PANEL_EDGE = 0xFF242933;
    private static final int PANEL_BORDER = 0xFF4B5260;
    private static final int LABEL_COLOR = 0xFF9CA3AF;
    private static final int TEXT_COLOR = 0xFFE5E7EB;
    private static final int ACCENT_COLOR = 0xFFDDBF63;

    private final RecipeType<T> recipeType;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;

    protected AbstractNiceRecipeCategory(
            IGuiHelper helper,
            RecipeType<T> recipeType,
            Component title,
            IDrawable icon
    ) {
        this(helper, recipeType, title, icon, CategoryMode.NORMAL);
    }

    protected AbstractNiceRecipeCategory(
            IGuiHelper helper,
            RecipeType<T> recipeType,
            Component title,
            IDrawable icon,
            CategoryMode mode
    ) {
        this(helper, recipeType, title, icon, mode, mode.scale(176), mode.scale(86));
    }

    protected AbstractNiceRecipeCategory(
            IGuiHelper helper,
            RecipeType<T> recipeType,
            Component title,
            IDrawable icon,
            int width,
            int height
    ) {
        this(helper, recipeType, title, icon, CategoryMode.NORMAL, width, height);
    }

    protected AbstractNiceRecipeCategory(
            IGuiHelper helper,
            RecipeType<T> recipeType,
            Component title,
            IDrawable icon,
            CategoryMode mode,
            int width,
            int height
    ) {
        this.mode = mode;
        this.recipeType = recipeType;
        this.title = title;
        this.icon = icon;

        this.width = width;
        this.height = height;

        this.leftX = mode.scale(8);
        this.leftW = mode.scale(60);
        this.rightX = mode.scale(108);
        this.rightW = mode.scale(60);
        this.panelY = mode.scale(18);
        this.panelH = mode.scale(50);

        this.background = helper.createBlankDrawable(width, height);
    }

    protected int s(int value) {
        return mode.scale(value);
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawFrame(guiGraphics);
    }

    @Override
    public List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return List.of();
    }

    protected List<Component> tooltip(Component... lines) {
        return List.of(lines);
    }

    protected List<Component> tooltip(String... lines) {
        Component[] components = new Component[lines.length];
        for (int i = 0; i < lines.length; i++) {
            components[i] = Component.literal(lines[i]);
        }
        return List.of(components);
    }

    protected List<Component> fluidTankTooltip(String title, Fluid fluid, int amount, int capacity) {
        return tooltip(
                Component.literal(title),
                Component.literal(fluidName(fluid)),
                Component.literal(amount + " / " + capacity + " mB")
        );
    }

    protected List<Component> fluidTankTooltip(String title, ResourceLocation fluidId, int amount, int capacity) {
        return fluidTankTooltip(title, JeiCompatUtil.fluid(fluidId), amount, capacity);
    }

    protected List<Component> gasTankTooltip(String title, ResourceLocation gasId, long amount, long capacity) {
        return tooltip(
                Component.literal(title),
                Component.literal(gasId == null ? "empty" : GasJeiUtil.displayName(gasId)),
                Component.literal(amount + " / " + capacity)
        );
    }

    protected List<Component> gasTankTooltip(String title, ResourceLocation gasId, long amount, long capacity, String unit) {
        return tooltip(
                Component.literal(title),
                Component.literal(gasId == null ? "empty" : GasJeiUtil.displayName(gasId)),
                Component.literal(amount + " / " + capacity + " " + unit)
        );
    }

    protected List<Component> amountTooltip(String title, long amount, long capacity) {
        return tooltip(
                Component.literal(title),
                Component.literal(amount + " / " + capacity)
        );
    }

    protected List<Component> amountTooltip(String title, long amount, long capacity, String unit) {
        return tooltip(
                Component.literal(title),
                Component.literal(amount + " / " + capacity + " " + unit)
        );
    }

    protected String fluidName(Fluid fluid) {
        return fluid == null || fluid == Fluids.EMPTY ? "empty" : fluid.getFluidType().getDescription().getString();
    }

    protected void drawFrame(GuiGraphics g) {
        drawNicePanel(g, leftX - 3, panelY - 3, leftW + 6, panelH + 6);
        drawNicePanel(g, rightX - 3, panelY - 3, rightW + 6, panelH + 6);

        drawCentered(g, "INPUT", leftX, s(6), leftW, LABEL_COLOR);
        drawCentered(g, "OUTPUT", rightX, s(6), rightW, LABEL_COLOR);

        int gapStart = gutterX();
        int gapWidth = gutterW();
        int arrowX = LayoutMath.centeredAt(gapStart, gapWidth, s(18));
        int arrowY = LayoutMath.centeredAt(panelY, panelH, s(10));
        drawArrow(g, arrowX, arrowY);
    }

    private void drawNicePanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, PANEL_BG);
        drawBorderAbsolute(g, x, y, w, h, PANEL_LIGHT_BORDER, PANEL_DARK_BORDER);
    }

    private void drawArrow(GuiGraphics g, int x, int y) {
        drawArrow(g, x, y, s(18), s(10), ACCENT_COLOR);
    }

    protected int leftInnerX() {
        return leftX + s(6);
    }

    protected int leftInnerY() {
        return panelY + s(8);
    }

    protected int leftInnerW() {
        return leftW - s(12);
    }

    protected int leftInnerH() {
        return panelH - s(16);
    }

    protected int rightInnerX() {
        return rightX + s(6);
    }

    protected int rightInnerY() {
        return panelY + s(8);
    }

    protected int rightInnerW() {
        return rightW - s(12);
    }

    protected int rightInnerH() {
        return panelH - s(16);
    }

    protected int gutterX() {
        return leftX + leftW;
    }

    protected int gutterW() {
        return rightX - gutterX();
    }

    protected int centeredY() {
        return LayoutMath.centeredAt(panelY, panelH, SLOT);
    }

    protected int centeredInLeftX() {
        return LayoutMath.centeredAt(leftInnerX(), leftInnerW(), SLOT);
    }

    protected int centeredInRightX() {
        return LayoutMath.centeredAt(rightInnerX(), rightInnerW(), SLOT);
    }

    protected int centeredInGutterX() {
        return LayoutMath.centeredAt(gutterX(), gutterW(), SLOT);
    }

    protected int spreadLeftX(int count, int index) {
        return LayoutMath.horizontalSpreadWithOuterGaps(leftInnerX(), leftInnerW(), SLOT, count, index);
    }

    protected int spreadRightX(int count, int index) {
        return LayoutMath.horizontalSpreadWithOuterGaps(rightInnerX(), rightInnerW(), SLOT, count, index);
    }

    protected int spreadLeftY(int count, int index) {
        return LayoutMath.verticalSpreadWithOuterGaps(leftInnerY(), leftInnerH(), SLOT, count, index);
    }

    protected int spreadRightY(int count, int index) {
        return LayoutMath.verticalSpreadWithOuterGaps(rightInnerY(), rightInnerH(), SLOT, count, index);
    }

    protected int spreadRegionX(int start, int width, int count, int index) {
        return LayoutMath.horizontalSpreadWithOuterGaps(start, width, SLOT, count, index);
    }

    protected int spreadRegionY(int start, int height, int count, int index) {
        return LayoutMath.verticalSpreadWithOuterGaps(start, height, SLOT, count, index);
    }

    protected void drawCentered(GuiGraphics g, String text, int x, int y, int width, int color) {
        var font = Minecraft.getInstance().font;
        int drawX = x + Math.max(0, (width - font.width(text)) / 2);
        g.drawString(font, text, drawX, y, color, false);
    }

    protected void drawPanelLabel(GuiGraphics g, String text, int x, int y, int width) {
        drawCentered(g, text, x, y, width, LABEL_COLOR);
    }

    protected void drawBottomInfo(GuiGraphics g, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        var font = Minecraft.getInstance().font;
        String clipped = font.plainSubstrByWidth(text, width - 10);
        drawCentered(g, clipped, 5, height - 11, width - 10, TEXT_COLOR);
    }

    protected void drawRightInfo(GuiGraphics g, String text, int y) {
        if (text == null || text.isBlank()) {
            return;
        }
        var font = Minecraft.getInstance().font;
        String clipped = font.plainSubstrByWidth(text, rightW + 8);
        drawCentered(g, clipped, rightX - 4, y, rightW + 8, TEXT_COLOR);
    }

    protected void drawSlotCount(GuiGraphics g, int slotX, int slotY, int count) {
        if (count <= 1) {
            return;
        }
        g.drawString(Minecraft.getInstance().font, "x" + count, slotX + 9, slotY + 10, 0xFFF0D57A, false);
    }

    protected void addItemIngredient(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, Ingredient ingredient) {
        builder.addSlot(role, x, y).addIngredients(ingredient);
    }

    protected void addItemStack(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, ItemStack stack) {
        if (!stack.isEmpty()) {
            builder.addSlot(role, x, y).addItemStack(stack);
        }
    }

    protected void addFluid(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, Fluid fluid, int amount) {
        if (fluid != Fluids.EMPTY && amount > 0) {
            builder.addSlot(role, x, y).add(fluid, amount);
        }
    }

    protected void addFluid(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, ResourceLocation fluidId, int amount) {
        addFluid(builder, role, x, y, JeiCompatUtil.fluid(fluidId), amount);
    }

    protected void addGas(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, ResourceLocation gasId, long amount) {
        if (gasId != null && amount > 0) {
            builder.addSlot(role, x, y).addIngredient(GasJeiTypes.GAS, GasJeiIngredient.of(gasId, amount));
        }
    }

    protected void drawGasTankVisual(
            GuiGraphics g,
            ResourceLocation gasId,
            long amount,
            int x,
            int y,
            int w,
            int h
    ) {
        drawGasTankVisual(g, gasId, amount, x, y, w, h, 0.9f);
    }

    protected void drawGasTankVisual(
            GuiGraphics g,
            ResourceLocation gasId,
            long amount,
            int x,
            int y,
            int w,
            int h,
            float fillRatio
    ) {
        if (gasId == null || amount <= 0) {
            return;
        }

        int border = 0xFF657084;
        int bg = 0xFF0C1016;
        int fill = gasColor(gasId);

        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);

        int innerX = x + 2;
        int innerY = y + 2;
        int innerW = Math.max(1, w - 4);
        int innerH = Math.max(1, h - 4);
        int filled = Mth.clamp((int) (innerH * fillRatio), 1, innerH);

        g.fill(innerX, innerY, innerX + innerW, innerY + filled, fill);

        for (int yy = innerY + 2; yy < innerY + filled; yy += 4) {
            g.fill(innerX, yy, innerX + innerW, Math.min(yy + 1, innerY + filled), 0x30FFFFFF);
        }

        var font = Minecraft.getInstance().font;
        String name = JeiCompatUtil.shortId(gasId);
        String clipped = font.plainSubstrByWidth(name, w - 4);
        g.drawString(font, clipped, x + Math.max(1, (w - font.width(clipped)) / 2), y + h + 2, TEXT_COLOR, false);

        String amt = Long.toString(amount);
        g.drawString(font, amt, x + Math.max(1, (w - font.width(amt)) / 2), y - 9, 0xFFD7E3FF, false);
    }

    @Override
    public int drawWidth() {
        return width;
    }

    @Override
    public int drawHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}