package org.mydrugs.mydrugs.pipe.item;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mydrugs.mydrugs.pipe.PipeConnectionMode;
import org.mydrugs.mydrugs.pipe.PipeSideSelector;
import org.mydrugs.mydrugs.pipe.blockentity.PipeBlockEntity;

import java.util.Locale;

public class PipeWrenchItem extends Item {
    public PipeWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof PipeBlockEntity pipe)) {
            return InteractionResult.PASS;
        }

        Direction side = PipeSideSelector.selectSide(context);

        if (!context.getLevel().isClientSide()) {
            PipeConnectionMode mode;
            if (context.getPlayer() != null && context.getPlayer().isCrouching()) {
                mode = pipe.disableSide(side);
            } else {
                mode = pipe.cycleSide(side);
            }

            Player player = context.getPlayer();
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable(
                                "message.mydrugs.pipe.side_mode",
                                Component.translatable("direction.mydrugs." + side.getSerializedName()),
                                Component.translatable("pipe_mode.mydrugs." + mode.name().toLowerCase(Locale.ROOT))
                        ),
                        true
                );
            }

            context.getLevel().playSound(
                    null,
                    context.getClickedPos(),
                    SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.BLOCKS,
                    0.35F,
                    1.4F
            );
        }

        return InteractionResult.SUCCESS;
    }
}