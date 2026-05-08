package org.mydrugs.mydrugs.items.rolling;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.mydrugs.mydrugs.core.drug.DrugId;

import java.util.List;

public record RolledDrugContent(
        DrugId first,
        DrugId second,
        DrugId third,
        boolean firstBrightened,
        boolean secondBrightened,
        boolean thirdBrightened
) {
    public static final Codec<DrugId> DRUG_ID_CODEC =
            Codec.STRING.comapFlatMap(
                    name -> DrugId.bySerializedName(name)
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "Unknown drug id: " + name)),
                    DrugId::serializedName
            );

    public static final StreamCodec<ByteBuf, DrugId> DRUG_ID_STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(
                    name -> DrugId.bySerializedName(name).orElse(DrugId.TOBACCO),
                    DrugId::serializedName
            );

    public static final Codec<RolledDrugContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DRUG_ID_CODEC.fieldOf("first").forGetter(RolledDrugContent::first),
            DRUG_ID_CODEC.fieldOf("second").forGetter(RolledDrugContent::second),
            DRUG_ID_CODEC.fieldOf("third").forGetter(RolledDrugContent::third),
            Codec.BOOL.optionalFieldOf("first_brightened", false).forGetter(RolledDrugContent::firstBrightened),
            Codec.BOOL.optionalFieldOf("second_brightened", false).forGetter(RolledDrugContent::secondBrightened),
            Codec.BOOL.optionalFieldOf("third_brightened", false).forGetter(RolledDrugContent::thirdBrightened)
    ).apply(instance, RolledDrugContent::new));

    public static final StreamCodec<ByteBuf, RolledDrugContent> STREAM_CODEC = StreamCodec.composite(
            DRUG_ID_STREAM_CODEC, RolledDrugContent::first,
            DRUG_ID_STREAM_CODEC, RolledDrugContent::second,
            DRUG_ID_STREAM_CODEC, RolledDrugContent::third,
            ByteBufCodecs.BOOL, RolledDrugContent::firstBrightened,
            ByteBufCodecs.BOOL, RolledDrugContent::secondBrightened,
            ByteBufCodecs.BOOL, RolledDrugContent::thirdBrightened,
            RolledDrugContent::new
    );

    public RolledDrugContent(DrugId first, DrugId second, DrugId third) {
        this(first, second, third, false, false, false);
    }

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

    public boolean isBrightened(int index) {
        return switch (index) {
            case 0 -> firstBrightened;
            case 1 -> secondBrightened;
            case 2 -> thirdBrightened;
            default -> false;
        };
    }
}
