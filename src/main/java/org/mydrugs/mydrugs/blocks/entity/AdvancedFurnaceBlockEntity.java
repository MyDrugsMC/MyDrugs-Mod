package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.menu.AdvancedFurnaceMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.advanced_furnace.AdvancedFurnaceRecipe;
import org.mydrugs.mydrugs.recipes.advanced_furnace.AdvancedFurnaceRecipeInput;

import java.util.Optional;


public class AdvancedFurnaceBlockEntity extends BaseContainerBlockEntity {
    public static final int INPUT_A_SLOT = 0;
    public static final int INPUT_B_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int OUTPUT_A_SLOT = 3;
    public static final int OUTPUT_B_SLOT = 4;
    public static final int SLOT_COUNT = 5;

    public static final int TANK_CAPACITY = 4000;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private int progress = 0;
    private int maxProgress = 200;

    private int burnTime = 0;
    private int burnDuration = 0;

    private ResourceLocation tankFluidId = null;
    private int tankAmount = 0;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> AdvancedFurnaceBlockEntity.this.progress;
                case 1 -> AdvancedFurnaceBlockEntity.this.maxProgress;
                case 2 -> AdvancedFurnaceBlockEntity.this.burnTime;
                case 3 -> AdvancedFurnaceBlockEntity.this.burnDuration;
                case 4 -> AdvancedFurnaceBlockEntity.this.tankAmount;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> AdvancedFurnaceBlockEntity.this.progress = value;
                case 1 -> AdvancedFurnaceBlockEntity.this.maxProgress = value;
                case 2 -> AdvancedFurnaceBlockEntity.this.burnTime = value;
                case 3 -> AdvancedFurnaceBlockEntity.this.burnDuration = value;
                case 4 -> AdvancedFurnaceBlockEntity.this.tankAmount = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public AdvancedFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_FURNACE.get(), pos, state);
    }

    public static int getFuelTime(ItemStack stack) {
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) return 1600;
        if (stack.is(Items.BLAZE_ROD)) return 2400;
        return 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AdvancedFurnaceBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }

        boolean changed = false;

        if (be.burnTime > 0) {
            be.burnTime--;
            changed = true;
        }

        Optional<RecipeHolder<AdvancedFurnaceRecipe>> recipeHolder = be.getCurrentRecipe((ServerLevel) level);

        if (recipeHolder.isPresent()) {
            AdvancedFurnaceRecipe recipe = recipeHolder.get().value();
            be.maxProgress = recipe.cookTime();

            boolean canCraft = be.canCraft(recipe);

            if (canCraft) {
                if (be.burnTime <= 0) {
                    ItemStack fuelStack = be.getItem(FUEL_SLOT);
                    int fuelTime = getFuelTime(fuelStack);

                    if (fuelTime > 0) {
                        be.burnTime = fuelTime;
                        be.burnDuration = fuelTime;
                        fuelStack.shrink(1);
                        changed = true;
                    }
                }

                if (be.burnTime > 0) {
                    be.progress++;
                    changed = true;

                    if (be.progress >= be.maxProgress) {
                        be.craft(recipe);
                        be.progress = 0;
                        changed = true;
                    }
                } else {
                    if (be.progress != 0) {
                        be.progress = 0;
                        changed = true;
                    }
                }
            } else {
                if (be.progress != 0) {
                    be.progress = 0;
                    changed = true;
                }
            }
        } else {
            if (be.progress != 0) {
                be.progress = 0;
                changed = true;
            }
        }

        if (changed) {
            be.setChanged();
        }
    }

    private static boolean canInsertItem(ItemStack existing, ItemStack result) {
        if (result.isEmpty()) return true;
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, result)) return false;
        return existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.advanced_furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new AdvancedFurnaceMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
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
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case OUTPUT_A_SLOT, OUTPUT_B_SLOT -> false;
            case FUEL_SLOT -> getFuelTime(stack) > 0;
            default -> true;
        };
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getBurnDuration() {
        return burnDuration;
    }

    public int getTankAmount() {
        return tankAmount;
    }

    public ResourceLocation getTankFluidId() {
        return tankFluidId;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.progress = input.getIntOr("progress", 0);
        this.maxProgress = input.getIntOr("max_progress", 200);
        this.burnTime = input.getIntOr("burn_time", 0);
        this.burnDuration = input.getIntOr("burn_duration", 0);

        String fluidId = input.getStringOr("tank_fluid", "");
        this.tankFluidId = fluidId.isEmpty() ? null : ResourceLocation.parse(fluidId);
        this.tankAmount = input.getIntOr("tank_amount", 0);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("progress", this.progress);
        output.putInt("max_progress", this.maxProgress);
        output.putInt("burn_time", this.burnTime);
        output.putInt("burn_duration", this.burnDuration);
        output.putInt("tank_amount", this.tankAmount);

        if (this.tankFluidId != null && this.tankAmount > 0) {
            output.putString("tank_fluid", this.tankFluidId.toString());
        }
    }

    private Optional<RecipeHolder<AdvancedFurnaceRecipe>> getCurrentRecipe(ServerLevel level) {
        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.ADVANCED_FURNACE.get(),
                new AdvancedFurnaceRecipeInput(this.getItem(INPUT_A_SLOT), this.getItem(INPUT_B_SLOT)),
                level
        );
    }

    private boolean canCraft(AdvancedFurnaceRecipe recipe) {
        if (!canInsertItem(this.getItem(OUTPUT_A_SLOT), recipe.resultA())) return false;
        if (!canInsertItem(this.getItem(OUTPUT_B_SLOT), recipe.resultB())) return false;
        return canInsertFluid(recipe.fluidOutput(), recipe.fluidAmount());
    }

    private boolean canInsertFluid(ResourceLocation fluidId, int amount) {
        if (amount <= 0) return true;
        if (this.tankAmount == 0) {
            return amount <= TANK_CAPACITY;
        }
        if (!fluidId.equals(this.tankFluidId)) {
            return false;
        }
        return this.tankAmount + amount <= TANK_CAPACITY;
    }

    private void craft(AdvancedFurnaceRecipe recipe) {
        this.removeItem(INPUT_A_SLOT, 1);

        if (recipe.inputB().isPresent()) {
            this.removeItem(INPUT_B_SLOT, 1);
        }

        insertItem(OUTPUT_A_SLOT, recipe.resultA());
        insertItem(OUTPUT_B_SLOT, recipe.resultB());
        insertFluid(recipe.fluidOutput(), recipe.fluidAmount());
    }

    private void insertItem(int slot, ItemStack result) {
        if (result.isEmpty()) return;

        ItemStack existing = this.getItem(slot);
        if (existing.isEmpty()) {
            this.setItem(slot, result.copy());
        } else {
            existing.grow(result.getCount());
            this.setItem(slot, existing);
        }
    }

    private void insertFluid(ResourceLocation fluidId, int amount) {
        if (amount <= 0) return;

        if (this.tankAmount == 0) {
            this.tankFluidId = fluidId;
            this.tankAmount = amount;
            return;
        }

        if (fluidId.equals(this.tankFluidId)) {
            this.tankAmount = Mth.clamp(this.tankAmount + amount, 0, TANK_CAPACITY);
        }
    }

    public boolean tryExtractFluid(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty()) return false;

        ResourceLocation sourceId;
        int sourceAmount;

        if (tankFluidId != null && tankAmount > 0) {
            sourceId = tankFluidId;
            sourceAmount = tankAmount;
        } else {
            return false;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(sourceId);
        if (fluid == null || fluid == Fluids.EMPTY) return false;

        ItemAccess access = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) return false;

        FluidResource resource = FluidResource.of(fluid);

        int transferred;
        try (var tx = Transaction.openRoot()) {
            transferred = handler.insert(resource, sourceAmount, tx);
            if (transferred <= 0) {
                return false;
            }

            tx.commit();
        }

        removeFromTank(transferred);
        setChanged(); // or setChanged() + sendBlockUpdated(...)
        return true;
    }

    private void removeFromTank(int amount) {
        tankAmount -= amount;
        if (tankAmount <= 0) {
            tankAmount = 0;
            tankFluidId = null;
        }
    }

    //tg dark
}