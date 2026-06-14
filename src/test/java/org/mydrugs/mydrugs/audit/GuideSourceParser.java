package org.mydrugs.mydrugs.audit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

final class GuideSourceParser {
    private GuideSourceParser() {
    }

    static JsonObject parse(String markdown) {
        JsonArray pages = new JsonArray();
        JsonArray elements = new JsonArray();
        List<String> paragraph = new ArrayList<>();
        String title = "";
        boolean pastHeader = false;

        for (String rawLine : markdown.split("\\R", -1)) {
            String line = rawLine;
            if (!pastHeader) {
                if (line.trim().equals("---")) {
                    pastHeader = true;
                }
                continue;
            }
            if (line.trim().equals("---")) {
                flushParagraph(elements, paragraph);
                if (!title.isEmpty() || !elements.isEmpty()) {
                    pages.add(page(title, elements));
                }
                title = "";
                elements = new JsonArray();
                continue;
            }
            if (line.startsWith("# ")) {
                flushParagraph(elements, paragraph);
                if (title.isEmpty()) {
                    title = line.substring(2).trim();
                } else {
                    elements.add(element("heading", line.substring(2).trim()));
                }
                continue;
            }
            if (line.startsWith("## ")) {
                flushParagraph(elements, paragraph);
                elements.add(element("heading", line.substring(3).trim()));
                continue;
            }
            String callout = callout(line, "TIP");
            if (callout != null) {
                flushParagraph(elements, paragraph);
                elements.add(element("tip", callout));
                continue;
            }
            callout = callout(line, "WARN");
            if (callout != null) {
                flushParagraph(elements, paragraph);
                elements.add(element("warning", callout));
                continue;
            }
            callout = callout(line, "GOAL");
            if (callout != null) {
                flushParagraph(elements, paragraph);
                elements.add(element("goal", callout));
                continue;
            }
            if (line.startsWith("@link ")) {
                flushParagraph(elements, paragraph);
                String raw = line.substring("@link ".length()).trim();
                String[] parts = raw.split("\\|", 2);
                JsonObject link = element("link", parts.length == 2 ? parts[1].trim() : parts[0].trim());
                link.addProperty("target", parts[0].trim());
                elements.add(link);
                continue;
            }
            if (line.startsWith("@title ")) {
                flushParagraph(elements, paragraph);
                elements.add(element("title", line.substring("@title ".length()).trim()));
                continue;
            }
            if (line.startsWith("@item ")) {
                flushParagraph(elements, paragraph);
                elements.add(element("item", line.substring("@item ".length()).trim().split("\\s+")[0]));
                continue;
            }
            if (line.trim().equals("***")) {
                flushParagraph(elements, paragraph);
                elements.add(element("separator", ""));
                continue;
            }
            if (line.isBlank()) {
                flushParagraph(elements, paragraph);
            } else {
                paragraph.add(line.trim());
            }
        }
        flushParagraph(elements, paragraph);
        if (!title.isEmpty() || !elements.isEmpty()) {
            pages.add(page(title, elements));
        }
        JsonObject root = new JsonObject();
        root.add("pages", pages);
        return root;
    }

    private static JsonObject page(String title, JsonArray elements) {
        JsonObject page = new JsonObject();
        page.addProperty("title", title);
        page.add("elements", elements.deepCopy());
        return page;
    }

    private static JsonObject element(String type, String text) {
        JsonObject element = new JsonObject();
        element.addProperty("type", type);
        element.addProperty("text", text);
        return element;
    }

    private static String callout(String line, String kind) {
        String prefix = "> [" + kind + "]";
        return line.startsWith(prefix) ? line.substring(prefix.length()).trim() : null;
    }

    private static void flushParagraph(JsonArray elements, List<String> paragraph) {
        if (!paragraph.isEmpty()) {
            String text = String.join(" ", paragraph).trim();
            if (!text.isEmpty()) {
                elements.add(element("text", text));
            }
            paragraph.clear();
        }
    }
}
