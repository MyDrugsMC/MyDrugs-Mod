package org.mydrugs.mydrugs.audit;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.gas.GasTankContents;
import org.mydrugs.mydrugs.items.data.ComponentCodecs;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterConfig;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterMode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Representative-invalid-value tests for the bounded component codecs and normalizing records. */
class ComponentCodecValidationTest {

    @Test
    void checkedVarIntRejectsOutOfRange() {
        ByteBuf buf = Unpooled.buffer();
        ByteBufCodecs.VAR_INT.encode(buf, 150);
        assertThrows(DecoderException.class, () -> ComponentCodecs.checkedVarInt(0, 100).decode(buf));

        ByteBuf ok = Unpooled.buffer();
        ByteBufCodecs.VAR_INT.encode(ok, 42);
        assertEquals(42, ComponentCodecs.checkedVarInt(0, 100).decode(ok));
    }

    @Test
    void boundedStringStreamRejectsTooLong() {
        ByteBuf buf = Unpooled.buffer();
        ByteBufCodecs.STRING_UTF8.encode(buf, "way-too-long-string");
        assertThrows(RuntimeException.class, () -> ComponentCodecs.boundedStringStream(4).decode(buf));
    }

    @Test
    void boundedListStreamRejectsOverCountWithoutSilentClamp() {
        StreamCodec<ByteBuf, List<String>> unbounded =
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list());
        ByteBuf buf = Unpooled.buffer();
        unbounded.encode(buf, List.of("a", "b", "c", "d", "e"));

        // Decoding with a smaller bound must reject (throw) rather than read fewer and leave bytes.
        assertThrows(DecoderException.class,
                () -> ComponentCodecs.boundedListStream(ByteBufCodecs.STRING_UTF8, 3).decode(buf));
    }

    @Test
    void persistentIntRangeRejectsOutOfRange() {
        DataResult<Integer> tooHigh = ComponentCodecs.intRange(0, 100)
                .parse(JsonOps.INSTANCE, new com.google.gson.JsonPrimitive(150));
        assertTrue(tooHigh.error().isPresent(), "intRange should reject 150");

        DataResult<Integer> ok = ComponentCodecs.intRange(0, 100)
                .parse(JsonOps.INSTANCE, new com.google.gson.JsonPrimitive(50));
        assertEquals(50, ok.result().orElseThrow());
    }

    @Test
    void persistentBoundedStringRejectsTooLong() {
        DataResult<String> tooLong = ComponentCodecs.boundedString(4)
                .parse(JsonOps.INSTANCE, new com.google.gson.JsonPrimitive("abcdef"));
        assertTrue(tooLong.error().isPresent(), "boundedString should reject over-long strings");
    }

    @Test
    void gasTankContentsNormalizesNegativeAndZero() {
        GasTankContents negative = new GasTankContents("mydrugs:methane", -5);
        assertEquals(0L, negative.amount(), "negative amount normalizes to 0");
        assertTrue(negative.gasId().isEmpty(), "empty tank normalizes to empty gas id");

        GasTankContents zero = new GasTankContents("mydrugs:methane", 0);
        assertTrue(zero.gasId().isEmpty(), "zero amount normalizes to empty gas id");

        GasTankContents full = new GasTankContents("mydrugs:methane", 10);
        assertEquals("mydrugs:methane", full.gasId());
        assertEquals(10L, full.amount());
    }

    @Test
    void pipeFilterConfigCapsEntries() {
        List<ResourceLocation> entries = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            entries.add(ResourceLocation.fromNamespaceAndPath("mydrugs", "item_" + i));
        }
        PipeFilterConfig config = new PipeFilterConfig(PipeResourceKind.ITEM, PipeFilterMode.DENY_LIST, entries);
        assertEquals(PipeFilterConfig.MAX_ENTRIES, config.entries().size());
    }
}
