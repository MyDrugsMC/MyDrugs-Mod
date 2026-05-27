package org.mydrugs.mydrugs.items;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public final class BiomeFinderSelectableBiomes {
    private BiomeFinderSelectableBiomes() {
    }

    public static boolean isSelectableBiome(ResourceLocation id) {
        if (!"minecraft".equals(id.getNamespace())) return false;
        String path = id.getPath().toLowerCase(Locale.ROOT);
        if (path.contains("mushroom")) return false;
        return true;
    }

    public static boolean isExcluded(ResourceLocation id) {
        return !isSelectableBiome(id);
    }

    public static String prettyName(ResourceLocation id) {
        String[] parts = id.getPath().split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            if (parts[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            sb.append(parts[i].substring(1));
        }
        return sb.toString();
    }
}
