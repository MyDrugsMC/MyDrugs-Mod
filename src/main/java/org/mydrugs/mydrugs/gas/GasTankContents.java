package org.mydrugs.mydrugs.gas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.mydrugs.mydrugs.items.data.ComponentCodecs;

public record GasTankContents(String gasId, long amount) {
    /** Gas IDs are namespaced resource locations; this comfortably bounds them. */
    public static final int MAX_GAS_ID_LENGTH = 256;

    public static final GasTankContents EMPTY = new GasTankContents("", 0);

    public GasTankContents {
        if (gasId == null) {
            gasId = "";
        }
        // Never allow a negative amount, and normalize an empty/zero tank to the empty gas id.
        if (amount <= 0L) {
            amount = 0L;
            gasId = "";
        }
    }

    public static final Codec<GasTankContents> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ComponentCodecs.boundedString(MAX_GAS_ID_LENGTH).optionalFieldOf("gas_id", "").forGetter(GasTankContents::gasId),
                    ComponentCodecs.longRange(0L, Long.MAX_VALUE).optionalFieldOf("amount", 0L).forGetter(GasTankContents::amount)
            ).apply(instance, GasTankContents::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GasTankContents> STREAM_CODEC = StreamCodec.composite(
            ComponentCodecs.boundedStringStream(MAX_GAS_ID_LENGTH), GasTankContents::gasId,
            ComponentCodecs.checkedVarLong(0L, Long.MAX_VALUE), GasTankContents::amount,
            GasTankContents::new
    );
}
