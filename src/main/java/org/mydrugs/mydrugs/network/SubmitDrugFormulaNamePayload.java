package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.ritual.ServerDrugFormulaRegistry;

public record SubmitDrugFormulaNamePayload(String name) implements CustomPacketPayload {
    public static final Type<SubmitDrugFormulaNamePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "submit_drug_formula_name"));

    public static final StreamCodec<ByteBuf, SubmitDrugFormulaNamePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SubmitDrugFormulaNamePayload::name,
                    SubmitDrugFormulaNamePayload::new
            );

    public static void handleOnServer(SubmitDrugFormulaNamePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ServerDrugFormulaRegistry.submitName(player, payload.name());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
