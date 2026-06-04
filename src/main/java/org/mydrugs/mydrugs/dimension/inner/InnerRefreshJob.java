package org.mydrugs.mydrugs.dimension.inner;

import java.util.UUID;

public record InnerRefreshJob(UUID owner, int enqueuedChunks, boolean replacedExistingQueue) {
    public static InnerRefreshJob empty() {
        return new InnerRefreshJob(new UUID(0L, 0L), 0, false);
    }
}
