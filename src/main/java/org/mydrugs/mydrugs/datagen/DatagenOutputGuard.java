package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonElement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central save boundary for custom datagen providers.
 *
 * <p>NeoForge's cache can make duplicate writes appear harmless when both payloads happen to be
 * equal. They are still provider drift, so reject the second owner before either result is trusted.</p>
 */
public final class DatagenOutputGuard {
    private static final Map<Path, String> OWNERS = new ConcurrentHashMap<>();

    private DatagenOutputGuard() {
    }

    public static CompletableFuture<?> saveStable(
            String owner,
            CachedOutput output,
            JsonElement json,
            Path path
    ) {
        Path normalized = path.toAbsolutePath().normalize();
        String claim = owner + " at " + caller();
        String previous = OWNERS.putIfAbsent(normalized, claim);
        if (previous != null) {
            throw new IllegalStateException("Duplicate datagen output path '" + normalized
                    + "' from " + claim + "; already owned by " + previous);
        }
        return DataProvider.saveStable(output, json, path);
    }

    private static String caller() {
        return StackWalker.getInstance().walk(frames -> frames
                .filter(frame -> !frame.getClassName().equals(DatagenOutputGuard.class.getName()))
                .findFirst()
                .map(frame -> frame.getClassName() + "." + frame.getMethodName() + ":" + frame.getLineNumber())
                .orElse("unknown call site"));
    }
}
