package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.items.data.AdnScrapData;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.data.MutationStatValue;
import org.mydrugs.mydrugs.mutation.GeneticProfileGenerator;
import org.mydrugs.mydrugs.mutation.GeneticRarityTier;
import org.mydrugs.mydrugs.mutation.MutationStat;

import java.util.function.Consumer;

public final class AdnScrapItem extends Item {
    public AdnScrapItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createScrapFrom(LivingEntity entity) {
        ItemStack stack = new ItemStack(ModItems.ADN_SCRAP.get());
        AdnScrapData data = entity instanceof Player player
                ? GeneticProfileGenerator.fromPlayerMutations(player, player.getData(ModAttachments.PLAYER_MUTATIONS.get()).stats())
                : GeneticProfileGenerator.fromEntity(entity);
        stack.set(ModDataComponents.ADN_SCRAP_DATA.get(), data);
        return stack;
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

        AdnScrapData data = stack.get(ModDataComponents.ADN_SCRAP_DATA.get());
        if (data == null) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_scrap.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_scrap.flavor").withStyle(ChatFormatting.DARK_PURPLE));
        Component sourceType = data.isPlayerSource()
                ? Component.translatable("tooltip.mydrugs.adn_scrap.source_player")
                : sourceEntityTypeName(data.sourceEntityType());
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_scrap.source", data.sourceName(), sourceType).withStyle(ChatFormatting.GRAY));

        GeneticRarityTier tier = GeneticRarityTier.bySerializedName(data.rarityTier()).orElse(GeneticRarityTier.COMMON);
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_scrap.rarity", Component.translatable(tier.translationKey())).withStyle(tierColor(tier)));
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_scrap.signature", data.geneticSignature()).withStyle(ChatFormatting.DARK_PURPLE));

        for (MutationStatValue statValue : data.stats()) {
            MutationStat stat = MutationStat.bySerializedNameOrNull(statValue.statId());
            Component statName = stat == null
                    ? Component.translatable("mutation.mydrugs.stat.unknown", statValue.statId())
                    : Component.translatable(stat.translationKey());
            int percent = Math.round(statValue.value() * 100.0F);
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_scrap.stat", statName, percent).withStyle(valueColor(statValue.value())));

            if (flag.isAdvanced()) {
                tooltipAdder.accept(Component.translatable(
                        "tooltip.mydrugs.adn_scrap.improbability",
                        Math.round(statValue.improbabilityScore() * 100.0F)
                ).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        if (flag.isAdvanced()) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_scrap.source_uuid", data.sourceUuid()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static ChatFormatting tierColor(GeneticRarityTier tier) {
        return switch (tier) {
            case COMMON -> ChatFormatting.GRAY;
            case UNCOMMON -> ChatFormatting.WHITE;
            case RARE -> ChatFormatting.AQUA;
            case DANGEROUS -> ChatFormatting.LIGHT_PURPLE;
            case MYTHIC -> ChatFormatting.GOLD;
        };
    }

    private static Component sourceEntityTypeName(String sourceEntityType) {
        ResourceLocation id = ResourceLocation.tryParse(sourceEntityType);
        if (id == null) {
            return Component.translatable("tooltip.mydrugs.adn_scrap.unknown_entity", sourceEntityType);
        }

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id);
        if (type == null) {
            return Component.translatable("tooltip.mydrugs.adn_scrap.unknown_entity", sourceEntityType);
        }
        return type.getDescription();
    }

    private static ChatFormatting valueColor(float value) {
        if (value < 0.20F) {
            return ChatFormatting.DARK_GRAY;
        }
        if (value < 0.40F) {
            return ChatFormatting.WHITE;
        }
        if (value < 0.60F) {
            return ChatFormatting.GREEN;
        }
        if (value < 0.80F) {
            return ChatFormatting.AQUA;
        }
        return ChatFormatting.LIGHT_PURPLE;
    }
}
