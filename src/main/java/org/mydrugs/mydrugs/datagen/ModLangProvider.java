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

    @Override
    protected void addTranslations() {
        ModItems.SPACE_FOODS_BY_BASE_ID.forEach((baseId, item)
                -> this.addItem(item, "Space " + pretty(baseId.getPath())));

        // Blocks
        add(ModBlocks.ADVANCED_FURNACE.get(), "Advanced Furnace");
        add(ModBlocks.MIXING_VAT.get(), "Mixing Vat");
        add(ModBlocks.ELECTROLYZER.get(), "Electrolyzer");
        add(ModBlocks.BTX_FRACTIONATION_TOWER.get(), "BTX Fractionation Tower");
        add(ModBlocks.BASIC_ITEM_PIPE.get(), "Basic Item Pipe");
        add(ModBlocks.FAST_ITEM_PIPE.get(), "Fast Item Pipe");
        add(ModBlocks.BASIC_FLUID_PIPE.get(), "Basic Fluid Pipe");
        add(ModBlocks.FAST_FLUID_PIPE.get(), "Fast Fluid Pipe");
        add(ModBlocks.BASIC_GAS_PIPE.get(), "Basic Gas Pipe");
        add(ModBlocks.FAST_GAS_PIPE.get(), "Fast Gas Pipe");
        add(ModBlocks.FLUID_PUMP.get(), "Fluid Pump");
        add(ModBlocks.PLATINUM_ORE.get(), "Platinum Ore");
        add(ModBlocks.DEEPSLATE_PLATINUM_ORE.get(), "Deepslate Platinum Ore");
        add(ModBlocks.RAW_PLATINUM_BLOCK.get(), "Block of Raw Platinum");
        add(ModBlocks.PLATINUM_BLOCK.get(), "Block of Platinum");
        add(ModBlocks.ALUMINIUM_ORE.get(), "Aluminium Ore");
        add(ModBlocks.DEEPSLATE_ALUMINIUM_ORE.get(), "Deepslate Aluminium Ore");
        add(ModBlocks.RAW_ALUMINIUM_BLOCK.get(), "Block of Raw Aluminium");
        add(ModBlocks.ALUMINIUM_BLOCK.get(), "Block of Aluminium");
        add("container.mydrugs.btx_fractionation_tower", "BTX Fractionation Tower");

        // Items
        add(ModItems.GLASS_BOTTLE.get(), "Glass Bottle");
        add(ModItems.MIXING_SPATULA.get(), "Mixing Spatula");
        add(ModItems.PIPE_WRENCH.get(), "Pipe Wrench");
        add(ModItems.PIPE_FILTER_UPGRADE.get(), "Pipe Filter Upgrade");
        add(ModItems.MACHINE_TRANSFER_UPGRADE.get(), "Machine Transfer Upgrade");
        add(ModItems.ENERGY_UPGRADE.get(), "Energy Upgrade");
        add(ModItems.AUTOMATION_UPGRADE.get(), "Automation Upgrade");
        add(ModItems.RAW_PLATINUM.get(), "Raw Platinum");
        add(ModItems.PLATINUM_INGOT.get(), "Platinum Ingot");
        add(ModItems.RAW_ALUMINIUM.get(), "Raw Aluminium");
        add(ModItems.ALUMINIUM_INGOT.get(), "Aluminium Ingot");
        add("menu.mydrugs.pipe_filter", "Pipe Filter");
        add("menu.mydrugs.machine_transfer_config", "Machine Transfer Configuration");
        add("message.mydrugs.pipe.side_mode", "%s: %s");
        add("message.mydrugs.pipe_filter.applied", "%s filter applied: %s %s (%s entries)");
        add("tooltip.mydrugs.pipe_filter.kind", "Kind: %s");
        add("tooltip.mydrugs.pipe_filter.mode", "Mode: %s");
        add("tooltip.mydrugs.pipe_filter.entries", "Entries: %s");
        add("screen.mydrugs.pipe_filter.placeholder", "Filter editor skeleton");
        add("screen.mydrugs.pipe_filter.instructions", "Configured data is stored on the upgrade item.");
        add("message.mydrugs.transfer_upgrade.installed", "Transfer upgrade installed");
        add("message.mydrugs.transfer_upgrade.already_installed", "Transfer upgrade already installed");
        add("message.mydrugs.energy_upgrade.installed", "Energy Upgrade installed.");
        add("message.mydrugs.energy_upgrade.already_installed", "Energy Upgrade already installed.");
        add("message.mydrugs.energy_upgrade.has_automation", "This machine already has an Automation Upgrade.");
        add("message.mydrugs.energy_upgrade.unsupported", "This machine does not support this upgrade.");
        add("message.mydrugs.automation_upgrade.installed", "Automation Upgrade installed.");
        add("message.mydrugs.automation_upgrade.already_installed", "Automation Upgrade already installed.");
        add("message.mydrugs.automation_upgrade.has_energy", "This machine already has an Energy Upgrade.");
        add("message.mydrugs.automation_upgrade.unsupported", "This machine does not support this upgrade.");
        add("screen.mydrugs.machine_transfer.target", "Target: %s");
        add("screen.mydrugs.machine_transfer.ports", "Ports detected: %s");
        add("screen.mydrugs.machine_transfer.instructions", "Port and side rules will be synced here as machines migrate.");
        add("screen.mydrugs.machine_transfer.title", "Transfer Configuration");
        add("screen.mydrugs.machine_transfer.port", "Port");
        add("screen.mydrugs.machine_transfer.kind", "Kind");
        add("screen.mydrugs.machine_transfer.direction.north", "North");
        add("screen.mydrugs.machine_transfer.direction.south", "South");
        add("screen.mydrugs.machine_transfer.direction.east", "East");
        add("screen.mydrugs.machine_transfer.direction.west", "West");
        add("screen.mydrugs.machine_transfer.direction.up", "Up");
        add("screen.mydrugs.machine_transfer.direction.down", "Down");
        add("screen.mydrugs.machine_transfer.rule.disabled", "Disabled");
        add("screen.mydrugs.machine_transfer.rule.input", "Input");
        add("screen.mydrugs.machine_transfer.rule.output", "Output");
        add("screen.mydrugs.machine_transfer.rule.disabled_short", "Off");
        add("screen.mydrugs.machine_transfer.rule.input_short", "In");
        add("screen.mydrugs.machine_transfer.rule.output_short", "Out");
        add("screen.mydrugs.machine_transfer.open", "Transfer Configuration");
        add("screen.mydrugs.machine_transfer.open_short", "T");
        add("screen.mydrugs.machine_transfer.tooltip", "Transfer Configuration");
        add("screen.mydrugs.machine_transfer.click_to_cycle", "Click to cycle");
        add("screen.mydrugs.machine_transfer.toggle.on", "On");
        add("screen.mydrugs.machine_transfer.toggle.off", "Off");
        add("message.mydrugs.transfer_upgrade.required", "Install a Machine Transfer Upgrade first.");
        add("message.mydrugs.transfer_config.unsupported", "This menu does not support transfer configuration.");
        add("message.mydrugs.transfer_config.open_failed", "Could not open transfer configuration.");
        add("machine_transfer_port.mydrugs.item_input", "Item Input");
        add("machine_transfer_port.mydrugs.item_output", "Item Output");
        add("machine_transfer_port.mydrugs.fuel", "Fuel");
        add("machine_transfer_port.mydrugs.catalyst", "Catalyst");
        add("machine_transfer_port.mydrugs.fluid_input", "Fluid Input");
        add("machine_transfer_port.mydrugs.fluid_input_a", "Fluid Input A");
        add("machine_transfer_port.mydrugs.fluid_input_b", "Fluid Input B");
        add("machine_transfer_port.mydrugs.fluid_input_c", "Fluid Input C");
        add("machine_transfer_port.mydrugs.fluid_output", "Fluid Output");
        add("machine_transfer_port.mydrugs.gas_input", "Gas Input");
        add("machine_transfer_port.mydrugs.gas_output", "Gas Output");
        add("direction.mydrugs.down", "Down");
        add("direction.mydrugs.up", "Up");
        add("direction.mydrugs.north", "North");
        add("direction.mydrugs.south", "South");
        add("direction.mydrugs.west", "West");
        add("direction.mydrugs.east", "East");
        add("pipe_mode.mydrugs.disabled", "Disabled");
        add("pipe_mode.mydrugs.pipe", "Pipe");
        add("pipe_mode.mydrugs.input", "Input");
        add("pipe_mode.mydrugs.output", "Output");
        add("pipe.mydrugs.kind.item", "Item");
        add("pipe.mydrugs.kind.fluid", "Fluid");
        add("pipe.mydrugs.kind.gas", "Gas");
        add("pipe.mydrugs.filter_mode.allow_list", "Allow List");
        add("pipe.mydrugs.filter_mode.deny_list", "Deny List");

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
}
