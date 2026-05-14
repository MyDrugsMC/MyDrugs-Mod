package org.mydrugs.mydrugs.effects.addiction.events;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class OreFortuneDropEvents {
    private static final int MAX_VIRTUAL_FORTUNE_LEVEL = 3;

    private OreFortuneDropEvents() {
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        Entity breaker = event.getBreaker();
        if (!(breaker instanceof ServerPlayer player) || player.isCreative()) {
            return;
        }

        ServerLevel level = event.getLevel();
        BlockState state = event.getState();
        ItemStack tool = event.getTool();

        if (!state.is(Tags.Blocks.ORES)) {
            return;
        }

        if (hasSilkTouch(level, tool)) {
            return;
        }

        /*
         * Important:
         * BlockDropsEvent reçoit déjà les drops après application de la loot table vanilla.
         * Donc si la pioche a déjà Fortune, les drops sont déjà multipliés.
         *
         * Si on applique ORE_FORTUNE par-dessus, on obtient un double-dip :
         * Fortune vanilla x ORE_FORTUNE.
         *
         * Pour éviter ça, ORE_FORTUNE ne s'applique que si l'outil n'a pas Fortune.
         */
        if (getFortuneLevel(level, tool) > 0) {
            return;
        }

        int fortuneLevel = virtualFortuneLevel(player);
        if (fortuneLevel <= 0) {
            return;
        }

        List<ItemEntity> extras = new ArrayList<>();

        for (ItemEntity drop : event.getDrops()) {
            ItemStack original = drop.getItem();
            if (original.isEmpty()) {
                continue;
            }

            int extraCount = vanillaOreDropsExtraCount(original.getCount(), fortuneLevel, player);
            if (extraCount <= 0) {
                continue;
            }

            splitExtraDrops(level, drop, original, extraCount, extras);
        }

        event.getDrops().addAll(extras);
    }

    private static int virtualFortuneLevel(ServerPlayer player) {
        float intensity = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.ORE_FORTUNE);
        if (intensity < 1.0F) {
            return 0;
        }

        /*
         * 1.0 -> Fortune I
         * 2.0 -> Fortune II
         * 3.0 -> Fortune III
         *
         * On utilise floor plutôt que round pour éviter qu'une intensité 0.6 donne déjà Fortune I.
         */
        return Mth.clamp(Mth.floor(intensity), 1, MAX_VIRTUAL_FORTUNE_LEVEL);
    }

    /**
     * Reproduit la logique vanilla de Fortune pour les minerais.
     *
     * Vanilla fait essentiellement :
     *
     * int bonusMultiplier = random.nextInt(fortuneLevel + 2) - 1;
     * if (bonusMultiplier < 0) bonusMultiplier = 0;
     * finalCount = baseCount * (bonusMultiplier + 1);
     *
     * Ici, on retourne seulement la partie extra :
     *
     * extraCount = finalCount - baseCount
     * extraCount = baseCount * bonusMultiplier
     */
    private static int vanillaOreDropsExtraCount(int baseCount, int fortuneLevel, ServerPlayer player) {
        if (baseCount <= 0 || fortuneLevel <= 0) {
            return 0;
        }

        int bonusMultiplier = player.getRandom().nextInt(fortuneLevel + 2) - 1;
        if (bonusMultiplier < 0) {
            bonusMultiplier = 0;
        }

        return baseCount * bonusMultiplier;
    }

    private static void splitExtraDrops(
            ServerLevel level,
            ItemEntity originalEntity,
            ItemStack originalStack,
            int extraCount,
            List<ItemEntity> extras
    ) {
        int maxStackSize = originalStack.getMaxStackSize();

        while (extraCount > 0) {
            int count = Math.min(extraCount, maxStackSize);

            ItemStack extraStack = originalStack.copy();
            extraStack.setCount(count);

            ItemEntity extraEntity = new ItemEntity(
                    level,
                    originalEntity.getX(),
                    originalEntity.getY(),
                    originalEntity.getZ(),
                    extraStack
            );

            extraEntity.setDefaultPickUpDelay();
            extras.add(extraEntity);

            extraCount -= count;
        }
    }

    private static boolean hasSilkTouch(ServerLevel level, ItemStack tool) {
        return getEnchantmentLevel(level, tool, Enchantments.SILK_TOUCH) > 0;
    }

    private static int getFortuneLevel(ServerLevel level, ItemStack tool) {
        return getEnchantmentLevel(level, tool, Enchantments.FORTUNE);
    }

    private static int getEnchantmentLevel(
            ServerLevel level,
            ItemStack tool,
            net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> enchantment
    ) {
        if (tool.isEmpty()) {
            return 0;
        }

        try {
            return level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .get(enchantment)
                    .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, tool))
                    .orElse(0);
        } catch (RuntimeException ignored) {
            return 0;
        }
    }
}