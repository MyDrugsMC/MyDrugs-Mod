package org.mydrugs.mydrugs.client.diary;

import org.mydrugs.mydrugs.psyche.PsycheMapNodeDto;

import java.util.List;

/** Client-only mirror of the server-authoritative Psyche Map snapshot. */
public final class PsycheMapClientState {
    private static List<PsycheMapNodeDto> nodes = List.of();

    private PsycheMapClientState() {
    }

    public static void apply(List<PsycheMapNodeDto> snapshot) {
        nodes = snapshot == null ? List.of() : List.copyOf(snapshot);
    }

    public static List<PsycheMapNodeDto> nodes() {
        return nodes;
    }

    public static void clear() {
        nodes = List.of();
    }
}
