package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.PipeTier;
import org.mydrugs.mydrugs.pipe.blockentity.PipeBlockEntity;
import org.mydrugs.mydrugs.pipe.filter.ItemPipeFilter;

import java.util.ArrayList;
import java.util.List;

public final class ItemPipeNetworkLogic {
    private ItemPipeNetworkLogic() {
    }

    public static void tick(ServerLevel level, PipeNetworkManager manager) {
        for (PipeNetwork network : manager.networks(PipeResourceKind.ITEM).values()) {
            tickNetwork(level, network);
        }
    }

    private static void tickNetwork(ServerLevel level, PipeNetwork network) {
        long gameTime = level.getGameTime();

        for (PipeEndpoint source : network.inputs()) {
            PipeTier tier = resolveTier(level, source);
            if (network.hasItemTransferTick(source)
                    && gameTime - network.lastItemTransferTick(source) < tier.itemIntervalTicks()) {
                continue;
            }

            List<PipeRoute> routes = network.routeCache().itemRoutesFrom(source)
                    .stream()
                    .filter(route -> route.isLoadedPath(network))
                    .toList();
            if (routes.isEmpty()) {
                continue;
            }

            ResourceHandler<ItemResource> sourceHandler = network.itemHandler(level, source);
            if (sourceHandler != null && tryMoveFromSource(level, network, source, sourceHandler, routes, tier.itemAmount())) {
                network.setLastItemTransferTick(source, gameTime);
            }
        }
    }

    private static boolean tryMoveFromSource(
            ServerLevel level,
            PipeNetwork network,
            PipeEndpoint source,
            ResourceHandler<ItemResource> sourceHandler,
            List<PipeRoute> routes,
            int maxAmount
    ) {
        for (int slot = 0; slot < sourceHandler.size(); slot++) {
            ItemResource resource = sourceHandler.getResource(slot);
            int stored = sourceHandler.getAmountAsInt(slot);
            if (resource.isEmpty() || stored <= 0 || !allows(source, resource)) {
                continue;
            }

            int extractable = simulateExtract(sourceHandler, slot, resource, Math.min(stored, maxAmount));
            if (extractable <= 0) {
                continue;
            }

            if (tryDistribute(level, network, source, sourceHandler, slot, resource, extractable, routes)) {
                return true;
            }
        }

        return false;
    }

    private static boolean tryDistribute(
            ServerLevel level,
            PipeNetwork network,
            PipeEndpoint source,
            ResourceHandler<ItemResource> sourceHandler,
            int sourceSlot,
            ItemResource resource,
            int amount,
            List<PipeRoute> routes
    ) {
        List<Candidate> candidates = collectCandidates(level, network, source, resource, amount, routes);
        if (candidates.isEmpty()) {
            return false;
        }

        List<Allocation> allocations = allocateFairly(candidates, amount);
        int total = allocations.stream().mapToInt(Allocation::amount).sum();
        if (total <= 0) {
            return false;
        }

        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = sourceHandler.extract(sourceSlot, resource, total, transaction);
            if (extracted != total) {
                return false;
            }

            for (Allocation allocation : allocations) {
                int inserted = allocation.candidate().handler().insert(resource, allocation.amount(), transaction);
                if (inserted != allocation.amount()) {
                    return false;
                }
            }

            transaction.commit();
        }

        int nextRotation = (network.itemRouteRotation(source) + 1) % Math.max(1, routes.size());
        network.setItemRouteRotation(source, nextRotation);
        return true;
    }

    private static List<Candidate> collectCandidates(
            ServerLevel level,
            PipeNetwork network,
            PipeEndpoint source,
            ItemResource resource,
            int amount,
            List<PipeRoute> routes
    ) {
        ArrayList<Candidate> candidates = new ArrayList<>();
        int rotation = routes.isEmpty() ? 0 : Math.floorMod(network.itemRouteRotation(source), routes.size());

        for (int i = 0; i < routes.size(); i++) {
            PipeRoute route = routes.get((rotation + i) % routes.size());
            PipeEndpoint target = route.target();
            if (target.targetPos().equals(source.targetPos()) || !allows(target, resource)) {
                continue;
            }

            ResourceHandler<ItemResource> targetHandler = network.itemHandler(level, target);
            if (targetHandler == null) {
                continue;
            }

            int capacity = simulateInsert(targetHandler, resource, amount);
            if (capacity > 0) {
                candidates.add(new Candidate(route, targetHandler, capacity));
            }
        }

        return candidates;
    }

    private static List<Allocation> allocateFairly(List<Candidate> candidates, int amount) {
        ArrayList<Allocation> allocations = new ArrayList<>();
        int[] assigned = new int[candidates.size()];
        int remaining = amount;

        int base = amount / candidates.size();
        int remainder = amount % candidates.size();
        for (int i = 0; i < candidates.size(); i++) {
            int requested = base + (i < remainder ? 1 : 0);
            int accepted = Math.min(requested, candidates.get(i).capacity());
            assigned[i] += accepted;
            remaining -= accepted;
        }

        while (remaining > 0) {
            boolean moved = false;
            for (int i = 0; i < candidates.size() && remaining > 0; i++) {
                int spare = candidates.get(i).capacity() - assigned[i];
                if (spare <= 0) {
                    continue;
                }

                assigned[i]++;
                remaining--;
                moved = true;
            }

            if (!moved) {
                break;
            }
        }

        for (int i = 0; i < candidates.size(); i++) {
            if (assigned[i] > 0) {
                allocations.add(new Allocation(candidates.get(i), assigned[i]));
            }
        }

        return allocations;
    }

    private static int simulateExtract(ResourceHandler<ItemResource> handler, int slot, ItemResource resource, int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            return handler.extract(slot, resource, amount, transaction);
        }
    }

    private static int simulateInsert(ResourceHandler<ItemResource> handler, ItemResource resource, int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            return handler.insert(resource, amount, transaction);
        }
    }

    private static boolean allows(PipeEndpoint endpoint, ItemResource resource) {
        return endpoint.filter() == null || ItemPipeFilter.allows(endpoint.filter(), resource);
    }

    private static PipeTier resolveTier(ServerLevel level, PipeEndpoint source) {
        if (level.getBlockEntity(source.pipePos()) instanceof PipeBlockEntity pipe) {
            return pipe.tier();
        }

        return PipeTier.BASIC;
    }

    private record Candidate(PipeRoute route, ResourceHandler<ItemResource> handler, int capacity) {
    }

    private record Allocation(Candidate candidate, int amount) {
    }
}
