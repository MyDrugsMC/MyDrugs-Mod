package org.mydrugs.mydrugs.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.VanillaBiomeFinderItem;
import org.mydrugs.mydrugs.items.data.BiomeFinderTarget;
import org.mydrugs.mydrugs.items.data.ModDataComponents;

public record BiomeFinderSelectPayload(InteractionHand hand, ResourceLocation biome) implements CustomPacketPayload {
    public static final Type<BiomeFinderSelectPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "biome_finder_select"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BiomeFinderSelectPayload> STREAM_CODEC =
            StreamCodec.of(BiomeFinderSelectPayload::encode, BiomeFinderSelectPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, BiomeFinderSelectPayload payload) {
        ByteBufCodecs.STRING_UTF8.encode(buf, payload.hand().name());
        ResourceLocation.STREAM_CODEC.encode(buf, payload.biome());
    }

    private static BiomeFinderSelectPayload decode(RegistryFriendlyByteBuf buf) {
        InteractionHand hand = decodeHand(ByteBufCodecs.STRING_UTF8.decode(buf));
        ResourceLocation biome = ResourceLocation.STREAM_CODEC.decode(buf);
        return new BiomeFinderSelectPayload(hand, biome);
    }

    private static InteractionHand decodeHand(String name) {
        try {
            return InteractionHand.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return InteractionHand.MAIN_HAND;
        }
    }

    public static void handleOnServer(BiomeFinderSelectPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (VanillaBiomeFinderItem.isExcluded(payload.biome())) return;

        ItemStack stack = player.getItemInHand(payload.hand());
        if (!stack.is(ModItems.VANILLA_BIOME_FINDER.get())) {
            return;
        }

        boolean available = VanillaBiomeFinderItem.collectVanillaBiomes(level).contains(payload.biome());
        if (!available) {
            return;
        }

        stack.set(
                ModDataComponents.BIOME_FINDER_TARGET.get(),
                BiomeFinderTarget.EMPTY.withSelected(payload.biome())
        );
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "message.mydrugs.biome_finder.selected",
                        VanillaBiomeFinderItem.prettyName(payload.biome())
                ),
                true
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
