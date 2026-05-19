package org.mydrugs.mydrugs.client.recovery.music;

import net.minecraft.network.chat.Component;

public final class TrackImportJob {
    private volatile boolean done;
    private volatile Component message = Component.translatable("screen.mydrugs.music.importing");

    public boolean done() {
        return done;
    }

    public Component message() {
        return message;
    }

    void complete(Component message) {
        this.message = message;
        this.done = true;
    }
}
