package org.mydrugs.mydrugs.diary;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiaryBlockerRoutesTest {
    private static final Set<String> EXPECTED_SOURCE_KEYS = Set.of(
            "machine_status.mydrugs.missing_catalyst",
            "machine_status.mydrugs.missing_container",
            "machine_status.mydrugs.missing_diary_context",
            "machine_status.mydrugs.missing_input_fluid",
            "machine_status.mydrugs.missing_input_gas",
            "machine_status.mydrugs.missing_input_item",
            "machine_status.mydrugs.missing_recovery_context",
            "message.mydrugs.resonator.missing_dream_residue",
            "message.mydrugs.resonator.missing_drug_knowledge"
    );

    @Test
    void mapsEveryRequiredStatusToAStableDiaryRoute() {
        assertEquals(EXPECTED_SOURCE_KEYS, DiaryBlockerRoutes.all().stream()
                .map(DiaryBlockerRoutes.Route::sourceKey)
                .collect(Collectors.toSet()));

        for (String sourceKey : EXPECTED_SOURCE_KEYS) {
            DiaryBlockerRoutes.Route route = DiaryBlockerRoutes.fromSourceKey(sourceKey);
            assertNotNull(route);
            assertEquals(route, DiaryBlockerRoutes.fromBlockerType(route.blockerType()));
            assertTrue(route.blockerType().matches("[a-z0-9_-]{1,48}"));
            assertTrue(route.routeTextKey().startsWith("diary.mydrugs.blocker.route."));
            assertTrue(!route.guidePage().isBlank());
        }
    }

    @Test
    void everyRouteHasLocalizedActionableCopy() throws IOException {
        Path langPath = Path.of("src/main/resources/assets/mydrugs/lang/en_us.json");
        JsonObject language;
        try (var reader = Files.newBufferedReader(langPath)) {
            language = JsonParser.parseReader(reader).getAsJsonObject();
        }

        for (DiaryBlockerRoutes.Route route : DiaryBlockerRoutes.all()) {
            assertTrue(language.has(route.routeTextKey()), route.routeTextKey());
            String text = language.get(route.routeTextKey()).getAsString();
            assertTrue(text.startsWith("Blocked on "), route.routeTextKey());
            assertTrue(text.contains(". Read " + route.guidePage() + ". You need "), route.routeTextKey());
        }
        assertTrue(language.has("screen.mydrugs.diary.open_guide_page"));
    }
}
