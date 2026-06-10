package org.mydrugs.mydrugs.recovery.music;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class OggMetadataReader {
    private static final byte[] OGGS = {'O', 'g', 'g', 'S'};

    private OggMetadataReader() {
    }

    public static boolean isVorbis(Path file) {
        return readSampleRate(file) > 0;
    }

    public static long readDurationMs(Path file) {
        if (file == null || !Files.isRegularFile(file)) return 0L;
        try {
            long size = Files.size(file);
            int sampleRate = readSampleRate(file);
            long granule = readLastGranule(file, size);
            return sampleRate > 0 && granule > 0 ? granule * 1000L / sampleRate : 0L;
        } catch (IOException ignored) {
            return 0L;
        }
    }

    private static int readSampleRate(Path file) {
        if (file == null || !Files.isRegularFile(file)) return 0;
        try (InputStream input = Files.newInputStream(file)) {
            byte[] data = input.readNBytes(8192);
            int page = indexOf(data, OGGS, 0, Math.min(data.length, 256));
            if (page < 0 || page + 27 > data.length) return 0;
            int packet = page + 27 + (data[page + 26] & 0xFF);
            if (packet + 16 > data.length
                    || data[packet] != 1
                    || data[packet + 1] != 'v'
                    || data[packet + 2] != 'o'
                    || data[packet + 3] != 'r'
                    || data[packet + 4] != 'b'
                    || data[packet + 5] != 'i'
                    || data[packet + 6] != 's') {
                return 0;
            }
            return ByteBuffer.wrap(data, packet + 12, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } catch (IOException ignored) {
            return 0;
        }
    }

    private static long readLastGranule(Path file, long size) throws IOException {
        int window = (int) Math.min(65_536L, size);
        try (SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
            channel.position(Math.max(0L, size - window));
            ByteBuffer buffer = ByteBuffer.allocate(window);
            while (buffer.hasRemaining() && channel.read(buffer) >= 0) {
            }
            byte[] data = buffer.array();
            int last = -1;
            int from = 0;
            while (true) {
                int found = indexOf(data, OGGS, from, data.length);
                if (found < 0) break;
                last = found;
                from = found + 1;
            }
            if (last < 0 || last + 14 > data.length) return 0L;
            long granule = ByteBuffer.wrap(data, last + 6, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
            return Math.max(0L, granule);
        }
    }

    private static int indexOf(byte[] haystack, byte[] needle, int from, int until) {
        int max = Math.min(haystack.length, until) - needle.length;
        outer:
        for (int i = Math.max(0, from); i <= max; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue outer;
            }
            return i;
        }
        return -1;
    }
}
