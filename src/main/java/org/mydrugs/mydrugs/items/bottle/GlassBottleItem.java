package org.mydrugs.mydrugs.items.bottle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.EatingStrategy;
import org.mydrugs.mydrugs.core.drug.use.DrugUseResult;
import org.mydrugs.mydrugs.core.drug.use.DrugUseSource;
import org.mydrugs.mydrugs.fluids.FluidTypesEx;
import org.mydrugs.mydrugs.fluids.ModFluidTags;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.drugs.DrugTooltipBuilder;

import java.util.List;
import java.util.function.Consumer;

public class GlassBottleItem extends Item {
    public static final int CAPACITY_MB = 250;

    private static final int COMPOSTER_FILL_AMOUNT_MB = 5;
    private static final ResourceLocation AMMONIAC_ID = ModFluids.rl("ammoniac");

    private static final ConsumptionStrategy strategy = new EatingStrategy();

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
            clearContent(stack);
        } else {
            stack.set(ModDataComponents.BOTTLE_CONTENT.get(), new BottleFluidContent(fluidId, remaining));
            stack.set(DataComponents.MAX_STACK_SIZE, 1);
        }

        return extracted;
    }

    public static void setContent(ItemStack stack, @Nullable ResourceLocation fluidId, int amountMb) {
        if (fluidId == null || amountMb <= 0) {
            clearContent(stack);
            return;
        }

        int clamped = Math.min(CAPACITY_MB, amountMb);
        stack.set(ModDataComponents.BOTTLE_CONTENT.get(), new BottleFluidContent(fluidId, clamped));
        stack.set(DataComponents.MAX_STACK_SIZE, 1);
    }

    public static void clearContent(ItemStack stack) {
        stack.remove(ModDataComponents.BOTTLE_CONTENT.get());
        stack.set(DataComponents.MAX_STACK_SIZE, stack.getItem().getDefaultMaxStackSize());
    }

    private static boolean isReadyComposter(BlockState state) {
        return state.is(Blocks.COMPOSTER)
                && state.hasProperty(ComposterBlock.LEVEL)
                && state.getValue(ComposterBlock.LEVEL) == ComposterBlock.READY;
    }

    public static boolean isFluidBottlable(Fluid fluid) {
        return fluid != Fluids.EMPTY && fluid.defaultFluidState().is(ModFluidTags.BOTTLABLE);
    }

    public static boolean isDrinkable(ItemStack stack) {
        BottleFluidContent content = getContent(stack);
        if (content == null || content.amountMb() <= 0) {
            return false;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(content.fluidId());
        return fluid != null && FluidTypesEx.isDrinkable(fluid);
    }

    public static @Nullable DrugModel getBottleDrug(ItemStack stack) {
        BottleFluidContent content = getContent(stack);
        if (content == null || content.amountMb() <= 0) {
            return null;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(content.fluidId());
        return fluid == null ? null : FluidTypesEx.getDrugModel(fluid);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);


        if (!isDrinkable(stack)) {
            return InteractionResult.PASS;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getPlayer() == null) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!context.getPlayer().isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

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
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.bottle.empty").withStyle(ChatFormatting.GRAY));
            return;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(content.fluidId());
        if (fluid == null || fluid == Fluids.EMPTY) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.bottle.fluid_id", content.fluidId().toString()));
        } else {
            tooltipAdder.accept(fluid.getFluidType().getDescription());
        }

        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.bottle.amount", content.amountMb(), CAPACITY_MB));

        DrugModel drug = getBottleDrug(stack);
        if (drug != null) {
            DrugTooltipBuilder.append(stack, List.of(drug), strategy, flag, tooltipAdder);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!isDrinkable(stack)) {
            return stack;
        }

        if (!(livingEntity instanceof ServerPlayer player)) return stack;

        boolean crea = player.gameMode() == GameType.CREATIVE;

        DrugUseResult result = MyDrugs.DRUG_USE_SERVICE.consume(player, getBottleDrug(stack), strategy, DrugUseSource.BOTTLE, stack);
        if (result.status() == DrugUseResult.Status.BLOCKED_MISSING_KNOWLEDGE) {
            return stack;
        }

        if (!level.isClientSide() && !crea) {
            drain(stack, getStoredFluidId(stack), getStoredAmount(stack)); // or some smaller amount if one sip
        }
        return stack;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return isDrinkable(stack) ? ItemUseAnimation.DRINK : ItemUseAnimation.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return isDrinkable(stack) ? 40 : 0;
    }
}
