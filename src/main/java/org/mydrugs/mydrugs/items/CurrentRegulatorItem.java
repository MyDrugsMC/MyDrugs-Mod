package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mydrugs.mydrugs.blocks.entity.DistillateEngineBlockEntity;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachment;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachments;
import org.mydrugs.mydrugs.energy.PsyCurrentConstants;
import org.mydrugs.mydrugs.energy.PsyCurrentDistributor;
import org.mydrugs.mydrugs.energy.PsyCurrentStorage;
import org.mydrugs.mydrugs.energy.psycurrent.PsyCurrentTargetScan;
import org.mydrugs.mydrugs.energy.psycurrent.StrainRisk;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Right-click an engine or a Psy Current-compatible machine with the Current Regulator to read
 * the network status in chat. The same item still works as the engine's strain-reducing slot
 * item; this just adds an inspection interaction so the player has a non-GUI way to ask "what's
 * going on with this network?".
 */
public final class CurrentRegulatorItem extends Item {
    /** Radius around an inspected machine when looking for a nearby engine. Cheap, bounded scan. */
    private static final int NEAREST_ENGINE_SEARCH_RADIUS = PsyCurrentConstants.ENGINE_MAX_RADIUS;

    public CurrentRegulatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(Component.translatable(
                "tooltip.mydrugs.current_regulator"
        ).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            // Server side does the chat — client-side returns SUCCESS so the swing animation plays.
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return InteractionResult.PASS;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        if (blockEntity instanceof DistillateEngineBlockEntity engine) {
            sendEngineReport(serverPlayer, engine);
            return InteractionResult.SUCCESS;
        }
        if (MachineEnergyAttachments.hasEnergyStorage(blockEntity)) {
            sendMachineReport(serverPlayer, serverLevel, blockEntity);
            return InteractionResult.SUCCESS;
        }
        if (MachineEnergyAttachments.supportsEnergyUpgrade(blockEntity)
                || MachineEnergyAttachments.supportsAutomationUpgrade(blockEntity)) {
            serverPlayer.sendSystemMessage(
                    title("item.mydrugs.current_regulator.report.title"));
            serverPlayer.sendSystemMessage(
                    Component.translatable("item.mydrugs.current_regulator.report.missing_upgrade")
                            .withStyle(ChatFormatting.GRAY));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void sendEngineReport(ServerPlayer player, DistillateEngineBlockEntity engine) {
        PsyCurrentTargetScan scan = engine.cachedScan();
        PsyCurrentStorage current = engine.current();
        StrainRisk risk = StrainRisk.forStrain(engine.strain());

        player.sendSystemMessage(title("item.mydrugs.current_regulator.report.engine_title"));
        player.sendSystemMessage(line(
                "item.mydrugs.current_regulator.report.radius",
                Component.literal(Integer.toString(engine.powerRadius()))));
        player.sendSystemMessage(line(
                "item.mydrugs.current_regulator.report.targets",
                Component.literal(formatNumber(scan.validCount()
                        + scan.fullCount()
                        + scan.incompatibleCount()))));
        player.sendSystemMessage(line(
                "item.mydrugs.current_regulator.report.receiving",
                Component.literal(formatNumber(scan.validCount()))));
        player.sendSystemMessage(line(
                "item.mydrugs.current_regulator.report.full",
                Component.literal(formatNumber(scan.fullCount()))));
        player.sendSystemMessage(line(
                "item.mydrugs.current_regulator.report.incompatible",
                Component.literal(formatNumber(scan.incompatibleCount()))));
        player.sendSystemMessage(line(
                "item.mydrugs.current_regulator.report.receivable",
                Component.literal(formatNumber(scan.totalReceivable()) + " PC")));
        player.sendSystemMessage(line(
                "item.mydrugs.current_regulator.report.buffer",
                Component.literal(formatNumber(current.stored())
                        + " / " + formatNumber(current.capacity()) + " PC")));
        Component strainValue = risk.label().copy().withStyle(strainStyle(risk));
        player.sendSystemMessage(line(
                "item.mydrugs.current_regulator.report.strain",
                strainValue));
        if (engine.overloadTicks() > 0) {
            int seconds = (engine.overloadTicks() + 19) / 20;
            player.sendSystemMessage(line(
                    "item.mydrugs.current_regulator.report.overload_cooldown",
                    Component.literal(seconds + "s")));
        }
    }

    private void sendMachineReport(ServerPlayer player, ServerLevel level, BlockEntity blockEntity) {
        MachineEnergyAttachment attachment = MachineEnergyAttachments.get(blockEntity);
        PsyCurrentStorage storage = attachment.storage();
        BlockPos pos = blockEntity.getBlockPos();

        player.sendSystemMessage(title("item.mydrugs.current_regulator.report.machine_title"));
        player.sendSystemMessage(line(
                "item.mydrugs.current_regulator.report.stored",
                Component.literal(formatNumber(storage.stored())
                        + " / " + formatNumber(storage.capacity()) + " PC")));

        boolean full = storage.receive(1, true) <= 0;
        boolean starved = storage.stored() <= 0;
        String stateKey = full
                ? "item.mydrugs.current_regulator.report.state.full"
                : starved
                        ? "item.mydrugs.current_regulator.report.state.empty"
                        : "item.mydrugs.current_regulator.report.state.has_current";
        player.sendSystemMessage(line(
                "item.mydrugs.current_regulator.report.state",
                Component.translatable(stateKey)));

        // Cheap bounded scan: look for any Distillate Engine within engine-max-radius and report
        // the closest one's distance and whether this machine is actually inside its powered radius.
        EngineMatch match = findNearestEngine(level, pos);
        if (match == null) {
            player.sendSystemMessage(line(
                    "item.mydrugs.current_regulator.report.nearest_engine",
                    Component.translatable("item.mydrugs.current_regulator.report.no_engine")
                            .withStyle(ChatFormatting.GRAY)));
            return;
        }
        int distance = (int) Math.ceil(Math.sqrt(pos.distSqr(match.enginePos)));
        Component distanceComponent = Component.literal(distance + "b");
        player.sendSystemMessage(line(
                "item.mydrugs.current_regulator.report.nearest_engine",
                distanceComponent));
        Component inRange = match.inPoweredRadius
                ? Component.translatable("item.mydrugs.current_regulator.report.network_in_range")
                        .withStyle(ChatFormatting.GREEN)
                : Component.translatable("item.mydrugs.current_regulator.report.network_out_of_range")
                        .withStyle(ChatFormatting.YELLOW);
        player.sendSystemMessage(line(
                "item.mydrugs.current_regulator.report.network_state",
                inRange));
    }

    private static EngineMatch findNearestEngine(ServerLevel level, BlockPos origin) {
        int r = NEAREST_ENGINE_SEARCH_RADIUS;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        EngineMatch best = null;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -r; dy <= r; dy++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockEntity be = level.getBlockEntity(cursor);
                    if (!(be instanceof DistillateEngineBlockEntity engine)) {
                        continue;
                    }
                    BlockPos enginePos = engine.getBlockPos();
                    double distSqr = origin.distSqr(enginePos);
                    boolean inRange = isWithinChebyshev(origin, enginePos, engine.powerRadius());
                    if (best == null || distSqr < best.distSqr) {
                        best = new EngineMatch(enginePos.immutable(), distSqr, inRange);
                    }
                }
            }
        }
        return best;
    }

    private static boolean isWithinChebyshev(BlockPos a, BlockPos b, int radius) {
        return Math.abs(a.getX() - b.getX()) <= radius
                && Math.abs(a.getY() - b.getY()) <= radius
                && Math.abs(a.getZ() - b.getZ()) <= radius;
    }

    private static MutableComponent title(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
    }

    private static MutableComponent line(String labelKey, Component value) {
        return Component.translatable(labelKey)
                .withStyle(ChatFormatting.GRAY)
                .append(": ")
                .append(value.copy().withStyle(ChatFormatting.WHITE));
    }

    private static ChatFormatting strainStyle(StrainRisk risk) {
        return switch (risk) {
            case STABLE -> ChatFormatting.GREEN;
            case TENSE -> ChatFormatting.YELLOW;
            case DANGEROUS -> ChatFormatting.GOLD;
            case CRITICAL, OVERLOADED -> ChatFormatting.RED;
        };
    }

    private static String formatNumber(int value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    private record EngineMatch(BlockPos enginePos, double distSqr, boolean inPoweredRadius) {
    }
}
