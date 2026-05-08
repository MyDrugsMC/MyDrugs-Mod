package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.machine.manual.ManualMachineSpeedHelper;
import org.mydrugs.mydrugs.machine.manual.ManualMachineType;
import org.mydrugs.mydrugs.recipes.stomp_crafting.StompCrafterRecipeResolver;

import java.util.ArrayList;
import java.util.List;

public class StompCrafterBlockEntity extends BlockEntity {
    private static final int MAX_SLOTS = 32;
    private static final int DEFAULT_REQUIRED_WORK = 100;

    private final List<ItemStack> insertedItems = new ArrayList<>();
    private ItemStack displayStack = ItemStack.EMPTY;

    // raw work progress
    private int progress = 0;
    private int requiredWork = DEFAULT_REQUIRED_WORK;

    public StompCrafterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STOMP_CRAFTER.get(), pos, state);
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

            org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(this);
            clearCrafter();
        }
    }

    public void clearCrafter() {
        this.insertedItems.clear();
        this.displayStack = ItemStack.EMPTY;
        this.progress = 0;
        this.requiredWork = DEFAULT_REQUIRED_WORK;
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
        if (heldStack.isEmpty() || this.isFull()) {
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
}
