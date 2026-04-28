package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.server.level.ServerLevel;
import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.gas.IGasHandler;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.PipeTier;
import org.mydrugs.mydrugs.pipe.blockentity.PipeBlockEntity;
import org.mydrugs.mydrugs.pipe.filter.GasPipeFilter;

import java.util.ArrayList;
import java.util.List;

public final class GasPipeNetworkLogic {
    private GasPipeNetworkLogic() {
    }

    public static void tick(ServerLevel level, PipeNetworkManager manager) {
        for (PipeNetwork network : manager.networks(PipeResourceKind.GAS).values()) {
            tickNetwork(level, network);
        }
    }

    private static void tickNetwork(ServerLevel level, PipeNetwork network) {
        if (network.outputs().isEmpty()) {
            return;
        }

        for (PipeEndpoint source : network.inputs()) {
            IGasHandler sourceHandler = network.gasHandler(level, source);
            if (sourceHandler == null) {
                continue;
            }

            long amount = resolveTier(level, source).gasAmountPerTick();
            if (amount > 0 && tryMoveFromSource(level, network, source, sourceHandler, amount)) {
                int nextRotation = (network.gasOutputRotation(source) + 1) % Math.max(1, network.outputs().size());
                network.setGasOutputRotation(source, nextRotation);
            }
        }
    }

    private static boolean tryMoveFromSource(
            ServerLevel level,
            PipeNetwork network,
            PipeEndpoint source,
            IGasHandler sourceHandler,
            long maxAmount
    ) {
        for (int tank = 0; tank < sourceHandler.getTanks(); tank++) {
            GasStack stored = sourceHandler.getGasInTank(tank);
            if (stored.isEmpty() || !allows(source, stored)) {
                continue;
            }

            long extractable = Math.min(stored.amount(), maxAmount);
            GasStack simulatedDrain = sourceHandler.drain(tank, extractable, true);
            if (simulatedDrain.isEmpty() || !stored.sameGas(simulatedDrain)) {
                continue;
            }

            if (tryDistribute(level, network, source, sourceHandler, tank, simulatedDrain)) {
                return true;
            }
        }

        return false;
    }

    private static boolean tryDistribute(
            ServerLevel level,
            PipeNetwork network,
            PipeEndpoint source,
            IGasHandler sourceHandler,
            int sourceTank,
            GasStack resource
    ) {
        List<Candidate> candidates = collectCandidates(level, network, source, resource);
        if (candidates.isEmpty()) {
            return false;
        }

        List<Allocation> allocations = allocateFairly(candidates, resource.amount());
        long total = allocations.stream().mapToLong(Allocation::amount).sum();
        if (total <= 0) {
            return false;
        }

        GasStack drained = sourceHandler.drain(sourceTank, total, false);
        if (drained.isEmpty() || drained.amount() != total || !resource.sameGas(drained)) {
            return false;
        }

        for (Allocation allocation : allocations) {
            long inserted = allocation.candidate().handler().fill(resource.withAmount(allocation.amount()), false);
            if (inserted != allocation.amount()) {
                // Gas handlers are not transactional; capacity was simulated first, but if a target changed
                // mid-tick we leave the network dirty so the next pass rechecks capabilities and filters.
                PipeNetworkManager.get(level).markDirty(source.pipePos(), PipeResourceKind.GAS, PipeNetworkDirtyReason.CAPABILITY_INVALIDATED);
                return true;
            }
        }

        return true;
    }

    private static List<Candidate> collectCandidates(ServerLevel level, PipeNetwork network, PipeEndpoint source, GasStack resource) {
        ArrayList<Candidate> candidates = new ArrayList<>();
        List<PipeEndpoint> outputs = network.outputs();
        int rotation = outputs.isEmpty() ? 0 : Math.floorMod(network.gasOutputRotation(source), outputs.size());
        for (int i = 0; i < outputs.size(); i++) {
            PipeEndpoint target = outputs.get((rotation + i) % outputs.size());
            if (!allows(target, resource)) {
                continue;
            }

            IGasHandler targetHandler = network.gasHandler(level, target);
            if (targetHandler == null) {
                continue;
            }

            long capacity = targetHandler.fill(resource, true);
            if (capacity > 0) {
                candidates.add(new Candidate(targetHandler, capacity));
            }
        }

        return candidates;
    }

    private static List<Allocation> allocateFairly(List<Candidate> candidates, long amount) {
        ArrayList<Allocation> allocations = new ArrayList<>();
        long[] assigned = new long[candidates.size()];
        long remaining = amount;

        long base = amount / candidates.size();
        long remainder = amount % candidates.size();
        for (int i = 0; i < candidates.size(); i++) {
            long requested = base + (i < remainder ? 1 : 0);
            long accepted = Math.min(requested, candidates.get(i).capacity());
            assigned[i] += accepted;
            remaining -= accepted;
        }

        while (remaining > 0) {
            boolean moved = false;
            for (int i = 0; i < candidates.size() && remaining > 0; i++) {
                long spare = candidates.get(i).capacity() - assigned[i];
                if (spare <= 0) {
                    continue;
                }

                long movedAmount = Math.min(spare, remaining);
                assigned[i] += movedAmount;
                remaining -= movedAmount;
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

    private static boolean allows(PipeEndpoint endpoint, GasStack stack) {
        return endpoint.filter() == null || GasPipeFilter.allows(endpoint.filter(), stack);
    }

    private static PipeTier resolveTier(ServerLevel level, PipeEndpoint source) {
        if (level.getBlockEntity(source.pipePos()) instanceof PipeBlockEntity pipe) {
            return pipe.tier();
        }

        return PipeTier.BASIC;
    }

    private record Candidate(IGasHandler handler, long capacity) {
    }

    private record Allocation(Candidate candidate, long amount) {
    }
}
