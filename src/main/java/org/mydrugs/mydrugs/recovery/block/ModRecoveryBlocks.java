package org.mydrugs.mydrugs.recovery.block;

import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import org.mydrugs.mydrugs.blocks.ModBlocks;

public final class ModRecoveryBlocks {
    public static final DeferredBlock<TherapistDeskBlock> THERAPIST_DESK =
            ModBlocks.BLOCKS.registerBlock("therapist_desk", props -> new TherapistDeskBlock(props.strength(2.5F)));
    public static final DeferredItem<BlockItem> THERAPIST_DESK_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(THERAPIST_DESK);

    public static final DeferredBlock<RecoveryAnchorBlock> RECOVERY_ANCHOR =
            ModBlocks.BLOCKS.registerBlock("recovery_anchor", props -> new RecoveryAnchorBlock(props.strength(2.0F).lightLevel(state -> 5)));
    public static final DeferredItem<BlockItem> RECOVERY_ANCHOR_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(RECOVERY_ANCHOR);

    public static final DeferredBlock<DiscScriberBlock> DISC_SCRIBER =
            ModBlocks.BLOCKS.registerBlock("disc_scriber", props -> new DiscScriberBlock(props.strength(2.2F)));
    public static final DeferredItem<BlockItem> DISC_SCRIBER_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(DISC_SCRIBER);

    public static final DeferredBlock<RecoveryJukeboxBlock> RECOVERY_JUKEBOX =
            ModBlocks.BLOCKS.registerBlock("recovery_jukebox", props -> new RecoveryJukeboxBlock(props.strength(2.0F).lightLevel(state -> 6)));
    public static final DeferredItem<BlockItem> RECOVERY_JUKEBOX_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(RECOVERY_JUKEBOX);

    private ModRecoveryBlocks() {
    }
}
