package org.mydrugs.mydrugs.gas;

public record GasSpec(
        String path,
        int tint,
        boolean toxic,
        boolean flammable
) {
}
