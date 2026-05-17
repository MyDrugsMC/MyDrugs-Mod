package org.mydrugs.mydrugs.client.diary;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Central helper for the on-disk layout of memory frames.
 * Both the capture pipeline and the playback/map UI go through here so the
 * same folder is targeted on save and on load.
 *
 * Layout: gameDir/mydrugs_memories/&lt;level-or-server-key&gt;/&lt;player_uuid&gt;/&lt;node_id&gt;/frame_00.png ...
 */
public final class MemoryStoragePaths {
    private MemoryStoragePaths() {
    }

    public static Path memoryRoot() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("mydrugs_memories");
    }

    public static Path memoryDir(String nodeId) {
        Minecraft mc = Minecraft.getInstance();
        UUID uuid = mc.getUser() == null ? null : mc.getUser().getProfileId();
        String uuidPart = uuid == null ? "unknown" : uuid.toString();
        return memoryRoot()
                .resolve(safe(resolveLevelKey(mc)))
                .resolve(uuidPart)
                .resolve(safe(nodeId));
    }

    public static String resolveLevelKey(Minecraft mc) {
        ServerData server = mc.getCurrentServer();
        if (server != null && server.ip != null && !server.ip.isEmpty()) {
            return "server_" + server.ip;
        }
        IntegratedServer sps = mc.getSingleplayerServer();
        if (sps != null) {
            return "singleplayer_" + sps.getWorldData().getLevelName();
        }
        return "world";
    }

    public static String safe(String s) {
        if (s == null) return "unknown";
        return s.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
