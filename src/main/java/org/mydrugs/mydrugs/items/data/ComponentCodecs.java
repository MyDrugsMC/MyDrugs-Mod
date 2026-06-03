package org.mydrugs.mydrugs.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * Shared helpers for bounded / validated data-component codecs.
 *
 * <p>The goal is to keep persistent {@link Codec} behaviour and network {@link StreamCodec}
 * behaviour aligned: where the persistent codec rejects an out-of-range value, the matching
 * stream codec rejects it too (rather than silently accepting hostile input). List stream codecs
 * reject over-long counts up front via {@link ByteBufCodecs#list(int)}, which throws before reading
 * any elements and therefore never leaves unread element bytes in the buffer.</p>
 */
public final class ComponentCodecs {
    private ComponentCodecs() {
    }

    // ===== Strings =====

    /** Persistent string codec that rejects values longer than {@code maxLength}. */
    public static Codec<String> boundedString(int maxLength) {
        return Codec.STRING.validate(value -> value.length() <= maxLength
                ? DataResult.success(value)
                : DataResult.error(() -> "String too long: " + value.length() + " > " + maxLength));
    }

    /** Network string codec with the same length bound as {@link #boundedString(int)}. */
    public static StreamCodec<ByteBuf, String> boundedStringStream(int maxLength) {
        return ByteBufCodecs.stringUtf8(maxLength);
    }

    // ===== Lists =====

    /** Persistent list codec that rejects lists larger than {@code maxSize}. */
    public static <T> Codec<List<T>> boundedList(Codec<T> element, int maxSize) {
        return element.listOf().validate(list -> list.size() <= maxSize
                ? DataResult.success(list)
                : DataResult.error(() -> "List too long: " + list.size() + " > " + maxSize));
    }

    /**
     * Network list codec that rejects over-long counts up front. The underlying
     * {@link ByteBufCodecs#list(int)} throws a {@link DecoderException} when the encoded count
     * exceeds {@code maxSize} before reading any element, so a hostile count never leaves unread
     * element bytes in the buffer.
     */
    public static <B extends ByteBuf, T> StreamCodec<B, List<T>> boundedListStream(
            StreamCodec<B, T> element, int maxSize) {
        return element.apply(ByteBufCodecs.list(maxSize));
    }

    // ===== Ints =====

    /** Persistent int codec rejecting values outside {@code [min, max]}. */
    public static Codec<Integer> intRange(int min, int max) {
        return Codec.intRange(min, max);
    }

    /** Network int codec rejecting values outside {@code [min, max]} (matches {@link #intRange}). */
    public static StreamCodec<ByteBuf, Integer> checkedVarInt(int min, int max) {
        return ByteBufCodecs.VAR_INT.map(
                value -> requireRange(value, min, max),
                value -> value);
    }

    /** Persistent int codec that clamps decoded values into {@code [min, max]}. */
    public static Codec<Integer> clampedInt(int min, int max) {
        return Codec.INT.xmap(value -> Mth.clamp(value, min, max), value -> Mth.clamp(value, min, max));
    }

    /** Network int codec that clamps decoded values into {@code [min, max]}. */
    public static StreamCodec<ByteBuf, Integer> clampedVarInt(int min, int max) {
        return ByteBufCodecs.VAR_INT.map(
                value -> Mth.clamp(value, min, max),
                value -> Mth.clamp(value, min, max));
    }

    // ===== Longs =====

    /** Persistent long codec rejecting values outside {@code [min, max]}. */
    public static Codec<Long> longRange(long min, long max) {
        return Codec.LONG.validate(value -> value >= min && value <= max
                ? DataResult.success(value)
                : DataResult.error(() -> "Value out of range [" + min + ", " + max + "]: " + value));
    }

    /** Network long codec rejecting values outside {@code [min, max]} (matches {@link #longRange}). */
    public static StreamCodec<ByteBuf, Long> checkedVarLong(long min, long max) {
        return ByteBufCodecs.VAR_LONG.map(
                value -> requireRange(value, min, max),
                value -> value);
    }

    // ===== Floats =====

    /** Persistent float codec rejecting values outside {@code [min, max]}. */
    public static Codec<Float> floatRange(float min, float max) {
        return Codec.floatRange(min, max);
    }

    /** Network float codec rejecting values outside {@code [min, max]} (matches {@link #floatRange}). */
    public static StreamCodec<ByteBuf, Float> checkedFloat(float min, float max) {
        return ByteBufCodecs.FLOAT.map(
                value -> requireRange(value, min, max),
                value -> value);
    }

    /** Persistent float codec that clamps decoded values into {@code [min, max]}. */
    public static Codec<Float> clampedFloat(float min, float max) {
        return Codec.FLOAT.xmap(value -> Mth.clamp(value, min, max), value -> Mth.clamp(value, min, max));
    }

    /** Network float codec that clamps decoded values into {@code [min, max]}. */
    public static StreamCodec<ByteBuf, Float> clampedFloatStream(float min, float max) {
        return ByteBufCodecs.FLOAT.map(
                value -> Mth.clamp(value, min, max),
                value -> Mth.clamp(value, min, max));
    }

    private static int requireRange(int value, int min, int max) {
        if (value < min || value > max) {
            throw new DecoderException("Value out of range [" + min + ", " + max + "]: " + value);
        }
        return value;
    }

    private static long requireRange(long value, long min, long max) {
        if (value < min || value > max) {
            throw new DecoderException("Value out of range [" + min + ", " + max + "]: " + value);
        }
        return value;
    }

    private static float requireRange(float value, float min, float max) {
        if (value < min || value > max) {
            throw new DecoderException("Value out of range [" + min + ", " + max + "]: " + value);
        }
        return value;
    }
}
