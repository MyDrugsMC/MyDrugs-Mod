package org.mydrugs.mydrugs.recovery;

import net.minecraft.core.BlockPos;

import java.util.List;

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
        int dangerPenalty,
        List<String> goodKeys,
        List<String> improvementKeys
) {
    public RecoveryRoomReport {
        anchorPos = anchorPos.immutable();
        min = min.immutable();
        max = max.immutable();
        interior = List.copyOf(interior);
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
                dangerPenalty,
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
