package org.mydrugs.mydrugs.recovery;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record RecoveryRoomReport(
        BlockPos anchorPos,
        BlockPos min,
        BlockPos max,
        List<BlockPos> interior,
        int score,
        RecoveryRoomScore scoreBreakdown,
        boolean valid,
        boolean enclosedEnough,
        boolean playerInside,
        int volume,
        int floorArea,
        float averageHeight,
        float averageLight,
        int doorCount,
        int bedCount,
        int softLightCount,
        int carpetCount,
        int plantCount,
        int bookshelfCount,
        int musicCount,
        int activeMusicCount,
        int dangerPenalty,
        Set<SanctuaryModule> sanctuaryModules,
        List<String> sanctuarySuggestionKeys,
        List<String> goodKeys,
        List<String> improvementKeys
) {
    public RecoveryRoomReport {
        anchorPos = anchorPos.immutable();
        min = min.immutable();
        max = max.immutable();
        interior = List.copyOf(interior);
        sanctuaryModules = sanctuaryModules == null || sanctuaryModules.isEmpty()
                ? Set.of()
                : EnumSet.copyOf(sanctuaryModules);
        sanctuarySuggestionKeys = sanctuarySuggestionKeys == null ? List.of() : List.copyOf(sanctuarySuggestionKeys);
        goodKeys = List.copyOf(goodKeys);
        improvementKeys = List.copyOf(improvementKeys);
        score = Math.max(0, Math.min(100, score));
    }

    public RecoveryRoomTier tier() {
        return RecoveryRoomTier.fromScore(score);
    }

    public float comfort01() {
        return score / 100.0F;
    }

    public boolean hasDoor() {
        return doorCount > 0;
    }

    public boolean hasBed() {
        return bedCount > 0;
    }

    public boolean hasSoftLight() {
        return softLightCount > 0 || (averageLight >= 7.0F && averageLight <= 12.5F);
    }

    public boolean hasCarpet() {
        return carpetCount > 0;
    }

    public boolean hasPlants() {
        return plantCount > 0;
    }

    public boolean hasBooks() {
        return bookshelfCount > 0;
    }

    public boolean hasMusic() {
        return musicCount > 0;
    }

    public boolean hasActiveMusic() {
        return activeMusicCount > 0;
    }

    public boolean hasModule(SanctuaryModule module) {
        return sanctuaryModules.contains(module);
    }

    public List<String> activeSanctuaryModuleKeys() {
        if (sanctuaryModules.isEmpty()) {
            return List.of();
        }
        List<String> keys = new ArrayList<>();
        for (SanctuaryModule module : SanctuaryModule.values()) {
            if (sanctuaryModules.contains(module)) {
                keys.add(module.translationKey());
            }
        }
        return keys;
    }

    public boolean contains(BlockPos pos) {
        return interior.contains(pos.immutable());
    }

    public RecoveryRoomReport withPlayerInside(boolean inside) {
        if (inside == playerInside) {
            return this;
        }
        return new RecoveryRoomReport(
                anchorPos,
                min,
                max,
                interior,
                score,
                scoreBreakdown,
                valid,
                enclosedEnough,
                inside,
                volume,
                floorArea,
                averageHeight,
                averageLight,
                doorCount,
                bedCount,
                softLightCount,
                carpetCount,
                plantCount,
                bookshelfCount,
                musicCount,
                activeMusicCount,
                dangerPenalty,
                sanctuaryModules,
                sanctuarySuggestionKeys,
                goodKeys,
                improvementKeys
        );
    }

    public List<BlockPos> particleSamples(int maxSamples) {
        if (interior.size() <= maxSamples) {
            return interior;
        }
        int step = Math.max(1, interior.size() / maxSamples);
        java.util.ArrayList<BlockPos> samples = new java.util.ArrayList<>(maxSamples);
        for (int i = 0; i < interior.size() && samples.size() < maxSamples; i += step) {
            samples.add(interior.get(i));
        }
        return samples;
    }
}
