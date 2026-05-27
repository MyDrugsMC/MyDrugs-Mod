package org.mydrugs.mydrugs.recovery;

public record SanctuaryModuleScan(
        int beds,
        int softLights,
        float averageLight,
        int plants,
        int bookshelves,
        int lecterns,
        int seats,
        int tableLikeBlocks,
        int musicBlocks,
        int activeMusicBlocks,
        int teaHeatSources,
        int cauldrons,
        int teaStorageBlocks,
        int memoryDisplays,
        int integrationMarkers,
        int dangerBlocks,
        int clutterBlocks,
        int hostileCount
) {
    public boolean hasSafeLight() {
        return softLights > 0 || (averageLight >= 7.0F && averageLight <= 12.5F);
    }

    public boolean dangerFree() {
        return dangerBlocks <= 0 && hostileCount <= 0;
    }
}
