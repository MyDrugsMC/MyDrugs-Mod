package org.mydrugs.mydrugs.machine;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.IntUnaryOperator;

public final class MachineStorage {
    private MachineStorage() {
    }

    public static void saveItemStacks(ValueOutput output, String key, NonNullList<ItemStack> stacks) {
        ValueOutput.ValueOutputList itemList = output.childrenList(key);
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            ValueOutput child = itemList.addChild();
            child.putInt("slot", i);
            child.store("stack", ItemStack.CODEC, stack);
        }
    }

    public static void loadItemStacks(ValueInput input, String key, NonNullList<ItemStack> stacks) {
        loadItemStacks(input, key, stacks, IntUnaryOperator.identity(), false);
    }

    public static void loadItemStacks(
            ValueInput input,
            String key,
            NonNullList<ItemStack> stacks,
            IntUnaryOperator slotMapper,
            boolean keepExistingStacks
    ) {
        clearItemStacks(stacks);

        for (ValueInput child : input.childrenListOrEmpty(key)) {
            int slot = slotMapper.applyAsInt(child.getIntOr("slot", -1));
            ItemStack stack = child.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);

            if (slot >= 0 && slot < stacks.size() && (!keepExistingStacks || stacks.get(slot).isEmpty())) {
                stacks.set(slot, stack);
            }
        }
    }

    public static void clearItemStacks(NonNullList<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
    }
}
