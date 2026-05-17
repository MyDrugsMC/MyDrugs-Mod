package org.mydrugs.mydrugs.advancement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;
import org.mydrugs.mydrugs.psyche.PsycheMapMilestones;

import java.util.Optional;

public final class AdvancementEventHooks {
    private static final double MACHINE_ADVANCEMENT_RADIUS = 8.0D;

    private AdvancementEventHooks() {
    }

    public static void drugConsumed(ServerPlayer player, DrugKnowledgeResult result) {
        ModCriteriaTriggers.DRUG_CONSUMED.get().trigger(player, result);
    }

    public static void psyKnowledgeUnlocked(ServerPlayer player, PsyKnowledgeKey key) {
        ModCriteriaTriggers.PSY_KNOWLEDGE_UNLOCKED.get().trigger(player, key);
    }

    public static void recoveryAction(ServerPlayer player, String action) {
        ModCriteriaTriggers.RECOVERY_ACTION.get().trigger(player, action, "");
        mapRecoveryActionToPsyche(player, action);
    }

    public static void recoveryAction(ServerPlayer player, String action, String category) {
        ModCriteriaTriggers.RECOVERY_ACTION.get().trigger(player, action, category);
        mapRecoveryActionToPsyche(player, action);
    }

    private static void mapRecoveryActionToPsyche(ServerPlayer player, String action) {
        if (player == null || action == null) return;
        switch (action) {
            case "personal_diary" -> PsycheMapMilestones.diaryEntry(player);
            case "safe_zone" -> PsycheMapMilestones.recoveryAnchor(player);
            case "therapy" -> PsycheMapMilestones.therapistVisit(player);
            default -> { /* other actions are not psyche milestones */ }
        }
    }

    public static void machineRecipeCompleted(BlockEntity blockEntity) {
        if (!(blockEntity.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ResourceLocation machine = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock());
        MachineRecipeCompletedTrigger.Event event = new MachineRecipeCompletedTrigger.Event(
                machine,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        forNearbyPlayers(level, blockEntity.getBlockPos(), player ->
                ModCriteriaTriggers.MACHINE_RECIPE_COMPLETED.get().trigger(player, event));
    }

    public static void machineRecipeCompleted(BlockEntity blockEntity, Optional<ResourceLocation> recipe, Optional<ResourceLocation> resultItem) {
        if (!(blockEntity.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ResourceLocation machine = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock());
        MachineRecipeCompletedTrigger.Event event = new MachineRecipeCompletedTrigger.Event(
                machine,
                recipe,
                resultItem,
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        forNearbyPlayers(level, blockEntity.getBlockPos(), player ->
                ModCriteriaTriggers.MACHINE_RECIPE_COMPLETED.get().trigger(player, event));
    }

    public static void psychotropeEvent(ServerLevel level, BlockPos pos, String event, String drug, int amount, int threshold) {
        PsychotropeEnergyTrigger.Event payload = new PsychotropeEnergyTrigger.Event(event, drug, amount, threshold);
        boolean psycheWorthy = "powered_machine".equals(event) || amount > 0;
        forNearbyPlayers(level, pos, player -> {
            ModCriteriaTriggers.PSYCHOTROPE_ENERGY.get().trigger(player, payload);
            if (psycheWorthy) {
                PsycheMapMilestones.psychotropeEnergy(player);
            }
        });
    }

    public static void psychotropePoweredMachine(BlockEntity blockEntity) {
        if (blockEntity.getLevel() instanceof ServerLevel level) {
            psychotropeEvent(level, blockEntity.getBlockPos(), "powered_machine", "", 1, 0);
        }
    }

    private static void forNearbyPlayers(ServerLevel level, BlockPos pos, java.util.function.Consumer<ServerPlayer> consumer) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        double radiusSq = MACHINE_ADVANCEMENT_RADIUS * MACHINE_ADVANCEMENT_RADIUS;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(x, y, z) <= radiusSq) {
                consumer.accept(player);
            }
        }
    }
}
