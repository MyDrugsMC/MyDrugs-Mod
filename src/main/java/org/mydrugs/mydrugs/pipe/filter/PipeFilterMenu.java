package org.mydrugs.mydrugs.pipe.filter;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.menu.ModMenus;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;

import java.util.List;
import java.util.Optional;

public class PipeFilterMenu extends AbstractContainerMenu {
    public static final int GHOST_SLOT_COUNT = PipeFilterConfig.MAX_ENTRIES;
    public static final int GHOST_GRID_X = 62;
    public static final int GHOST_GRID_Y = 40;
    public static final int PLAYER_INV_X = 8;
    public static final int PLAYER_INV_Y = 102;
    public static final int HOTBAR_Y = 160;

    public static final int BUTTON_TOGGLE_MODE = 0;
    public static final int BUTTON_KIND_ITEM = 10;
    public static final int BUTTON_KIND_FLUID = 11;
    public static final int BUTTON_KIND_GAS = 12;
    public static final int BUTTON_CLEAR_ALL = 20;
    private static final int DATA_KIND = 0;
    private static final int DATA_MODE = 1;
    private static final int DATA_ENTRIES_START = 2;
    private static final int DATA_COUNT = DATA_ENTRIES_START + GHOST_SLOT_COUNT;

    private final Inventory playerInventory;
    private final InteractionHand hand;
    private final SimpleContainer ghostSlots = new SimpleContainer(GHOST_SLOT_COUNT);
    private final boolean clientSide;
    private final int[] clientData = new int[DATA_COUNT];

