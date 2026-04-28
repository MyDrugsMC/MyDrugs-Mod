package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.mydrugs.mydrugs.gas.IGasHandler;
import org.mydrugs.mydrugs.gas.ModGasCapabilities;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.PipeConnectionMode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PipeNetwork {
    private final PipeNetworkKey key;
    private final PipeResourceKind kind;
    private final Map<BlockPos, PipeNode> nodes = new HashMap<>();
    private final List<PipeEndpoint> inputs = new ArrayList<>();
    private final List<PipeEndpoint> outputs = new ArrayList<>();
    private final PipeRouteCache routeCache = new PipeRouteCache();
    private final Map<PipeEndpoint, Integer> itemRouteRotation = new HashMap<>();
    private final Map<PipeEndpoint, Integer> fluidOutputRotation = new HashMap<>();
    private final Map<PipeEndpoint, Integer> gasOutputRotation = new HashMap<>();
    private final Map<PipeEndpoint, Long> lastItemTransferTick = new HashMap<>();
    private final Map<PipeEndpoint, BlockCapabilityCache<ResourceHandler<ItemResource>, Direction>> itemCapabilityCaches = new HashMap<>();
    private final Map<PipeEndpoint, BlockCapabilityCache<ResourceHandler<FluidResource>, Direction>> fluidCapabilityCaches = new HashMap<>();
    private final Map<PipeEndpoint, BlockCapabilityCache<IGasHandler, Direction>> gasCapabilityCaches = new HashMap<>();

    public PipeNetwork(PipeNetworkKey key, PipeResourceKind kind) {
        this.key = key;
        this.kind = kind;
    }

    public PipeNetworkKey key() {
        return this.key;
    }

    public PipeResourceKind kind() {
        return this.kind;
    }

    public Map<BlockPos, PipeNode> nodes() {
        return this.nodes;
    }

    public List<PipeEndpoint> inputs() {
        return this.inputs;
    }

    public List<PipeEndpoint> outputs() {
        return this.outputs;
    }

    public PipeRouteCache routeCache() {
        return this.routeCache;
    }

    public int itemRouteRotation(PipeEndpoint source) {
        return this.itemRouteRotation.getOrDefault(source, 0);
    }

    public void setItemRouteRotation(PipeEndpoint source, int rotation) {
        this.itemRouteRotation.put(source, rotation);
    }

    public int fluidOutputRotation(PipeEndpoint source) {
        return this.fluidOutputRotation.getOrDefault(source, 0);
    }

    public void setFluidOutputRotation(PipeEndpoint source, int rotation) {
        this.fluidOutputRotation.put(source, rotation);
    }

    public int gasOutputRotation(PipeEndpoint source) {
        return this.gasOutputRotation.getOrDefault(source, 0);
    }

    public void setGasOutputRotation(PipeEndpoint source, int rotation) {
        this.gasOutputRotation.put(source, rotation);
    }

    public boolean hasItemTransferTick(PipeEndpoint source) {
        return this.lastItemTransferTick.containsKey(source);
    }

    public long lastItemTransferTick(PipeEndpoint source) {
        return this.lastItemTransferTick.getOrDefault(source, 0L);
    }

    public void setLastItemTransferTick(PipeEndpoint source, long tick) {
        this.lastItemTransferTick.put(source, tick);
    }

    public boolean isLoadedPipe(BlockPos pos) {
        return this.nodes.containsKey(pos);
    }

    public ResourceHandler<ItemResource> itemHandler(ServerLevel level, PipeEndpoint endpoint) {
        BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> cache = this.itemCapabilityCaches.computeIfAbsent(
                endpoint,
                key -> BlockCapabilityCache.create(
                        Capabilities.Item.BLOCK,
                        level,
                        key.targetPos(),
                        key.targetSide(),
                        () -> true,
                        () -> PipeNetworkManager.get(level).markDirty(
                                key.pipePos(),
                                PipeResourceKind.ITEM,
                                PipeNetworkDirtyReason.CAPABILITY_INVALIDATED
                        )
                )
        );

        return cache.getCapability();
    }

    public ResourceHandler<FluidResource> fluidHandler(ServerLevel level, PipeEndpoint endpoint) {
        BlockCapabilityCache<ResourceHandler<FluidResource>, Direction> cache = this.fluidCapabilityCaches.computeIfAbsent(
                endpoint,
                key -> BlockCapabilityCache.create(
                        Capabilities.Fluid.BLOCK,
                        level,
                        key.targetPos(),
                        key.targetSide(),
                        () -> true,
                        () -> PipeNetworkManager.get(level).markDirty(
                                key.pipePos(),
                                PipeResourceKind.FLUID,
                                PipeNetworkDirtyReason.CAPABILITY_INVALIDATED
                        )
                )
        );

        return cache.getCapability();
    }

    public IGasHandler gasHandler(ServerLevel level, PipeEndpoint endpoint) {
        BlockCapabilityCache<IGasHandler, Direction> cache = this.gasCapabilityCaches.computeIfAbsent(
                endpoint,
                key -> BlockCapabilityCache.create(
                        ModGasCapabilities.BLOCK,
                        level,
                        key.targetPos(),
                        key.targetSide(),
                        () -> true,
                        () -> PipeNetworkManager.get(level).markDirty(
                                key.pipePos(),
                                PipeResourceKind.GAS,
                                PipeNetworkDirtyReason.CAPABILITY_INVALIDATED
                        )
                )
        );

        return cache.getCapability();
    }

    public void rebuildRoutes(ServerLevel level) {
        this.routeCache.clear();
        this.itemCapabilityCaches.clear();
        this.fluidCapabilityCaches.clear();
        this.gasCapabilityCaches.clear();
        for (PipeEndpoint input : this.inputs) {
            for (PipeEndpoint output : this.outputs) {
                findPath(input.pipePos(), output.pipePos()).ifPresent(path ->
                        this.routeCache.addItemRoute(new PipeRoute(input, output, path))
                );
            }
        }
        // TODO Phase 4/5: precompute source/output sets for fair fluid and gas distribution.
    }

    private java.util.Optional<List<BlockPos>> findPath(BlockPos start, BlockPos target) {
        if (!this.nodes.containsKey(start) || !this.nodes.containsKey(target)) {
            return java.util.Optional.empty();
        }

        ArrayDeque<BlockPos> open = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        Map<BlockPos, BlockPos> previous = new HashMap<>();
        open.add(start);
        visited.add(start);

        while (!open.isEmpty()) {
            BlockPos current = open.removeFirst();
            if (current.equals(target)) {
                return java.util.Optional.of(reconstructPath(previous, target));
            }

            PipeNode node = this.nodes.get(current);
            for (Direction direction : Direction.values()) {
                if (node.sideConfigs().get(direction).mode() != PipeConnectionMode.PIPE) {
                    continue;
                }

                BlockPos next = current.relative(direction);
                PipeNode nextNode = this.nodes.get(next);
                if (nextNode == null || nextNode.sideConfigs().get(direction.getOpposite()).mode() != PipeConnectionMode.PIPE) {
                    continue;
                }

                if (visited.add(next)) {
                    previous.put(next, current);
                    open.add(next);
                }
            }
        }

        return java.util.Optional.empty();
    }

    private static List<BlockPos> reconstructPath(Map<BlockPos, BlockPos> previous, BlockPos target) {
        ArrayList<BlockPos> reversed = new ArrayList<>();
        BlockPos current = target;
        reversed.add(current);
        while (previous.containsKey(current)) {
            current = previous.get(current);
            reversed.add(current);
        }

        ArrayList<BlockPos> path = new ArrayList<>(reversed.size());
        for (int i = reversed.size() - 1; i >= 0; i--) {
            path.add(reversed.get(i));
        }
        return path;
    }
}
