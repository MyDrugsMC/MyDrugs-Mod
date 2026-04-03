package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.menu.GrowthChamberMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.growthchamber.GrowthChamberItemStack;
import org.mydrugs.mydrugs.recipes.growthchamber.GrowthChamberRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Optional;

public class GrowthChamberBlockEntity extends BaseContainerBlockEntity {
    public static final int WATER_CAPACITY = 4000;

    public static final int INPUT_SLOT = 0;
    public static final int BIOMASS_SLOT = 1;
    public static final int MIDDLE_SLOT = 2;
    public static final int FINAL_SLOT = 3;

    private NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);

    private int waterAmount = 0;

    private int growthProgress = 0;
    private int growthMaxProgress = 200;

    private int matureProgress = 0;
    private int matureMaxProgress = 200;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> waterAmount;
                case 1 -> growthProgress;
                case 2 -> growthMaxProgress;
                case 3 -> matureProgress;
                case 4 -> matureMaxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> waterAmount = value;
                case 1 -> growthProgress = value;
                case 2 -> growthMaxProgress = value;
                case 3 -> matureProgress = value;
                case 4 -> matureMaxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public GrowthChamberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GROWTH_CHAMBER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GrowthChamberBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = false;

        // -------------------------
        // 1) GROWTH STEP
        // -------------------------
        Optional<RecipeHolder<GrowthChamberRecipe>> stage1Holder = be.getStage1Recipe(serverLevel);

        if (stage1Holder.isPresent()) {
            GrowthChamberRecipe recipe = stage1Holder.get().value();
            if (be.growthMaxProgress != recipe.baseTicks()) {
                be.growthMaxProgress = recipe.baseTicks();
                changed = true;
            }

            if (be.canCraftStage1(recipe)) {
                be.growthProgress++;
                changed = true;

                if (be.growthProgress >= be.growthMaxProgress) {
                    be.craftStage1(recipe);
                    be.growthProgress = 0;
                    changed = true;
                }
            } else if (be.growthProgress != 0) {
                be.growthProgress = 0;
                changed = true;
            }
        } else {
            if (be.growthProgress != 0) {
                be.growthProgress = 0;
                changed = true;
            }
            if (be.growthMaxProgress != 200) {
                be.growthMaxProgress = 200;
                changed = true;
            }
        }

        // -------------------------
        // 2) MATURING STEP
        // -------------------------
        // Re-query AFTER growth so fresh middle items can start maturing immediately.
        Optional<RecipeHolder<GrowthChamberRecipe>> stage2Holder = be.getStage2Recipe(serverLevel);

        if (stage2Holder.isPresent()) {
            GrowthChamberRecipe recipe = stage2Holder.get().value();
            if (be.matureMaxProgress != recipe.baseTicks()) {
                be.matureMaxProgress = recipe.baseTicks();
                changed = true;
            }

            if (be.canCraftStage2(recipe)) {
                be.matureProgress++;
                changed = true;

                if (be.matureProgress >= be.matureMaxProgress) {
                    be.craftStage2(recipe);
                    be.matureProgress = 0;
                    changed = true;
                }
            } else if (be.matureProgress != 0) {
                be.matureProgress = 0;
                changed = true;
            }
        } else {
            if (be.matureProgress != 0) {
                be.matureProgress = 0;
                changed = true;
            }
            if (be.matureMaxProgress != 200) {
                be.matureMaxProgress = 200;
                changed = true;
            }
        }

        if (changed) {
            be.sync();
        }
    }

    private Optional<RecipeHolder<GrowthChamberRecipe>> getStage1Recipe(ServerLevel level) {
        ItemStack inputStack = this.getItem(INPUT_SLOT);
        ItemStack biomassStack = this.getItem(BIOMASS_SLOT);

        if (inputStack.isEmpty() || biomassStack.isEmpty()) {
            return Optional.empty();
        }

        for (RecipeHolder<GrowthChamberRecipe> holder : level.recipeAccess()
                .recipeMap()
                .byType(ModRecipeTypes.GROWTH_CHAMBER.get())) {
            if (holder.value().matchesStage1(inputStack, biomassStack)) {
                return Optional.of(holder);
            }
        }

        return Optional.empty();
    }

    private Optional<RecipeHolder<GrowthChamberRecipe>> getStage2Recipe(ServerLevel level) {
        ItemStack middleStack = this.getItem(MIDDLE_SLOT);
        if (middleStack.isEmpty()) {
            return Optional.empty();
        }

        for (RecipeHolder<GrowthChamberRecipe> holder : level.recipeAccess()
                .recipeMap()
                .byType(ModRecipeTypes.GROWTH_CHAMBER.get())) {
            if (holder.value().matchesMiddle(middleStack)) {
                return Optional.of(holder);
            }
        }

        return Optional.empty();
    }

    private boolean canCraftStage1(GrowthChamberRecipe recipe) {
        ItemStack inputStack = this.getItem(INPUT_SLOT);
        ItemStack biomassStack = this.getItem(BIOMASS_SLOT);

        if (!recipe.input().matches(inputStack)) {
            return false;
        }

        if (!recipe.biomassInput().matches(biomassStack)) {
            return false;
        }

        if (this.waterAmount < recipe.water()) {
            return false;
        }

        return canAcceptOutput(MIDDLE_SLOT, recipe.middleResult());
    }

    private boolean canCraftStage2(GrowthChamberRecipe recipe) {
        ItemStack middleStack = this.getItem(MIDDLE_SLOT);

        if (!recipe.middleResult().matches(middleStack)) {
            return false;
        }

        return canAcceptOutput(FINAL_SLOT, recipe.finalResult());
    }

    private boolean canAcceptOutput(int slot, GrowthChamberItemStack output) {
        ItemStack current = this.getItem(slot);
        ItemStack result = output.toStack();

        if (current.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameComponents(current, result)) {
            return false;
        }

        return current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    private void craftStage1(GrowthChamberRecipe recipe) {
        removeItems(INPUT_SLOT, recipe.input().count());
        removeItems(BIOMASS_SLOT, recipe.biomassInput().count());

        this.waterAmount -= recipe.water();
        if (this.waterAmount < 0) {
            this.waterAmount = 0;
        }

        addResult(MIDDLE_SLOT, recipe.middleResult());
    }

    private void craftStage2(GrowthChamberRecipe recipe) {
        removeItems(MIDDLE_SLOT, recipe.middleResult().count());
        addResult(FINAL_SLOT, recipe.finalResult());
    }

    private void removeItems(int slot, int count) {
        ItemStack stack = this.getItem(slot);
        stack.shrink(count);
        if (stack.isEmpty()) {
            this.setItem(slot, ItemStack.EMPTY);
        }
    }

    private void addResult(int slot, GrowthChamberItemStack result) {
        ItemStack current = this.getItem(slot);
        ItemStack toInsert = result.toStack();

        if (current.isEmpty()) {
            this.setItem(slot, toInsert);
            return;
        }

        current.grow(toInsert.getCount());
        this.setItem(slot, current);
    }

    public boolean tryInsertWaterFromHeld(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty()) {
            return false;
        }

        ItemAccess access = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) {
            return false;
        }

        FluidResource resource = handler.getResource(0);
        int containedAmount = handler.getAmountAsInt(0);

        if (resource.isEmpty() || containedAmount <= 0 || resource.getFluid() != Fluids.WATER) {
            return false;
        }

        int freeSpace = WATER_CAPACITY - this.waterAmount;
        if (freeSpace <= 0) {
            return false;
        }

        int request = Math.min(containedAmount, freeSpace);

        if (held.getItem() instanceof BucketItem) {
            if (freeSpace < FluidType.BUCKET_VOLUME) {
                return false;
            }
            request = FluidType.BUCKET_VOLUME;
        }

        try (var tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, request, tx);
            if (extracted != request) {
                return false;
            }

            tx.commit();
            this.waterAmount = Math.min(WATER_CAPACITY, this.waterAmount + extracted);
        }

        sync();
        return true;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.growth_chamber");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new GrowthChamberMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    @Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);

        this.waterAmount = input.getIntOr("WaterAmount", 0);

        this.growthProgress = input.getIntOr("GrowthProgress", 0);
        this.growthMaxProgress = input.getIntOr("GrowthMaxProgress", 200);

        this.matureProgress = input.getIntOr("MatureProgress", 0);
        this.matureMaxProgress = input.getIntOr("MatureMaxProgress", 200);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);

        output.putInt("WaterAmount", this.waterAmount);

        output.putInt("GrowthProgress", this.growthProgress);
        output.putInt("GrowthMaxProgress", this.growthMaxProgress);

        output.putInt("MatureProgress", this.matureProgress);
        output.putInt("MatureMaxProgress", this.matureMaxProgress);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case INPUT_SLOT, BIOMASS_SLOT -> true;
            case MIDDLE_SLOT, FINAL_SLOT -> false;
            default -> false;
        };
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}