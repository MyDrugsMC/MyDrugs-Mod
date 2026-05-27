package org.mydrugs.mydrugs.recovery;

import java.util.Collection;

public enum SanctuaryModule {
    REST_MODULE("rest_module", 1 << 0),
    DIARY_DESK("diary_desk", 1 << 1),
    MUSIC_CORNER("music_corner", 1 << 2),
    PLANT_BREATHING_CORNER("plant_breathing_corner", 1 << 3),
    TEA_KITCHEN("tea_kitchen", 1 << 4),
    MEMORY_WALL("memory_wall", 1 << 5),
    INTEGRATION_ALCOVE("integration_alcove", 1 << 6);

    private final String id;
    private final int networkBit;

    SanctuaryModule(String id, int networkBit) {
        this.id = id;
        this.networkBit = networkBit;
    }

    public String id() {
        return id;
    }

    public int networkBit() {
        return networkBit;
    }

    public String translationKey() {
        return "recovery.mydrugs.room.module." + id;
    }

    public String suggestionKey() {
        return "recovery.mydrugs.room.module_suggestion." + id;
    }

    public static int flags(Collection<SanctuaryModule> modules) {
        int flags = 0;
        if (modules == null) {
            return flags;
        }
        for (SanctuaryModule module : modules) {
            flags |= module.networkBit;
        }
        return flags;
    }
}
