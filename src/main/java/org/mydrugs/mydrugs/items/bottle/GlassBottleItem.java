package org.mydrugs.mydrugs.items.bottle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.fluids.ModFluidTags;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.registry.ModDataComponents;

import java.util.function.Consumer;

public class GlassBottleItem extends Item {
    public static final int CAPACITY_MB = 250;

    private static final int COMPOSTER_FILL_AMOUNT_MB = 5;
    private static final ResourceLocation AMMONIAC_ID = ModFluids.rl("ammoniac");
    public GlassBottleItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static BottleFluidContent getContent(ItemStack stack) {
        return stack.get(ModDataComponents.BOTTLE_CONTENT.get());
    }

    @Nullable
    public static ResourceLocation getStoredFluidId(ItemStack stack) {
        BottleFluidContent content = getContent(stack);
        return content == null ? null : content.fluidId();
    }

    public static int getStoredAmount(ItemStack stack) {
        BottleFluidContent content = getContent(stack);
        return content == null ? 0 : content.amountMb();
    }

    public static boolean isEmptyBottle(ItemStack stack) {
        return getStoredAmount(stack) <= 0;
    }

    public static int getRemainingCapacity(ItemStack stack, @Nullable ResourceLocation incomingFluidId) {
        ResourceLocation storedFluidId = getStoredFluidId(stack);
        if (storedFluidId != null && incomingFluidId != null && !storedFluidId.equals(incomingFluidId)) {
            return 0;
        }

        return Math.max(0, CAPACITY_MB - getStoredAmount(stack));
    }

    public static boolean canAcceptFluid(ItemStack stack, ResourceLocation fluidId, int amountMb) {
        return amountMb > 0 && getRemainingCapacity(stack, fluidId) > 0;
    }

    public static int fill(ItemStack stack, ResourceLocation fluidId, int amountMb) {
        if (amountMb <= 0) {
            return 0;
        }

        int storedAmount = getStoredAmount(stack);
        int space = getRemainingCapacity(stack, fluidId);
        if (space <= 0) {
            return 0;
        }

        int inserted = Math.min(space, amountMb);
        setContent(stack, fluidId, storedAmount + inserted);
        return inserted;
    }

    public static int drain(ItemStack stack, ResourceLocation fluidId, int amountMb) {
        if (amountMb <= 0) {
            return 0;
        }

        BottleFluidContent content = getContent(stack);
        if (content == null || !content.fluidId().equals(fluidId)) {
            return 0;
        }

        int extracted = Math.min(content.amountMb(), amountMb);
        int remaining = content.amountMb() - extracted;

        if (remaining <= 0) {
            stack.remove(ModDataComponents.BOTTLE_CONTENT.get());
        } else {
            stack.set(ModDataComponents.BOTTLE_CONTENT.get(), new BottleFluidContent(fluidId, remaining));
        }

        return extracted;
    }

    public static void setContent(ItemStack stack, @Nullable ResourceLocation fluidId, int amountMb) {
        if (fluidId == null || amountMb <= 0) {
            stack.remove(ModDataComponents.BOTTLE_CONTENT.get());
            return;
        }

        int clamped = Math.min(CAPACITY_MB, amountMb);
        stack.set(ModDataComponents.BOTTLE_CONTENT.get(), new BottleFluidContent(fluidId, clamped));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!isReadyComposter(state)) {
            return InteractionResult.PASS;
        }

        if (!canAcceptFluid(stack, AMMONIAC_ID, COMPOSTER_FILL_AMOUNT_MB)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            fill(stack, AMMONIAC_ID, COMPOSTER_FILL_AMOUNT_MB);
            level.setBlock(pos, state.setValue(ComposterBlock.LEVEL, 0), Block.UPDATE_ALL);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

        BottleFluidContent content = getContent(stack);
        if (content == null) {
            tooltipAdder.accept(Component.literal("Empty"));
            return;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(content.fluidId());
        if (fluid == null || fluid == Fluids.EMPTY) {
            tooltipAdder.accept(Component.literal(content.fluidId().toString()));
        } else {
            tooltipAdder.accept(fluid.getFluidType().getDescription());
        }

        tooltipAdder.accept(Component.literal(content.amountMb() + " / " + CAPACITY_MB + " mB"));
    }

    private static boolean isReadyComposter(BlockState state) {
        return state.is(Blocks.COMPOSTER)
                && state.hasProperty(ComposterBlock.LEVEL)
                && state.getValue(ComposterBlock.LEVEL) == ComposterBlock.READY;
    }

    public static boolean isFluidBottlable(Fluid fluid) {
        return fluid != Fluids.EMPTY && fluid.defaultFluidState().is(ModFluidTags.BOTTLABLE);
    }
}
