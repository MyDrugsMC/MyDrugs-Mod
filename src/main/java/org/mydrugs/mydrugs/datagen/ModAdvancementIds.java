package org.mydrugs.mydrugs.datagen;

import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public final class ModAdvancementIds {
    private ModAdvancementIds() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, path);
    }

    public static String translationKey(String path, String suffix) {
        return "advancement." + MyDrugs.MODID + "." + path.replace('/', '.') + "." + suffix;
    }
}
