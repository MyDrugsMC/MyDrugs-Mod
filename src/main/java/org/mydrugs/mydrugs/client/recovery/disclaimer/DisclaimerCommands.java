package org.mydrugs.mydrugs.client.recovery.disclaimer;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.mydrugs.mydrugs.MyDrugs;

/**
 * Client command to re-open or reset the content notice — the reset option for testing/admins.
 *
 * <ul>
 *   <li>{@code /mydrugs disclaimer} re-opens the notice screen;</li>
 *   <li>{@code /mydrugs disclaimer reset} clears the acknowledgement so it shows again next join.</li>
 * </ul>
 */
@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class DisclaimerCommands {
    private DisclaimerCommands() {
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> disclaimer =
                net.minecraft.commands.Commands.literal("disclaimer")
                        .executes(ctx -> {
                            Minecraft.getInstance().execute(() ->
                                    Minecraft.getInstance().setScreen(new DisclaimerScreen(true)));
                            return 1;
                        })
                        .then(net.minecraft.commands.Commands.literal("reset")
                                .executes(ctx -> {
                                    FirstWorldDisclaimerHandler.resetAcknowledgementForNextJoin();
                                    ctx.getSource().sendSuccess(() ->
                                            Component.translatable("message.mydrugs.disclaimer.reset"), false);
                                    return 1;
                                }));

        event.getDispatcher().register(
                net.minecraft.commands.Commands.literal("mydrugs").then(disclaimer));
    }
}
