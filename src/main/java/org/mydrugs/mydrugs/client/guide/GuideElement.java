package org.mydrugs.mydrugs.client.guide;

public record GuideElement(Type type, String text, String target) {

    public enum Type {
        TEXT,       // body paragraph
        HEADING,    // section title inside a page
        TIP,        // green callout
        WARNING,    // red callout
        GOAL,       // blue callout
        LINK,       // text is shown, target resolves to a page title/slug
        TITLE,      // large centered title on title pages
        ITEM,       // text holds the ResourceLocation id (e.g. "mydrugs:tobacco_leaf")
        SEPARATOR   // horizontal rule
    }

    public static GuideElement text(String t)    { return new GuideElement(Type.TEXT, t, ""); }
    public static GuideElement heading(String t) { return new GuideElement(Type.HEADING, t, ""); }
    public static GuideElement tip(String t)     { return new GuideElement(Type.TIP, t, ""); }
    public static GuideElement warning(String t) { return new GuideElement(Type.WARNING, t, ""); }
    public static GuideElement goal(String t)    { return new GuideElement(Type.GOAL, t, ""); }
    public static GuideElement link(String t, String target) { return new GuideElement(Type.LINK, t, target); }
    public static GuideElement title(String t)   { return new GuideElement(Type.TITLE, t, ""); }
    public static GuideElement item(String id)   { return new GuideElement(Type.ITEM, id, ""); }
    public static GuideElement separator()       { return new GuideElement(Type.SEPARATOR, "", ""); }
}
