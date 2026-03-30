package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.rolling.RolledDrugContent;
import org.mydrugs.mydrugs.registry.ModDataComponents;
import org.mydrugs.mydrugs.ModSounds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JointItem extends DrugItem {
    public JointItem(Properties properties, DrugId id, ConsumptionStrategy strategy) {
        super(properties, id, strategy);
    }

    @Override
    public List<DrugModel> getDrugModels(ItemStack stack) {
        RolledDrugContent content = stack.get(ModDataComponents.ROLLED_CONTENT.get());
        if (content == null) {
            return List.of();
        }

        return content.asList().stream()
                .map(DrugRegistry::getDrug)
                .toList();
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

        RolledDrugContent content = stack.get(ModDataComponents.ROLLED_CONTENT.get());
        if (content == null) {
            return;
        }

        Map<DrugId, Integer> counts = new HashMap<>();
        counts.put(content.first(), 1);
        counts.merge(content.second(), 1, Integer::sum);
        counts.merge(content.third(), 1, Integer::sum);

        for (Map.Entry<DrugId, Integer> entry : counts.entrySet()) {
            String idName = pretty(entry.getKey().name());
            int percent = Math.round(entry.getValue() / 3.0f * 100.0f);
            tooltipAdder.accept(Component.literal(idName + " : " + percent + "%"));
        }
    }

    private static String pretty(String name) {
        String[] parts = name.split("_");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) builder.append(' ');
            builder.append(Character.toUpperCase(parts[i].charAt(0)));
            builder.append(parts[i].substring(1).toLowerCase());
        }

        return builder.toString();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.SMOKE.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 40;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.TOOT_HORN;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        boolean creative = livingEntity instanceof Player player && player.getAbilities().instabuild;

        if (!creative) {
            stack.shrink(1);
        }

        if (!level.isClientSide() && !creative && livingEntity instanceof Player player) {
            ItemStack filterStack = new ItemStack(ModItems.FILTER.get());

            if (!player.getInventory().add(filterStack)) {
                player.drop(filterStack, false);
            }
        }

        return super.finishUsingItem(stack, level, livingEntity);
    }
}