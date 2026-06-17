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
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(player.getUUID());
        return currentObjective(data, island, player.getUUID(), player.blockPosition(), liveTargets(level, island));
    }

    static Objective currentObjectiveForTest(
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
        if (data.hasPendingTrialReturn(owner)) {
            return targets.anchor(Kind.RETURN_TO_ANCHOR, "message.mydrugs.inner_objective.return_anchor");
        }
        DrugId nearest = InnerTrialManager.nearestIncompleteTrial(island, origin);
        if (nearest != null) {
            return targets.trial(nearest);
        }
        if (!island.allInnerTrialsCompleted()
                && island.completedInnerTrialCount() > 0
                && !data.hasProgressMarker(owner, InnerDimensionConstants.MARKER_FIRST_VAULT_OPENED)) {
            return new Objective(
                    Kind.VAULT,
                    Component.translatable("message.mydrugs.inner_objective.vault"),
                    null,
                    null
            );
        }
        if (!island.allInnerTrialsCompleted()
                && island.completedInnerTrialCount() > 0
                && island.restoredScarMarkers().isEmpty()) {
            return new Objective(
                    Kind.SCAR,
                    Component.translatable("message.mydrugs.inner_objective.scar"),
                    null,
                    null
            );
        }
        if (island.allInnerTrialsCompleted()) {
            if (!data.isSpiralCourtPlaced(owner)) {
                return targets.anchor(Kind.OPEN_SPIRAL_COURT, "message.mydrugs.inner_objective.open_spiral");
            }
            if (!data.hasProgressMarker(owner, InnerDimensionConstants.MARKER_SPIRAL_COMPLETED)) {
                return targets.anchor(Kind.COMPLETE_SPIRAL_COURT, "message.mydrugs.inner_objective.complete_spiral");
            }
            return new Objective(
                    Kind.COMPLETE,
                    Component.translatable("message.mydrugs.inner_objective.complete"),
                    null,
                    null
            );
        }
        if (allIntegratedTrialsCompleted(island)) {
            return new Objective(
                    Kind.WAIT_FOR_INTEGRATION,
                    Component.translatable("message.mydrugs.inner_objective.wait_integration"),
                    null,
                    null
            );
        }
        return new Objective(
                Kind.COMPLETE,
                Component.translatable("message.mydrugs.inner_objective.explore"),
                null,
                null
        );
    }

    public static Component memoryCompassMessage(ServerPlayer player) {
        if (player == null || !player.level().dimension().equals(InnerDimensions.INNER_LEVEL)) {
            return outsideObjective(player).message();
        }
        ServerLevel level = (ServerLevel) player.level();
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(player.getUUID());
        return memoryCompassMessage(
                data,
                island,
                player.getUUID(),
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
        if (data.hasPendingTrialReturn(owner)) {
            return compassTargetMessage(playerX, playerZ,
                    targets.anchor(Kind.RETURN_TO_ANCHOR, "message.mydrugs.inner_objective.return_anchor"));
        }
        DrugId nearest = InnerTrialManager.nearestIncompleteTrial(island, origin);
        if (nearest != null) {
            return compassTargetMessage(playerX, playerZ, targets.trial(nearest));
        }
        if (island.integratedDrugCount() < 9 && allIntegratedTrialsCompleted(island)) {
            return Component.translatable("message.mydrugs.memory_compass.wait_integration");
        }
        if (island.allInnerTrialsCompleted() && !data.isSpiralCourtPlaced(owner)) {
            return compassTargetMessage(playerX, playerZ,
                    targets.anchor(Kind.OPEN_SPIRAL_COURT, "message.mydrugs.inner_objective.open_spiral"));
        }
        if (data.isSpiralCourtPlaced(owner)
                && !data.hasProgressMarker(owner, InnerDimensionConstants.MARKER_SPIRAL_COMPLETED)) {
            return compassTargetMessage(playerX, playerZ,
                    targets.anchor(Kind.COMPLETE_SPIRAL_COURT, "message.mydrugs.inner_objective.complete_spiral"));
        }
        return Component.translatable("message.mydrugs.memory_compass.rest");
    }

    private static Objective outsideObjective(@Nullable ServerPlayer player) {
        if (player == null) {
            return new Objective(
                    Kind.ENTER,
                    Component.translatable("message.mydrugs.inner_objective.unavailable"),
                    null,
                    null
            );
        }
        String key = switch (InnerDimensionService.openStatus(player)) {
            case READY -> "message.mydrugs.inner_objective.enter_ready";
            case MISSING_INTEGRATION -> "message.mydrugs.inner_objective.need_integration";
            case MISSING_DREAM_ALIGNMENT -> "message.mydrugs.inner_objective.need_alignment";
            case UNAVAILABLE -> "message.mydrugs.inner_objective.unavailable";
        };
        return new Objective(Kind.ENTER, Component.translatable(key), null, null);
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

    private static boolean allIntegratedTrialsCompleted(InnerDimensionSavedData.IslandState island) {
        return island.allIntegratedTrialsCompleted();
    }
}
