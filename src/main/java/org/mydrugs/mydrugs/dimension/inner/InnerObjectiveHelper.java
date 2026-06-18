package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.InnerDimensionService;
import org.mydrugs.mydrugs.dimension.InnerDimensions;

import java.util.UUID;

public final class InnerObjectiveHelper {
    private static final int SCAR_TARGET_NEAR_RADIUS = 128;
    private static final int SCAR_TARGET_NEAR_STEP = 16;
    private static final int SCAR_TARGET_CENTER_RADIUS = 720;
    private static final int SCAR_TARGET_CENTER_STEP = 24;
    private static final int SCAR_TARGET_CENTER_DIRECTIONS = 32;

    private InnerObjectiveHelper() {
    }

    public enum Kind {
        ENTER,
        TRIAL,
        RETURN_TO_ANCHOR,
        VAULT,
        SCAR,
        WAIT_FOR_INTEGRATION,
        OPEN_SPIRAL_COURT,
        COMPLETE_SPIRAL_COURT,
        COMPLETE
    }

    public record Objective(
            Kind kind,
            Component message,
            @Nullable BlockPos target,
            @Nullable Component targetName
    ) {
    }

    public static Objective currentObjective(ServerPlayer player) {
        if (player == null || !player.level().dimension().equals(InnerDimensions.INNER_LEVEL)) {
            return outsideObjective(player);
        }
        ServerLevel level = (ServerLevel) player.level();
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        InnerIslandContext context = InnerIslandContext.resolve(player, data);
        if (context == null || !context.playerInsideOwnIsland()) {
            return unavailableObjective();
        }
        InnerDimensionSavedData.IslandState island = context.island();
        return currentObjective(data, island, context.owner(), player.blockPosition(), liveTargets(level, island));
    }

    static Objective currentObjectiveForTest(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            UUID owner,
            BlockPos origin
    ) {
        return currentObjectiveFromState(data, island, owner, origin);
    }

    static Objective currentObjectiveFromState(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            UUID owner,
            BlockPos origin
    ) {
        return currentObjective(data, island, owner, origin, testTargets(island));
    }

    private static Objective currentObjective(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            UUID owner,
            BlockPos origin,
            ObjectiveTargets targets
    ) {
        InnerGameplayLoop.State state = InnerGameplayLoop.inside(data, island, owner, origin);
        return switch (state.phase()) {
            case RETURN_TO_ANCHOR -> targets.anchor(
                    Kind.RETURN_TO_ANCHOR,
                    "message.mydrugs.inner_objective.return_anchor"
            );
            case REFLECT_UNLOCK -> reflectionObjective(data, island, owner, origin);
            case SEEK_TRIAL, ATTEMPT_TRIAL, SEEK_NEXT -> state.nearestTrial() == null
                    ? exploreObjective()
                    : targets.trial(state.nearestTrial());
            case WAIT_FOR_INTEGRATION -> new Objective(
                    Kind.WAIT_FOR_INTEGRATION,
                    Component.translatable("message.mydrugs.inner_objective.wait_integration"),
                    null,
                    null
            );
            case OPEN_SPIRAL_COURT -> targets.anchor(
                    Kind.OPEN_SPIRAL_COURT,
                    "message.mydrugs.inner_objective.open_spiral"
            );
            case COMPLETE_SPIRAL_COURT -> targets.anchor(
                    Kind.COMPLETE_SPIRAL_COURT,
                    "message.mydrugs.inner_objective.complete_spiral"
            );
            case COMPLETE -> new Objective(
                    Kind.COMPLETE,
                    Component.translatable("message.mydrugs.inner_objective.complete"),
                    null,
                    null
            );
            case ENTER_READY, UNAVAILABLE -> unavailableObjective();
        };
    }

    public static Component memoryCompassMessage(ServerPlayer player) {
        if (player == null || !player.level().dimension().equals(InnerDimensions.INNER_LEVEL)) {
            return outsideObjective(player).message();
        }
        ServerLevel level = (ServerLevel) player.level();
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        InnerIslandContext context = InnerIslandContext.resolve(player, data);
        if (context == null || !context.playerInsideOwnIsland()) {
            return unavailableObjective().message();
        }
        InnerDimensionSavedData.IslandState island = context.island();
        return memoryCompassMessage(
                data,
                island,
                context.owner(),
                player.blockPosition(),
                player.getX(),
                player.getZ(),
                liveTargets(level, island)
        );
    }

