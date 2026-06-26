package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachments;
import org.mydrugs.mydrugs.energy.PsyCurrentMachines;
import org.mydrugs.mydrugs.machine.manual.ManualMachineSpeedHelper;
import org.mydrugs.mydrugs.machine.manual.ManualMachineType;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferAttachments;
import org.mydrugs.mydrugs.recipes.stomp_crafting.StompCrafterRecipeResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StompCrafterBlockEntity extends BlockEntity {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    private static final int MAX_SLOTS = 32;
    private static final int DEFAULT_REQUIRED_WORK = 100;

    private final List<ItemStack> insertedItems = new ArrayList<>();
    private ItemStack displayStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private double fractionalWork = 0.0D;
    private final StompItemHandler itemHandler = new StompItemHandler();

    // raw work progress
    private int progress = 0;
    private int requiredWork = DEFAULT_REQUIRED_WORK;

    public StompCrafterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STOMP_CRAFTER.get(), pos, state);
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, StompCrafterBlockEntity be) {
        if (level instanceof ServerLevel serverLevel) {
            be.tickAutomation(serverLevel);
        }
    }

    private static ItemStack buildDisplayStackFor(List<ItemStack> items, ItemStack basis) {
        int count = 0;
        for (ItemStack stack : items) {
            if (ItemStack.isSameItemSameComponents(stack, basis)) {
                count++;
            }
        }

        ItemStack shown = basis.copy();
        shown.setCount(Math.min(count, shown.getMaxStackSize()));
        return shown;
    }

    // For rendering: always expose 0..100
    public int getProgressPercent() {
        if (this.requiredWork <= 0) {
            return 0;
        }
        return Mth.clamp((this.progress * 100) / this.requiredWork, 0, 100);
    }

    public boolean isFull() {
        return this.insertedItems.size() >= MAX_SLOTS;
    }

    public List<ItemStack> getUniqueExampleStacks() {
        List<ItemStack> result = new ArrayList<>();

        for (ItemStack stack : this.insertedItems) {
            if (stack.isEmpty()) continue;

            boolean alreadyPresent = false;
            for (ItemStack existing : result) {
                if (ItemStack.isSameItemSameComponents(existing, stack)) {
                    alreadyPresent = true;
                    break;
                }
            }

            if (!alreadyPresent) {
                result.add(stack.copyWithCount(1));
            }
        }

        return result;
    }

    public void addProgressFromFall(ServerLevel level, double fallDistance) {
        addProgressFromFall(level, fallDistance, null);
    }

    public void addProgressFromFall(ServerLevel level, double fallDistance, Player player) {
        if (!this.outputStack.isEmpty()) {
            return;
        }
        StompCrafterRecipeResolver.ProcessMatch match =
                StompCrafterRecipeResolver.findExactMatch(level, this.insertedItems);

        if (match == null) {
            return;
        }

        this.requiredWork = Math.max(1, match.requiredWork());

        float speed = player instanceof ServerPlayer serverPlayer
                ? ManualMachineSpeedHelper.getSpeedMultiplier(serverPlayer, ManualMachineType.STOMP_CRAFTER)
                : 1.0F;
        int gained = Math.max(1, (int) Math.floor(fallDistance * 12.0D * speed));
        this.progress = Mth.clamp(this.progress + gained, 0, this.requiredWork);
        this.markUpdated();

        if (this.progress >= this.requiredWork) {
            ItemStack result = match.assemble(level, this.insertedItems);

            Containers.dropItemStack(
                    level,
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 1.0D,
                    this.worldPosition.getZ() + 0.5D,
                    result
            );

            org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(
                    this,
                    recipeId(match),
                    org.mydrugs.mydrugs.machine.MachineCompletionHelper.itemId(result),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            );
            clearCrafter();
        }
    }

    private static Optional<ResourceLocation> recipeId(StompCrafterRecipeResolver.ProcessMatch match) {
        if (match instanceof StompCrafterRecipeResolver.GrindingMatch grinding) {
            return Optional.of(grinding.holder().id().location());
        }
        if (match instanceof StompCrafterRecipeResolver.StompMatch stomp) {
            return Optional.of(stomp.holder().id().location());
        }
        return Optional.empty();
    }

    public void clearCrafter() {
        this.insertedItems.clear();
        this.displayStack = ItemStack.EMPTY;
        this.progress = 0;
        this.requiredWork = DEFAULT_REQUIRED_WORK;
        this.fractionalWork = 0.0D;
        this.markUpdated();
    }

    private void markUpdated() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.progress = input.getIntOr("progress", 0);
        this.requiredWork = input.getIntOr("required_work", DEFAULT_REQUIRED_WORK);

        this.insertedItems.clear();
        this.insertedItems.addAll(input.read("items", ItemStack.CODEC.listOf()).orElse(List.of()));

        if (this.insertedItems.size() > MAX_SLOTS) {
            this.insertedItems.subList(MAX_SLOTS, this.insertedItems.size()).clear();
        }

        this.displayStack = input.read("display", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.outputStack = input.read("output", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.fractionalWork = input.getDoubleOr("fractional_work", 0.0D);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("progress", this.progress);
        output.putInt("required_work", this.requiredWork);
        output.store("items", ItemStack.CODEC.listOf(), this.insertedItems);

        if (!this.displayStack.isEmpty()) {
            output.store("display", ItemStack.CODEC, this.displayStack);
        }
        if (!this.outputStack.isEmpty()) {
            output.store("output", ItemStack.CODEC, this.outputStack);
        }
        output.putDouble("fractional_work", this.fractionalWork);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        for (ItemStack stack : this.insertedItems) {
            if (!stack.isEmpty()) {
                Containers.dropItemStack(
                        this.level,
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        stack
                );
            }
        }
        if (!this.outputStack.isEmpty()) {
            Containers.dropItemStack(
                    this.level,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    this.outputStack
            );
        }
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        super.onDataPacket(connection, input);
    }

    public boolean canAcceptInsertion(ServerLevel level, ItemStack heldStack) {
        if (heldStack.isEmpty() || this.isFull() || !this.outputStack.isEmpty()) {
            return false;
        }

        List<ItemStack> test = new ArrayList<>(this.insertedItems);
        test.add(heldStack.copyWithCount(1));

        return StompCrafterRecipeResolver.canAcceptPartial(level, test);
    }

    public void insertAcceptedItem(ItemStack oneItem) {
        if (oneItem.isEmpty() || this.isFull()) {
            return;
        }

        ItemStack inserted = oneItem.copyWithCount(1);
        this.insertedItems.add(inserted);
        this.displayStack = buildDisplayStackFor(this.insertedItems, inserted);
        this.progress = 0;
        this.requiredWork = DEFAULT_REQUIRED_WORK;
        this.markUpdated();
    }

    public ResourceHandler<ItemResource> getItemCapability(net.minecraft.core.Direction side) {
        return this.itemHandler;
    }

    private void tickAutomation(ServerLevel level) {
        if (!MachineEnergyAttachments.get(this).hasAutomationUpgrade() || !this.outputStack.isEmpty()) {
            return;
        }

        StompCrafterRecipeResolver.ProcessMatch match =
                StompCrafterRecipeResolver.findExactMatch(level, this.insertedItems);
        if (match == null) {
            return;
        }

        this.requiredWork = Math.max(1, match.requiredWork());
        if (!PsyCurrentMachines.tryUseAutomationCurrentTick(this)) {
            return;
        }

        this.fractionalWork += 1.0D;
        int gained = (int) this.fractionalWork;
        if (gained <= 0) {
            return;
        }
        this.fractionalWork -= gained;
        this.progress = Mth.clamp(this.progress + gained, 0, this.requiredWork);
        if (this.progress >= this.requiredWork) {
            this.outputStack = match.assemble(level, this.insertedItems);
            org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(
                    this,
                    recipeId(match),
                    org.mydrugs.mydrugs.machine.MachineCompletionHelper.itemId(this.outputStack),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            );
            this.insertedItems.clear();
            this.displayStack = ItemStack.EMPTY;
            this.progress = 0;
            this.requiredWork = DEFAULT_REQUIRED_WORK;
            this.fractionalWork = 0.0D;
        }
        this.markUpdated();
    }

    private final class StompItemHandler implements ResourceHandler<ItemResource> {
        private final OutputJournal outputJournal = new OutputJournal();

        @Override
        public int size() {
            return 2;
        }

        @Override
        public ItemResource getResource(int slot) {
            return slot == OUTPUT_SLOT && !outputStack.isEmpty() ? ItemResource.of(outputStack) : ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int slot) {
            return slot == OUTPUT_SLOT ? outputStack.getCount() : 0;
        }

        @Override
        public long getCapacityAsLong(int slot, ItemResource resource) {
            if (slot == INPUT_SLOT) {
                return MAX_SLOTS - insertedItems.size();
            }
            return slot == OUTPUT_SLOT ? outputStack.getMaxStackSize() : 0;
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            if (slot != INPUT_SLOT || resource.isEmpty() || !(level instanceof ServerLevel serverLevel)) {
                return false;
            }
            return canAcceptInsertion(serverLevel, resource.toStack(1));
        }

        @Override
        public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            if (!isValid(slot, resource) || amount <= 0) {
                return 0;
            }
            int accepted = 0;
            List<ItemStack> test = new ArrayList<>(insertedItems);
            while (accepted < amount && test.size() < MAX_SLOTS) {
                test.add(resource.toStack(1));
                if (!(level instanceof ServerLevel serverLevel) || !StompCrafterRecipeResolver.canAcceptPartial(serverLevel, test)) {
                    break;
                }
                accepted++;
            }
            if (accepted <= 0) {
                return 0;
            }
            outputJournal.updateSnapshots(transaction);
            for (int i = 0; i < accepted; i++) {
                insertedItems.add(resource.toStack(1));
            }
            displayStack = buildDisplayStackFor(insertedItems, resource.toStack(1));
            progress = 0;
            requiredWork = DEFAULT_REQUIRED_WORK;
            fractionalWork = 0.0D;
            return accepted;
        }

        @Override
        public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            if (slot != OUTPUT_SLOT || resource.isEmpty() || amount <= 0 || outputStack.isEmpty() || !resource.matches(outputStack)) {
                return 0;
            }
            int extracted = Math.min(amount, outputStack.getCount());
            outputJournal.updateSnapshots(transaction);
            outputStack.shrink(extracted);
            if (outputStack.isEmpty()) {
                outputStack = ItemStack.EMPTY;
            }
            return extracted;
        }
    }

    private final class OutputJournal extends SnapshotJournal<StompSnapshot> {
        @Override
        protected StompSnapshot createSnapshot() {
            return new StompSnapshot(List.copyOf(insertedItems), displayStack.copy(), outputStack.copy(), progress, requiredWork, fractionalWork);
        }

        @Override
        protected void revertToSnapshot(StompSnapshot snapshot) {
            insertedItems.clear();
            for (ItemStack stack : snapshot.inputs()) {
                insertedItems.add(stack.copy());
            }
            displayStack = snapshot.display().copy();
            outputStack = snapshot.output().copy();
            progress = snapshot.progress();
            requiredWork = snapshot.requiredWork();
            fractionalWork = snapshot.fractionalWork();
        }

        @Override
        protected void onRootCommit(StompSnapshot originalState) {
            MachineTransferAttachments.markCapabilityChanged(StompCrafterBlockEntity.this);
            markUpdated();
        }
    }

    private record StompSnapshot(
            List<ItemStack> inputs,
            ItemStack display,
            ItemStack output,
            int progress,
            int requiredWork,
            double fractionalWork
    ) {
    }
}
