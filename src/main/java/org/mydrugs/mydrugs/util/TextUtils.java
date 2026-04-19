package org.mydrugs.mydrugs.util;

public final class TextUtils {
    private TextUtils() {
    }

    public static String prettify(String name) {
        String[] parts = name.split("_");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) builder.append(' ');
            builder.append(Character.toUpperCase(parts[i].charAt(0)));
            builder.append(parts[i].substring(1).toLowerCase());
        }

        return builder.toString();
    }
}
