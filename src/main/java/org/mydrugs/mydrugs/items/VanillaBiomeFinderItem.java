package org.mydrugs.mydrugs.items;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.items.data.BiomeFinderTarget;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.network.BiomeFinderOpenScreenPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class VanillaBiomeFinderItem extends Item {
    private static final int SEARCH_RADIUS = 6400;
    private static final int SEARCH_HORIZONTAL_STEP = 32;
    private static final int SEARCH_VERTICAL_STEP = 64;
    private static final long CACHE_REFRESH_INTERVAL = 1200L;

    public VanillaBiomeFinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        BiomeFinderTarget target = stack.getOrDefault(ModDataComponents.BIOME_FINDER_TARGET.get(), BiomeFinderTarget.EMPTY);

        if (player.isShiftKeyDown()) {
            openSelectionScreen(serverPlayer, serverLevel, hand, target);
            return InteractionResult.SUCCESS;
        }

        if (target.selectedBiome().isEmpty()) {
            openSelectionScreen(serverPlayer, serverLevel, hand, target);
            return InteractionResult.SUCCESS;
        }

        ResourceLocation selected = target.selectedBiome().get();
        if (isExcluded(selected)) {
            serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.biome_finder.excluded"), true);
            stack.set(ModDataComponents.BIOME_FINDER_TARGET.get(), BiomeFinderTarget.EMPTY);
            return InteractionResult.SUCCESS;
        }

        serverPlayer.displayClientMessage(
                Component.translatable("message.mydrugs.biome_finder.searching", prettyName(selected)),
                true
        );

        BlockPos searchOrigin = serverPlayer.blockPosition();
        Pair<BlockPos, Holder<Biome>> found = findClosestSelectedBiome(serverLevel, searchOrigin, selected);

        long now = level.getGameTime();
        ResourceLocation dim = serverLevel.dimension().location();

        if (found == null) {
            serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.biome_finder.not_found"), true);
            BiomeFinderTarget cleared = target.cleared();
            stack.set(ModDataComponents.BIOME_FINDER_TARGET.get(), cleared);
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = found.getFirst();
        BiomeFinderTarget updated = target.withCachedPos(pos, dim, now);
        stack.set(ModDataComponents.BIOME_FINDER_TARGET.get(), updated);

        // Distance / direction in actionbar
        double dx = pos.getX() - searchOrigin.getX();
        double dz = pos.getZ() - searchOrigin.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 32.0) {
            serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.biome_finder.arrived"), true);
        } else {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.mydrugs.biome_finder.found", prettyName(selected), (int) dist),
                    true
            );
        }
        return InteractionResult.SUCCESS;
    }

    private BiomeFinderTarget cycleSelection(ServerLevel level, BiomeFinderTarget current) {
        List<ResourceLocation> available = collectVanillaBiomes(level);
        if (available.isEmpty()) {
            return current;
        }
        ResourceLocation currentBiome = current.selectedBiome().orElse(null);
        int index = -1;
        if (currentBiome != null) {
            for (int i = 0; i < available.size(); i++) {
                if (available.get(i).equals(currentBiome)) {
                    index = i;
                    break;
                }
            }
        }
        ResourceLocation next = available.get((index + 1) % available.size());
        return current.withSelected(next);
    }

    public static List<ResourceLocation> collectVanillaBiomes(ServerLevel level) {
        HolderLookup.RegistryLookup<Biome> registry = level.registryAccess().lookupOrThrow(Registries.BIOME);
        List<ResourceLocation> result = new ArrayList<>();
        registry.listElementIds().forEach(key -> {
            ResourceLocation id = key.location();
            if (!"minecraft".equals(id.getNamespace())) return;
            if (isExcluded(id)) return;
            result.add(id);
        });
        result.sort((a, b) -> prettyName(a).compareToIgnoreCase(prettyName(b)));
        return result;
    }

    public static Pair<BlockPos, Holder<Biome>> findClosestSelectedBiome(
            ServerLevel level,
            BlockPos origin,
            ResourceLocation selected
    ) {
        return level.findClosestBiome3d(
                holder -> holder.unwrapKey()
                        .map(key -> key.location().equals(selected))
                        .orElse(false),
                origin,
                SEARCH_RADIUS,
                SEARCH_HORIZONTAL_STEP,
                SEARCH_VERTICAL_STEP
        );
    }

    private static void openSelectionScreen(
            ServerPlayer player,
            ServerLevel level,
            InteractionHand hand,
            BiomeFinderTarget current
    ) {
        PacketDistributor.sendToPlayer(
                player,
                new BiomeFinderOpenScreenPayload(hand, current.selectedBiome(), collectVanillaBiomes(level))
        );
    }

    public static boolean isExcluded(ResourceLocation id) {
        if (!"minecraft".equals(id.getNamespace())) return true;
        String path = id.getPath().toLowerCase(Locale.ROOT);
        if (path.contains("mushroom")) return true;
        return false;
    }

    public static String prettyName(ResourceLocation id) {
        String[] parts = id.getPath().split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            if (parts[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            sb.append(parts[i].substring(1));
        }
        return sb.toString();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        BiomeFinderTarget target = stack.getOrDefault(ModDataComponents.BIOME_FINDER_TARGET.get(), BiomeFinderTarget.EMPTY);
        if (target.selectedBiome().isPresent()) {
            tooltip.accept(Component.translatable(
                    "tooltip.mydrugs.vanilla_biome_finder.selected",
                    prettyName(target.selectedBiome().get())
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.accept(Component.translatable("tooltip.mydrugs.vanilla_biome_finder.empty").withStyle(ChatFormatting.GRAY));
        }
    }

    public static boolean isInsideSelectedBiome(Level level, BlockPos pos, ResourceLocation selected) {
        Holder<Biome> here = level.getBiome(pos);
        return here.unwrapKey().map(key -> key.location().equals(selected)).orElse(false);
    }
}
