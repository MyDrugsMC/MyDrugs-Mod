package org.mydrugs.mydrugs.dimension.inner.v7;

import java.util.UUID;

public record InnerV7RegenerationJob(UUID owner, int enqueuedChunks, boolean replacedExistingQueue) {
}
