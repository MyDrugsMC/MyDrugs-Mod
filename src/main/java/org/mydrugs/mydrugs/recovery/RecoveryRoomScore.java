package org.mydrugs.mydrugs.recovery;

public record RecoveryRoomScore(
        int size,
        int enclosure,
        int door,
        int bed,
        int lighting,
        int floorComfort,
        int plants,
        int books,
        int music,
        int dangerPenalty
) {
    public int totalBeforeClamp() {
        return size + enclosure + door + bed + lighting + floorComfort + plants + books + music - dangerPenalty;
    }
}
