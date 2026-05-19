package org.mydrugs.mydrugs.recovery;

public enum RecoveryRoomComponent {
    BED("recovery.mydrugs.room.component.bed"),
    DOOR("recovery.mydrugs.room.component.door"),
    SOFT_LIGHTING("recovery.mydrugs.room.component.soft_lighting"),
    CARPETS("recovery.mydrugs.room.component.carpets"),
    FLOWERS("recovery.mydrugs.room.component.flowers"),
    BOOKS("recovery.mydrugs.room.component.books"),
    MUSIC("recovery.mydrugs.room.component.music"),
    ENCLOSURE("recovery.mydrugs.room.component.enclosure");

    private final String translationKey;

    RecoveryRoomComponent(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
