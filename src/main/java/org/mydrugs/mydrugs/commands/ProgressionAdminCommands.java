package org.mydrugs.mydrugs.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;
import org.mydrugs.mydrugs.progression.PsyKnowledgeManager;

/**
 * Admin/rescue commands grafted onto the {@code /mydrugs} tree by {@link ModCommands}.
 *
 * <p>Permissions: {@code diagnose} and {@code knowledge list} are read-only (level 2);
 * {@code grant}, {@code revoke}, {@code recover}, and {@code unlock_step} mutate player
 * progression and require level 3+. Brigadier permission gates mean there is no default
 * survival access unless the server has cheats/op enabled.
 */
final class ProgressionAdminCommands {
    private static final int PERM_READ = 2;
    private static final int PERM_WRITE = 3;
    private static final int DEFAULT_WIRE_AMOUNT = 2;

    private static final SuggestionProvider<CommandSourceStack> KNOWLEDGE_SUGGESTIONS = (context, builder) -> {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (PsyKnowledgeKey key : PsyKnowledgeKey.ORDERED) {
            String path = key.id().getPath();
            if (path.startsWith(remaining)) {
                builder.suggest(path);
            }
        }
        return builder.buildFuture();
    };

    private ProgressionAdminCommands() {
    }

    private static boolean commandsEnabled() {
        return Config.SERVER.enableRecoveryCommands.get();
    }

