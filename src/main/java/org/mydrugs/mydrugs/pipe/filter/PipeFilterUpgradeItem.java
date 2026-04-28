package org.mydrugs.mydrugs.pipe.filter;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.menu.ModMenus;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.PipeSideSelector;
import org.mydrugs.mydrugs.pipe.blockentity.PipeBlockEntity;

import java.util.function.Consumer;

public class PipeFilterUpgradeItem extends Item {
    public PipeFilterUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            openFilterMenu(player);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof PipeBlockEntity pipe)) {
            if (player != null && player.isSecondaryUseActive()) {
                if (!context.getLevel().isClientSide()) {
                    openFilterMenu(player);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        PipeFilterConfig filter = stack.get(ModDataComponents.PIPE_FILTER_CONFIG.get());
        if (filter == null || filter.kind() != pipe.kind()) {
            filter = PipeFilterConfig.empty(pipe.kind());
        }

        Direction side = PipeSideSelector.selectSide(context);

        if (!context.getLevel().isClientSide()) {
            pipe.applyFilter(side, filter);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.translatable(
                                "message.mydrugs.pipe_filter.applied",
                                Component.translatable("direction.mydrugs." + side.getSerializedName()),
                                Component.translatable("pipe.mydrugs.kind." + filter.kind().serializedName()),
                                Component.translatable("pipe.mydrugs.filter_mode." + filter.mode().serializedName()),
                                filter.entries().size()
                        ),
                        true
                );
            }
        }

        return InteractionResult.SUCCESS;
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
        PipeFilterConfig filter = stack.getOrDefault(ModDataComponents.PIPE_FILTER_CONFIG.get(), defaultConfig());
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.pipe_filter.kind",
                Component.translatable("pipe.mydrugs.kind." + filter.kind().serializedName())).withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.pipe_filter.mode",
                Component.translatable("pipe.mydrugs.filter_mode." + filter.mode().serializedName())).withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.pipe_filter.entries", filter.entries().size()).withStyle(ChatFormatting.DARK_GRAY));
    }

    public static PipeFilterConfig defaultConfig() {
        return PipeFilterConfig.empty(PipeResourceKind.ITEM);
    }

    private static void openFilterMenu(Player player) {
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, playerInventory, p) -> new PipeFilterMenu(containerId, playerInventory),
                Component.translatable("menu.mydrugs.pipe_filter")
        );
        player.openMenu(provider);
    }
}
