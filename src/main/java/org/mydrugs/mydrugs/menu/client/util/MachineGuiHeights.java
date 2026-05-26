package org.mydrugs.mydrugs.menu.client.util;

import org.mydrugs.mydrugs.menu.layout.*;

final class MachineGuiHeights {
    private MachineGuiHeights() {
    }

    static int advancedFurnaceHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? AdvancedFurnaceLayout.GUI_HEIGHT
                : AdvancedFurnaceLayout.MACHINE_PANEL_Y + AdvancedFurnaceLayout.MACHINE_PANEL_H + 14;
    }

    static int aromaticExtractorHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? AromaticExtractorLayout.GUI_HEIGHT
                : AromaticExtractorLayout.MACHINE_PANEL_Y + AromaticExtractorLayout.MACHINE_PANEL_H + 14;
    }

    static int biochemicalReactorHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? BiochemicalReactorLayout.GUI_HEIGHT
                : BiochemicalReactorLayout.MACHINE_PANEL_Y + BiochemicalReactorLayout.MACHINE_PANEL_H + 14;
    }

    static int advancedMixingVatHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? AdvancedMixingVatLayout.GUI_HEIGHT
                : AdvancedMixingVatLayout.MACHINE_PANEL_Y + AdvancedMixingVatLayout.MACHINE_PANEL_H + 14;
    }

    static int centrifugeHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? CentrifugeLayout.GUI_HEIGHT
                : CentrifugeLayout.MACHINE_PANEL_Y + CentrifugeLayout.MACHINE_PANEL_H + 14;
    }

    static int chemicalReactorHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? ChemicalReactorLayout.GUI_HEIGHT
                : ChemicalReactorLayout.MACHINE_PANEL_Y + ChemicalReactorLayout.MACHINE_PANEL_H + 14;
    }

    static int distillerHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? DistillerLayout.GUI_HEIGHT
                : DistillerLayout.MACHINE_PANEL_Y + DistillerLayout.MACHINE_PANEL_H + 14;
    }

    static int electrolyzerHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? ElectrolyzerLayout.GUI_HEIGHT
                : ElectrolyzerLayout.MACHINE_PANEL_Y + ElectrolyzerLayout.MACHINE_PANEL_H + 14;
    }

    static int fluidFiltererHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? FluidFiltererLayout.GUI_HEIGHT
                : FluidFiltererLayout.MACHINE_PANEL_Y + FluidFiltererLayout.MACHINE_PANEL_H + 14;
    }

    static int catalyticReformerHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? CatalyticReformerLayout.GUI_HEIGHT
                : CatalyticReformerLayout.MACHINE_PANEL_Y + CatalyticReformerLayout.MACHINE_PANEL_H + 14;
    }

    static int steamCrackerHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? SteamCrackerLayout.GUI_HEIGHT
                : SteamCrackerLayout.MACHINE_PANEL_Y + SteamCrackerLayout.MACHINE_PANEL_H + 14;
    }

    static int btxFractionationTowerHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? BTXFractionationTowerLayout.GUI_HEIGHT
                : BTXFractionationTowerLayout.MACHINE_PANEL_Y + BTXFractionationTowerLayout.MACHINE_PANEL_H + 14;
    }

    static int growthChamberHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? GrowthChamberLayout.GUI_HEIGHT
                : GrowthChamberLayout.MACHINE_PANEL_Y + GrowthChamberLayout.MACHINE_PANEL_H + 14;
    }

    static int gasifierHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? GasifierLayout.GUI_HEIGHT
                : GasifierLayout.MACHINE_PANEL_Y + GasifierLayout.MACHINE_PANEL_H + 14;
    }

    static int psychotropeDistilleryHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? PsychotropeDistilleryLayout.GUI_HEIGHT
                : PsychotropeDistilleryLayout.MACHINE_PANEL_Y + PsychotropeDistilleryLayout.MACHINE_PANEL_H + 14;
    }

    static int sieveHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? SieveLayout.GUI_HEIGHT
                : SieveLayout.MACHINE_PANEL_Y + SieveLayout.MACHINE_PANEL_H + 14;
    }
}
