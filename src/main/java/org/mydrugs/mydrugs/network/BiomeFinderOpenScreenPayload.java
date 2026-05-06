package org.mydrugs.mydrugs.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record BiomeFinderOpenScreenPayload(
        InteractionHand hand,
        Optional<ResourceLocation> selectedBiome,
        List<ResourceLocation> availableBiomes
) implements CustomPacketPayload {
    public static final Type<BiomeFinderOpenScreenPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "biome_finder_open_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BiomeFinderOpenScreenPayload> STREAM_CODEC =
            StreamCodec.of(BiomeFinderOpenScreenPayload::encode, BiomeFinderOpenScreenPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, BiomeFinderOpenScreenPayload payload) {
        ByteBufCodecs.STRING_UTF8.encode(buf, payload.hand().name());
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buf, payload.selectedBiome());
        ByteBufCodecs.VAR_INT.encode(buf, payload.availableBiomes().size());
        for (ResourceLocation biome : payload.availableBiomes()) {
            ResourceLocation.STREAM_CODEC.encode(buf, biome);
        }
    }

    private static BiomeFinderOpenScreenPayload decode(RegistryFriendlyByteBuf buf) {
        InteractionHand hand = decodeHand(ByteBufCodecs.STRING_UTF8.decode(buf));
        Optional<ResourceLocation> selected = ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buf);
        int size = ByteBufCodecs.VAR_INT.decode(buf);
        List<ResourceLocation> biomes = new ArrayList<>(Math.max(0, size));
        for (int i = 0; i < size; i++) {
            biomes.add(ResourceLocation.STREAM_CODEC.decode(buf));
        }
        return new BiomeFinderOpenScreenPayload(hand, selected, List.copyOf(biomes));
    }

    private static InteractionHand decodeHand(String name) {
        try {
            return InteractionHand.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return InteractionHand.MAIN_HAND;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
