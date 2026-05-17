package org.mydrugs.mydrugs.energy;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferAttachments;

import java.util.function.Consumer;

public final class MachineUpgradePersistence {
    private static final String ROOT_KEY = "mydrugs_machine_upgrades";
    private static final String TRANSFER_KEY = "transfer";
    private static final String ENERGY_KEY = "energy";
    private static final String AUTOMATION_KEY = "automation";

    private MachineUpgradePersistence() {
    }

    public static boolean copyUpgradesToStack(BlockEntity blockEntity, ItemStack stack) {
        CompoundTag upgrades = upgradesFrom(blockEntity);
        if (upgrades.isEmpty()) {
            return false;
        }

        CustomData current = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag root = current.copyTag();
        root.put(ROOT_KEY, upgrades);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        return true;
    }

    public static void applyUpgradesFromStack(ItemStack stack, BlockEntity blockEntity) {
        CompoundTag upgrades = upgradesFrom(stack);
        if (upgrades.isEmpty()) {
            return;
        }

        if (upgrades.getBooleanOr(TRANSFER_KEY, false)) {
            MachineTransferAttachments.install(blockEntity);
        }
        if (upgrades.getBooleanOr(ENERGY_KEY, false)) {
            MachineEnergyAttachments.installEnergyUpgrade(blockEntity);
        }
        if (upgrades.getBooleanOr(AUTOMATION_KEY, false)) {
            MachineEnergyAttachments.installAutomationUpgrade(blockEntity);
        }
    }

    public static void appendTooltip(ItemStack stack, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        CompoundTag upgrades = upgradesFrom(stack);
        if (upgrades.isEmpty()) {
            return;
        }

        if (upgrades.getBooleanOr(TRANSFER_KEY, false)) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.machine_upgrade.transfer"));
        }
        if (upgrades.getBooleanOr(ENERGY_KEY, false)) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.machine_upgrade.energy"));
        }
        if (upgrades.getBooleanOr(AUTOMATION_KEY, false)) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.machine_upgrade.automation"));
        }
    }

    private static CompoundTag upgradesFrom(BlockEntity blockEntity) {
        CompoundTag upgrades = new CompoundTag();

        if (MachineTransferAttachments.isSupported(blockEntity)
                && MachineTransferAttachments.get(blockEntity).installed()) {
            upgrades.putBoolean(TRANSFER_KEY, true);
        }

        MachineEnergyAttachment energy = MachineEnergyAttachments.get(blockEntity);
        if (energy.hasEnergyUpgrade()) {
            upgrades.putBoolean(ENERGY_KEY, true);
        }
        if (energy.hasAutomationUpgrade()) {
            upgrades.putBoolean(AUTOMATION_KEY, true);
        }

        return upgrades;
    }

    private static CompoundTag upgradesFrom(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return new CompoundTag();
        }

        return customData.copyTag().getCompound(ROOT_KEY).orElseGet(CompoundTag::new);
    }
}
