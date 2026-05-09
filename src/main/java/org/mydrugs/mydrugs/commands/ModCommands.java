package org.mydrugs.mydrugs.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.effect.EffectCategory;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionDebugOpenPayload;
import org.mydrugs.mydrugs.effects.payloads.DrugVisualPayload;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ModCommands {
    private ModCommands() {
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("mydrugs")
                        .then(Commands.literal("shader")
                                .requires(source -> source.getEntity() instanceof ServerPlayer player && player.isCreative())
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
                                .requires(source -> source.getEntity() instanceof ServerPlayer player && player.isCreative())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    PacketDistributor.sendToPlayer(player, AddictionDebugOpenPayload.from(player));
                                    return 1;
                                })
                        )
                        .then(debugCommand())
        );
        event.getDispatcher().register(
                Commands.literal("mydugs")
                        .then(debugCommand())
        );
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
                );
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
                        "Mining speed: mining_speed=%.2f precision=%.2f adrenaline=%.2f multiplier=x%.2f block_break_speed=%.2f",
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
}
