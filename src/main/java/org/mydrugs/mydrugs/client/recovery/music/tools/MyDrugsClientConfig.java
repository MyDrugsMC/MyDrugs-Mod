package org.mydrugs.mydrugs.client.recovery.music.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Small client-only persisted config: the first-world disclaimer acknowledgement and the
 * player-configured external tool paths / disabled flags.
 *
 * <p>Stored as JSON at {@code config/mydrugs_client.json}. It is loaded lazily and saved
 * immediately on every mutation, so the acknowledgement survives restarts.
 */
public final class MyDrugsClientConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static MyDrugsClientConfig instance;

    private boolean disclaimerAcknowledged = false;
    private Map<String, String> toolPaths = new HashMap<>();
    private Map<String, Boolean> toolDisabled = new HashMap<>();

    private MyDrugsClientConfig() {
    }

    public static synchronized MyDrugsClientConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve("mydrugs_client.json");
    }

    private static MyDrugsClientConfig load() {
        Path path = file();
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                MyDrugsClientConfig loaded = GSON.fromJson(reader, MyDrugsClientConfig.class);
                if (loaded != null) {
                    if (loaded.toolPaths == null) {
                        loaded.toolPaths = new HashMap<>();
                    }
                    if (loaded.toolDisabled == null) {
                        loaded.toolDisabled = new HashMap<>();
                    }
                    return loaded;
                }
            } catch (Exception e) {
                LOGGER.warn("Could not read mydrugs client config, using defaults: {}", e.getMessage());
            }
        }
        return new MyDrugsClientConfig();
    }

    private void save() {
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not save mydrugs client config: {}", e.getMessage());
        }
    }

    public boolean isDisclaimerAcknowledged() {
        return disclaimerAcknowledged;
    }

    public void setDisclaimerAcknowledged(boolean acknowledged) {
        if (this.disclaimerAcknowledged != acknowledged) {
            this.disclaimerAcknowledged = acknowledged;
            save();
        }
    }

    /** Player-configured absolute path for a tool, or empty when none is set. */
    public String getToolPath(ExternalTool tool) {
        String value = toolPaths.get(tool.id());
        return value == null ? "" : value;
    }

    public void setToolPath(ExternalTool tool, String path) {
        if (path == null || path.isBlank()) {
            toolPaths.remove(tool.id());
        } else {
            toolPaths.put(tool.id(), path.trim());
        }
        save();
    }

    public boolean isToolDisabled(ExternalTool tool) {
        return toolDisabled.getOrDefault(tool.id(), false);
    }

    public void setToolDisabled(ExternalTool tool, boolean disabled) {
        toolDisabled.put(tool.id(), disabled);
        save();
    }
}
