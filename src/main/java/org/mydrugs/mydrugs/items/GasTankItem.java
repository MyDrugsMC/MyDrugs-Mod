package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.mydrugs.mydrugs.blocks.GasTankBlock;
import org.mydrugs.mydrugs.gas.*;
import org.mydrugs.mydrugs.items.data.ModDataComponents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class GasTankItem extends BlockItem {
    public GasTankItem(Block block, Item.Properties properties) {
        super(block, properties.stacksTo(1));
        this.block = (GasTankBlock) block;
    }

    private final GasTankBlock block;

    private static final Map<UUID, Long> cowsLastHarvested = new HashMap<>();
    private static final long COW_COOLDOWN_TICK = 20L * 60L; // 1 minute

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        GasTankContents contents = stack.getOrDefault(
                ModDataComponents.GAS_TANK_CONTENTS.get(),
                GasTankContents.EMPTY
        );

        GasType gas = null;
        if (!contents.gasId().isBlank()) {
            gas = ModGases.get(ResourceLocation.parse(contents.gasId()));
        }

        Component gasName = gas == null
                ? Component.translatable("tooltip.mydrugs.empty")
                : Component.translatable("gas." + gas.id().getNamespace() + "." + gas.id().getPath());

        tooltipAdder.accept(
                Component.translatable("tooltip.mydrugs.gas", gasName)
                        .withStyle(ChatFormatting.GRAY)
        );

        tooltipAdder.accept(
                Component.translatable(
                        "tooltip.mydrugs.amount",
                        contents.amount(),
                        GasTankItemHandler.CAPACITY
                ).withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                  LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!(interactionTarget instanceof Cow cow)) {
            return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
        }

        Level level = player.level();

        // Only mutate on the server
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        long now = level.getGameTime(); // better for elapsed cooldown than dayTime()
        Long lastHarvest = cowsLastHarvested.get(cow.getUUID());
        boolean creative = player.getAbilities().instabuild;

        if (!creative && lastHarvest != null && now - lastHarvest < COW_COOLDOWN_TICK) {
            return InteractionResult.PASS;
        }

        IGasHandler gasHandler = stack.getCapability(ModGasCapabilities.ITEM);
        if (gasHandler == null) {
            return InteractionResult.PASS;
        }

        GasStack methane = GasStack.of(ModGases.METHANE, 100);

        // Prevent partial harvests: only do it if the tank can accept all 100 mB
        if (gasHandler.fill(methane, true) != 100) {
            return InteractionResult.PASS;
        }

        gasHandler.fill(methane, false);
        cowsLastHarvested.put(cow.getUUID(), now);

        return InteractionResult.CONSUME;
    }
}