    public PipeFilterMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, InteractionHand.MAIN_HAND);
    }

    public PipeFilterMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
    }

    public PipeFilterMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenus.PIPE_FILTER.get(), containerId);
        this.playerInventory = playerInventory;
        this.hand = hand;
        this.clientSide = playerInventory.player.level().isClientSide();
        for (int i = DATA_ENTRIES_START; i < this.clientData.length; i++) {
            this.clientData[i] = -1;
        }
        addGhostSlots();
        addPlayerInventorySlots(playerInventory);
        addDataSlots(createData());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (isGhostMenuSlot(index) || index < 0 || index >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        ItemStack source = this.slots.get(index).getItem();
        if (source.isEmpty() || player.level().isClientSide()) {
            return ItemStack.EMPTY;
        }

        return trySetEntryFromStack(player, -1, source) ? source.copyWithCount(1) : ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (isGhostMenuSlot(slotId)) {
            if (!player.level().isClientSide()) {
                handleGhostClick(player, slotId, button, clickType);
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (player.level().isClientSide() || !ensureValid(player)) {
            return false;
        }

        PipeFilterConfig config = config();
        PipeFilterConfig next = switch (id) {
            case BUTTON_TOGGLE_MODE -> config.withMode(config.mode().toggled());
            case BUTTON_KIND_ITEM -> config.withKind(PipeResourceKind.ITEM);
            case BUTTON_KIND_FLUID -> config.withKind(PipeResourceKind.FLUID);
            case BUTTON_KIND_GAS -> config.withKind(PipeResourceKind.GAS);
            case BUTTON_CLEAR_ALL -> new PipeFilterConfig(config.kind(), config.mode(), List.of());
            default -> null;
        };

        if (next == null || next.equals(config)) {
            return false;
        }

        writeConfig(next);
        broadcastChanges();
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return player != null
                && player.isAlive()
                && player == this.playerInventory.player
                && editingStack(player).getItem() instanceof PipeFilterUpgradeItem;
    }

    public InteractionHand hand() {
        return this.hand;
    }

    public PipeFilterConfig config() {
        if (this.clientSide) {
            return clientConfig();
        }
        return editingStack(this.playerInventory.player)
                .getOrDefault(ModDataComponents.PIPE_FILTER_CONFIG.get(), PipeFilterUpgradeItem.defaultConfig())
                .pruneInvalidEntries();
    }

    public Optional<ResourceLocation> entry(int slot) {
        List<ResourceLocation> entries = config().entries();
        return slot >= 0 && slot < entries.size() ? Optional.of(entries.get(slot)) : Optional.empty();
    }

    public ItemStack displayStack(int slot) {
        return entry(slot)
                .map(id -> PipeFilterEntryResolver.displayStack(config().kind(), id))
                .orElse(ItemStack.EMPTY);
    }

    public static int ghostSlotX(int index) {
        return GHOST_GRID_X + (index % 3) * 18;
    }

    public static int ghostSlotY(int index) {
        return GHOST_GRID_Y + (index / 3) * 18;
    }

    private void addGhostSlots() {
        for (int i = 0; i < GHOST_SLOT_COUNT; i++) {
            this.addSlot(new PipeFilterGhostSlot(this.ghostSlots, i, ghostSlotX(i), ghostSlotY(i)));
        }
    }

    private void addPlayerInventorySlots(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, PLAYER_INV_X + col * 18, HOTBAR_Y));
        }
    }

    private void handleGhostClick(Player player, int slot, int button, ClickType clickType) {
        if (!ensureValid(player)) {
            return;
        }
        if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE) {
            return;
        }

        ItemStack carried = getCarried();
        if (button == 1 || carried.isEmpty()) {
            clearEntry(player, slot);
            return;
        }

        trySetEntryFromStack(player, slot, carried);
    }

    private boolean trySetEntryFromStack(Player player, int requestedSlot, ItemStack stack) {
        if (!ensureValid(player)) {
            return false;
        }

        PipeFilterConfig config = config();
        Optional<ResourceLocation> resolved = PipeFilterEntryResolver.resolve(config.kind(), stack, player.level());
        if (resolved.isEmpty()) {
            message(player, "message.mydrugs.pipe_filter.invalid_sample");
            return false;
        }

        ResourceLocation id = resolved.get();
        if (!PipeFilterEntryResolver.exists(config.kind(), id)) {
            message(player, "message.mydrugs.pipe_filter.invalid_sample");
            return false;
        }

        List<ResourceLocation> entries = config.entries();
        if (entries.contains(id)) {
            message(player, "message.mydrugs.pipe_filter.duplicate");
            return false;
        }

        int slot = requestedSlot >= 0 ? requestedSlot : entries.size();
        if (slot >= PipeFilterConfig.MAX_ENTRIES || (slot >= entries.size() && entries.size() >= PipeFilterConfig.MAX_ENTRIES)) {
            message(player, "message.mydrugs.pipe_filter.full");
            return false;
        }

        writeConfig(config.withEntry(slot, id));
        broadcastChanges();
        return true;
    }

    private void clearEntry(Player player, int slot) {
        PipeFilterConfig config = config();
        PipeFilterConfig next = config.withoutEntry(slot);
        if (!next.equals(config)) {
            writeConfig(next);
            broadcastChanges();
        }
    }

    private boolean ensureValid(Player player) {
        if (stillValid(player)) {
            return true;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.closeContainer();
        }
        return false;
    }

    private ItemStack editingStack(Player player) {
        return player == null ? ItemStack.EMPTY : player.getItemInHand(this.hand);
    }

    private void writeConfig(PipeFilterConfig config) {
        ItemStack stack = editingStack(this.playerInventory.player);
        if (stack.getItem() instanceof PipeFilterUpgradeItem) {
            stack.set(ModDataComponents.PIPE_FILTER_CONFIG.get(), config);
        }
    }

    private PipeFilterConfig clientConfig() {
        PipeResourceKind kind = PipeResourceKind.byNetworkId(this.clientData[DATA_KIND]);
        PipeFilterMode mode = PipeFilterMode.byNetworkId(this.clientData[DATA_MODE]);
        List<ResourceLocation> entries = new java.util.ArrayList<>();
        for (int i = 0; i < GHOST_SLOT_COUNT; i++) {
            PipeFilterEntryResolver.fromSyncId(kind, this.clientData[DATA_ENTRIES_START + i]).ifPresent(entries::add);
        }
        return new PipeFilterConfig(kind, mode, entries);
    }

    private ContainerData createData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (clientSide) {
                    return clientData[index];
                }

                PipeFilterConfig config = config();
                if (index == DATA_KIND) {
                    return config.kind().networkId();
                }
                if (index == DATA_MODE) {
                    return config.mode().networkId();
                }
                int entryIndex = index - DATA_ENTRIES_START;
                if (entryIndex >= 0 && entryIndex < config.entries().size()) {
                    return PipeFilterEntryResolver.toSyncId(config.kind(), config.entries().get(entryIndex));
                }
                return -1;
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < clientData.length) {
                    clientData[index] = value;
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    private static boolean isGhostMenuSlot(int slotId) {
        return slotId >= 0 && slotId < GHOST_SLOT_COUNT;
    }

    private static void message(Player player, String key) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.translatable(key), true);
        }
    }

    private static final class PipeFilterGhostSlot extends Slot {
        PipeFilterGhostSlot(SimpleContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public ItemStack getItem() {
            return ItemStack.EMPTY;
        }

        @Override
        public void set(@Nullable ItemStack stack) {
        }

        @Override
        public void setChanged() {
        }
    }
}
