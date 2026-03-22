package org.mydrugs.mydrugs.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.ModItems;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput output) {
        super(output, MyDrugs.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        ModItems.SPACE_FOODS_BY_BASE_ID.forEach((baseId, item) -> {
            this.addItem(item, "Space " + titleCase(baseId.getPath()));
        });
    }

    private static String titleCase(String path) {
        String[] parts = path.split("_");
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) out.append(' ');
            String p = parts[i];
            out.append(Character.toUpperCase(p.charAt(0)));
            out.append(p.substring(1));
        }

        return out.toString();
    }
}