    static Component memoryCompassMessageForTest(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            UUID owner,
            BlockPos origin
    ) {
        return memoryCompassMessage(
                data,
                island,
                owner,
                origin,
                origin.getX(),
                origin.getZ(),
                testTargets(island)
        );
    }

    private static Component memoryCompassMessage(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            UUID owner,
            BlockPos origin,
            double playerX,
            double playerZ,
            ObjectiveTargets targets
    ) {
        InnerGameplayLoop.State state = InnerGameplayLoop.inside(data, island, owner, origin);
        return switch (state.phase()) {
            case RETURN_TO_ANCHOR -> compassTargetMessage(playerX, playerZ,
                    targets.anchor(Kind.RETURN_TO_ANCHOR, "message.mydrugs.inner_objective.return_anchor"));
            case SEEK_TRIAL, ATTEMPT_TRIAL, SEEK_NEXT -> state.nearestTrial() == null
                    ? Component.translatable("message.mydrugs.memory_compass.rest")
                    : compassTargetMessage(playerX, playerZ, targets.trial(state.nearestTrial()));
            case REFLECT_UNLOCK -> compassTargetMessage(
                    playerX,
                    playerZ,
                    reflectionObjective(data, island, owner, origin)
            );
            case WAIT_FOR_INTEGRATION -> Component.translatable("message.mydrugs.memory_compass.wait_integration");
            case OPEN_SPIRAL_COURT -> compassTargetMessage(playerX, playerZ,
                    targets.anchor(Kind.OPEN_SPIRAL_COURT, "message.mydrugs.inner_objective.open_spiral"));
            case COMPLETE_SPIRAL_COURT -> compassTargetMessage(playerX, playerZ,
                    targets.anchor(Kind.COMPLETE_SPIRAL_COURT, "message.mydrugs.inner_objective.complete_spiral"));
            case COMPLETE -> Component.translatable("message.mydrugs.memory_compass.rest");
            case ENTER_READY, UNAVAILABLE -> unavailableObjective().message();
        };
    }

    private static Objective outsideObjective(@Nullable ServerPlayer player) {
        if (player == null) {
            return unavailableObjective();
        }
        InnerDimensionService.OpenStatus status = InnerDimensionService.openStatus(player);
        InnerGameplayLoop.Phase phase = InnerGameplayLoop.outside(status);
        String key = switch (status) {
            case READY -> phase == InnerGameplayLoop.Phase.ENTER_READY
                    ? "message.mydrugs.inner_objective.enter_ready"
                    : "message.mydrugs.inner_objective.unavailable";
            case MISSING_INTEGRATION -> "message.mydrugs.inner_objective.need_integration";
            case MISSING_DREAM_ALIGNMENT -> "message.mydrugs.inner_objective.need_alignment";
            case UNAVAILABLE -> "message.mydrugs.inner_objective.unavailable";
        };
        return new Objective(Kind.ENTER, Component.translatable(key), null, null);
    }

    private static Objective unavailableObjective() {
        return new Objective(
                Kind.ENTER,
                Component.translatable("message.mydrugs.inner_objective.unavailable"),
                null,
                null
        );
    }

    private static Objective trialObjective(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            DrugId drug
    ) {
        BlockPos landmark = InnerRegionMap.landmarkFor(island.centerX(), island.centerZ(), drug);
        BlockPos target = InnerPlacement.surfaceTop(level, landmark.getX(), landmark.getZ());
        InnerTrialDefinition definition = InnerTrialDefinition.forDrug(drug);
        Component title = definition == null
                ? Component.translatable("drug.mydrugs." + drug.serializedName())
                : Component.translatable(definition.titleKey());
        return new Objective(
                Kind.TRIAL,
                Component.translatable(
                        "message.mydrugs.inner_objective.trial",
                        Component.translatable("drug.mydrugs." + drug.serializedName()),
                        title
                ),
                target,
                title
        );
    }

