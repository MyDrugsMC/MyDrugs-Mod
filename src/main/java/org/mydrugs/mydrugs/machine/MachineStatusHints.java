package org.mydrugs.mydrugs.machine;

import net.minecraft.network.chat.Component;

import java.util.List;

public final class MachineStatusHints {
    private MachineStatusHints() {
    }

    public static List<Component> generic(MachineStatus status) {
        return switch (status) {
            case IDLE -> List.of(Component.translatable("machine_status_hint.mydrugs.idle"));
            case RUNNING -> List.of(Component.translatable("machine_status_hint.mydrugs.running"));
            case MISSING_INPUT_ITEM -> List.of(Component.translatable("machine_status_hint.mydrugs.missing_input_item"));
            case MISSING_INPUT_FLUID -> List.of(Component.translatable("machine_status_hint.mydrugs.missing_input_fluid"));
            case MISSING_INPUT_GAS -> List.of(Component.translatable("machine_status_hint.mydrugs.missing_input_gas"));
            case MISSING_CATALYST -> List.of(Component.translatable("machine_status_hint.mydrugs.missing_catalyst"));
            case NO_MATCHING_RECIPE -> List.of(Component.translatable("machine_status_hint.mydrugs.no_matching_recipe"));
            case OUTPUT_SLOT_FULL -> List.of(Component.translatable("machine_status_hint.mydrugs.output_slot_full"));
            case OUTPUT_TANK_FULL, OUTPUT_GAS_TANK_FULL, OUTPUT_TANK_A_FULL, OUTPUT_TANK_B_FULL ->
                    List.of(Component.translatable("machine_status_hint.mydrugs.output_tank_full"));
            case MISSING_CONTAINER -> List.of(Component.translatable("machine_status_hint.mydrugs.missing_container"));
            case NOT_ENOUGH_ENERGY -> List.of(Component.translatable("machine_status_hint.mydrugs.not_enough_energy"));
            case NOT_ENOUGH_HEAT -> List.of(Component.translatable("machine_status_hint.mydrugs.not_enough_heat"));
            case BLOCKED_BY_TRANSFER -> List.of(Component.translatable("machine_status_hint.mydrugs.blocked_by_transfer"));
            case PAUSED -> List.of(Component.translatable("machine_status_hint.mydrugs.paused"));
            case INSUFFICIENT_INPUT_FLUID -> List.of(Component.translatable("machine_status_hint.mydrugs.insufficient_input_fluid"));
            default -> List.of(Component.translatable("screen.mydrugs.machine_handbook.status_hint"));
        };
    }
}
