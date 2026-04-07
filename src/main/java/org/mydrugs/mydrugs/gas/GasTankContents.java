package org.mydrugs.mydrugs.gas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GasTankContents(String gasId, long amount) {
    public static final GasTankContents EMPTY = new GasTankContents("", 0);

    public static final Codec<GasTankContents> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("gas_id", "").forGetter(GasTankContents::gasId),
                    Codec.LONG.optionalFieldOf("amount", 0L).forGetter(GasTankContents::amount)
            ).apply(instance, GasTankContents::new)
    );
}