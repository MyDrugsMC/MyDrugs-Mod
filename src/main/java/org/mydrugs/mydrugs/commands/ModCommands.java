package org.mydrugs.mydrugs.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.mydrugs.mydrugs.client.shaders.ShaderManager;
import org.mydrugs.mydrugs.core.drug.effect.EffectCategory;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = "mydrugs")
public final class ModCommands {
    private static final List<String> DRUG_NAMES = List.of(
            "ergot",
            "magic_mushroom",
            "magic_mushroom_powder",
            "rye"
    );

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("mydrugs")
                        .then(Commands.literal("shader")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestDrugNames(builder))
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");
                                            EffectType type = EffectType.valueOf(name);
                                            ShaderManager.INSTANCE.add(5 * 20, type);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("You selected: " + name),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )
        );
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestDrugNames(
            SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining().toLowerCase();

        for (EffectType effectType : EffectType.values()) {
            if (effectType.getCategory() == EffectCategory.SHADER) {
                String path = effectType.name();
                if (path.toLowerCase().startsWith(remaining)) {
                    builder.suggest(path);
                }
            }
        }

        return builder.buildFuture();
    }
}