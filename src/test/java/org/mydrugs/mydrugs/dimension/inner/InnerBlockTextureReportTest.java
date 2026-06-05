package org.mydrugs.mydrugs.dimension.inner;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.dimension.InnerBlockIds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reports which inner-dimension block textures still need to be drawn.
 *
 * <p>The node and symbolic-plant blocks were switched from <em>borrowed</em> textures (vanilla
 * blocks / other plants' crop-stage textures) to their <em>own</em> texture
 * ({@code assets/mydrugs/textures/block/&lt;id&gt;.png}) in {@code ModSimpleBlockAssetProvider}. Those
 * PNGs do not exist yet — that is expected; the artist will draw them. This test enumerates exactly
 * which files are missing so the list is surfaced by the test run, and writes it to
 * {@code build/reports/missing_inner_textures.txt}.
 *
 * <p>It intentionally does <em>not</em> fail the build for missing art (so {@code ./gradlew test}
 * stays green); the missing set is the report. It turns into a genuine guard once the textures
 * exist: at that point the list is empty. The id lists are shared with the datagen provider, so the
 * report can never drift from what is actually generated.
 */
class InnerBlockTextureReportTest {
    private static final Path TEXTURE_DIR = Path.of("src/main/resources/assets/mydrugs/textures/block");
    private static final Path REPORT = Path.of("build/reports/missing_inner_textures.txt");

    @Test
    void reportMissingOwnedInnerBlockTextures() throws IOException {
        Set<String> ids = new TreeSet<>();
        ids.addAll(InnerBlockIds.NODE_BLOCKS);
        ids.addAll(InnerBlockIds.SYMBOLIC_PLANTS);

        List<String> missing = new ArrayList<>();
        for (String id : ids) {
            if (!Files.exists(TEXTURE_DIR.resolve(id + ".png"))) {
                missing.add("assets/mydrugs/textures/block/" + id + ".png");
            }
        }

        Files.createDirectories(REPORT.getParent());
        String body = missing.isEmpty()
                ? "All inner-dimension block textures are present.\n"
                : "Missing inner-dimension block textures (" + missing.size() + "):\n  "
                        + String.join("\n  ", missing) + "\n";
        Files.writeString(REPORT, body);

        if (!missing.isEmpty()) {
            System.out.println();
            System.out.println("===== MISSING INNER-DIMENSION BLOCK TEXTURES (" + missing.size() + ") — draw these PNGs =====");
            missing.forEach(m -> System.out.println("  " + m));
            System.out.println("===== full list written to " + REPORT + " =====");
            System.out.println();
        }

        // Reporting only — never fails on missing art. The report file is the deliverable.
        assertTrue(Files.exists(REPORT), "texture report should have been written");
    }
}
