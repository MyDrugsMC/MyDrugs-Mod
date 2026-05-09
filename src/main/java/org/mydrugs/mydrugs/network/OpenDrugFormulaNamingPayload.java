package org.mydrugs.mydrugs.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.ritual.MixedDrugData;

public record OpenDrugFormulaNamingPayload(MixedDrugData formula) implements CustomPacketPayload {
    public static final Type<OpenDrugFormulaNamingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "open_drug_formula_naming"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDrugFormulaNamingPayload> STREAM_CODEC =
            StreamCodec.composite(
                    MixedDrugData.STREAM_CODEC, OpenDrugFormulaNamingPayload::formula,
                    OpenDrugFormulaNamingPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