    static LiteralArgumentBuilder<CommandSourceStack> knowledge() {
        return Commands.literal("knowledge")
                .requires(source -> commandsEnabled())
                .then(Commands.literal("grant")
                        .requires(source -> source.hasPermission(PERM_WRITE))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .suggests(KNOWLEDGE_SUGGESTIONS)
                                        .executes(ProgressionAdminCommands::grantKnowledge))))
                .then(Commands.literal("revoke")
                        .requires(source -> source.hasPermission(PERM_WRITE))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .suggests(KNOWLEDGE_SUGGESTIONS)
                                        .executes(ProgressionAdminCommands::revokeKnowledge))))
                .then(Commands.literal("list")
                        .requires(source -> source.hasPermission(PERM_READ))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ProgressionAdminCommands::listKnowledge)));
    }

    static LiteralArgumentBuilder<CommandSourceStack> recover() {
        return Commands.literal("recover")
                .requires(source -> commandsEnabled() && source.hasPermission(PERM_WRITE))
                .then(Commands.literal("psy_receptacle")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ProgressionAdminCommands::recoverReceptacle)))
                .then(Commands.literal("wires")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> recoverWires(context, DEFAULT_WIRE_AMOUNT))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                        .executes(context -> recoverWires(
                                                context, IntegerArgumentType.getInteger(context, "amount"))))));
    }

    static LiteralArgumentBuilder<CommandSourceStack> progression() {
        return Commands.literal("progression")
                .requires(source -> commandsEnabled())
                .then(Commands.literal("diagnose")
                        .requires(source -> source.hasPermission(PERM_READ))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ProgressionAdminCommands::diagnose)))
                .then(Commands.literal("unlock_step")
                        .requires(source -> source.hasPermission(PERM_WRITE))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("step", StringArgumentType.word())
                                        .suggests(KNOWLEDGE_SUGGESTIONS)
                                        .executes(ProgressionAdminCommands::unlockStep))));
    }

    // --- knowledge ---------------------------------------------------------

    private static int grantKnowledge(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        PsyKnowledgeKey key = resolveKey(context.getSource(), StringArgumentType.getString(context, "key"));
        if (key == null) {
            return 0;
        }
        boolean granted = PsyKnowledgeManager.grant(target, key);
        String name = target.getName().getString();
        String path = key.id().getPath();
        if (granted) {
            context.getSource().sendSuccess(
                    () -> Component.literal("Granted knowledge " + path + " to " + name), true);
            return 1;
        }
        context.getSource().sendSuccess(
                () -> Component.literal(name + " already knows " + path), false);
        return 0;
    }

    private static int revokeKnowledge(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        PsyKnowledgeKey key = resolveKey(context.getSource(), StringArgumentType.getString(context, "key"));
        if (key == null) {
            return 0;
        }
        boolean revoked = PsyKnowledgeManager.revoke(target, key);
        String name = target.getName().getString();
        String path = key.id().getPath();
        if (revoked) {
            context.getSource().sendSuccess(
                    () -> Component.literal("Revoked knowledge " + path + " from " + name), true);
            return 1;
        }
        context.getSource().sendSuccess(
                () -> Component.literal(name + " did not have " + path), false);
        return 0;
    }

    private static int listKnowledge(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        Set<PsyKnowledgeKey> known = PsyKnowledgeManager.getKnown(target);
        String name = target.getName().getString();
        List<String> ordered = new ArrayList<>();
        for (PsyKnowledgeKey key : PsyKnowledgeKey.ORDERED) {
            if (known.contains(key)) {
                ordered.add(key.id().getPath());
            }
        }
        String summary = ordered.isEmpty() ? "(none)" : String.join(", ", ordered);
        context.getSource().sendSuccess(
                () -> Component.literal("Knowledge for " + name + " (" + ordered.size() + "/"
                        + PsyKnowledgeKey.ORDERED.size() + "): " + summary), false);
        return ordered.size();
    }

    // --- recover -----------------------------------------------------------

    private static int recoverReceptacle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        String name = target.getName().getString();
        Item receptacle = ModItems.PSY_RECEPTACLE.get();
        if (hasItemInInventory(target, receptacle) || hasItemInEnderChest(target, receptacle)) {
            context.getSource().sendSuccess(
                    () -> Component.literal(name + " already has a Psy Receptacle — none given"), false);
            return 0;
        }
        giveItem(target, new ItemStack(receptacle));
        context.getSource().sendSuccess(
                () -> Component.literal("Gave a Psy Receptacle to " + name), true);
        return 1;
    }

    private static int recoverWires(CommandContext<CommandSourceStack> context, int amount)
            throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        String name = target.getName().getString();
        giveItem(target, new ItemStack(ModItems.INSULATED_WIRE.get(), amount));
        context.getSource().sendSuccess(
                () -> Component.literal("Gave " + amount + " Centrifuge Wire(s) to " + name), true);
        return amount;
    }

    // --- progression -------------------------------------------------------

    private static int diagnose(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        String name = target.getName().getString();
        Set<PsyKnowledgeKey> known = PsyKnowledgeManager.getKnown(target);

        StringBuilder report = new StringBuilder("MyDrugs progression diagnosis for " + name + ":");
        for (PsyKnowledgeKey key : PsyKnowledgeKey.ORDERED) {
            report.append("\n- Has ")
                    .append(key.id().getPath().toUpperCase(Locale.ROOT))
                    .append(": ")
                    .append(yesNo(known.contains(key)));
        }

        boolean receptacleInInventory = hasItemInInventory(target, ModItems.PSY_RECEPTACLE.get());
        boolean receptacleInEnderChest = hasItemInEnderChest(target, ModItems.PSY_RECEPTACLE.get());
        boolean canCraftWires = known.contains(PsyKnowledgeKey.FERMENTED);
        report.append("\n- Psy Receptacle in inventory: ").append(yesNo(receptacleInInventory));
        report.append("\n- Psy Receptacle in ender chest: ").append(yesNo(receptacleInEnderChest));
        report.append("\n- Can craft Centrifuge Wires: ").append(yesNo(canCraftWires));
        report.append("\n- Suggested rescue: ").append(suggestRescue(
                name, known, receptacleInInventory || receptacleInEnderChest, target));

        context.getSource().sendSuccess(() -> Component.literal(report.toString()), false);
        return 1;
    }

    private static String suggestRescue(String name, Set<PsyKnowledgeKey> known,
                                        boolean hasReceptacle, ServerPlayer target) {
        if (known.contains(PsyKnowledgeKey.CAFFEINE) && !hasReceptacle) {
            return "/mydrugs recover psy_receptacle " + name;
        }
        boolean hasWires = hasItemInInventory(target, ModItems.INSULATED_WIRE.get())
                || hasItemInEnderChest(target, ModItems.INSULATED_WIRE.get());
        if (known.contains(PsyKnowledgeKey.FERMENTED) && !hasWires) {
            return "/mydrugs recover wires " + name;
        }
        PsyKnowledgeKey gap = firstMissingBeforeProgress(known);
        if (gap != null) {
            return "/mydrugs progression unlock_step " + name + " " + gap.id().getPath();
        }
        return "none — progression looks consistent";
    }

    /** Earliest ordered key the player lacks while already holding a later one (a stuck gap). */
    private static @Nullable PsyKnowledgeKey firstMissingBeforeProgress(Set<PsyKnowledgeKey> known) {
        int highestKnown = -1;
        for (int i = 0; i < PsyKnowledgeKey.ORDERED.size(); i++) {
            if (known.contains(PsyKnowledgeKey.ORDERED.get(i))) {
                highestKnown = i;
            }
        }
        for (int i = 0; i < highestKnown; i++) {
            PsyKnowledgeKey key = PsyKnowledgeKey.ORDERED.get(i);
            if (!known.contains(key)) {
                return key;
            }
        }
        return null;
    }

    private static int unlockStep(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        PsyKnowledgeKey step = resolveKey(context.getSource(), StringArgumentType.getString(context, "step"));
        if (step == null) {
            return 0;
        }
        String name = target.getName().getString();
        int stepIndex = PsyKnowledgeKey.ORDERED.indexOf(step);
        List<String> newlyGranted = new ArrayList<>();
        for (int i = 0; i <= stepIndex; i++) {
            PsyKnowledgeKey key = PsyKnowledgeKey.ORDERED.get(i);
            if (PsyKnowledgeManager.grant(target, key)) {
                newlyGranted.add(key.id().getPath());
            }
        }
        if (newlyGranted.isEmpty()) {
            context.getSource().sendSuccess(
                    () -> Component.literal(name + " already had every step up to "
                            + step.id().getPath()), false);
            return 0;
        }
        context.getSource().sendSuccess(
                () -> Component.literal("Unlocked steps for " + name + ": "
                        + String.join(", ", newlyGranted)), true);
        return newlyGranted.size();
    }

    // --- helpers -----------------------------------------------------------

    private static @Nullable PsyKnowledgeKey resolveKey(CommandSourceStack source, String raw) {
        String path = raw;
        if (raw.contains(":")) {
            ResourceLocation parsed = ResourceLocation.tryParse(raw);
            path = parsed == null ? raw : parsed.getPath();
        }
        for (PsyKnowledgeKey key : PsyKnowledgeKey.ORDERED) {
            if (key.id().getPath().equalsIgnoreCase(path)) {
                return key;
            }
        }
        source.sendFailure(Component.literal("Unknown knowledge key: " + raw));
        return null;
    }

    private static boolean hasItemInInventory(ServerPlayer player, Item item) {
        return player.getInventory().contains(new ItemStack(item));
    }

    private static boolean hasItemInEnderChest(ServerPlayer player, Item item) {
        PlayerEnderChestContainer enderChest = player.getEnderChestInventory();
        for (int slot = 0; slot < enderChest.getContainerSize(); slot++) {
            if (enderChest.getItem(slot).is(item)) {
                return true;
            }
        }
        return false;
    }

    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }
}
