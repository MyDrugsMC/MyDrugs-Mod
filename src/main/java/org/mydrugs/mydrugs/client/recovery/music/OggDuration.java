package org.mydrugs.mydrugs.client.recovery.music;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Reads the duration of an Ogg Vorbis file by:
 * <ol>
 *   <li>Parsing the first Ogg page to find the Vorbis identification packet (channels, sample rate).</li>
 *   <li>Scanning backwards from the end of file for the last <code>OggS</code> capture pattern and
 *       reading its 64-bit little-endian granule position.</li>
 * </ol>
 * Duration in milliseconds is {@code granulePosition * 1000 / sampleRate}.
 */
public final class OggDuration {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final byte[] OGGS = {'O', 'g', 'g', 'S'};

    private OggDuration() {
    }

    public static long readMs(Path file) {
        if (file == null || !Files.isRegularFile(file)) {
            return 0L;
        }
        try {
            long size = Files.size(file);
            if (size < 64L) {
                return 0L;
            }
            int sampleRate = readSampleRate(file);
            if (sampleRate <= 0) {
                return 0L;
            }
            long granule = readLastGranule(file, size);
            if (granule <= 0L) {
                return 0L;
            }
            return granule * 1000L / sampleRate;
        } catch (Exception ex) {
            LOGGER.debug("Failed to read OGG duration for {}: {}", file, ex.getMessage());
            return 0L;
        }
    }

    /**
     * Try to open the file at the given resource-style path inside Minecraft's resources and
     * compute its duration. We copy the stream to a temporary buffer first since OGG duration
     * needs seeking.
     */
    public static long readMs(InputStream input) {
        if (input == null) {
            return 0L;
        }
        try {
            byte[] bytes = input.readAllBytes();
            if (bytes.length < 64) {
                return 0L;
            }
            int sampleRate = readSampleRateFromBytes(bytes);
            if (sampleRate <= 0) {
                return 0L;
            }
            long granule = readLastGranuleFromBytes(bytes);
            if (granule <= 0L) {
                return 0L;
            }
            return granule * 1000L / sampleRate;
        } catch (IOException ex) {
            return 0L;
        }
    }

    private static int readSampleRate(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            byte[] head = in.readNBytes(8192);
            return readSampleRateFromBytes(head);
        }
    }

    private static int readSampleRateFromBytes(byte[] data) {
        // Locate first "OggS" page; then the first packet payload begins at offset 27 + segmentCount.
        int idx = indexOf(data, OGGS, 0, Math.min(data.length, 256));
        if (idx < 0) {
            return 0;
        }
        if (idx + 27 > data.length) {
            return 0;
        }
        int segmentCount = data[idx + 26] & 0xFF;
        int packetStart = idx + 27 + segmentCount;
        // Vorbis identification packet:
        //   byte 0     : packet type (0x01)
        //   bytes 1-6  : "vorbis"
        //   bytes 7-10 : vorbis_version
        //   byte 11    : audio_channels
        //   bytes 12-15: audio_sample_rate (little-endian)
        if (packetStart + 16 > data.length) {
            return 0;
        }
        if (data[packetStart] != 0x01
                || data[packetStart + 1] != 'v'
                || data[packetStart + 2] != 'o'
                || data[packetStart + 3] != 'r'
                || data[packetStart + 4] != 'b'
                || data[packetStart + 5] != 'i'
                || data[packetStart + 6] != 's') {
            return 0;
        }
        ByteBuffer bb = ByteBuffer.wrap(data, packetStart + 12, 4).order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    private static long readLastGranule(Path file, long size) throws IOException {
        int window = (int) Math.min(65_536L, size);
        try (SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
            channel.position(size - window);
            ByteBuffer buf = ByteBuffer.allocate(window);
            while (buf.hasRemaining()) {
                if (channel.read(buf) < 0) break;
            }
            byte[] tail = buf.array();
            return readLastGranuleFromBytes(tail);
        }
    }

    private static long readLastGranuleFromBytes(byte[] data) {
        int lastIdx = -1;
        int from = 0;
        while (true) {
            int idx = indexOf(data, OGGS, from, data.length);
            if (idx < 0) break;
            lastIdx = idx;
            from = idx + 1;
        }
        if (lastIdx < 0 || lastIdx + 14 > data.length) {
            return 0L;
        }
        ByteBuffer bb = ByteBuffer.wrap(data, lastIdx + 6, 8).order(ByteOrder.LITTLE_ENDIAN);
        long granule = bb.getLong();
        // -1 indicates "no packets finish on this page" — fall back to nothing in that case.
        return granule < 0 ? 0L : granule;
    }

    private static int indexOf(byte[] haystack, byte[] needle, int from, int until) {
        int max = Math.min(haystack.length, until) - needle.length;
        outer:
        for (int i = Math.max(0, from); i <= max; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
