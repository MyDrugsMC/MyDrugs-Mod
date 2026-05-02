package org.mydrugs.mydrugs.pipe.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public enum MachineTransferSideRule {
    DISABLED("disabled", 0),
    INPUT("input", 1),
    OUTPUT("output", 2);

    private final String serializedName;
    private final int networkId;

    MachineTransferSideRule(String serializedName, int networkId) {
        this.serializedName = serializedName;
        this.networkId = networkId;
    }

    public static final Codec<MachineTransferSideRule> CODEC =
            Codec.STRING.comapFlatMap(
                    name -> bySerializedName(name)
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "Unknown machine transfer side rule: " + name)),
                    MachineTransferSideRule::serializedName
            );

    public String serializedName() {
        return this.serializedName;
    }

    public int networkId() {
        return this.networkId;
    }

    public static Optional<MachineTransferSideRule> bySerializedName(String name) {
        return Optional.ofNullable(bySerializedNameOrNull(name));
    }

    public static @Nullable MachineTransferSideRule bySerializedNameOrNull(String name) {
        if (name == null) {
            return null;
        }

        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (MachineTransferSideRule rule : values()) {
            if (rule.serializedName.equals(normalized) || rule.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return rule;
            }
        }
        return null;
    }

    public static MachineTransferSideRule byNetworkId(int networkId) {
        for (MachineTransferSideRule rule : values()) {
            if (rule.networkId == networkId) {
                return rule;
            }
        }
        return DISABLED;
    }
}
