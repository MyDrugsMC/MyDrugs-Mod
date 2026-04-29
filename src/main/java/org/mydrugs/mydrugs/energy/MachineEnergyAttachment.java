package org.mydrugs.mydrugs.energy;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public final class MachineEnergyAttachment implements ValueIOSerializable {
    private final PsychotropeEnergyStorage storage = new PsychotropeEnergyStorage();
    private boolean energyUpgradeInstalled;
    private boolean automationUpgradeInstalled;

    public boolean hasEnergyUpgrade() {
        return this.energyUpgradeInstalled;
    }

    public boolean hasAutomationUpgrade() {
        return this.automationUpgradeInstalled;
    }

    public PsychotropeEnergyStorage storage() {
        return this.storage;
    }

    public boolean installEnergyUpgrade() {
        if (this.automationUpgradeInstalled || this.energyUpgradeInstalled) {
            return false;
        }
        this.energyUpgradeInstalled = true;
        return true;
    }

    public boolean installAutomationUpgrade() {
        if (this.energyUpgradeInstalled || this.automationUpgradeInstalled) {
            return false;
        }
        this.automationUpgradeInstalled = true;
        return true;
    }

    public boolean hasAnyEnergyStorageUpgrade() {
        return this.energyUpgradeInstalled || this.automationUpgradeInstalled;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putBoolean("energy_upgrade", this.energyUpgradeInstalled);
        output.putBoolean("automation_upgrade", this.automationUpgradeInstalled);
        this.storage.serialize(output.child("psychotrope_energy"));
    }

    @Override
    public void deserialize(ValueInput input) {
        this.energyUpgradeInstalled = input.getBooleanOr("energy_upgrade", false);
        this.automationUpgradeInstalled = input.getBooleanOr("automation_upgrade", false);
        this.storage.deserialize(input.childOrEmpty("psychotrope_energy"));
    }
}
