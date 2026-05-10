package org.mydrugs.mydrugs.effects.addiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.effects.addiction.manager.state.BadTripState;

public record BadTripPayload(
        boolean active,
        float threshold,
        float severity,
        int ticksActive,
        String sourceDrug,
        String sourceCategory,
        float symptomIntensity
) implements CustomPacketPayload {
    public static final Type<BadTripPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "bad_trip"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BadTripPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, BadTripPayload::active,
                    ByteBufCodecs.FLOAT, BadTripPayload::threshold,
                    ByteBufCodecs.FLOAT, BadTripPayload::severity,
                    ByteBufCodecs.VAR_INT, BadTripPayload::ticksActive,
                    ByteBufCodecs.STRING_UTF8, BadTripPayload::sourceDrug,
                    ByteBufCodecs.STRING_UTF8, BadTripPayload::sourceCategory,
                    ByteBufCodecs.FLOAT, BadTripPayload::symptomIntensity,
                    BadTripPayload::new
            );

    public static BadTripPayload from(BadTripState state) {
        return new BadTripPayload(
                state.active,
                state.threshold,
                state.severity,
                state.ticksActive,
                state.sourceDrug == null ? "" : state.sourceDrug.serializedName(),
                state.sourceCategory == null ? DrugCategory.OTHER.name() : state.sourceCategory.name(),
                state.symptomIntensity
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
