package org.mydrugs.mydrugs.audit;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceAuditManifestTest {
    private static final Path SNAPSHOT = Path.of(
            "src/test/resources/mydrugs/resource_audit_manifest.json");

    @Test
    void committedAuditSnapshotMatchesTheCurrentResourceContract() {
        assertTrue(Files.isRegularFile(SNAPSHOT),
                "Missing audit snapshot. Run '.\\gradlew.bat updateResourceAuditManifest'.");
        assertEquals(SourceIndex.read(SNAPSHOT), ResourceAuditManifest.pretty(ResourceAuditManifest.generate()),
                "Resource audit snapshot drifted. Review changes, then run updateResourceAuditManifest.");
    }
}
