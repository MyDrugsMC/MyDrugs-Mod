package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.world.level.ChunkPos;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Chunk-collection utilities for the Inner Dimension overlay and recreate queues.
 * Owns {@link ChunkCollector}, dedup keys, and the lightweight {@link LandmarkChunk}
 * cache key.
 *
 * <p>Extracted from {@link InnerOverlayQueue} to separate chunk-enumeration concerns
 * from scheduling and metrics.
 */
final class InnerChunkCollector {

    private InnerChunkCollector() {
    }

    static long chunkKey(ChunkPos chunkPos) {
        return chunkKey(chunkPos.x, chunkPos.z);
    }

    static long chunkKey(int x, int z) {
        return ((long) x & 0xffffffffL) | (((long) z & 0xffffffffL) << 32);
    }

    static long centerKey(int centerX, int centerZ) {
        return ((long) centerX & 0xffffffffL) | (((long) centerZ & 0xffffffffL) << 32);
    }

    /** Chunk coordinate of a landmark, used as a small cache key. */
    record LandmarkChunk(int x, int z) {
    }

    /**
     * Deduplicating, ordered collection of chunk positions.
     * Supports sorting by distance from a centre chunk so overlay waves radiate outward.
     */
    static final class ChunkCollector {
        final ArrayDeque<ChunkPos> chunks = new ArrayDeque<>();
        private final Set<Long> keys = new LinkedHashSet<>();

        void add(ChunkPos chunkPos) {
            if (keys.add(chunkKey(chunkPos))) {
                chunks.add(chunkPos);
            }
        }

        void sortByDistanceFromChunk(int centerChunkX, int centerChunkZ) {
            java.util.List<ChunkPos> sorted = new java.util.ArrayList<>(chunks);
            sorted.sort(java.util.Comparator.comparingLong(c -> {
                long dx = (long) c.x - centerChunkX;
                long dz = (long) c.z - centerChunkZ;
                return dx * dx + dz * dz;
            }));
            chunks.clear();
            chunks.addAll(sorted);
        }

        int size() {
            return chunks.size();
        }

        ArrayDeque<ChunkPos> chunks() {
            return new ArrayDeque<>(chunks);
        }

        Set<Long> keys() {
            return new LinkedHashSet<>(keys);
        }
    }
}
