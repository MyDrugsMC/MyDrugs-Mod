package org.mydrugs.mydrugs.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.PsychotropeMultiblock;
import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;
import org.mydrugs.mydrugs.network.PsyBlueprintPreviewPayload;

import java.util.ArrayList;
import java.util.List;

public final class PsyBlueprintItem extends Item {
    private static final int PREVIEW_TICKS = 20 * 30;

    public PsyBlueprintItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        BlockPos clicked = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clicked);
        List<PsyBlueprintPreviewPayload.Entry> entries;

        if (clickedState.is(ModBlocks.PSYCHOTROPE_CORE.get())) {
            entries = psychotropeEntries(level, clicked);
        } else if (clickedState.is(ModBlocks.PAINTED_CLAY_BOWL.get())) {
            Direction facing = player.getDirection().getOpposite();
            entries = psyMixerEntries(level, clicked, facing);
        } else {
            serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.psy_blueprint.unsupported"), true);
            return InteractionResult.SUCCESS;
        }

        PacketDistributor.sendToPlayer(serverPlayer, new PsyBlueprintPreviewPayload(
                level.dimension().location(),
                clicked,
                entries,
                PREVIEW_TICKS
        ));

        serverPlayer.displayClientMessage(Component.translatable(
                entries.isEmpty() ? "message.mydrugs.psy_blueprint.complete" : "message.mydrugs.psy_blueprint.preview",
                entries.size()
        ), true);
        return InteractionResult.SUCCESS;
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
