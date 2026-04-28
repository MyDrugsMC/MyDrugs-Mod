package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class MachineTransferConfig {
    public static final int CURRENT_VERSION = 2;

    private final Map<MachineTransferPortId, EnumMap<MachineLocalSide, MachineTransferSideRule>> sideRules = new HashMap<>();

    public MachineTransferSideRule getRule(MachineTransferPortId port, MachineLocalSide side) {
        return this.sideRules
                .getOrDefault(port, new EnumMap<>(MachineLocalSide.class))
                .getOrDefault(side, MachineTransferSideRule.DISABLED);
    }

    public void setRule(MachineTransferPortSpec port, MachineLocalSide side, MachineTransferSideRule rule) {
        this.setRule(port.id(), side, port.supports(rule) ? rule : MachineTransferSideRule.DISABLED);
    }

    public void setRule(MachineTransferPortId port, MachineLocalSide side, MachineTransferSideRule rule) {
        this.sideRules
                .computeIfAbsent(port, ignored -> new EnumMap<>(MachineLocalSide.class))
                .put(side, rule);
    }

    public MachineTransferSideRule cycleRule(MachineTransferPortSpec port, MachineLocalSide side) {
        MachineTransferSideRule next = port.access().next(getRule(port.id(), side));
        setRule(port, side, next);
        return next;
    }

    public Map<MachineTransferPortId, EnumMap<MachineLocalSide, MachineTransferSideRule>> sideRules() {
        return this.sideRules;
    }

    public void seedDefaults(MachineTransferSpec spec) {
        for (MachineTransferPortSpec port : spec.ports()) {
            MachineTransferSideRule defaultRule = switch (port.access()) {
                case INPUT_ONLY, BIDIRECTIONAL -> MachineTransferSideRule.INPUT;
                case OUTPUT_ONLY -> MachineTransferSideRule.OUTPUT;
            };
            for (MachineLocalSide side : port.defaultLocalSides()) {
                setRule(port, side, defaultRule);
            }
        }
    }

    public boolean sanitizeAgainst(MachineTransferSpec spec) {
        boolean changed = false;
        Set<MachineTransferPortId> validPortIds = new HashSet<>();
        for (MachineTransferPortSpec port : spec.ports()) {
            validPortIds.add(port.id());
        }

        Iterator<Map.Entry<MachineTransferPortId, EnumMap<MachineLocalSide, MachineTransferSideRule>>> portIterator = this.sideRules.entrySet().iterator();
        while (portIterator.hasNext()) {
            Map.Entry<MachineTransferPortId, EnumMap<MachineLocalSide, MachineTransferSideRule>> entry = portIterator.next();
            if (!validPortIds.contains(entry.getKey())) {
                portIterator.remove();
                changed = true;
            }
        }

        for (MachineTransferPortSpec port : spec.ports()) {
            EnumMap<MachineLocalSide, MachineTransferSideRule> rules = this.sideRules.computeIfAbsent(
                    port.id(),
                    ignored -> new EnumMap<>(MachineLocalSide.class)
            );
            if (rules.isEmpty() && !port.defaultLocalSides().isEmpty()) {
                MachineTransferSideRule defaultRule = switch (port.access()) {
                    case INPUT_ONLY, BIDIRECTIONAL -> MachineTransferSideRule.INPUT;
                    case OUTPUT_ONLY -> MachineTransferSideRule.OUTPUT;
                };
                for (MachineLocalSide side : port.defaultLocalSides()) {
                    rules.put(side, defaultRule);
                }
                changed = true;
            }

            for (Map.Entry<MachineLocalSide, MachineTransferSideRule> ruleEntry : rules.entrySet()) {
                if (!port.supports(ruleEntry.getValue())) {
                    ruleEntry.setValue(MachineTransferSideRule.DISABLED);
                    changed = true;
                }
            }
        }

        return changed;
    }

    public void serialize(ValueOutput output) {
        output.putInt("transfer_config_version", CURRENT_VERSION);
        ValueOutput rules = output.child("rules");
        int index = 0;
        for (Map.Entry<MachineTransferPortId, EnumMap<MachineLocalSide, MachineTransferSideRule>> portEntry : this.sideRules.entrySet()) {
            for (Map.Entry<MachineLocalSide, MachineTransferSideRule> sideEntry : portEntry.getValue().entrySet()) {
                ValueOutput child = rules.child("entry_" + index++);
                child.putString("port", portEntry.getKey().id().toString());
                child.putString("side", sideEntry.getKey().getSerializedName());
                child.putString("rule", sideEntry.getValue().name());
            }
        }
        rules.putInt("count", index);
    }

    public void deserialize(ValueInput input) {
        this.sideRules.clear();
        if (input.getIntOr("transfer_config_version", 1) < CURRENT_VERSION) {
            return;
        }

        ValueInput rules = input.childOrEmpty("rules");
        int count = rules.getIntOr("count", 0);
        for (int i = 0; i < count; i++) {
            ValueInput child = rules.childOrEmpty("entry_" + i);
            ResourceLocation portId = ResourceLocation.tryParse(child.getStringOr("port", ""));
            MachineLocalSide side = parseSide(child.getStringOr("side", ""));
            if (portId == null || side == null) {
                continue;
            }

            try {
                MachineTransferSideRule rule = MachineTransferSideRule.valueOf(child.getStringOr("rule", MachineTransferSideRule.DISABLED.name()));
                this.setRule(new MachineTransferPortId(portId), side, rule);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private static MachineLocalSide parseSide(String name) {
        for (MachineLocalSide side : MachineLocalSide.values()) {
            if (side.getSerializedName().equals(name) || side.name().equals(name)) {
                return side;
            }
        }
        return null;
    }
}
