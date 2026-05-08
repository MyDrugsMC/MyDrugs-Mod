package org.mydrugs.mydrugs.client.guide;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.mydrugs.mydrugs.MyDrugs;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class GuideLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation PAGES_ID =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "guide/pages.json");

    private GuideLoader() {}

    public static List<GuidePage> load(ResourceManager rm) {
        var resource = rm.getResource(PAGES_ID);
        if (resource.isEmpty()) {
            LOGGER.warn("[MyDrugs Guide] pages.json not found at {}. Showing fallback.", PAGES_ID);
            return fallback();
        }
        try (var reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
            JsonObject root = GsonHelper.parse(reader);
            List<GuidePage> pages = parsePages(root);
            if (pages.isEmpty()) {
                LOGGER.warn("[MyDrugs Guide] pages.json contained no pages. Showing fallback.");
                return fallback();
            }
            return withGeneratedNavigation(pages);
        } catch (Exception e) {
            LOGGER.warn("[MyDrugs Guide] Failed to parse pages.json: {}", e.getMessage());
            return fallback();
        }
    }

    private static List<GuidePage> parsePages(JsonObject root) {
        List<GuidePage> pages = new ArrayList<>();
        JsonArray pagesArr = GsonHelper.getAsJsonArray(root, "pages", new JsonArray());
        for (JsonElement pageEl : pagesArr) {
            JsonObject pageObj = pageEl.getAsJsonObject();
            String title = GsonHelper.getAsString(pageObj, "title", "");
            List<GuideElement> elements = new ArrayList<>();
            JsonArray elemArr = GsonHelper.getAsJsonArray(pageObj, "elements", new JsonArray());
            for (JsonElement el : elemArr) {
                JsonObject obj = el.getAsJsonObject();
                String type = GsonHelper.getAsString(obj, "type", "text");
                String text = GsonHelper.getAsString(obj, "text", "");
                String target = GsonHelper.getAsString(obj, "target", "");
                elements.add(switch (type) {
                    case "heading"   -> GuideElement.heading(text);
                    case "tip"       -> GuideElement.tip(text);
                    case "warning"   -> GuideElement.warning(text);
                    case "goal"      -> GuideElement.goal(text);
                    case "link"      -> GuideElement.link(text, target);
                    case "title"     -> GuideElement.title(text);
                    case "separator" -> GuideElement.separator();
                    case "item"      -> GuideElement.item(text);
                    default          -> GuideElement.text(text);
                });
            }
            pages.add(new GuidePage(title, List.copyOf(elements)));
        }
        return pages;
    }

    private static List<GuidePage> withGeneratedNavigation(List<GuidePage> pages) {
        if (pages.isEmpty() || hasPage(pages, "Table of Contents")) {
            return pages;
        }

        List<GuideElement> toc = new ArrayList<>();
        toc.add(GuideElement.text("Choose a section to jump directly to it."));
        for (GuidePage page : pages) {
            if ("MyDrugs Field Guide".equals(page.title())) {
                continue;
            }
            toc.add(GuideElement.link(page.title(), page.title()));
        }

        List<GuidePage> result = new ArrayList<>(pages.size() + 1);
        result.add(pages.get(0));
        result.add(new GuidePage("Table of Contents", List.copyOf(toc)));
        result.addAll(pages.subList(1, pages.size()));
        return List.copyOf(result);
    }

    private static boolean hasPage(List<GuidePage> pages, String title) {
        for (GuidePage page : pages) {
            if (title.equals(page.title())) {
                return true;
            }
        }
        return false;
    }

    private static List<GuidePage> fallback() {
        return List.of(new GuidePage("Progression Guide", List.of(
                GuideElement.text("The guide content could not be loaded."),
                GuideElement.tip("Run tools/sync_progression_guide.ps1 and rebuild.")
        )));
    }
}
