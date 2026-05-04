package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.psy_anvil.PsyAnvilRecipe;
import org.mydrugs.mydrugs.recipes.psy_anvil.PsyAnvilRecipeInput;

import java.util.Optional;

public final class PsyAnvilBlockEntity extends BlockEntity implements Container {
    public static final int SLOT_COUNT = 9;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public PsyAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PSY_ANVIL.get(), pos, state);
    }

    public boolean insertOne(ItemStack source, Player player) {
        if (!(this.level instanceof ServerLevel) || source.isEmpty() || isHammer(source)) {
            return false;
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (this.items.get(i).isEmpty()) {
                this.items.set(i, source.copyWithCount(1));
                if (!player.getAbilities().instabuild) {
                    source.shrink(1);
                }
                markDirtyAndSync();
                return true;
            }
        }
        return false;
    }

    public boolean takeLast(Player player) {
        if (!(this.level instanceof ServerLevel)) {
            return false;
        }
        System.out.println("counting...");
        for (int i = SLOT_COUNT - 1; i >= 0; i--) {
            ItemStack stack = this.items.get(i);
            if (!stack.isEmpty()) {
                this.items.set(i, ItemStack.EMPTY);
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                markDirtyAndSync();
                return true;
            }
        }
        return false;
    }

    public boolean craftWithHammer(ServerPlayer player, ItemStack hammer) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return false;
        }

        Optional<RecipeHolder<PsyAnvilRecipe>> match = serverLevel.recipeAccess().getRecipeFor(
                ModRecipeTypes.PSY_ANVIL.get(),
                PsyAnvilRecipeInput.of(this.items),
                serverLevel
        );
        if (match.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mydrugs.psy_anvil.no_recipe"), true);
            return false;
        }

        PsyAnvilRecipe recipe = match.get().value();
        if (!recipe.canCraft(player)) {
            Component required = recipe.requiredKnowledgeKey()
                    .map(PsyKnowledgeKey::translationKey)
                    .map(Component::translatable)
                    .orElse(Component.translatable("screen.mydrugs.psy_anvil.unknown"));
            player.displayClientMessage(Component.translatable("message.mydrugs.psy_anvil.locked", required), true);
            return false;
        }

        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = this.items.get(i);
            if (!stack.isEmpty()) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    this.items.set(i, ItemStack.EMPTY);
                }
            }
        }

        ItemStack result = recipe.result();
        ResourceLocation resultId = BuiltInRegistries.ITEM.getKey(result.getItem());
        if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }
        damageHammer(player, hammer);
        AdvancementEventHooks.machineRecipeCompleted(this, Optional.of(match.get().id().location()), Optional.of(resultId));
        markDirtyAndSync();
        return true;
    }

    public ItemStack getRenderStack(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? this.items.get(slot) : ItemStack.EMPTY;
    }

    public static boolean isHammer(ItemStack stack) {
        Item item = stack.getItem();
        return item == ModItems.STONE_HAMMER.get()
                || item == ModItems.IRON_HAMMER.get()
                || item == ModItems.STEEL_HAMMER.get();
    }

    private static void damageHammer(ServerPlayer player, ItemStack hammer) {
        if (player.getAbilities().instabuild || !hammer.isDamageableItem()) {
            return;
        }
        hammer.setDamageValue(hammer.getDamageValue() + 1);
        if (hammer.getDamageValue() >= hammer.getMaxDamage()) {
            hammer.shrink(1);
        }
    }

    private void markDirtyAndSync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? this.items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SLOT_COUNT || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = this.items.get(slot).split(amount);
        if (this.items.get(slot).isEmpty()) {
            this.items.set(slot, ItemStack.EMPTY);
        }
        markDirtyAndSync();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.items.get(slot);
        this.items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) return;
        this.items.set(slot, stack.copy());
        markDirtyAndSync();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.items.set(i, ItemStack.EMPTY);
        }
        markDirtyAndSync();
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.items.set(i, ItemStack.EMPTY);
        }
        for (ValueInput child : input.childrenListOrEmpty("items")) {
            int slot = child.getIntOr("slot", -1);
            ItemStack stack = child.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            if (slot >= 0 && slot < SLOT_COUNT && !stack.isEmpty()) {
                this.items.set(slot, stack);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput.ValueOutputList list = output.childrenList("items");
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = this.items.get(i);
            if (stack.isEmpty()) continue;
            ValueOutput child = list.addChild();
            child.putInt("slot", i);
            child.store("stack", ItemStack.CODEC, stack);
        }
        if (list.isEmpty()) {
            output.discard("items");
        }
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        super.onDataPacket(connection, input);
    }
}
