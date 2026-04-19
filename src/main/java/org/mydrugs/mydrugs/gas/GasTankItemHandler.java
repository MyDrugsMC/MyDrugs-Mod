package org.mydrugs.mydrugs.gas;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.registry.ModDataComponents;

public class GasTankItemHandler implements IGasHandler {
    public static final long CAPACITY = 8_000; // change to 4 if you literally want 4 mB

    private final ItemStack stack;

    public GasTankItemHandler(ItemStack stack) {
        this.stack = stack;
    }

    private GasTankContents getContents() {
        return this.stack.getOrDefault(
                ModDataComponents.GAS_TANK_CONTENTS.get(),
                GasTankContents.EMPTY
        );
    }

    private void setContents(GasTankContents contents) {
        this.stack.set(ModDataComponents.GAS_TANK_CONTENTS.get(), contents);
    }

    private GasTank loadTankFromStack() {
        GasTank tank = new GasTank(CAPACITY, gas -> true, () -> {
        });

        GasTankContents contents = getContents();
        GasType gas = contents.gasId().isBlank()
                ? null
                : ModGases.get(ResourceLocation.parse(contents.gasId()));

        tank.loadStored(gas, contents.amount());
        return tank;
    }

    private void saveTankToStack(GasTank tank) {
        GasType gas = tank.getGasType();

        setContents(new GasTankContents(
                gas == null ? "" : gas.id().toString(),
                tank.getAmount()
        ));
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public GasStack getGasInTank(int tank) {
        return loadTankFromStack().getGasInTank(tank);
    }

    @Override
    public long getTankCapacity(int tank) {
        return tank == 0 ? CAPACITY : 0;
    }

    @Override
    public boolean isGasValid(int tank, GasStack stack) {
        return tank == 0 && stack != null && !stack.isEmpty();
    }

    @Override
    public long fill(GasStack resource, boolean simulate) {
        GasTank tank = loadTankFromStack();
        long filled = tank.fill(resource, simulate);

        if (!simulate && filled > 0) {
            saveTankToStack(tank);
        }

        return filled;
    }

    @Override
    public GasStack drain(long amount, boolean simulate) {
        GasTank tank = loadTankFromStack();
        GasStack drained = tank.drain(amount, simulate);

        if (!simulate && !drained.isEmpty()) {
            saveTankToStack(tank);
        }

        return drained;
    }
}