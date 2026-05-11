package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.network.PsyBlueprintPreviewPayload;

import java.util.ArrayList;
import java.util.List;

public final class PsyBlueprintPreviewService {
    public static final int PREVIEW_TICKS = 20 * 30;

    private PsyBlueprintPreviewService() {
    }

    public static boolean sendForBlueprint(ServerPlayer player, Level level, BlockPos clicked, Direction facing) {
        return sendPreview(player, level, clicked, facing, false);
    }

    public static boolean sendForPsychedelicInsight(ServerPlayer player, Level level, BlockPos clicked, Direction facing) {
        return sendPreview(player, level, clicked, facing, true);
    }

    private static boolean sendPreview(ServerPlayer player, Level level, BlockPos clicked, Direction facing, boolean requireInsight) {
        if (requireInsight && !hasPsychedelicInsight(player)) {
            return false;
        }

        BlockState clickedState = level.getBlockState(clicked);
        List<PsyBlueprintPreviewPayload.Entry> entries;
        if (clickedState.is(ModBlocks.PSYCHOTROPE_CORE.get())) {
            entries = psychotropeEntries(level, clicked);
        } else if (clickedState.is(ModBlocks.PAINTED_CLAY_BOWL.get())) {
            entries = psyMixerEntries(level, clicked, facing);
        } else {
            return false;
        }

        PacketDistributor.sendToPlayer(player, new PsyBlueprintPreviewPayload(
                level.dimension().location(),
                clicked,
                entries,
                PREVIEW_TICKS
        ));

        player.displayClientMessage(Component.translatable(
                entries.isEmpty() ? "message.mydrugs.psy_blueprint.complete" : "message.mydrugs.psy_blueprint.preview",
                entries.size()
        ), true);
        return true;
    }

    private static boolean hasPsychedelicInsight(ServerPlayer player) {
        float focus = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.RITUAL_FOCUS);
        float warp = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.ACID_WARP);
        float vision = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.MULTIBLOCK_VISION);
        if (vision >= 0.5F) {
            return true;
        }
        return Math.max(focus, warp) >= 1.25F;
    }

    private static List<PsyBlueprintPreviewPayload.Entry> psychotropeEntries(Level level, BlockPos corePos) {
        List<PsyBlueprintPreviewPayload.Entry> entries = new ArrayList<>();
        Block expected = ModBlocks.PSYCHOTROPE_COMPONENT.get();
        for (BlockPos offset : PsychotropeMultiblock.componentOffsets()) {
            BlockPos pos = corePos.offset(offset);
            BlockState current = level.getBlockState(pos);
            if (!current.is(expected)) {
                entries.add(entry(pos, expected, isWrongBlock(current)));
            }
        }
        return entries;
    }

    private static List<PsyBlueprintPreviewPayload.Entry> psyMixerEntries(Level level, BlockPos bowlPos, Direction facing) {
        List<PsyBlueprintPreviewPayload.Entry> entries = new ArrayList<>();
        for (PsyMixerMultiblock.Slot slot : PsyMixerMultiblock.template()) {
            if (slot.expected() == null) {
                continue;
            }
            Block expected = slot.expected().get();
            BlockPos pos = bowlPos.offset(PsyMixerMultiblock.rotate(slot.localOffset(), facing));
            BlockState current = level.getBlockState(pos);
            if (!current.is(expected)) {
                entries.add(entry(pos, expected, isWrongBlock(current)));
            }
        }
        return entries;
    }

    private static boolean isWrongBlock(BlockState current) {
        return !current.isAir();
    }

    private static PsyBlueprintPreviewPayload.Entry entry(BlockPos pos, Block expected, boolean wrongBlock) {
        return new PsyBlueprintPreviewPayload.Entry(pos.immutable(), BuiltInRegistries.BLOCK.getKey(expected), wrongBlock);
    }
}