    private static Objective reflectionObjective(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            UUID owner,
            BlockPos origin
    ) {
        if (!data.hasProgressMarker(owner, InnerDimensionConstants.MARKER_FIRST_VAULT_OPENED)) {
            return vaultObjective(island, origin);
        }
        if (island.restoredScarMarkers().isEmpty()) {
            return scarObjective(island, origin);
        }
        return exploreObjective();
    }

    private static Objective vaultObjective(
            InnerDimensionSavedData.IslandState island,
            BlockPos origin
    ) {
        BlockPos target = vaultTarget(island, origin);
        return new Objective(
                Kind.VAULT,
                Component.translatable("message.mydrugs.inner_objective.vault"),
                target,
                target == null ? null : Component.translatable("message.mydrugs.inner_target.memory_vault")
        );
    }

    private static Objective scarObjective(
            InnerDimensionSavedData.IslandState island,
            BlockPos origin
    ) {
        BlockPos target = scarTarget(island, origin);
        return new Objective(
                Kind.SCAR,
                Component.translatable("message.mydrugs.inner_objective.scar"),
                target,
                target == null ? null : Component.translatable("message.mydrugs.inner_target.scar")
        );
    }

    private static @Nullable BlockPos vaultTarget(
            InnerDimensionSavedData.IslandState island,
            BlockPos origin
    ) {
        InnerVaults.Vault vault = InnerVaults.nearestUnlockedVault(island, origin);
        if (vault == null) {
            return null;
        }
        InnerTerrain.Sample sample = InnerTerrain.sample(
                island.centerX(),
                island.centerZ(),
                vault.x(),
                vault.z()
        );
        return new BlockPos(vault.x(), InnerVaults.chestY(vault, sample.topY()), vault.z());
    }

    private static @Nullable BlockPos scarTarget(
            InnerDimensionSavedData.IslandState island,
            BlockPos origin
    ) {
        if (island == null) {
            return null;
        }
        BlockPos near = origin == null
                ? new BlockPos(island.centerX(), InnerDimensionConstants.BASE_Y, island.centerZ())
                : origin;
        BlockPos target = scarTargetNear(island, near, SCAR_TARGET_NEAR_RADIUS, SCAR_TARGET_NEAR_STEP);
        if (target != null) {
            return target;
        }
        return scarTargetFromCenter(island);
    }

