package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.PipeTier;
import org.mydrugs.mydrugs.pipe.blockentity.PipeBlockEntity;
import org.mydrugs.mydrugs.pipe.filter.FluidPipeFilter;

import java.util.ArrayList;
import java.util.List;

public final class FluidPipeNetworkLogic {
    private FluidPipeNetworkLogic() {
    }

    public static void tick(ServerLevel level, PipeNetworkManager manager) {
        for (PipeNetwork network : manager.networks(PipeResourceKind.FLUID).values()) {
            tickNetwork(level, network);
        }
    }

    private static void tickNetwork(ServerLevel level, PipeNetwork network) {
        if (network.outputs().isEmpty()) {
            return;
        }

        for (PipeEndpoint source : network.inputs()) {
            ResourceHandler<FluidResource> sourceHandler = network.fluidHandler(level, source);
            if (sourceHandler == null) {
                continue;
            }

            int amount = resolveTier(level, source).fluidAmountPerTick();
            if (amount > 0 && tryMoveFromSource(level, network, source, sourceHandler, amount)) {
                int nextRotation = (network.fluidOutputRotation(source) + 1) % Math.max(1, network.outputs().size());
                network.setFluidOutputRotation(source, nextRotation);
            }
        }
    }

    private static boolean tryMoveFromSource(
            ServerLevel level,
            PipeNetwork network,
            PipeEndpoint source,
            ResourceHandler<FluidResource> sourceHandler,
            int maxAmount
    ) {
        for (int slot = 0; slot < sourceHandler.size(); slot++) {
            FluidResource resource = sourceHandler.getResource(slot);
            int stored = sourceHandler.getAmountAsInt(slot);
            if (resource.isEmpty() || stored <= 0 || !allows(source, resource)) {
                continue;
            }

            int extractable = simulateExtract(sourceHandler, slot, resource, Math.min(stored, maxAmount));
            if (extractable <= 0) {
                continue;
            }

            if (tryDistribute(level, network, source, sourceHandler, slot, resource, extractable)) {
                return true;
            }
        }

        return false;
    }

    private static boolean tryDistribute(
            ServerLevel level,
            PipeNetwork network,
            PipeEndpoint source,
            ResourceHandler<FluidResource> sourceHandler,
            int sourceSlot,
            FluidResource resource,
            int amount
    ) {
        List<Candidate> candidates = collectCandidates(level, network, source, resource, amount);
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

        return true;
    }

    private static List<Candidate> collectCandidates(
            ServerLevel level,
            PipeNetwork network,
            PipeEndpoint source,
            FluidResource resource,
            int amount
    ) {
        ArrayList<Candidate> candidates = new ArrayList<>();
        List<PipeEndpoint> outputs = network.outputs();
        int rotation = outputs.isEmpty() ? 0 : Math.floorMod(network.fluidOutputRotation(source), outputs.size());
        for (int i = 0; i < outputs.size(); i++) {
            PipeEndpoint target = outputs.get((rotation + i) % outputs.size());
            if (target.targetPos().equals(source.targetPos()) || !allows(target, resource)) {
                continue;
            }

            ResourceHandler<FluidResource> targetHandler = network.fluidHandler(level, target);
            if (targetHandler == null) {
                continue;
            }

            int capacity = simulateInsert(targetHandler, resource, amount);
            if (capacity > 0) {
                candidates.add(new Candidate(targetHandler, capacity));
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

    private static int simulateExtract(ResourceHandler<FluidResource> handler, int slot, FluidResource resource, int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            return handler.extract(slot, resource, amount, transaction);
        }
    }

    private static int simulateInsert(ResourceHandler<FluidResource> handler, FluidResource resource, int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            return handler.insert(resource, amount, transaction);
        }
    }

    private static boolean allows(PipeEndpoint endpoint, FluidResource resource) {
        return endpoint.filter() == null || FluidPipeFilter.allows(endpoint.filter(), resource);
    }

    private static PipeTier resolveTier(ServerLevel level, PipeEndpoint source) {
        if (level.getBlockEntity(source.pipePos()) instanceof PipeBlockEntity pipe) {
            return pipe.tier();
        }

        return PipeTier.BASIC;
    }

    private record Candidate(ResourceHandler<FluidResource> handler, int capacity) {
    }

    private record Allocation(Candidate candidate, int amount) {
    }
}
