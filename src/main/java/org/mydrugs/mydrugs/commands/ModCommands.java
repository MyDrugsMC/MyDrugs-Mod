package org.mydrugs.mydrugs.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.effect.EffectCategory;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.core.drug.integration.IntegratedTrait;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationService;
import org.mydrugs.mydrugs.core.drug.runtime.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.addiction.network.AddictionDebugOpenPayload;
import org.mydrugs.mydrugs.network.DrugVisualPayload;
import org.mydrugs.mydrugs.entity.InnerDemonSpawnManager;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.InnerDimensions;
import org.mydrugs.mydrugs.dimension.inner.InnerDimensionSystem;
import org.mydrugs.mydrugs.dimension.inner.InnerGenerationMetrics;
import org.mydrugs.mydrugs.dimension.inner.InnerLocation;
import org.mydrugs.mydrugs.dimension.inner.InnerRefreshJob;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.mutation.ActiveMutationStat;
import org.mydrugs.mydrugs.mutation.InfectionState;
import org.mydrugs.mydrugs.mutation.MutationStat;
import org.mydrugs.mydrugs.mutation.PlayerMutationsAttachment;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ModCommands {
    static final int INNER_DIMENSION_ADMIN_PERMISSION_LEVEL = 2;
    static final String BURST_RECREATE_CONFIRM_LITERAL = "confirm";

    private ModCommands() {
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("mydrugs")
                        .then(Commands.literal("shader")
                                .requires(source -> source.hasPermission(2) && source.getEntity() instanceof ServerPlayer)
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestShaderNames(builder))
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            EffectType type = EffectType.bySerializedName(name)
                                                    .filter(effectType -> effectType.getCategory() == EffectCategory.SHADER)
                                                    .orElse(null);
                                            if (type == null) {
                                                context.getSource().sendFailure(Component.translatable(
                                                        "command.mydrugs.shader.invalid",
                                                        name
                                                ));
                                                return 0;
                                            }

                                            PacketDistributor.sendToPlayer(player, new DrugVisualPayload(type, 5 * 20, 1.0F));
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable(
                                                            "command.mydrugs.shader.sent",
                                                            type.serializedName()
                                                    ),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("addiction_debug")
                                .requires(source -> source.hasPermission(2) && source.getEntity() instanceof ServerPlayer)
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    PacketDistributor.sendToPlayer(player, AddictionDebugOpenPayload.from(player));
                                    return 1;
                                })
                        )
                        .then(ProgressionAdminCommands.knowledge())
                        .then(ProgressionAdminCommands.recover())
                        .then(ProgressionAdminCommands.progression())
                        .then(mutationsCommand())
                        .then(infectionCommand())
                        .then(debugCommand())
        );
        event.getDispatcher().register(
                Commands.literal("mydugs")
                        .then(debugCommand())
        );
        event.getDispatcher().register(innerDimensionCommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> mutationsCommand() {
        return Commands.literal("mutations")
                .requires(source -> source.getEntity() instanceof ServerPlayer)
                .executes(context -> showMutations(context.getSource()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> infectionCommand() {
        return Commands.literal("infection")
                .requires(source -> source.getEntity() instanceof ServerPlayer)
                .executes(context -> showInfection(context.getSource()));
    }

    private static int showMutations(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerMutationsAttachment attachment = player.getData(ModAttachments.PLAYER_MUTATIONS.get());
        if (attachment.activeStats().isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.mydrugs.mutations.none"), false);
            return 1;
        }
        source.sendSuccess(() -> Component.translatable("command.mydrugs.mutations.header"), false);
        for (ActiveMutationStat active : attachment.activeStats()) {
            MutationStat stat = MutationStat.bySerializedNameOrNull(active.statId());
            Component name = stat == null
                    ? Component.translatable("mutation.mydrugs.stat.unknown", active.statId())
                    : Component.translatable(stat.translationKey());
            source.sendSuccess(() -> Component.translatable(
                    "command.mydrugs.mutations.entry",
                    name,
                    Math.round(active.currentValue() * 100.0F),
                    Math.round(active.targetValue() * 100.0F),
                    Math.round(active.assimilationProgress() * 100.0F),
                    String.join(", ", active.sourceNames())
            ), false);
        }
        return attachment.activeStats().size();
    }

    private static int showInfection(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        InfectionState infection = player.getData(ModAttachments.PLAYER_MUTATIONS.get()).infection();
        if (!infection.active()) {
            source.sendSuccess(() -> Component.translatable("command.mydrugs.infection.inactive"), false);
            return 1;
        }
        int nextStageTicks = switch (infection.stage()) {
            case 1 -> 20 * 60 * 2;
            case 2 -> 20 * 60 * 5;
            case 3 -> 20 * 60 * 9;
            default -> infection.ticks();
        };
        int remainingSeconds = Math.max(0, nextStageTicks - infection.ticks()) / 20;
        source.sendSuccess(() -> Component.translatable(
                "command.mydrugs.infection.active",
                infection.stage(),
                Math.round(infection.severity() * 100.0F),
                remainingSeconds
        ), false);
        return infection.stage();
    }

    private static LiteralArgumentBuilder<CommandSourceStack> debugCommand() {
        return Commands.literal("debug")
                .requires(source -> source.getEntity() instanceof ServerPlayer player && player.isCreative())
                .then(Commands.literal("mining_speed")
                        .executes(context -> applyMiningSpeed(context.getSource(), 1.0F, 10))
                        .then(Commands.argument("intensity", FloatArgumentType.floatArg(0.0F))
                                .executes(context -> applyMiningSpeed(
                                        context.getSource(),
                                        FloatArgumentType.getFloat(context, "intensity"),
                                        10
                                ))
                                .then(Commands.argument("duration", StringArgumentType.word())
                                        .executes(context -> applyMiningSpeed(
                                                context.getSource(),
                                                FloatArgumentType.getFloat(context, "intensity"),
                                                parseDurationSeconds(StringArgumentType.getString(context, "duration"))
                                        ))
                                )
                        )
                )
                .then(Commands.literal("print_mining_speed")
                        .executes(context -> printMiningSpeed(context.getSource()))
                )
                .then(Commands.literal("spawn_inner_demon")
                        .executes(context -> spawnInnerDemon(context.getSource(), false))
                        .then(Commands.argument("droppable", BoolArgumentType.bool())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> spawnInnerDemon(
                                        context.getSource(),
                                        BoolArgumentType.getBool(context, "droppable")
                                ))
                        )
                )
                .then(Commands.literal("integrate")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("drug", StringArgumentType.word())
                                .suggests((context, builder) -> suggestIntegratableDrugs(builder))
                                .executes(context -> integrateDrug(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "drug")
                                ))
                        )
                )
                .then(Commands.literal("refresh_inner_dimension")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> refreshInnerDimension(context.getSource()))
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> innerDimensionCommand() {
        return Commands.literal("innerdim")
                .requires(source -> source.hasPermission(INNER_DIMENSION_ADMIN_PERMISSION_LEVEL))
                .then(Commands.literal("status")
                        .executes(context -> innerDimensionStatus(context.getSource()))
                )
                .then(Commands.literal("metrics")
                        .executes(context -> innerDimensionMetrics(context.getSource()))
                )
                .then(Commands.literal("refresh_owner")
                        .executes(context -> innerDimensionRefreshOwner(context.getSource()))
                )
                .then(Commands.literal("burst_recreate")
                        .then(Commands.literal(BURST_RECREATE_CONFIRM_LITERAL)
                                .executes(context -> innerDimensionBurstRecreate(context.getSource()))
                        )
                )
                .then(Commands.literal("queue_status")
                        .executes(context -> innerDimensionQueueStatus(context.getSource()))
                )
                .then(Commands.literal("cancel_refresh")
                        .executes(context -> innerDimensionCancelRefresh(context.getSource()))
                )
                .then(Commands.literal("locate_landmark")
                        .then(Commands.argument("drug", StringArgumentType.word())
                                .suggests((context, builder) -> suggestCuratedDrugs(builder))
                                .executes(context -> innerDimensionLocateLandmark(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "drug")
                                ))
                        )
                );
    }

    private static int innerDimensionStatus(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel innerLevel = innerLevelOrNull(source);
        if (innerLevel == null) {
            return 0;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(innerLevel);
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(player.getUUID());
        source.sendSuccess(() -> Component.literal(InnerDimensionSystem.debugStatus(
                innerLevel,
                island,
                player.blockPosition()
        )), false);
        return 1;
    }

    private static int innerDimensionMetrics(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        source.sendSuccess(() -> Component.literal(InnerDimensionSystem.lastMetricsFor(player.getUUID())), false);
        return 1;
    }

    private static int innerDimensionRefreshOwner(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel innerLevel = innerLevelOrNull(source);
        if (innerLevel == null) {
            return 0;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(innerLevel);
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(player.getUUID());
        InnerRefreshJob job = InnerDimensionSystem.recreateOwnerDebug(innerLevel, island);
        source.sendSuccess(() -> Component.literal("Queued destructive Inner Dimension owner recreate: chunks="
                + job.enqueuedChunks() + ", replaced_existing_queue=" + job.replacedExistingQueue() + "."), true);
        return Math.max(1, job.enqueuedChunks());
    }

    private static int innerDimensionBurstRecreate(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel innerLevel = innerLevelOrNull(source);
        if (innerLevel == null) {
            return 0;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(innerLevel);
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(player.getUUID());
        InnerGenerationMetrics metrics = InnerDimensionSystem.burstRecreateOwnerDebug(innerLevel, island);
        source.sendSuccess(() -> Component.literal("Destructive Inner Dimension burst recreate completed. "
                + metrics.toDebugString()
                + ". This did not preserve player builds inside the generated island radius."), true);
        return Math.max(1, metrics.processedChunks());
    }

    private static int innerDimensionQueueStatus(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        source.sendSuccess(() -> Component.literal(InnerDimensionSystem.queueStatus(player.getUUID())), false);
        return 1;
    }

    private static int innerDimensionCancelRefresh(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        boolean cancelled = InnerDimensionSystem.cancelRefresh(player.getUUID());
        source.sendSuccess(() -> Component.literal(cancelled
                ? "Cancelled Inner Dimension overlay refresh queue."
                : "No Inner Dimension overlay refresh queue was active."), true);
        return cancelled ? 1 : 0;
    }

    private static int innerDimensionLocateLandmark(CommandSourceStack source, String drugName) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        DrugId drugId = DrugId.bySerializedNameOrNull(drugName);
        if (drugId == null || !CuratedDrugChain.ORDER.contains(drugId)) {
            source.sendFailure(Component.literal("No curated Inner Dimension region named '" + drugName + "'."));
            return 0;
        }
        ServerLevel innerLevel = innerLevelOrNull(source);
        if (innerLevel == null) {
            return 0;
        }
        InnerDimensionSavedData.IslandState island = InnerDimensionSavedData.get(innerLevel).getOrCreateIsland(player.getUUID());
        InnerLocation location = InnerDimensionSystem.locateLandmark(island, drugId);
        BlockPos pos = location.pos();
        source.sendSuccess(() -> Component.literal("Inner Dimension " + drugId.serializedName() + " "
                + location.kind() + ": " + pos.getX() + " " + pos.getY() + " " + pos.getZ()), false);
        return 1;
    }

    private static ServerLevel innerLevelOrNull(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel innerLevel = player.level().getServer().getLevel(InnerDimensions.INNER_LEVEL);
        if (innerLevel == null) {
            source.sendFailure(Component.literal("Inner Dimension level is not available."));
        }
        return innerLevel;
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestCuratedDrugs(
            SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (DrugId drugId : CuratedDrugChain.ORDER) {
            String name = drugId.serializedName();
            if (name.startsWith(remaining)) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }

    private static int refreshInnerDimension(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel innerLevel = player.level().getServer().getLevel(InnerDimensions.INNER_LEVEL);
        if (innerLevel == null) {
            source.sendFailure(Component.literal("Inner Dimension level is not available."));
            return 0;
        }

        InnerDimensionSavedData data = InnerDimensionSavedData.get(innerLevel);
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(player.getUUID());
        InnerRefreshJob job = InnerDimensionSystem.recreateOwnerDebug(innerLevel, island);
        source.sendSuccess(
                () -> Component.literal("Queued destructive Inner Dimension recreate from saved integrations (chunks="
                        + job.enqueuedChunks() + ", replaced_existing_queue=" + job.replacedExistingQueue() + ")."),
                true
        );
        return Math.max(1, job.enqueuedChunks());
    }

    private static int integrateDrug(CommandSourceStack source, String drugName) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        DrugId drugId = DrugId.bySerializedNameOrNull(drugName);
        if (drugId == null || !IntegrationService.forceIntegrateUnsafe(player, drugId)) {
            source.sendFailure(Component.literal("No integratable drug named '" + drugName + "'."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Force-integrated " + drugId.serializedName() + "."), false);
        return 1;
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestIntegratableDrugs(
            SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (IntegratedTrait trait : IntegratedTrait.values()) {
            String name = trait.source().serializedName();
            if (name.startsWith(remaining)) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }

    private static int spawnInnerDemon(CommandSourceStack source, boolean droppable) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!InnerDemonSpawnManager.spawnDebug(player, droppable)) {
            source.sendFailure(Component.literal("Could not spawn inner demon."));
            return 0;
        }
        source.sendSuccess(
                () -> Component.literal(droppable
                        ? "Spawned a droppable test Inner Demon."
                        : "Spawned a non-droppable test Inner Demon."),
                false
        );
        return 1;
    }

    private static int applyMiningSpeed(CommandSourceStack source, float intensity, int durationSeconds)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int durationTicks = durationSeconds * 20;
        DrugEffectRuntimeManager.addEffect(player, EffectType.MINING_SPEED, intensity, durationTicks);
        float multiplier = DrugEffectRuntimeManager.getMiningSpeedMultiplier(player);
        source.sendSuccess(
                () -> Component.literal(String.format(
                        Locale.ROOT,
                        "Applied mining_speed intensity %.2f for %ds. Current multiplier: x%.2f",
                        intensity,
                        durationSeconds,
                        multiplier
                )),
                false
        );
        return 1;
    }

    private static int parseDurationSeconds(String rawDuration) throws CommandSyntaxException {
        String normalized = rawDuration.toLowerCase(Locale.ROOT);
        String numeric = normalized;
        if (numeric.endsWith("seconds")) {
            numeric = numeric.substring(0, numeric.length() - "seconds".length());
        } else if (numeric.endsWith("secs")) {
            numeric = numeric.substring(0, numeric.length() - "secs".length());
        } else if (numeric.endsWith("sec")) {
            numeric = numeric.substring(0, numeric.length() - "sec".length());
        } else if (numeric.endsWith("s")) {
            numeric = numeric.substring(0, numeric.length() - 1);
        }

        try {
            int seconds = Integer.parseInt(numeric);
            if (seconds >= 1) {
                return seconds;
            }
        } catch (NumberFormatException ignored) {
            // Fall through to the command error below.
        }

        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().create(rawDuration);
    }

    private static int printMiningSpeed(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        float mining = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.MINING_SPEED);
        float precision = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.PRECISION);
        float adrenaline = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.ADRENALINE_SURGE);
        float multiplier = DrugEffectRuntimeManager.getMiningSpeedMultiplier(mining, precision, adrenaline);
        double attribute = player.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
        source.sendSuccess(
                () -> Component.literal(String.format(
                        Locale.ROOT,
                        "Mining speed: mining_speed=%.2f precision=%.2f adrenaline=%.2f formula=1+mining+precision*0.20+adrenaline*0.45 multiplier=x%.2f block_break_speed=%.2f",
                        mining,
                        precision,
                        adrenaline,
                        multiplier,
                        attribute
                )),
                false
        );
        return 1;
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestShaderNames(
            SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining().toLowerCase();

        for (EffectType effectType : EffectType.values()) {
            if (effectType.getCategory() == EffectCategory.SHADER) {
                String path = effectType.serializedName();
                if (path.startsWith(remaining)) {
                    builder.suggest(path);
                }
            }
        }

        return builder.buildFuture();
    }

    static boolean burstRecreateRequiresConfirmForTest() {
        return "confirm".equals(BURST_RECREATE_CONFIRM_LITERAL)
                && INNER_DIMENSION_ADMIN_PERMISSION_LEVEL >= 2;
    }
}
