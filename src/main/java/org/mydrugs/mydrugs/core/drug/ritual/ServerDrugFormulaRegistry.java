package org.mydrugs.mydrugs.core.drug.ritual;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;
import org.mydrugs.mydrugs.blocks.entity.FormedPsyMixerCoreBlockEntity;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.network.OpenDrugFormulaNamingPayload;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class ServerDrugFormulaRegistry {
    private static final int MAX_NAME_LENGTH = 32;
    private static final Pattern ALLOWED_NAME = Pattern.compile("[A-Za-z0-9 _'\\-]+");
    private static final Map<UUID, PendingFormula> PENDING = new ConcurrentHashMap<>();

    private ServerDrugFormulaRegistry() {
    }

    public static boolean finishOrRequestName(ServerPlayer player, FormedPsyMixerCoreBlockEntity mixer, RitualDrugFormula formula) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            mixer.placeIntoOutput(createStack(MixedDrugData.pending(formula)));
            return true;
        }
        DrugPatentSavedData patents = DrugPatentSavedData.get(server);
        MixedDrugData existing = patents.bySignature(formula.canonicalSignature()).orElse(null);
        if (existing != null) {
            mixer.placeIntoOutput(createStack(existing));
            return true;
        }

        PENDING.put(player.getUUID(), new PendingFormula(mixer.getBlockPos(), formula));
        PacketDistributor.sendToPlayer(player, new OpenDrugFormulaNamingPayload(MixedDrugData.pending(formula)));
        player.displayClientMessage(Component.translatable("message.mydrugs.formula.naming_required").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return false;
    }

    public static void submitName(ServerPlayer player, String rawName) {
        PendingFormula pending = PENDING.get(player.getUUID());
        if (pending == null) {
            player.displayClientMessage(Component.translatable("message.mydrugs.formula.no_pending").withStyle(ChatFormatting.RED), true);
            return;
        }

        String name = rawName.trim();
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH || !ALLOWED_NAME.matcher(name).matches()) {
            player.displayClientMessage(Component.translatable("message.mydrugs.formula.invalid_name", MAX_NAME_LENGTH).withStyle(ChatFormatting.RED), true);
            return;
        }

        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return;
        }
        DrugPatentSavedData patents = DrugPatentSavedData.get(server);
        synchronized (patents) {
            MixedDrugData existing = patents.bySignature(pending.formula.canonicalSignature()).orElse(null);
            if (existing != null) {
                finish(player, pending, existing);
                PENDING.remove(player.getUUID());
                return;
            }
            if (patents.isNameTakenByOtherFormula(name, pending.formula.canonicalSignature())) {
                player.displayClientMessage(Component.translatable("message.mydrugs.formula.name_taken").withStyle(ChatFormatting.RED), true);
                return;
            }
            MixedDrugData patented = patents.patent(pending.formula, name, player.getUUID(), player.getName().getString());
            finish(player, pending, patented);
            PENDING.remove(player.getUUID());
        }
    }

    private static void finish(ServerPlayer player, PendingFormula pending, MixedDrugData data) {
        if (!(player.level() instanceof ServerLevel level)
                || !(level.getBlockEntity(pending.mixerPos) instanceof FormedPsyMixerCoreBlockEntity mixer)
                || !mixer.stillValid(player)) {
            player.getInventory().placeItemBackInInventory(createStack(data));
            return;
        }
        ItemStack output = mixer.getItem(PsyMixerMultiblock.SLOT_OUTPUT);
        if (!output.isEmpty()) {
            player.getInventory().placeItemBackInInventory(createStack(data));
            return;
        }
        mixer.placeIntoOutput(createStack(data));
        player.displayClientMessage(Component.translatable("message.mydrugs.formula.patented", data.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    public static ItemStack createStack(MixedDrugData data) {
        ItemStack stack = new ItemStack(itemFor(data.baseDrug()));
        stack.set(ModDataComponents.MIXED_DRUG_DATA.get(), data);
        return stack;
    }

    private static Item itemFor(org.mydrugs.mydrugs.core.drug.DrugId baseDrug) {
        return switch (baseDrug) {
            case WEED -> ModItems.MIXED_WEED_DRUG.get();
            case TOBACCO -> ModItems.MIXED_TOBACCO_DRUG.get();
            case LSD -> ModItems.MIXED_LSD_DRUG.get();
            case MUSHROOMS -> ModItems.MIXED_MUSHROOMS_DRUG.get();
            case HASH -> ModItems.MIXED_HASH_DRUG.get();
            case METH -> ModItems.MIXED_METH_DRUG.get();
            case COCAINE -> ModItems.MIXED_COCAINE_DRUG.get();
            case CRACK -> ModItems.MIXED_CRACK_DRUG.get();
            case COFFEE -> ModItems.MIXED_COFFEE_DRUG.get();
            case ALCOHOL -> ModItems.DEFIANT_SPIRIT_BOTTLE.get();
            default -> ModItems.MIXED_DRUG.get();
        };
    }

    private record PendingFormula(BlockPos mixerPos, RitualDrugFormula formula) {
    }
}
