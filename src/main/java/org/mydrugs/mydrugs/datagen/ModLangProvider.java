package org.mydrugs.mydrugs.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.fluids.FluidEntry;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.items.ModItems;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput output) {
        super(output, MyDrugs.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        ModItems.SPACE_FOODS_BY_BASE_ID.forEach((baseId, item)
                -> this.addItem(item, "Space " + pretty(baseId.getPath())));

        // Blocks
        add(ModBlocks.ADVANCED_FURNACE.get(), "Advanced Furnace");
        add(ModBlocks.MIXING_VAT.get(), "Mixing Vat");

        // Items
        add(ModItems.GLASS_BOTTLE.get(), "Glass Bottle");
        add(ModItems.MIXING_SPATULA.get(), "Mixing Spatula");

        // Fluids + fluid buckets
        for (FluidEntry entry : ModFluids.ALL.values()) {
            String pretty = pretty(entry.name());

            // If you keep the default FluidType description id, the registry name is <name>_type
            add("fluid_type." + MyDrugs.MODID + "." + entry.name() + "_type", pretty);

            // Optional fallback if you later set a custom description id without "_type"
            add("fluid_type." + MyDrugs.MODID + "." + entry.name(), pretty);

            add("item." + MyDrugs.MODID + "." + entry.name() + "_bucket", pretty + " Bucket");
            add("block." + MyDrugs.MODID + "." + entry.name(), pretty);
        }
    }

    private static String pretty(String name) {
        String[] parts = name.split("_");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) builder.append(' ');
            builder.append(Character.toUpperCase(parts[i].charAt(0)));
            builder.append(parts[i].substring(1));
        }

        return builder.toString();
    }
}