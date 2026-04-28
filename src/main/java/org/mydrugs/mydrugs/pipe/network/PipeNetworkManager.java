package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class PipeNetworkManager {
    private static final WeakHashMap<ServerLevel, PipeNetworkManager> MANAGERS = new WeakHashMap<>();

    private final ServerLevel level;
    private final EnumMap<PipeResourceKind, Set<BlockPos>> dirtySeeds = new EnumMap<>(PipeResourceKind.class);
    private final EnumMap<PipeResourceKind, Map<PipeNetworkKey, PipeNetwork>> networks = new EnumMap<>(PipeResourceKind.class);
    private long nextNetworkId = 1L;

    private PipeNetworkManager(ServerLevel level) {
        this.level = level;
        for (PipeResourceKind kind : PipeResourceKind.values()) {
            this.dirtySeeds.put(kind, new HashSet<>());
            this.networks.put(kind, new java.util.HashMap<>());
        }
    }

    public static PipeNetworkManager get(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level, PipeNetworkManager::new);
    }

    public static void markDirty(Level level, BlockPos pos, PipeResourceKind kind, PipeNetworkDirtyReason reason) {
        if (level instanceof ServerLevel serverLevel) {
            get(serverLevel).markDirty(pos, kind, reason);
        }
    }

    public void markDirty(BlockPos pos, PipeResourceKind kind, PipeNetworkDirtyReason reason) {
        this.dirtySeeds.get(kind).add(pos.immutable());
    }

    public void tick() {
        for (PipeResourceKind kind : PipeResourceKind.values()) {
            rebuildDirty(kind);
        }
        PipeTransferTicker.tick(this.level, this);
    }

    public Map<PipeNetworkKey, PipeNetwork> networks(PipeResourceKind kind) {
        return this.networks.get(kind);
    }

    private void rebuildDirty(PipeResourceKind kind) {
        Set<BlockPos> seeds = this.dirtySeeds.get(kind);
        if (seeds.isEmpty()) {
            return;
        }

        Map<PipeNetworkKey, PipeNetwork> kindNetworks = this.networks.get(kind);
        Set<BlockPos> covered = new HashSet<>();
        for (BlockPos seed : Set.copyOf(seeds)) {
            if (covered.contains(seed)) {
                continue;
            }

            if (!this.level.isLoaded(seed)) {
                removeNetworksContaining(kindNetworks, seed);
                continue;
            }

            PipeNetworkKey key = new PipeNetworkKey(kind, this.nextNetworkId++);
            PipeNetwork network = PipeNetworkScanner.scan(this.level, key, seed, kind);
            if (!network.nodes().isEmpty()) {
                covered.addAll(network.nodes().keySet());
                removeOverlappingNetworks(kindNetworks, network.nodes().keySet());
                kindNetworks.put(key, network);
            } else {
                removeNetworksContaining(kindNetworks, seed);
            }
        }

        seeds.clear();
    }

    private static void removeNetworksContaining(Map<PipeNetworkKey, PipeNetwork> networks, BlockPos pos) {
        networks.entrySet().removeIf(entry -> entry.getValue().nodes().containsKey(pos));
    }

    private static void removeOverlappingNetworks(Map<PipeNetworkKey, PipeNetwork> networks, Set<BlockPos> positions) {
        networks.entrySet().removeIf(entry -> containsAny(entry.getValue().nodes().keySet(), positions));
    }

    private static boolean containsAny(Set<BlockPos> left, Set<BlockPos> right) {
        Set<BlockPos> smaller = left.size() <= right.size() ? left : right;
        Set<BlockPos> larger = left.size() <= right.size() ? right : left;
        for (BlockPos pos : smaller) {
            if (larger.contains(pos)) {
                return true;
            }
        }
        return false;
    }
}