    private static @Nullable BlockPos scarTargetNear(
            InnerDimensionSavedData.IslandState island,
            BlockPos origin,
            int radius,
            int step
    ) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        InnerTerrain.beginCachePass();
        try {
            for (int dz = -radius; dz <= radius; dz += step) {
                for (int dx = -radius; dx <= radius; dx += step) {
                    if (dx * dx + dz * dz > radius * radius) {
                        continue;
                    }
                    BlockPos candidate = scarCandidate(island, origin.getX() + dx, origin.getZ() + dz);
                    if (candidate == null) {
                        continue;
                    }
                    double distance = (double) dx * dx + (double) dz * dz;
                    if (distance < bestDistance) {
                        best = candidate;
                        bestDistance = distance;
                    }
                }
            }
        } finally {
            InnerTerrain.endCachePass();
        }
        return best;
    }

    private static @Nullable BlockPos scarTargetFromCenter(InnerDimensionSavedData.IslandState island) {
        InnerTerrain.beginCachePass();
        try {
            for (int radius = InnerDimensionConstants.CORE_RADIUS + 64;
                 radius <= SCAR_TARGET_CENTER_RADIUS;
                 radius += SCAR_TARGET_CENTER_STEP) {
                for (int i = 0; i < SCAR_TARGET_CENTER_DIRECTIONS; i++) {
                    double angle = Math.PI * 2.0D * i / SCAR_TARGET_CENTER_DIRECTIONS;
                    int x = island.centerX() + (int) Math.round(Math.cos(angle) * radius);
                    int z = island.centerZ() + (int) Math.round(Math.sin(angle) * radius);
                    BlockPos candidate = scarCandidate(island, x, z);
                    if (candidate != null) {
                        return candidate;
                    }
                }
            }
        } finally {
            InnerTerrain.endCachePass();
        }
        return null;
    }

    private static @Nullable BlockPos scarCandidate(
            InnerDimensionSavedData.IslandState island,
            int x,
            int z
    ) {
        if (InnerTerrain.slotCenter(x) != island.centerX() || InnerTerrain.slotCenter(z) != island.centerZ()) {
            return null;
        }
        InnerTerrain.Sample sample = InnerTerrain.sample(island.centerX(), island.centerZ(), x, z);
        if (!sample.land() || !sample.scar()) {
            return null;
        }
        BlockPos target = new BlockPos(x, sample.topY() + 1, z);
        return InnerScarHealer.isRestoredAt(island, target) ? null : target;
    }

    private static Objective exploreObjective() {
        return new Objective(
                Kind.COMPLETE,
                Component.translatable("message.mydrugs.inner_objective.explore"),
                null,
                null
        );
    }

    private static ObjectiveTargets liveTargets(ServerLevel level, InnerDimensionSavedData.IslandState island) {
        return new ObjectiveTargets() {
            @Override
            public Objective trial(DrugId drug) {
                return trialObjective(level, island, drug);
            }

            @Override
            public Objective anchor(Kind kind, String messageKey) {
                return anchorObjective(level, island, kind, messageKey);
            }
        };
    }

    private static ObjectiveTargets testTargets(InnerDimensionSavedData.IslandState island) {
        return new ObjectiveTargets() {
            @Override
            public Objective trial(DrugId drug) {
                BlockPos target = InnerRegionMap.landmarkFor(island.centerX(), island.centerZ(), drug);
                InnerTrialDefinition definition = InnerTrialDefinition.forDrug(drug);
                Component title = definition == null
                        ? Component.translatable("drug.mydrugs." + drug.serializedName())
                        : Component.translatable(definition.titleKey());
                return new Objective(
                        Kind.TRIAL,
                        Component.translatable(
                                "message.mydrugs.inner_objective.trial",
                                Component.translatable("drug.mydrugs." + drug.serializedName()),
                                title
                        ),
                        target,
                        title
                );
            }

            @Override
            public Objective anchor(Kind kind, String messageKey) {
                return new Objective(
                        kind,
                        Component.translatable(messageKey),
                        new BlockPos(island.centerX(), InnerDimensionConstants.BASE_Y, island.centerZ()),
                        Component.translatable(kind == Kind.COMPLETE_SPIRAL_COURT
                                ? "message.mydrugs.inner_target.spiral_court"
                                : "message.mydrugs.inner_target.self_anchor")
                );
            }
        };
    }

    private interface ObjectiveTargets {
        Objective trial(DrugId drug);

        Objective anchor(Kind kind, String messageKey);
    }

    private static Objective anchorObjective(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            Kind kind,
            String messageKey
    ) {
        BlockPos target = InnerPlacement.surfaceTop(level, island.centerX(), island.centerZ());
        return new Objective(
                kind,
                Component.translatable(messageKey),
                target,
                Component.translatable(kind == Kind.COMPLETE_SPIRAL_COURT
                        ? "message.mydrugs.inner_target.spiral_court"
                        : "message.mydrugs.inner_target.self_anchor")
        );
    }

    private static Component compassTargetMessage(double playerX, double playerZ, Objective objective) {
        if (objective.target() == null || objective.targetName() == null) {
            return objective.message();
        }
        double dx = objective.target().getX() - playerX;
        double dz = objective.target().getZ() - playerZ;
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        return Component.translatable(
                "message.mydrugs.memory_compass.target",
                objective.targetName(),
                direction(dx, dz),
                distance
        );
    }

    private static Component direction(double dx, double dz) {
        double degrees = Math.toDegrees(Math.atan2(dz, dx));
        int sector = Math.floorMod((int) Math.round(degrees / 45.0D), 8);
        String direction = switch (sector) {
            case 0 -> "east";
            case 1 -> "southeast";
            case 2 -> "south";
            case 3 -> "southwest";
            case 4 -> "west";
            case 5 -> "northwest";
            case 6 -> "north";
            default -> "northeast";
        };
        return Component.translatable("message.mydrugs.direction." + direction);
    }

    static Component compassTargetMessageForTest(Objective objective, BlockPos origin) {
        return compassTargetMessage(origin.getX(), origin.getZ(), objective);
    }

}
