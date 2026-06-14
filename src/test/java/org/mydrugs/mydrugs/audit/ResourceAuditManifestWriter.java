package org.mydrugs.mydrugs.audit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ResourceAuditManifestWriter {
    private ResourceAuditManifestWriter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 3) {
            throw new IllegalArgumentException(
                    "Usage: ResourceAuditManifestWriter <report> [snapshot] [known-texture-baseline]");
        }
        write(Path.of(args[0]), ResourceAuditManifest.pretty(ResourceAuditManifest.generate()));
        if (args.length >= 2) {
            write(Path.of(args[1]), ResourceAuditManifest.pretty(ResourceAuditManifest.generate()));
        }
        if (args.length == 3) {
            StringBuilder baseline = new StringBuilder();
            baseline.append("# Existing texture art debt. Every entry must retain a review reason.\n");
            baseline.append("# New references fail validation; remove entries as final art is added.\n");
            ResourceAuditManifest.missingTextures().forEach((path, owners) ->
                    owners.stream().sorted().forEach(owner ->
                            baseline.append(path)
                                    .append(" <- ")
                                    .append(owner)
                                    .append(" | Existing art backlog tracked by docs/ASSET_TODO.md.\n")));
            write(Path.of(args[2]), baseline.toString());
        }
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.toAbsolutePath().normalize().getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }
}
