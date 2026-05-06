package org.mydrugs.mydrugs.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.effect.EffectCategory;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionDebugOpenPayload;
import org.mydrugs.mydrugs.effects.payloads.DrugVisualPayload;

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
        );
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
