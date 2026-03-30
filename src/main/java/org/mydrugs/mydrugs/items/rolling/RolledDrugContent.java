package org.mydrugs.mydrugs.items.rolling;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.mydrugs.mydrugs.core.drug.DrugId;

import java.util.List;

public record RolledDrugContent(DrugId first, DrugId second, DrugId third) {
    public static final Codec<DrugId> DRUG_ID_CODEC =
            Codec.STRING.xmap(DrugId::valueOf, DrugId::name);

    public static final StreamCodec<ByteBuf, DrugId> DRUG_ID_STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(DrugId::valueOf, DrugId::name);

    public static final Codec<RolledDrugContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DRUG_ID_CODEC.fieldOf("first").forGetter(RolledDrugContent::first),
            DRUG_ID_CODEC.fieldOf("second").forGetter(RolledDrugContent::second),
            DRUG_ID_CODEC.fieldOf("third").forGetter(RolledDrugContent::third)
    ).apply(instance, RolledDrugContent::new));

    public static final StreamCodec<ByteBuf, RolledDrugContent> STREAM_CODEC = StreamCodec.composite(
            DRUG_ID_STREAM_CODEC, RolledDrugContent::first,
            DRUG_ID_STREAM_CODEC, RolledDrugContent::second,
            DRUG_ID_STREAM_CODEC, RolledDrugContent::third,
            RolledDrugContent::new
    );

    public static RolledDrugContent allTobacco() {
        return new RolledDrugContent(DrugId.TOBACCO, DrugId.TOBACCO, DrugId.TOBACCO);
    }

    public boolean isAllTobacco() {
        return first == DrugId.TOBACCO
                && second == DrugId.TOBACCO
                && third == DrugId.TOBACCO;
    }

    public List<DrugId> asList() {
        return List.of(first, second, third);
    }
}