package org.mydrugs.mydrugs.recovery;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.network.PersonalDiscPlaybackPayload;
import org.mydrugs.mydrugs.recovery.item.PersonalMusicDiscItem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class VanillaPersonalJukeboxEvents {
    private static final Map<ServerLevel, Map<BlockPos, ItemStack>> ACTIVE = new WeakHashMap<>();

    private VanillaPersonalJukeboxEvents() {
    }

    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK) {
            return;
        }

        UseOnContext context = event.getUseOnContext();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(Blocks.JUKEBOX) || !(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox)) {
            return;
        }

        ItemStack held = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null) {
            return;
        }

        if (PersonalMusicDiscItem.isPersonalDisc(held) && jukebox.getTheItem().isEmpty()) {
            event.cancelWithResult(InteractionResult.SUCCESS_SERVER);
            if (level.isClientSide()) {
                return;
            }
            ItemStack disc = held.copyWithCount(1);
            jukebox.setTheItem(disc);
            level.setBlock(pos, level.getBlockState(pos).setValue(JukeboxBlock.HAS_RECORD, true), 3);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            start((ServerLevel) level, pos, disc);
            return;
        }

        if (held.isEmpty() && PersonalMusicDiscItem.isPersonalDisc(jukebox.getTheItem())) {
            event.cancelWithResult(InteractionResult.SUCCESS_SERVER);
            if (level.isClientSide()) {
                return;
            }
            stop((ServerLevel) level, pos);
            jukebox.popOutTheItem();
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Map<BlockPos, ItemStack> active = ACTIVE.get(level);
        if (active == null || active.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<BlockPos, ItemStack>> iterator = active.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, ItemStack> entry = iterator.next();
            BlockPos pos = entry.getKey();
            if (!level.getBlockState(pos).is(Blocks.JUKEBOX)
                    || !(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox)
                    || !PersonalMusicDiscItem.isPersonalDisc(jukebox.getTheItem())) {
                PersonalDiscPlaybackManager.stop(level, pos, PersonalDiscPlaybackPayload.Source.VANILLA_JUKEBOX);
                iterator.remove();
                continue;
            }
            if (level.getGameTime() % 100L == 0L) {
                PersonalDiscPlaybackManager.start(level, pos, jukebox.getTheItem(), PersonalDiscPlaybackPayload.Source.VANILLA_JUKEBOX, level.getGameTime());
            }
        }
    }

    private static void start(ServerLevel level, BlockPos pos, ItemStack disc) {
        ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>()).put(pos.immutable(), disc.copy());
        PersonalDiscPlaybackManager.start(level, pos, disc, PersonalDiscPlaybackPayload.Source.VANILLA_JUKEBOX, level.getGameTime());
    }

    private static void stop(ServerLevel level, BlockPos pos) {
        Map<BlockPos, ItemStack> active = ACTIVE.get(level);
        if (active != null) {
            active.remove(pos.immutable());
        }
        PersonalDiscPlaybackManager.stop(level, pos, PersonalDiscPlaybackPayload.Source.VANILLA_JUKEBOX);
    }
}
