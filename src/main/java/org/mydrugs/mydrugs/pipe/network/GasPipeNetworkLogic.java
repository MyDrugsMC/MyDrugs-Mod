package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.gas.IGasHandler;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.PipeTier;
import org.mydrugs.mydrugs.pipe.blockentity.PipeBlockEntity;
import org.mydrugs.mydrugs.pipe.filter.GasPipeFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public final class GasPipeNetworkLogic {
    private GasPipeNetworkLogic() {
    }

    public static void tick(ServerLevel level, PipeNetworkManager manager) {
        for (PipeNetwork network : manager.networks(PipeResourceKind.GAS).values()) {
            tickNetwork(level, network);
        }
    }

    private static void tickNetwork(ServerLevel level, PipeNetwork network) {
        PipeNetworkDiagnostics.networkTicked(PipeResourceKind.GAS, network.inputs().size() + network.outputs().size());
        if (network.outputs().isEmpty()) {
            return;
        }

        Set<BlockPos> usedSourceStorages = new HashSet<>();
        for (PipeEndpoint source : network.inputs()) {
            if (!usedSourceStorages.add(source.targetPos())) {
                continue;
            }
            if (!network.isEndpointLoaded(level, source)) {
                PipeNetworkManager.get(level).markDirty(source.pipePos(), PipeResourceKind.GAS, PipeNetworkDirtyReason.CHUNK_UNLOAD);
                continue;
            }

            IGasHandler sourceHandler = network.gasHandler(level, source);
            if (sourceHandler == null) {
                continue;
            }

            long amount = resolveTier(level, source).gasAmountPerTick();
            if (amount > 0 && tryMoveFromSource(level, network, source, sourceHandler, amount)) {
                int nextRotation = (network.gasOutputRotation(source) + 1) % Math.max(1, network.outputCandidates(source).size());
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
        PipeNetworkDiagnostics.transferAttempted();

        long moved = 0L;
        for (Allocation allocation : allocations) {
            GasStack moving = resource.withAmount(allocation.amount());
            long currentCapacity = allocation.candidate().handler().fill(moving, true);
            if (currentCapacity < allocation.amount()) {
                PipeNetworkManager.get(level).markDirty(source.pipePos(), PipeResourceKind.GAS, PipeNetworkDirtyReason.CAPABILITY_INVALIDATED);
                continue;
            }

            GasStack drained = sourceHandler.drain(sourceTank, allocation.amount(), false);
            if (drained.isEmpty() || drained.amount() != allocation.amount() || !resource.sameGas(drained)) {
                if (!drained.isEmpty() && resource.sameGas(drained)) {
                    refundToSource(sourceHandler, sourceTank, drained);
                }
                PipeNetworkManager.get(level).markDirty(source.pipePos(), PipeResourceKind.GAS, PipeNetworkDirtyReason.CAPABILITY_INVALIDATED);
                break;
            }

            long inserted = allocation.candidate().handler().fill(drained, false);
            if (inserted != allocation.amount()) {
                long missing = allocation.amount() - inserted;
                boolean refunded = refundToSource(sourceHandler, sourceTank, resource.withAmount(missing));
                PipeNetworkManager.get(level).markDirty(source.pipePos(), PipeResourceKind.GAS, PipeNetworkDirtyReason.CAPABILITY_INVALIDATED);
                if (inserted > 0) {
                    moved += inserted;
                }
                return refunded && moved > 0;
            }

            moved += inserted;
        }

        if (moved > 0) {
            PipeNetworkDiagnostics.transferSucceeded();
            return true;
        }
        return false;
    }

    private static List<Candidate> collectCandidates(ServerLevel level, PipeNetwork network, PipeEndpoint source, GasStack resource) {
        ArrayList<Candidate> candidates = new ArrayList<>();
        Set<BlockPos> seenTargets = new HashSet<>();
        Set<IGasHandler> seenHandlers = Collections.newSetFromMap(new IdentityHashMap<>());
        List<PipeEndpoint> outputs = network.outputCandidates(source);
        int rotation = outputs.isEmpty() ? 0 : Math.floorMod(network.gasOutputRotation(source), outputs.size());
        for (int i = 0; i < outputs.size(); i++) {
            PipeEndpoint target = outputs.get((rotation + i) % outputs.size());
            PipeNetworkDiagnostics.candidateConsidered();
            if (!network.isEndpointLoaded(level, target) || !allows(target, resource)) {
                continue;
            }

            IGasHandler targetHandler = network.gasHandler(level, target);
            if (targetHandler == null || seenTargets.contains(target.targetPos()) || seenHandlers.contains(targetHandler)) {
                continue;
            }

            long capacity = targetHandler.fill(resource, true);
            if (capacity > 0) {
                seenTargets.add(target.targetPos());
                seenHandlers.add(targetHandler);
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

    private static boolean refundToSource(IGasHandler sourceHandler, int sourceTank, GasStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        long refunded = sourceHandler.fill(sourceTank, stack, false);
        if (refunded == stack.amount()) {
            return true;
        }

        GasStack remaining = stack.withAmount(stack.amount() - refunded);
        long fallbackRefunded = sourceHandler.fill(remaining, false);
        return fallbackRefunded == remaining.amount();
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
